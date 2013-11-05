With this plugin, you can restrict access from any package/class to target package/class/method inside the compiled code and the dependencies.

We use this plugin to be ensure that some restricted packages are not accessed by in-house developed components. Maven build is broken in restricted access is found.

This can not detect access via reflection however we do restrict reflection as well.

Please see https://github.com/yamanyar/restrict-maven-plugin/wiki/About-restrict-maven-plugin for more details.

Sample Usage:

  <plugin>

                <groupId>com.yamanyar</groupId>
                <artifactId>restrict-maven-plugin</artifactId>
                <version>0.4-SNAPSHOT</version>

                <executions>
                    <execution>
                        <phase>verify</phase>
                        <goals>
                            <goal>restrict</goal>
                        </goals>
                    </execution>
                </executions>

                <configuration>
                    <continueOnError>false</continueOnError>
                    <restrictions>
                        <!-- Restrict all access from com.ya* (except from com.yamanyar.test.MyTestDef) to  java.util.regex.*-->
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