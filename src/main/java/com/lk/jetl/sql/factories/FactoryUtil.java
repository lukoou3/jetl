package com.lk.jetl.sql.factories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lk.jetl.configuration.Option;
import com.lk.jetl.configuration.Options;
import com.lk.jetl.configuration.ReadonlyConfig;
import com.lk.jetl.configuration.util.ConfigValidator;
import com.lk.jetl.format.DecodingFormat;
import com.lk.jetl.format.EncodingFormat;
import com.lk.jetl.sql.Row;
import com.lk.jetl.util.ServiceLoaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FactoryUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FactoryUtil.class);

    public static final Option<Map<String, Object>> FORMAT = Options.key("format")
                    .type(new TypeReference<Map<String, Object>>() {})
                    .defaultValue(Collections.EMPTY_MAP)
                    .withDescription("Defines the format identifier for encoding data. "
                                    + "The identifier is used to discover a suitable format factory.");
    public static final Option<String> FORMAT_TYPE = Options.key("format.type")
            .stringType()
            .defaultValue("json")
            .withDescription("format type");

    public static final Option<Integer> PARALLELISM = Options.key("parallelism")
            .intType()
            .defaultValue(1)
            .withDescription("parallelism");

    public static <T extends Factory> T discoverFactory(
            ClassLoader classLoader, Class<T> factoryClass, String factoryIdentifier) {
        final List<Factory> factories = discoverFactories(classLoader);

        final List<Factory> foundFactories =
                factories.stream()
                        .filter(f -> factoryClass.isAssignableFrom(f.getClass()))
                        .collect(Collectors.toList());

        if (foundFactories.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Could not find any factories that implement '%s' in the classpath.",
                            factoryClass.getName()));
        }

        final List<Factory> matchingFactories =
                foundFactories.stream()
                        .filter(f -> f.factoryIdentifier().equals(factoryIdentifier))
                        .collect(Collectors.toList());

        if (matchingFactories.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Could not find any factory for identifier '%s' that implements '%s' in the classpath.\n\n"
                                    + "Available factory identifiers are:\n\n"
                                    + "%s",
                            factoryIdentifier,
                            factoryClass.getName(),
                            foundFactories.stream()
                                    .map(Factory::factoryIdentifier)
                                    .distinct()
                                    .sorted()
                                    .collect(Collectors.joining("\n"))));
        }
        if (matchingFactories.size() > 1) {
            throw new IllegalArgumentException(
                    String.format(
                            "Multiple factories for identifier '%s' that implement '%s' found in the classpath.\n\n"
                                    + "Ambiguous factory classes are:\n\n"
                                    + "%s",
                            factoryIdentifier,
                            factoryClass.getName(),
                            matchingFactories.stream()
                                    .map(f -> f.getClass().getName())
                                    .sorted()
                                    .collect(Collectors.joining("\n"))));
        }

        return (T) matchingFactories.get(0);
    }

    static List<Factory> discoverFactories(ClassLoader classLoader) {
        final List<Factory> result = new LinkedList<>();
        ServiceLoaderUtil.load(Factory.class, classLoader)
                .forEach(
                        loadResult -> {
                            if (loadResult.hasFailed()) {
                                if (loadResult.getError() instanceof NoClassDefFoundError) {
                                    LOG.debug(
                                            "NoClassDefFoundError when loading a "
                                                    +  Factory.class
                                                    + ". This is expected when trying to load a format dependency but no flink-connector-files is loaded.",
                                            loadResult.getError());
                                    // After logging, we just ignore this failure
                                    return;
                                }
                                throw new IllegalArgumentException(
                                        "Unexpected error when trying to load service provider for factories.",
                                        loadResult.getError());
                            }
                            result.add(loadResult.getService());
                        });
        return result;
    }

    public static <T extends TableFactory> T discoverTableFactory(
            Class<T> factoryClass, String connector) {
        return discoverFactory(Thread.currentThread().getContextClassLoader(), factoryClass, connector);
    }

    public static <T extends TransformFactory> T discoverTransformFactory(
            Class<T> factoryClass, String type) {
        return discoverFactory(Thread.currentThread().getContextClassLoader(), factoryClass, type);
    }

    public static <T extends DecodingFormatFactory> DecodingFormat<Row> discoverDecodingFormat(
            Class<T> factoryClass, ReadonlyConfig options, String type) {
        T formatFactory = discoverDecodingFormatFactory(factoryClass, type);
        ConfigValidator.of(options).validate(formatFactory.optionRule());
        return formatFactory.createDecodingFormat(options);
    }

    public static <T extends DecodingFormatFactory> T discoverDecodingFormatFactory(
            Class<T> factoryClass, String type) {
        return discoverFactory(Thread.currentThread().getContextClassLoader(), factoryClass, type);
    }

    public static <T extends EncodingFormatFactory> EncodingFormat<Row> discoverEncodingFormat(
            Class<T> factoryClass, ReadonlyConfig options, String type) {
        T formatFactory = discoverEncodingFormatFactory(factoryClass, type);
        ConfigValidator.of(options).validate(formatFactory.optionRule());
        return formatFactory.createEncodingFormat(options);
    }

    public static <T extends EncodingFormatFactory> T discoverEncodingFormatFactory(
            Class<T> factoryClass, String type) {
        return discoverFactory(Thread.currentThread().getContextClassLoader(), factoryClass, type);
    }
}
