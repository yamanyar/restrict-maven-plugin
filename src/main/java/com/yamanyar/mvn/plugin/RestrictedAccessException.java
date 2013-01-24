package com.yamanyar.mvn.plugin;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Simple exception to break maven build.
 * It is thrown if at lest one access exception is found. user needs to check log for details.
 *
 * @author Kaan Yamanyar
 */
public class RestrictedAccessException extends MojoExecutionException {
    private  static final long serialVersionUID = 1L;

    public RestrictedAccessException(int count) {
        super("There are " + count + " access exceptions!");
    }
}
