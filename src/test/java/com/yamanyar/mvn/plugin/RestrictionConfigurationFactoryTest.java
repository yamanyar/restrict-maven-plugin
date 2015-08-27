package com.yamanyar.mvn.plugin;

import com.yamanyar.mvn.plugin.utils.WildcardMatcher;
import org.apache.maven.plugin.logging.Log;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static com.yamanyar.mvn.plugin.RCTestHelper.t;
import static org.junit.Assert.*;

/**
 * Test following functionality: Reads restriction configurations and put them inside a map
 *
 * @author Kaan Yamanyar
 */
public class RestrictionConfigurationFactoryTest {
    @Test
    public void testProduceConfiguration() throws Exception {

        String[] testArray = {"org.*,com.*,!com.yamanyar.* to java.lang.Thread",
                "com.yamanyar.*,org.yamanyar.* to java.awt.color.*,java.beans.AppletInitializer",
                "com.yamanyar.* to java.awt.color.Red",
                "fromNone.* to *,!com.yamanyar.*",
                "* to toNone",
        };
        Log mockLog = EasyMock.createMock(Log.class);
        RCTestHelper.logger = mockLog;
        mockLog.info(EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();
        mockLog.debug(EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockLog);

        Map<WildcardMatcher, Set<WildcardMatcher>> rMap = RestrictionConfigurationFactory.produceConfiguration(testArray, mockLog);
        assertTrue(rMap.size() == 6);

        //Check if key com.yamanyar.* has values [java.beans.AppletInitializer, java.awt.color.Red, java.awt.color.*]
        Set<WildcardMatcher> rule_1 = rMap.get(t("com.yamanyar.*"));
        assertTrue(rule_1.contains(t("java.beans.AppletInitializer")));
        assertTrue(rule_1.contains(t("java.awt.color.Red")));
        assertTrue(rule_1.contains(t("java.awt.color.*")));
        //Check if key com.yamanyar.* has only 3 values. (no extra value check)
        assertTrue(rule_1.size() == 3);

        Set<WildcardMatcher> toWildcards = rMap.keySet();
        boolean fromExceptionsTested = false;
        for (WildcardMatcher toWildcard : toWildcards) {
            if (t("com.*").equals(toWildcard)) {
                assertTrue(toWildcard.match("com.microsoft"));
                assertFalse(toWildcard.match("com.yamanyar.test"));
                fromExceptionsTested = true;
            }
        }
        if (!fromExceptionsTested) fail("Exception is not found!");


        //Check if key org.yamanyar.* has values [java.beans.AppletInitializer, java.awt.color.*]
        Set<WildcardMatcher> rule_2 = rMap.get(t("org.yamanyar.*"));
        assertTrue(rule_2.contains(t("java.beans.AppletInitializer")));
        assertTrue(rule_2.contains(t("java.awt.color.*")));
        //Check if key org.yamanyar.* has only 2 values. (no extra value check)
        assertTrue(rule_2.size() == 2);

        //Check if key fromNone.* has values [*]
        Set<WildcardMatcher> rule_3 = rMap.get(t("fromNone.*"));
        assertTrue(rule_3.contains(t("*")));
        //Check if key fromNone.* has only 1 values. (no extra value check)
        assertTrue(rule_3.size() == 1);
        WildcardMatcher exceptionTest = rule_3.iterator().next();
        assertTrue(exceptionTest.match("anyValue"));
        assertTrue(exceptionTest.match("com.other.Abc"));
        assertFalse(exceptionTest.match("com.yamanyar.text.Abc"));

        //Check if key * has values [toNone]
        Set<WildcardMatcher> rule_4 = rMap.get(t("*"));
        assertTrue(rule_4.contains(t("toNone")));
        //Check if key * has only 1 values. (no extra value check)
        assertTrue(rule_4.size() == 1);

        //Check if key com.* has values [java.lang.Thread]
        Set<WildcardMatcher> rule_5 = rMap.get(t("com.*"));
        assertTrue(rule_5.contains(t("java.lang.Thread")));
        //Check if key com.* has only 1 values. (no extra value check)
        assertTrue(rule_5.size() == 1);

        //Check if key org.* has values [java.lang.Thread]
        Set<WildcardMatcher> rule_6 = rMap.get(t("org.*"));
        assertTrue(rule_6.contains(t("java.lang.Thread")));
        //Check if key org.* has only 1 values. (no extra value check)
        assertTrue(rule_6.size() == 1);





    }


}

class RCTestHelper {
    static Log logger;

    static WildcardMatcher t(String pattern) {
        return new WildcardMatcher(pattern, logger);
    }
}
