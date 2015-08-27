With this plugin, you can restrict access from any package/class to target package/class/method inside the compiled code and the (jar/war/ear) dependencies. 

We use this plugin to be ensure that some restricted packages are not accessed by in-house developed components.

We generally restrict access to reflection, threads or some architectural components.

You can use wildcard both in from and to targets with exception cases.

See release notes at [here](https://github.com/yamanyar/restrict-maven-plugin/wiki/Release-Notes).

Please See restriction samples at [here](https://github.com/yamanyar/restrict-maven-plugin/wiki/Restriction-Samples).

Current version is 0.7.

With 0.8 Release now you can:
1) Use "-Drestrict.skip=true" to skip this plugin
2) Can set <onlyInspectFolder>true</onlyInspectFolder> just to restrict build directory (dependencies are not checked)
3) Cans set <printDebugs>false</printDebugs> not to print debugs.



Is there feature or a bug you noticed? Please inform [here](https://github.com/yamanyar/restrict-maven-plugin/issues).

Following is a sample usage from integration test, please note that restrictions below are meaningless; just for testing. (Artifact is deployed to maven central repository; so you do not need to download and install it to your local.)

    <build>
        <plugins>
            <plugin>

                <groupId>com.yamanyar</groupId>
                <artifactId>restrict-maven-plugin</artifactId>
                <version>0.7</version>

                <executions>
                    <execution>
                        <phase>verify</phase>
                        <goals>
                            <goal>restrict</goal>
                        </goals>
                    </execution>
                </executions>

                <configuration>
                    <!-- You can break maven build is restrictions found; or let build continue and just print logs. -->
                    <continueOnError>false</continueOnError>
                    <!-- Allowed is 0.8:
                    <onlyInspectFolder>false</onlyInspectFolder>
                    <printDebugs>true</printDebugs>
                    -->
                    <restrictions>
                        <!-- Restrict all access from com.ya* (except from com.yamanyar.test.MyTestDef) to  java.util.regex.* and to java.io.PrintStre*.pri*ln() -->
                        <!-- To mark a restriction to method you must finish with "()". Overloading is not supported; so all the matching methods will be restricted; inform me if you ever need.-->
                        <restriction>com.ya*,!com.yamanyar.test.MyTestDef to java.util.regex.*,java.io.PrintStre*.pri*ln()</restriction>
                        <!-- Restrict all access from * (except from *MyTestDef and com.yamanyar.none.*) to  sun.net.www.http.HttpClient -->
                        <restriction>*,!*MyTestDef,!com.yamanyar.none.* to sun.net.www.http.HttpClient</restriction>
                        <!-- Restrict all access from org.apache.commons.io.* to java.nio.* (except to java.nio.Buffer) -->
                        <restriction>org.apache.commons.io.* to java.nio.*,!java.nio.Buffer</restriction>
                        <!-- This is not tested in integration test; just for sample usage
                        Restrict all access from org.*,com.* (except from net.*,gov.*)
                                to co.uk.*,com.tr.* (except to eu.*,li.*)-->
                        <restriction>org.*,com.*,!net.*,!gov.* to co.uk.*,com.tr.*,!eu.*,!li.*</restriction>
                    </restrictions>
                </configuration>

            </plugin>
        </plugins>
    </build>

You can check target\restrict-maven-plugin.txt file for compiled rules and the violations. Here is an excerpt from our report for the configuration above:

##### Restriction Rules (begins) ######  
[7-8] Access from * to sun.net.www.http.HttpClient will be not allowed.  
[1-2] Access from com.ya* to java.util.regex.* will be not allowed.  
[1-5] Access from com.ya* to java.io.PrintStre*.pri*ln() will be not allowed  .
[16-17] Access from com.* to co.uk.* will be not allowed.  
[16-23] Access from com.* to com.tr.* will be not allowed.  
[11-12] Access from org.apache.commons.io.* to java.nio.* will be not allowed.  
[14-15] Access from org.* to co.uk.* will be not allowed.  
[14-21] Access from org.* to com.tr.* will be not allowed.  
###### Restriction Rules (ends)   ######  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.FileUtils to: java.nio.channels.ReadableByteChannel due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.FileUtils to: java.nio.charset.Charset due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.FileUtils to: java.nio.ByteBuffer due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.FileUtils to: java.nio.channels.FileChannel due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.FileUtils to: java.nio.CharBuffer due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.input.ProxyReader to: java.nio.CharBuffer due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.input.ReaderInputStream to: java.nio.charset.CharsetEncoder due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.input.ReaderInputStream to: java.nio.charset.CoderResult due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.input.ReaderInputStream to: java.nio.ByteBuffer due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.input.ReaderInputStream to: java.nio.CharBuffer due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.input.ReaderInputStream to: java.nio.charset.Charset due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.input.ReaderInputStream to: java.nio.charset.CodingErrorAction due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.output.FileWriterWithEncoding to: java.nio.charset.CharsetEncoder due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.output.FileWriterWithEncoding to: java.nio.charset.Charset due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.output.WriterOutputStream to: java.nio.charset.CoderResult due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.output.WriterOutputStream to: java.nio.ByteBuffer due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.output.WriterOutputStream to: java.nio.charset.CharsetDecoder due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.output.WriterOutputStream to: java.nio.CharBuffer due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.output.WriterOutputStream to: java.nio.charset.Charset due to rule [11-12]  
Restricted access from:(/home/kaan/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar)   org.apache.commons.io.output.WriterOutputStream to: java.nio.charset.CodingErrorAction due to rule [11-12]  
Restricted access from:(/home/kaan/codes/restrict-maven-plugin/src/it/simple-it/target/classes/com/yamanyar/test/MyTestAbc.class) com.yamanyar.test.MyTestAbc to: sun.net.www.http.HttpClient due to rule [7-8]  
Restricted access from:(/home/kaan/codes/restrict-maven-plugin/src/it/simple-it/target/classes/com/yamanyar/test/MyTestAbc.class) com.yamanyar.test.MyTestAbc to: java.util.regex.Matcher due to rule [1-2]  
Restricted access from:(/home/kaan/codes/restrict-maven-plugin/src/it/simple-it/target/classes/com/yamanyar/test/MyTestAbc.class) com.yamanyar.test.MyTestAbc to: java.io.PrintStream.println() due to rule [1-5]  
Build is broken due to 23 restriction policies!  
