package com.lk.jetl.configuration.util;


import com.lk.jetl.configuration.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.lk.jetl.configuration.util.OptionUtil.getOptionKeys;

public interface RequiredOption {

    List<Option<?>> getOptions();

    /** These options are mutually exclusive, allowing only one set of options to be configured. */
    class ExclusiveRequiredOptions implements RequiredOption {
        private final List<Option<?>> exclusiveOptions;

        public ExclusiveRequiredOptions(List<Option<?>> exclusiveOptions) {
            this.exclusiveOptions = exclusiveOptions;
        }

        public static ExclusiveRequiredOptions of(Option<?>... options) {
            return new ExclusiveRequiredOptions(new ArrayList<>(Arrays.asList(options)));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ExclusiveRequiredOptions)) {
                return false;
            }
            ExclusiveRequiredOptions that = (ExclusiveRequiredOptions) obj;
            return Objects.equals(this.exclusiveOptions, that.exclusiveOptions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exclusiveOptions);
        }

        @Override
        public String toString() {
            return String.format(
                    "Exclusive required set options: %s", getOptionKeys(exclusiveOptions));
        }

        @Override
        public List<Option<?>> getOptions() {
            return exclusiveOptions;
        }

        public List<Option<?>> getExclusiveOptions() {
            return exclusiveOptions;
        }
    }

    /** The option is required. */
    class AbsolutelyRequiredOptions implements RequiredOption {
        private final List<Option<?>> requiredOption;

        AbsolutelyRequiredOptions(List<Option<?>> requiredOption) {
            this.requiredOption = requiredOption;
        }

        public static AbsolutelyRequiredOptions of(Option<?>... requiredOption) {
            return new AbsolutelyRequiredOptions(new ArrayList<>(Arrays.asList(requiredOption)));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbsolutelyRequiredOptions)) {
                return false;
            }
            AbsolutelyRequiredOptions that = (AbsolutelyRequiredOptions) obj;
            return Objects.equals(this.requiredOption, that.requiredOption);
        }

        @Override
        public int hashCode() {
            return this.requiredOption.hashCode();
        }

        @Override
        public String toString() {
            return String.format(
                    "Absolutely required options: '%s'", getOptionKeys(requiredOption));
        }

        @Override
        public List<Option<?>> getOptions() {
            return requiredOption;
        }

        public List<Option<?>> getRequiredOption() {
            return requiredOption;
        }
    }

    class ConditionalRequiredOptions implements RequiredOption {
        private final Expression expression;
        private final List<Option<?>> requiredOption;

        ConditionalRequiredOptions(Expression expression, List<Option<?>> requiredOption) {
            this.expression = expression;
            this.requiredOption = requiredOption;
        }

        public static ConditionalRequiredOptions of(
                Expression expression, List<Option<?>> requiredOption) {
            return new ConditionalRequiredOptions(expression, requiredOption);
        }

        public static ConditionalRequiredOptions of(
                Condition<?> condition, List<Option<?>> requiredOption) {
            return new ConditionalRequiredOptions(Expression.of(condition), requiredOption);
        }

        public Expression getExpression() {
            return expression;
        }

        public List<Option<?>> getRequiredOption() {
            return requiredOption;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ConditionalRequiredOptions)) {
                return false;
            }
            ConditionalRequiredOptions that = (ConditionalRequiredOptions) obj;
            return Objects.equals(this.expression, that.expression)
                    && Objects.equals(this.requiredOption, that.requiredOption);
        }

        @Override
        public int hashCode() {
            return this.requiredOption.hashCode();
        }

        @Override
        public String toString() {
            return String.format(
                    "Condition expression: %s, Required options: %s",
                    expression, getOptionKeys(requiredOption));
        }

        @Override
        public List<Option<?>> getOptions() {
            return requiredOption;
        }
    }

    /** These options are bundled, must be present or absent together. */
    class BundledRequiredOptions implements RequiredOption {
        private final List<Option<?>> requiredOption;

        BundledRequiredOptions(List<Option<?>> requiredOption) {
            this.requiredOption = requiredOption;
        }

        public static BundledRequiredOptions of(Option<?>... requiredOption) {
            return new BundledRequiredOptions(new ArrayList<>(Arrays.asList(requiredOption)));
        }

        public static BundledRequiredOptions of(List<Option<?>> requiredOption) {
            return new BundledRequiredOptions(requiredOption);
        }

        public List<Option<?>> getRequiredOption() {
            return requiredOption;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BundledRequiredOptions)) {
                return false;
            }
            BundledRequiredOptions that = (BundledRequiredOptions) obj;
            return Objects.equals(this.requiredOption, that.requiredOption);
        }

        @Override
        public int hashCode() {
            return this.requiredOption.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Bundled Required options: %s", getOptionKeys(requiredOption));
        }

        @Override
        public List<Option<?>> getOptions() {
            return requiredOption;
        }
    }
}
