package com.sai.app.banking.txn;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.Data;

import java.util.UUID;

@Table(keyspace = "card_txns_2", name = "txns_by_bank_date",
        readConsistency = "QUORUM",
        writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false,
        caseSensitiveTable = false)
@Data
public class TransactionsByBankDate {
    @PartitionKey(0)
    private String day;

    @PartitionKey(1)
    private String bank;

    @ClusteringColumn(0)
    private long id;

    @ClusteringColumn(1)
    private Double amount;
}