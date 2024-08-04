package com.lk.jetl.configuration;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class SingleChoiceOption<T> extends Option<T> {

    private final List<T> optionValues;

    public SingleChoiceOption(
            String key, TypeReference<T> typeReference, List<T> optionValues, T defaultValue) {
        super(key, typeReference, defaultValue);
        this.optionValues = optionValues;
    }

    public List<T> getOptionValues() {
        return optionValues;
    }

    @Override
    public SingleChoiceOption<T> withDescription(String description) {
        this.description = description;
        return this;
    }

}
