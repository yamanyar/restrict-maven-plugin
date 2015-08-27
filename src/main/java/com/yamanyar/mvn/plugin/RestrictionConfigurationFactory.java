package com.yamanyar.mvn.plugin;

import com.yamanyar.mvn.plugin.utils.WildcardMatcher;
import org.apache.maven.plugin.logging.Log;

import java.util.*;

/**
 * Read restriction conf from array to map
 *
 * @author Kaan Yamanyar
 */
public class RestrictionConfigurationFactory {


    public RestrictionConfigurationFactory() {
    }

    /**
     * Reads restriction configurations and put them inside a map.
     * Restriction is "from" is represented by keys, "targets" are represented by values.
     * For example:
     * "org.*,com.* to java.lang.Thread"
     * In map view:
     * [key] org.*  to [value] java.lang.Thread
     * [key] com.*  to [value] java.lang.Thread
     *
     * @param restrictions Map of Restrictions:  "from" is represented by keys, "targets" are represented by values.
     * @param log logger
     * @return Returns restriction configs
     */
    public static Map<WildcardMatcher, Set<WildcardMatcher>> produceConfiguration(String[] restrictions, Log log) {
        Map<WildcardMatcher, Set<WildcardMatcher>> restrictionsMap;
        restrictionsMap = new HashMap<WildcardMatcher, Set<WildcardMatcher>>();

        for (String restriction : restrictions) {
            String token = " to ";
            int i = restriction.indexOf(token);
            if (i < 0 || i + 2 > restriction.length())
                throw new IllegalArgumentException("Please check restriction configuration:" + restriction);

            String fromPart = restriction.substring(0, i);
            String toPart = restriction.substring(i + token.length());
            List<WildcardMatcher> fromExceptions = new ArrayList<WildcardMatcher>();
            List<WildcardMatcher> toExceptions = new ArrayList<WildcardMatcher>();

            String[] fromList = fromPart.split(",");
            String[] toList = toPart.split(",");

            for (String to : toList) {

                String toString = to.trim();

                if ("!".equals(toString)) {
                    throw new IllegalArgumentException("Please check restriction configuration: (! must be followed by a pattern like !com.*)" + restriction);
                } else if (toString.startsWith("!")) {
                    toExceptions.add(new WildcardMatcher(toString.substring(1), log));
                } else {
                    for (String from : fromList) {
                        String fromString = from.trim();
                        if ("!".equals(fromString)) {
                            throw new IllegalArgumentException("Please check restriction configuration: (! must be followed by a pattern like !com.*)" + restriction);
                        } else if (fromString.startsWith("!")) {
                            fromExceptions.add(new WildcardMatcher(fromString.substring(1), log));
                        } else {
                            WildcardMatcher fromMatcher = new WildcardMatcher(fromString, log);
                            fromMatcher.setExceptions(fromExceptions);
                            Set<WildcardMatcher> toMatcherSet = getToMatcherSet(restrictionsMap, fromMatcher);
                            WildcardMatcher toMatcher = new WildcardMatcher(toString, log);
                            toMatcher.setExceptions(toExceptions);
                            toMatcherSet.add(toMatcher);
                        }

                    }
                }
            }

        }


        log.info("###### Restriction Rules (begins) ######");
        for (Map.Entry<WildcardMatcher, Set<WildcardMatcher>> entry : restrictionsMap.entrySet()) {
            WildcardMatcher from = entry.getKey();
            for (WildcardMatcher to : entry.getValue()) {

                String exceptFrom = from.getExceptions();
                String exceptTo = from.getExceptions();

                exceptFrom = exceptFrom == null ? "" : " (except " + exceptFrom + ")";
                exceptTo = exceptTo == null ? "" : " (except " + exceptTo + ")";

                String message = String.format("[%d-%d] Access from %s%s to %s%s will be not allowed.", from.getRuleNo(), to.getRuleNo(), from, exceptFrom, to, exceptTo);
                log.info(message);
            }
        }
        log.info("###### Restriction Rules (ends)   ######");
        return restrictionsMap;
    }

    private static Set<WildcardMatcher> getToMatcherSet(Map<WildcardMatcher, Set<WildcardMatcher>> restrictionsMap, WildcardMatcher fromMatcher) {
        Set<WildcardMatcher> toMatcherSet = restrictionsMap.get(fromMatcher);
        if (toMatcherSet == null) {
            toMatcherSet = new HashSet<WildcardMatcher>();
            restrictionsMap.put(fromMatcher, toMatcherSet);
        }
        return toMatcherSet;
    }

}