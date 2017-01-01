package com.sai.app.banking.txn.searchlets;

import com.sai.app.banking.txn.TransactionSearchCriteria;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.Searchlet;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;

public class TransactionSearchlet extends Searchlet<TransactionSearchCriteria> {

    public TransactionSearchlet(final EventConfig eventConfig) {
        super(eventConfig);
    }

    @Override
    public String toElasticsearchQuery(final TransactionSearchCriteria transactionSearchCriteria, final XContentBuilder queryBuilder) {
        try {
            return queryBuilder.startObject()
                    .field("from", 0)
                    .field("size", 50)
                    .endObject()
                    .string();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
