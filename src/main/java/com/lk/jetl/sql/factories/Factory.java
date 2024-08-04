package com.lk.jetl.sql.factories;

import com.lk.jetl.configuration.util.OptionRule;

public interface Factory {
    String factoryIdentifier();

    OptionRule optionRule();
}
