package com.yamanyar.mvn.plugin;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * This logger uses the default logger but also create a special file to write down all the restrict faults.
 *
 * @author Kaan Yamanyar
 */
public class RestrictLogger extends SystemStreamLog {
    private BufferedWriter bw;

    public RestrictLogger(File report, Log log) {
        try {
            this.bw = new BufferedWriter(new FileWriter(report));
            super.info("Restriction report path is:" + report.getAbsolutePath());
        } catch (IOException e) {
            log.warn("Can not create extra report! We will skip creating report!");
        }
    }

    public void info(CharSequence content) {
        write(content);
        super.info(content);
    }

    public void error(CharSequence content) {
        write(content);
        super.error(content);
    }

    private void write(CharSequence content) {
        try {
            if (this.bw != null) {
                this.bw.write(content.toString());
                this.bw.newLine();
            }
        } catch (IOException e) {
        }
    }

    public void debug(CharSequence content) {
        super.debug(content);
        write(content);
    }

    public void close() {
        try {
            if (this.bw != null) this.bw.close();
        } catch (IOException e) {
        }
    }
}