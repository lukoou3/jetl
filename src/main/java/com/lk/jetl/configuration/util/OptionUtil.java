package com.lk.jetl.configuration.util;

import com.lk.jetl.configuration.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptionUtil {

    private OptionUtil() {}

    public static String getOptionKeys(List<Option<?>> options) {
        StringBuilder builder = new StringBuilder();
        boolean flag = false;
        for (Option<?> option : options) {
            if (flag) {
                builder.append(", ");
            }
            builder.append("'").append(option.key()).append("'");
            flag = true;
        }
        return builder.toString();
    }

    public static String getOptionKeys(
            List<Option<?>> options, List<RequiredOption.BundledRequiredOptions> bundledOptions) {
        List<List<Option<?>>> optionList = new ArrayList<>();
        for (Option<?> option : options) {
            optionList.add(Collections.singletonList(option));
        }
        for (RequiredOption.BundledRequiredOptions bundledOption : bundledOptions) {
            optionList.add(bundledOption.getRequiredOption());
        }
        boolean flag = false;
        StringBuilder builder = new StringBuilder();
        for (List<Option<?>> optionSet : optionList) {
            if (flag) {
                builder.append(", ");
            }
            builder.append("[").append(getOptionKeys(optionSet)).append("]");
            flag = true;
        }
        return builder.toString();
    }

    private static String formatUnderScoreCase(String camel) {
        StringBuilder underScore =
                new StringBuilder(String.valueOf(Character.toLowerCase(camel.charAt(0))));
        for (int i = 1; i < camel.length(); i++) {
            char c = camel.charAt(i);
            underScore.append(Character.isLowerCase(c) ? c : "_" + Character.toLowerCase(c));
        }
        return underScore.toString();
    }
}
