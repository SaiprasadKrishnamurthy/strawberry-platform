package com.sai.app.banking.txn;

import com.sai.strawberry.api.Callback;

import java.util.Map;

/**
 * Created by saipkri on 01/01/17.
 */
public class TransactionAmountAnomalyWebHookTransformer implements Callback {

    @Override
    public String call(final Map eventPayload) {
        StringBuilder builder = new StringBuilder("The transaction transactionId: %s appears to be a high value transaction amount: %s, card number: %s, bank: %s. Kindly act on it as early as possible.");
        return String.format(builder.toString(), eventPayload.get("transactionId"), eventPayload.get("cardNumber"), eventPayload.get("amount"), eventPayload.get("bank"));
    }
}
