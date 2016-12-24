import com.google.gson.JsonObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.strawberry.ui.controllers.org.strawberry.ui.util.ConvertInfixToPrefix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by saipkri on 23/12/16.
 */

public class Scratchpad {
    public static void main(String[] args) {
        List<String> ops = Arrays.asList("=","<","!=", "<=", ">=");
//        ['Name'].equals('Sai') && ['Age'] < 20
        String expr = "Name = 'Sai' and (Age < 20 or XX <=20)";
        String orig = expr;
        expr = expr.replace("or", "or ")
                .replace("and", " and")
                .replace("(", " ( ")
                .replace(")", " ) ")
                .replace("<=", " <= ")
                .replace(">=", " >= ")
                .replace(">", " > ")
                .replace("<", " < ")
                .replace("=", " = ")
                .replace("!=", " != ")
                .replace("<  =", " <=")
                .replace(">  =", " >=")
                .replace("!  =", " !=");
        System.out.println(expr);
        List<String> fieldNames = new ArrayList<>();
        List<String> tokens = Stream.of(expr.split(" ")).filter(s -> s.trim().length() > 0).collect(Collectors.toList());
        for(int i=0; i<tokens.size(); i++) {
                if (ops.contains(tokens.get(i))) {
                    fieldNames.add(tokens.get(i - 1));
                }
        }
        System.out.println(fieldNames);
        for(String fieldName: fieldNames) {
        }
    }

}
