package com.lk.jetl.sql.analysis;


import com.lk.jetl.sql.api.UDF;
import com.lk.jetl.sql.expressions.*;
import com.lk.jetl.sql.expressions.conditional.*;
import com.lk.jetl.sql.expressions.nvl.*;
import com.lk.jetl.sql.expressions.regexp.RegExpExtract;
import com.lk.jetl.sql.expressions.regexp.StringSplit;
import com.lk.jetl.sql.expressions.string.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class FunctionRegistryUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FunctionRegistryUtils.class);
    static Map<String, FunctionBuilder> expressions = new HashMap<>();

    static {
        // misc
        expressions.put("if", expression(If.class));
        expressions.put("coalesce", expression(Coalesce.class));
        expressions.put("isnull", expression(IsNull.class));
        expressions.put("isnotnull", expression(IsNotNull.class));
        // math functions
        // string functions
        expressions.put("length", expression(Length.class));
        expressions.put("split", expression(StringSplit.class));
        expressions.put("substr", expression(Substring.class));
        expressions.put("substring", expression(Substring.class));
        expressions.put("trim", expression(StringTrim.class));
        expressions.put("ltrim", expression(StringTrimLeft.class));
        expressions.put("rtrim", expression(StringTrimRight.class));
        expressions.put("replace", expression(StringReplace.class));
        expressions.put("regexp_extract", expression(RegExpExtract.class));
        // cast
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

    public static void register(String name, UDF f){
        FunctionBuilder builder =  (n, expressions) -> {
            if(f.inputTypes().size() != expressions.size()){
                throw  new IllegalArgumentException("Invalid number of arguments for function " + n + ". Expected: " + f.inputTypes().size() + "; Found: " + expressions.size());
            }
            return new UDFExpression(f, expressions);
        };
        registerFunction(name, builder);
    }

    private static void registerFunction(String name, FunctionBuilder builder){
        String normalizedName = name.toLowerCase();
        FunctionBuilder previous = expressions.put(normalizedName, builder);
        if (previous != null) {
            LOG.warn("The function {} replaced a previously registered function.", normalizedName);
        }
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
