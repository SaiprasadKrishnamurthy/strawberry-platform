package com.sai.strawberry.micro;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by saipkri on 17/11/16.
 */
public class Scratchpad {
    public static void mains(String[] args) throws Exception {

        String sql = "CREATE TABLE IF NOT EXISTS card_txns(ID INT PRIMARY KEY, NAME VARCHAR(255))";
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
        });


    }

}
