package com.lk.jetl.configuration.util;

import com.lk.jetl.configuration.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Validation rule for {@link Option}.
 *
 * <p>The option rule is typically built in one of the following pattern:
 *
 * <pre>{@code
 * // simple rule
 * OptionRule simpleRule = OptionRule.builder()
 *     .optional(POLL_TIMEOUT, POLL_INTERVAL)
 *     .required(CLIENT_SERVICE_URL)
 *     .build();
 *
 * // basic full rule
 * OptionRule fullRule = OptionRule.builder()
 *     .optional(POLL_TIMEOUT, POLL_INTERVAL, CURSOR_STARTUP_MODE)
 *     .required(CLIENT_SERVICE_URL, ADMIN_SERVICE_URL)
 *     .exclusive(TOPIC_PATTERN, TOPIC)
 *     .conditional(CURSOR_STARTUP_MODE, StartMode.TIMESTAMP, CURSOR_STARTUP_TIMESTAMP)
 *     .build();
 *
 * // complex conditional rule
 * // moot expression
 * Expression expression = Expression.of(TOPIC_DISCOVERY_INTERVAL, 200)
 *     .and(Expression.of(Condition.of(CURSOR_STARTUP_MODE, StartMode.EARLIEST)
 *         .or(CURSOR_STARTUP_MODE, StartMode.LATEST)))
 *     .or(Expression.of(Condition.of(TOPIC_DISCOVERY_INTERVAL, 100)))
 *
 * OptionRule complexRule = OptionRule.builder()
 *     .optional(POLL_TIMEOUT, POLL_INTERVAL, CURSOR_STARTUP_MODE)
 *     .required(CLIENT_SERVICE_URL, ADMIN_SERVICE_URL)
 *     .exclusive(TOPIC_PATTERN, TOPIC)
 *     .conditional(expression, CURSOR_RESET_MODE)
 *     .build();
 * }</pre>
 */
public class OptionRule {

    /**
     * Optional options with default value.
     *
     * <p>This options will not be validated.
     *
     * <p>This is used by the web-UI to show what options are available.
     */
    private final List<Option<?>> optionalOptions;

    /**
     * Required options with no default value.
     *
     * <p>Verify that the option is valid through the defined rules.
     */
    private final List<RequiredOption> requiredOptions;

    OptionRule(List<Option<?>> optionalOptions, List<RequiredOption> requiredOptions) {
        this.optionalOptions = optionalOptions;
        this.requiredOptions = requiredOptions;
    }

    public List<Option<?>> getOptionalOptions() {
        return optionalOptions;
    }

    public List<RequiredOption> getRequiredOptions() {
        return requiredOptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OptionRule)) {
            return false;
        }
        OptionRule that = (OptionRule) o;
        return Objects.equals(optionalOptions, that.optionalOptions)
                && Objects.equals(requiredOptions, that.requiredOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionalOptions, requiredOptions);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link OptionRule}. */
    public static class Builder {
        private final List<Option<?>> optionalOptions = new ArrayList<>();
        private final List<RequiredOption> requiredOptions = new ArrayList<>();

        private Builder() {}

        /**
         * Optional options
         *
         * <p>This options will not be validated.
         *
         * <p>This is used by the web-UI to show what options are available.
         */
        public Builder optional(Option<?>... options) {
            for (Option<?> option : options) {
                verifyOptionOptionsDuplicate(option, "OptionsOption");
            }
            this.optionalOptions.addAll(Arrays.asList(options));
            return this;
        }

        /** Absolutely required options without any constraints. */
        public Builder required(Option<?>... options) {
            RequiredOption.AbsolutelyRequiredOptions requiredOption =
                    RequiredOption.AbsolutelyRequiredOptions.of(options);
            verifyRequiredOptionDuplicate(requiredOption);
            this.requiredOptions.add(requiredOption);
            return this;
        }

        /** Exclusive options, only one of the options needs to be configured. */
        public Builder exclusive(Option<?>... options) {
            if (options.length <= 1) {
                throw new IllegalArgumentException(
                        "The number of exclusive options must be greater than 1.");
            }
            RequiredOption.ExclusiveRequiredOptions exclusiveRequiredOption =
                    RequiredOption.ExclusiveRequiredOptions.of(options);
            verifyRequiredOptionDuplicate(exclusiveRequiredOption);
            this.requiredOptions.add(exclusiveRequiredOption);
            return this;
        }

        public <T> Builder conditional(
                Option<T> conditionalOption,
                List<T> expectValues,
                Option<?>... requiredOptions) {
            verifyConditionalExists(conditionalOption);

            if (expectValues.size() == 0) {
                throw new IllegalArgumentException(
                        String.format(
                                "conditional option '%s' must have expect values .",
                                conditionalOption.key()));
            }

            /** Each parameter can only be controlled by one other parameter */
            Expression expression =
                    Expression.of(Condition.of(conditionalOption, expectValues.get(0)));
            for (int i = 0; i < expectValues.size(); i++) {
                if (i != 0) {
                    expression =
                            expression.or(
                                    Expression.of(
                                            Condition.of(conditionalOption, expectValues.get(i))));
                }
            }

            RequiredOption.ConditionalRequiredOptions option =
                    RequiredOption.ConditionalRequiredOptions.of(
                            expression, new ArrayList<>(Arrays.asList(requiredOptions)));
            verifyRequiredOptionDuplicate(option);
            this.requiredOptions.add(option);
            return this;
        }

        public <T> Builder conditional(
                Option<T> conditionalOption,
                T expectValue,
                Option<?>... requiredOptions) {
            verifyConditionalExists(conditionalOption);

            /** Each parameter can only be controlled by one other parameter */
            Expression expression = Expression.of(Condition.of(conditionalOption, expectValue));
            RequiredOption.ConditionalRequiredOptions conditionalRequiredOption =
                    RequiredOption.ConditionalRequiredOptions.of(
                            expression, new ArrayList<>(Arrays.asList(requiredOptions)));

            verifyRequiredOptionDuplicate(conditionalRequiredOption);
            this.requiredOptions.add(conditionalRequiredOption);
            return this;
        }

        /** Bundled options, must be present or absent together. */
        public Builder bundled(Option<?>... requiredOptions) {
            RequiredOption.BundledRequiredOptions bundledRequiredOption =
                    RequiredOption.BundledRequiredOptions.of(requiredOptions);
            verifyRequiredOptionDuplicate(bundledRequiredOption);
            this.requiredOptions.add(bundledRequiredOption);
            return this;
        }

        public OptionRule build() {
            return new OptionRule(optionalOptions, requiredOptions);
        }

        private void verifyRequiredOptionDefaultValue(Option<?> option) {
            if (option.defaultValue() != null) {
                throw new IllegalArgumentException(
                        String.format(
                                "Required option '%s' should have no default value.",
                                option.key()));
            }
        }

        private void verifyDuplicateWithOptionOptions(
                Option<?> option, String currentOptionType) {
            if (optionalOptions.contains(option)) {
                throw new IllegalArgumentException(
                        String.format(
                                "%s '%s' duplicate in option options.",
                                currentOptionType, option.key()));
            }
        }

        private void verifyRequiredOptionDuplicate(RequiredOption requiredOption) {
            requiredOption
                    .getOptions()
                    .forEach(
                            option -> {
                                verifyDuplicateWithOptionOptions(
                                        option, requiredOption.getClass().getSimpleName());
                                requiredOptions.forEach(
                                        ro -> {
                                            if (ro
                                                            instanceof
                                                            RequiredOption
                                                                    .ConditionalRequiredOptions
                                                    && requiredOption
                                                            instanceof
                                                            RequiredOption
                                                                    .ConditionalRequiredOptions) {
                                                Option<?> requiredOptionCondition =
                                                        ((RequiredOption.ConditionalRequiredOptions)
                                                                        requiredOption)
                                                                .getExpression()
                                                                .getCondition()
                                                                .getOption();

                                                Option<?> roOptionCondition =
                                                        ((RequiredOption.ConditionalRequiredOptions)
                                                                        ro)
                                                                .getExpression()
                                                                .getCondition()
                                                                .getOption();

                                                if (ro.getOptions().contains(option)
                                                        && !requiredOptionCondition.equals(
                                                                roOptionCondition)) {
                                                    throw new IllegalArgumentException(
                                                            String.format(
                                                                    "%s '%s' duplicate in %s options.",
                                                                    requiredOption
                                                                            .getClass()
                                                                            .getSimpleName(),
                                                                    option.key(),
                                                                    ro.getClass().getSimpleName()));
                                                }
                                            } else {
                                                if (ro.getOptions().contains(option)) {
                                                    throw new IllegalArgumentException(
                                                            String.format(
                                                                    "%s '%s' duplicate in %s options.",
                                                                    requiredOption
                                                                            .getClass()
                                                                            .getSimpleName(),
                                                                    option.key(),
                                                                    ro.getClass().getSimpleName()));
                                                }
                                            }
                                        });
                            });
        }

        private void verifyOptionOptionsDuplicate(
                Option<?> option, String currentOptionType) {
            verifyDuplicateWithOptionOptions(option, currentOptionType);

            requiredOptions.forEach(
                    requiredOption -> {
                        if (requiredOption.getOptions().contains(option)) {
                            throw new IllegalArgumentException(
                                    String.format(
                                            "%s '%s' duplicate in '%s'.",
                                            currentOptionType,
                                            option.key(),
                                            requiredOption.getClass().getSimpleName()));
                        }
                    });
        }

        private void verifyConditionalExists(Option<?> option) {
            boolean inOptions = optionalOptions.contains(option);
            AtomicBoolean inRequired = new AtomicBoolean(false);
            requiredOptions.forEach(
                    requiredOption -> {
                        if (requiredOption.getOptions().contains(option)) {
                            inRequired.set(true);
                        }
                    });

            if (!inOptions && !inRequired.get()) {
                throw new IllegalArgumentException(
                        String.format("Conditional '%s' not found in options.", option.key()));
            }
        }
    }
}
