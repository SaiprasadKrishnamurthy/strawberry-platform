package com.sai.strawberry.micro.actor;

import akka.actor.UntypedActor;
import com.sai.strawberry.micro.model.ProcessorEvent;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by saipkri on 08/09/16.
 */
public class OpsIndexActor extends UntypedActor {

    private final JestClient jestClient;
    private final Lock lock = new ReentrantLock();
    private final int indexBatchSize;
    private String opsIndexName;
    private List<ProcessorEvent> payloadsTobeIndexedToEs = new ArrayList<>();


    public OpsIndexActor(final JestClient jestClient, final int indexBatchSize, final String opsIndexName) {
        this.jestClient = jestClient;
        this.indexBatchSize = indexBatchSize;
        this.opsIndexName = opsIndexName;
    }

    @Override
    public void onReceive(final Object message) throws Throwable {
        if (message instanceof ProcessorEvent) {
            ProcessorEvent event = (ProcessorEvent) message;
            payloadsTobeIndexedToEs.add(event);
            if (payloadsTobeIndexedToEs.size() > indexBatchSize) {
                try {
                    lock.lock();
                    Bulk.Builder bulkBuilder = new Bulk.Builder();
                    for (ProcessorEvent payloadDoc : payloadsTobeIndexedToEs) {
                        Index index = new Index.Builder(payloadDoc).index(opsIndexName).type(opsIndexName).id(UUID.randomUUID().toString()).build();
                        bulkBuilder.addAction(index);
                    }
                    jestClient.execute(bulkBuilder.build());
                    payloadsTobeIndexedToEs.clear();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
