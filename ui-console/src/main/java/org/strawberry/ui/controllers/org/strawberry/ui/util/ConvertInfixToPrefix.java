package org.strawberry.ui.controllers.org.strawberry.ui.util;

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ConvertInfixToPrefix {
    public static String[] operators = {"and", "or"};

    public String convert(String infix) {
        StringBuffer sb = new StringBuffer(infix);
        int index = sb.length();
        String operand = "";
        int steps = 0;
        for (int i = 0; i < sb.length(); i++) {
            for (int x = 0; x < operators.length; x++) {
                int tempIndex = sb.indexOf(operators[x], i);
                if (tempIndex < index && tempIndex >= 0) {
                    index = tempIndex;
                    operand = operators[x];
                }
            }
            if (index == sb.length()) {
                break;
            }
            sb.delete(index, index + operand.length() + 1);
            for (int x = index - 1; x >= 0; x--) {
                if (sb.charAt(x) == '(' && steps == 0) {
                    sb.insert(x, operand);
                    break;
                } else if (sb.charAt(x) == '(') {
                    steps--;
                } else if (sb.charAt(x) == ')') {
                    steps++;
                }
            }
            i = index;
            index = sb.length();
        }
        return sb.toString();
    }
}