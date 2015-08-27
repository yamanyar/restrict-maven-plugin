package com.yamanyar.mvn.plugin.utils;

import org.apache.maven.plugin.logging.Log;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * Test wildcard matching algorithm.
 *
 * @author Kaan Yamnyar
 */
public class WildcardMatcherTest {
    @Test
    public void testMatch() throws Exception {
        Log log = EasyMock.createMock(Log.class);
        log.info(EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(log);


        WildcardMatcher wildcardMatcher = new WildcardMatcher("*and*the*", log,false);
        assertTrue(wildcardMatcher.match("Tree and the river"));
        assertFalse(wildcardMatcher.match("Tree an the river"));
        assertTrue(wildcardMatcher.match("and the"));
        assertTrue(wildcardMatcher.match("andthe"));
        assertFalse(wildcardMatcher.match("Tree and he river"));

        wildcardMatcher = new WildcardMatcher("*.yamanyar*", log,false);
        assertTrue(wildcardMatcher.match("com.yamanyar.esb.Main"));


        wildcardMatcher = new WildcardMatcher("*.yamanyar.Abc.Test()", log,false);
        assertTrue(wildcardMatcher.match("com.yamanyar.Abc"));
        assertTrue(wildcardMatcher.matchMethod("com.yamanyar.Abc.Test()"));
        assertFalse(wildcardMatcher.match("com.yamanyar.Abcd"));

        wildcardMatcher = new WildcardMatcher("*.yamanyar.Ab*.Test()", log,false);
        assertTrue(wildcardMatcher.match("com.yamanyar.Abc"));
        assertTrue(wildcardMatcher.match("com.yamanyar.Abcd"));
        assertTrue(wildcardMatcher.matchMethod("com.yamanyar.Abcd.Test()"));
        assertFalse(wildcardMatcher.matchMethod("com.yamanyar.Abcd.UTest()"));

        wildcardMatcher = new WildcardMatcher("*.yamanyar.Ab*.Te*t()", log,false);
        assertTrue(wildcardMatcher.match("com.yamanyar.Abc"));
        assertTrue(wildcardMatcher.match("com.yamanyar.Abcd"));
        assertTrue(wildcardMatcher.matchMethod("com.yamanyar.Abcd.Test()"));
        assertFalse(wildcardMatcher.matchMethod("com.yamanyar.Abcd.UTest()"));
        assertTrue(wildcardMatcher.matchMethod("com.yamanyar.Abcd.TeUzzzZt()"));

    }
}
