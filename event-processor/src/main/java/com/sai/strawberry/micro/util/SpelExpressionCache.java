package com.sai.strawberry.micro.util;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by saipkri on 23/12/16.
 */
public final class SpelExpressionCache {
    private static ConcurrentHashMap<String, Expression> COMPILED_EXPRESSION_CACHE = new ConcurrentHashMap<>();
    private static final ExpressionParser expressionParser = new SpelExpressionParser();

    public static Expression compiledExpression(final String expression) {
        return COMPILED_EXPRESSION_CACHE.computeIfAbsent(expression, expressionParser::parseExpression);
    }
}
