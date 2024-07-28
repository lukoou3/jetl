package com.lk.jetl.sql.analysis;


import com.lk.jetl.sql.expressions.Cast;
import com.lk.jetl.sql.expressions.Expression;
import com.lk.jetl.sql.expressions.arithmetic.Add;
import com.lk.jetl.sql.expressions.conditional.If;
import com.lk.jetl.sql.expressions.string.Substring;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class FunctionRegistryUtils {
    static Map<String, FunctionBuilder> expressions = new HashMap<>();

    static {
        expressions.put("if", expression(If.class));
        expressions.put("substr", expression(Substring.class));
        expressions.put("substring", expression(Substring.class));
        expressions.put("cast", expression(Cast.class));
    }

    // lookupFunction(name: FunctionIdentifier, children: Seq[Expression]): Expression
    public static Expression lookupFunction(String name, List<Expression> args) {
        FunctionBuilder func = expressions.get(name);
        if (func == null) {
            throw new IllegalArgumentException("undefined function " + name);
        }
        return func.build(name, args);
    }

    private static FunctionBuilder expression(Class<? extends Expression> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        // See if we can find a constructor that accepts Seq[Expression]
        // pass

        Constructor<?> varargCtor = Arrays.stream(constructors).filter(x -> {
            Class<?>[] parameterTypes = x.getParameterTypes();
            return parameterTypes.length == 1 && parameterTypes[0] == List.class;
        }).findFirst().orElse(null);
        if (varargCtor != null) {
            // If there is an apply method that accepts Seq[Expression], use that one.
            return (name, expressions) -> {
                try {
                    return (Expression) varargCtor.newInstance(expressions);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }

        // Otherwise, find a constructor method that matches the number of arguments, and use that.
        return (name, expressions) -> {
            Constructor<?> constructor = Arrays.stream(constructors).filter(x -> {
                Class<?>[] parameterTypes = x.getParameterTypes();
                return parameterTypes.length == expressions.size() && Arrays.stream(parameterTypes).allMatch(c -> c == Expression.class);
            }).findFirst().orElse(null);
            if (constructor == null) {
                List<Integer> validParametersCount = new ArrayList<>();
                for (Constructor<?> contr : constructors) {
                    if (Arrays.stream(contr.getParameterTypes()).allMatch(c -> c == Expression.class)) {
                        validParametersCount.add(contr.getParameterCount());
                    }
                }
                Collections.sort(validParametersCount);
                String invalidArgumentsMsg;
                if (validParametersCount.size() == 0) {
                    invalidArgumentsMsg = "Invalid arguments for function " + name;
                } else {
                    String expectedNumberOfParameters;
                    if (validParametersCount.size() == 1) {
                        expectedNumberOfParameters = validParametersCount.get(0).toString();
                    } else {
                        expectedNumberOfParameters = validParametersCount.stream().map(x -> x.toString()).collect(Collectors.joining(","));
                    }
                    invalidArgumentsMsg = String.format("Invalid number of arguments for function %s. Expected: %s; Found: %d", name, expectedNumberOfParameters, expressions.size());
                }
                throw new IllegalArgumentException(invalidArgumentsMsg);
            }
            try {
                return (Expression) constructor.newInstance(expressions.toArray());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    public interface FunctionBuilder {
        Expression build(String name, List<Expression> expressions);
    }
}
