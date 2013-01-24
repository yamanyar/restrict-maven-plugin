def file = new File(basedir, 'target/restrict-maven-plugin.txt')
assert file.exists()
def report = file.getText()
assert report.contains("Inspecting commons-io:commons-io:jar:2.1:compile");
assert report.contains("Restricted access from: org.apache.commons.io.FileUtils to: java.nio.channels.ReadableByteChannel due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.FileUtils to: java.nio.charset.Charset due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.FileUtils to: java.nio.ByteBuffer due to rule");
assert report.contains("An exception to a restriction (java.nio.* of rule 9) matched java.nio.Buffer.");
assert report.contains("Restricted access from: org.apache.commons.io.FileUtils to: java.nio.channels.FileChannel due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.FileUtils to: java.nio.CharBuffer due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.input.ProxyReader to: java.nio.CharBuffer due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.input.ReaderInputStream to: java.nio.charset.CharsetEncoder due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.input.ReaderInputStream to: java.nio.ByteBuffer due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.input.ReaderInputStream to: java.nio.charset.CoderResult due to rule");
assert report.contains("An exception to a restriction (java.nio.* of rule 9) matched java.nio.Buffer.");
assert report.contains("Restricted access from: org.apache.commons.io.input.ReaderInputStream to: java.nio.CharBuffer due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.input.ReaderInputStream to: java.nio.charset.Charset due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.input.ReaderInputStream to: java.nio.charset.CodingErrorAction due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.output.FileWriterWithEncoding to: java.nio.charset.CharsetEncoder due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.output.FileWriterWithEncoding to: java.nio.charset.Charset due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.output.WriterOutputStream to: java.nio.ByteBuffer due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.output.WriterOutputStream to: java.nio.charset.CoderResult due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.output.WriterOutputStream to: java.nio.charset.CharsetDecoder due to rule");
assert report.contains("An exception to a restriction (java.nio.* of rule 9) matched java.nio.Buffer.");
assert report.contains("Restricted access from: org.apache.commons.io.output.WriterOutputStream to: java.nio.CharBuffer due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.output.WriterOutputStream to: java.nio.charset.Charset due to rule");
assert report.contains("Restricted access from: org.apache.commons.io.output.WriterOutputStream to: java.nio.charset.CodingErrorAction due to rule");
assert report.contains("Restricted access from: com.yamanyar.test.MyTestAbc to: sun.net.www.http.HttpClient due to rule")
assert report.contains("Restricted access from: com.yamanyar.test.MyTestAbc to: java.util.regex.Matcher due to rule")
assert report.contains("An exception to a restriction (* of rule 4) matched com.yamanyar.test.MyTestDef.")
assert report.contains("An exception to a restriction (com.ya* of rule 1) matched com.yamanyar.test.MyTestDef.")
assert report.contains("Build is broken due to 22 restriction policies!")
assert report.contains("Build is not broken since continueOnError is set to true!")

