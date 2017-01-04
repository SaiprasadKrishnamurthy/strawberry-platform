package com.sai.strawberry.micro;

import com.datastax.driver.core.Cluster;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.strawberry.api.CustomProcessorHook;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.MongoBackedDataTransformer;
import org.apache.commons.lang3.ClassPathUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ResourceUtils;

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

        ObjectMapper m = new ObjectMapper();
        ExpressionParser expressionParser = new SpelExpressionParser();
        Map json = m.readValue(new FileInputStream("/Users/saipkri/learning/new/strawberry/event-processor/output.json"), Map.class);
        StandardEvaluationContext context = new StandardEvaluationContext(json);
        String spel = "['result'] != null && ['result'].equals('SUCCESS') == false && ['lowerTimeWindow'] <= ['endTimestamp'] && ['upperTimeWindow'] > ['endTimestamp'] ";
        Expression expression = expressionParser.parseExpression(spel);
        System.out.println(expression.getValue(context, Boolean.class));
    }

    static void foo(String...args) {
        System.out.println(args.length);
    }

}
