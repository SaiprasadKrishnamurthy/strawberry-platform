package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.micro.model.EventProcessingContext;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by saipkri on 08/09/16.
 */
public class ESIndexActor extends UntypedActor {

    private final JestClient jestClient;
    private final Lock lock = new ReentrantLock();
    private final int indexBatchSize;
    private List<Map> payloadsTobeIndexedToEs = new ArrayList<>();


    public ESIndexActor(final JestClient jestClient, final int indexBatchSize) {
        this.jestClient = jestClient;
        this.indexBatchSize = indexBatchSize;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof EventProcessingContext) {
            EventProcessingContext context = (EventProcessingContext) message;
            payloadsTobeIndexedToEs.add(context.getDoc());
            if (payloadsTobeIndexedToEs.size() > indexBatchSize) {
                try {
                    lock.lock();
                    Bulk.Builder bulkBuilder = new Bulk.Builder();
                    for (Map payloadDoc : payloadsTobeIndexedToEs) {
                        System.out.println(payloadDoc);
                        Index index = new Index.Builder(payloadDoc).index(payloadDoc.get("__configId__").toString()).type(payloadDoc.get("__configId__").toString()).id(payloadDoc.get("__naturalId__").toString()).build();
                        bulkBuilder.addAction(index);
                    }
                    jestClient.execute(bulkBuilder.build());
                    System.out.println("\t\t Indexed -- ");
                    payloadsTobeIndexedToEs.clear();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
