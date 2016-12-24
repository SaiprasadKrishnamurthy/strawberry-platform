package org.strawberry.ui.controllers.org.strawberry.ui.util;

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Stack;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by saipkri on 24/12/16.
 */
public final class QueryUtil {

    public static String esQuery(final String infix) throws Exception {
//        infix = "(" + infix + ")";
        ConvertInfixToPrefix ConvertInfixToPrefix = new ConvertInfixToPrefix();
        String prefix = ConvertInfixToPrefix.convert(infix);
        prefix = prefix.replace("or", "or ")
                .replace("and", " and")
                .replace("(", " ( ")
                .replace(")", " ) ")
                .replace(" = ", "=").replace(" =", "=").replace("= ", "=")
                .replace(" <= ", "<=").replace(" <=", "<=").replace("<= ", "<=")
                .replace(" >= ", ">=").replace(" >=", ">=").replace(">= ", ">=")
                .replace(" > ", ">").replace(" >", ">").replace("> ", ">")
                .replace(" < ", "<").replace(" <", "<").replace("< ", "<")
                .replace(" != ", "!=").replace(" !=", "!=").replace("!= ", "!=");
//
        prefix = "and ( " + prefix + " ) ";
        System.out.println(prefix);


        XContentBuilder builder = jsonBuilder().prettyPrint();
        builder.startObject()
                .startObject("query")
                .startObject("filtered")
                .startObject("query")
                .startObject("match_all")
                .endObject()
                .endObject()
                .startObject("filter")
                .startObject("bool");

        // build conditions.
        buildConditions(builder, prefix);

        builder.endObject()
                .endObject()
                .endObject()
                .endObject()
                .endObject();


        return builder.string();


    }

    private static void buildConditions(final XContentBuilder builder, final String prefix) throws Exception {
        String[] tokens = prefix.split(" ");
        Stack<String> expressions = new Stack<>();
        Stack<String> booleans = new Stack<>();
        for (String token : tokens) {
            if (token.trim().length() > 0) {
                if (token.equals("and") || token.equals("or")) {
                    booleans.push(token);
                } else if (!token.equals(")")) {
                    expressions.push(token);
                } else {
                    String bool = booleans.pop();
                    if (bool.equals("and")) {
                        builder.startArray("must");
                    } else if (bool.equals("or")) {
                        builder.startArray("should");
                    }
                    while (!expressions.peek().equals("(")) {
                        String expr = expressions.pop();
                        if (expr.contains(">=")) {
                            String[] split = expr.split(">=");
                            rangeQuery(builder, split, "gte");
                        } else if (expr.contains("<=")) {
                            String[] split = expr.split("<=");
                            rangeQuery(builder, split, "lte");
                        } else if (expr.contains(">")) {
                            String[] split = expr.split(">");
                            rangeQuery(builder, split, "gt");
                        } else if (expr.contains("<")) {
                            String[] split = expr.split("<");
                            rangeQuery(builder, split, "lt");
                        } else if (expr.contains("!=")) {
                            String[] split = expr.split("!=");
                            builder.startObject()
                                    .startObject("not")
                                    .startObject("term")
                                    .field(split[0], split[1].replace("'", ""))
                                    .endObject()
                                    .endObject()
                                    .endObject();
                        } else if (expr.contains("=")) {
                            String[] split = expr.split("=");
                            builder.startObject()
                                    .startObject("term")
                                    .field(split[0], split[1].replace("'", ""))
                                    .endObject()
                                    .endObject();
                        }
                    }
                    expressions.pop();
                    builder.endArray();
                }
            }
        }
    }

    private static void rangeQuery(XContentBuilder builder, String[] split, String clause) throws IOException {
        builder.startObject()
                .startObject("range")
                .startObject(split[0])
                .field(clause, new BigDecimal(split[1].replace("'", "")))
                .endObject()
                .endObject()
                .endObject();
    }
}
