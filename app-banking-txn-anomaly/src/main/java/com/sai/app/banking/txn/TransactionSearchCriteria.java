package com.sai.app.banking.txn;

import lombok.Data;

/**
 * Created by saipkri on 30/12/16.
 */
@Data
public class TransactionSearchCriteria {
    private String bankName;
    private double amount;
    private String transactionDateTime = "2016-12-14T10:00:00";
}
