package com.sai.app.banking.txn.searchlets;

import com.sai.app.banking.txn.TransactionSearchCriteria;
import com.sai.strawberry.api.EventConfig;
import com.sai.strawberry.api.Searchlet;

/**
 * Created by saipkri on 30/12/16.
 */
public class TransactionSearchlet extends Searchlet<TransactionSearchCriteria> {

    public TransactionSearchlet(final EventConfig eventConfig) {
        super(eventConfig);
    }

    @Override
    public String toElasticsearchQuery(TransactionSearchCriteria transactionSearchCriteria) {
        return null;
    }

    @Override
    public TransactionSearchCriteria newSearchCriteria() {
        return new TransactionSearchCriteria();
    }
}
