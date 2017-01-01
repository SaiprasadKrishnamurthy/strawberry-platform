package com.sai.app.banking.txn;

import com.sai.app.banking.txn.searchlets.TransactionSearchlet;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * Created by saipkri on 14/12/16.
 */
public class Scratchpad {
    public static void main(String[] args) throws Exception {

        TransactionSearchlet t = new TransactionSearchlet(null);
        System.out.println(t.searchCriteriaClass());


    }
}
