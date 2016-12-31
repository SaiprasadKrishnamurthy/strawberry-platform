package com.sai.app.banking.txn;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.sai.strawberry.api.CassandraBackedDataTransformer;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.MongoBackedDataTransformer;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by saipkri on 30/11/16.
 */
public class CassandraBasedCardTxnDataTransformer extends CassandraBackedDataTransformer {

    @Override
    public Map process(Session session, MappingManager mappingManager, EventConfig eventConfig, Map map) {
        System.out.println(" incoming data: "+map);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        Mapper<TransactionsByBankDate> mapper = mappingManager.mapper(TransactionsByBankDate.class);
        TransactionsByBankDate trx = new TransactionsByBankDate();
        trx.setDay(sdf.format(new Date()));
        trx.setId(System.nanoTime());
        trx.setAmount(Double.parseDouble(map.get("amount").toString()));
        trx.setBank(map.get("bank").toString());
        mapper.save(trx);
        return map;
    }
}
