package com.sai.strawberry.micro;

import com.datastax.driver.core.Cluster;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.CustomProcessorHook;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.MongoBackedDataTransformer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by saipkri on 17/11/16.
 */
public class Scratchpad {
    public static void mains(String[] args) throws Exception {

        /*String sql = "CREATE TABLE IF NOT EXISTS card_txns(ID INT PRIMARY KEY, NAME VARCHAR(255))";
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:h2:mem:card-txns;DB_CLOSE_DELAY=-1");
        ds.setDriverClassName("org.h2.Driver");
        ds.setInitialSize(10);
        ds.setMaxTotal(30);
        ds.setPoolPreparedStatements(true);
        ds.setMaxOpenPreparedStatements(30);

        JdbcTemplate j = new JdbcTemplate(ds);
        j.execute(sql);

        j.query("select * from card_txns", new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {

                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println(rsmd.getColumnName(i));
                    System.out.println(rsmd.getColumnClassName(i));
                }
                return columnCount;
            }
        });*/

        /*ObjectMapper m = new ObjectMapper();
        System.out.println(m.readValue(new FileInputStream("/Users/saipkri/learning/new/strawberry/event-processor/output.json"), EventConfig.class));
*/
        /*ExpressionParser expressionParser = new SpelExpressionParser();
        Map<String, Object> salaryByWorkers = new HashMap<>();
        salaryByWorkers.put("Name", "Sai");
        salaryByWorkers.put("Age", 18);
        salaryByWorkers.put("Location", "Chennai");
        salaryByWorkers.put("Status", "Offline");
        StandardEvaluationContext context = new StandardEvaluationContext(salaryByWorkers);
        Expression expression = expressionParser.parseExpression("['Name'].equals('Sai') && ['Age'] < 20");
        String result = (String) expression.getValue(context, String.class);
        System.out.println(result);*/

        System.out.println(CustomProcessorHook.class.isAssignableFrom(MongoBackedDataTransformer.class));


        foo(new String[]{"a", "b"});





    }

    static void foo(String...args) {
        System.out.println(args.length);
    }

}
