package com.yamanyar.mvn.plugin.utils;

import java.io.*;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**********************************************
 *
 *
 * This class has been copy/pasted from JBoss https://github.com/jesperpedersen/tattletale project!
 *
 *
 *********************************************/


/**
 * Class that would be used in order to obtain .jar files stored within a zipped up .war file.
 *
 * @author Navin Surtani
 */
public class Extractor {
    /**
     * Extract a JAR type file
     *
     * @param jarFile The war/ear file
     * @return The root of the extracted JAR file
     * @throws java.io.IOException Thrown if an error occurs
     */
    public static File extract(JarFile jarFile) throws IOException {
        String basedir = new File(System.getProperty("java.io.tmpdir")).getCanonicalPath();
        String fileName = new File(jarFile.getName()).getCanonicalPath();
        File target;

        if (fileName.startsWith(basedir)) {
            target = new File(fileName);
        } else {
            if (fileName.indexOf(":") != -1 &&
                    System.getProperty("os.name").toLowerCase(Locale.US).indexOf("windows") != -1) {
                fileName = fileName.substring(fileName.indexOf(":") + 1);
            }

            target = new File(basedir, fileName);
        }

        if (target.exists()) {
            recursiveDelete(target);
        }

        if (!target.mkdirs()) {
            throw new IOException("Could not create " + target);
        }

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry je = entries.nextElement();
            File copy = new File(target, je.getName());

            if (!je.isDirectory()) {
                InputStream in = null;
                OutputStream out = null;

                // Make sure that the directory is _really_ there
                if (copy.getParentFile() != null && !copy.getParentFile().exists()) {
                    if (!copy.getParentFile().mkdirs())
                        throw new IOException("Could not create " + copy.getParentFile());
                }

                try {
                    in = new BufferedInputStream(jarFile.getInputStream(je));
                    out = new BufferedOutputStream(new FileOutputStream(copy));

                    byte[] buffer = new byte[4096];
                    for (; ; ) {
                        int nBytes = in.read(buffer);
                        if (nBytes <= 0)
                            break;

                        out.write(buffer, 0, nBytes);
                    }
                    out.flush();
                } finally {
                    try {
                        if (out != null)
                            out.close();
                    } catch (IOException ignore) {
                        // Ignore
                    }

                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException ignore) {
                        // Ignore
                    }
                }
            } else {
                if (!copy.exists()) {
                    if (!copy.mkdirs())
                        throw new IOException("Could not create " + copy);
                } else {
                    if (!copy.isDirectory())
                        throw new IOException(copy + " isn't a directory");
                }
            }
        }
        return target;
    }

    private static void recursiveDelete(File file) throws IOException {
        if (file != null && file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        recursiveDelete(files[i]);
                    } else if (!files[i].delete()) {
                        throw new IOException("Could not delete the file of: " + files[i]);
                    }
                }
            }

            if (!file.delete()) {
                throw new IOException("Could not delete the file of: " + file);
            }
        }
    }
}

