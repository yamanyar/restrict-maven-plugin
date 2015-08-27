package com.yamanyar.mvn.plugin.inspectors;

import com.yamanyar.mvn.plugin.RestrictedAccessException;
import com.yamanyar.mvn.plugin.utils.Extractor;
import com.yamanyar.mvn.plugin.utils.WildcardMatcher;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * Inspects the class files, jar files and war files inside given ear resource
 *
 * @author Kaan Yamanyar
 */

public class Inspector {

    final private static String errorMessage = "Restricted access from:(%s) %s to: %s due to rule [%d-%d]";
    private final Log log;
    private final Map<WildcardMatcher, Set<WildcardMatcher>> restrictionsMap;
    private int count = 0;

    public Inspector(Log log, Map<WildcardMatcher, Set<WildcardMatcher>> restrictionsMap) {
        this.log = log;
        this.restrictionsMap = restrictionsMap;
    }

    public void inspectJar(String path) throws IOException {
        JarFile jarFile = new JarFile(path);
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String entryName = jarEntry.getName();
            InputStream entryStream = null;
            if (entryName.endsWith(".class")) {
                try {
                    entryStream = jarFile.getInputStream(jarEntry);
                    inspectClass(entryStream, jarFile.getName());

                } finally {
                    if (entryStream != null) entryStream.close();
                }
            }
        }
    }

    public void inspectWar(String path) throws IOException {
        inspectJar(path);
        File war = new File(path);
        JarFile warFile = new JarFile(path);

        File extractedDir = war.isFile() ? Extractor.extract(warFile) : war;
        Enumeration<JarEntry> jarEntries = warFile.entries();

        while (jarEntries.hasMoreElements()) {

            JarEntry jarEntry = jarEntries.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.endsWith(".jar")) {
                File subJarFile = new File(extractedDir.getCanonicalPath(), entryName);
                inspectJar(subJarFile.getAbsolutePath());
            }

        }
    }

    public void inspectEar(String path) throws IOException {
        inspectJar(path);
        File war = new File(path);
        JarFile warFile = new JarFile(path);

        File extractedDir = war.isFile() ? Extractor.extract(warFile) : war;
        Enumeration<JarEntry> jarEntries = warFile.entries();

        while (jarEntries.hasMoreElements()) {

            JarEntry jarEntry = jarEntries.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.endsWith(".jar")) {
                File subJarFile = new File(extractedDir.getCanonicalPath(), entryName);
                inspectJar(subJarFile.getAbsolutePath());
            } else if (entryName.endsWith(".war")) {
                File warJarFile = new File(extractedDir.getCanonicalPath(), entryName);
                inspectWar(warJarFile.getAbsolutePath());
            }

        }
    }

    protected void inspectClass(InputStream entryStream, String path) throws IOException {
        ClassPool classPool = new ClassPool();
        CtClass currentClass = classPool.makeClass(entryStream);

        Set<WildcardMatcher> fromList = restrictionsMap.keySet();

        for (WildcardMatcher from : fromList) {
            //check if current class matches one of the rule's from set. We only want to inspect classes that are in the "FROM" part of a rule.
            if (from.match(currentClass.getName())) {
                Collection refClasses = currentClass.getRefClasses();
                if (refClasses != null)
                    for (Object targetReference : refClasses) {
                        Set<WildcardMatcher> restrictedTargets = restrictionsMap.get(from);
                        for (WildcardMatcher restrictedTarget : restrictedTargets) {

                            //Let's check if target reference is restricted.

                            //If target is a reference restriction; it's simple to check::
                            if (!restrictedTarget.isMethod()) {
                                if (restrictedTarget.match((String) targetReference)) {
                                    count++;
                                    log.error(String.format(errorMessage, path, currentClass.getName(), targetReference, from.getRuleNo(), restrictedTarget.getRuleNo()));
                                }
                            } else {
                                //If target is a restriction to a method; first let's be sure that target class is matched. It means class containing the target method is referenced..
                                //With this check we will not dive into method's body for invoke checks
                                if (restrictedTarget.match((String) targetReference)) {
                                    //check method invokes
                                    CtMethod[] declaredMethods = currentClass.getDeclaredMethods();
                                    for (CtMethod declaredMethod : declaredMethods) {

                                        MethodInfo minfo = declaredMethod.getMethodInfo();
                                        minfo.getCodeAttribute();
                                        CodeAttribute ca = minfo.getCodeAttribute();
                                        if(null!=ca) for (CodeIterator ci = ca.iterator(); ci.hasNext(); ) {
                                            int index;
                                            try {
                                                index = ci.next();
                                            } catch (BadBytecode badBytecode) {
                                                throw new IOException(badBytecode);
                                            }
                                            int op = ci.byteAt(index);

                                            String desc = null;
                                            //TODO Check why invoke without method name occurs..
                                            if (index+1 < ci.getCodeLength() - 1) {
                                                int theIndex = ci.u16bitAt(index + 1);
                                                ConstPool constPool = ca.getConstPool();
                                                switch (op) {
                                                    case Opcode.INVOKEVIRTUAL:
                                                    case Opcode.INVOKESPECIAL:
                                                    case Opcode.INVOKESTATIC:
                                                        // As of JDK8, interfaces can have static methods! So if this is not a methodref,
                                                        // try falling through to the INVOKEINTERFACE case, as it might just be an interfacemethodref
                                                        if(constPool.getTag(theIndex) == ConstPool.CONST_Methodref) {
                                                            desc = constPool.getMethodrefClassName(theIndex) + "." + constPool.getMethodrefName(theIndex) + "()";
                                                            break;
                                                        }
                                                    case Opcode.INVOKEINTERFACE:
                                                        desc = constPool.getInterfaceMethodrefClassName(theIndex) + "." + constPool.getInterfaceMethodrefName(theIndex) + "()";

                                                        break;
                                                }

                                                //if we found an invocation; let's check pattern matching
                                                if (desc != null) {
                                                    log.debug("Checking " + restrictedTarget.toString() + " against " + desc);
                                                    if (restrictedTarget.matchMethod(desc)) {
                                                        log.debug("Method signature matched: " + restrictedTarget.toString() + " against " + desc);
                                                        count++;
                                                        log.error(String.format(errorMessage, path, currentClass.getName(), desc, from.getRuleNo(), restrictedTarget.getRuleNo()));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                        }
                    }
            }
        }


    }

    /**
     * Returns number of exceptions!
     *
     * @param artifacts Artifacts to be inspected.
     * @throws IOException
     */
    public void inspectArtifacts(Set<Artifact> artifacts) throws IOException {
        for (Artifact artifact : artifacts) {
            log.debug("Inspecting " + artifact.toString());
            String path = artifact.getFile().getPath();
            if (path.endsWith(".jar")) {
                inspectJar(path);
            } else if (path.endsWith(".war")) {
                inspectWar(path);
            } else if (path.endsWith(".ear")) {
                inspectEar(path);
            } else if (path.endsWith(".class")) {
                File classFile = artifact.getFile();
                inspectClassFile(classFile);
            }


        }

    }

    private void inspectClassFile(File classFile) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(classFile);
            inspectClass(is, classFile.getAbsolutePath());
        } finally {
            if (is != null) is.close();
        }
    }

    public void inspectFolder(File buildDirectory) throws IOException {
        Collection<File> files = FileUtils.listFiles(buildDirectory, new String[]{"class"}, true);
        for (File file : files)
            inspectClassFile(file);

    }

    public void breakIfError(boolean continueOnError) throws RestrictedAccessException {
        if (count > 0) {
            log.error("Build is broken due to " + count + " restriction policies!");
            if (continueOnError) {
                log.error("Build is not broken since continueOnError is set to true!");
            } else {
                throw new RestrictedAccessException(count);
            }
        } else log.info("No restricted access is found");
    }
}