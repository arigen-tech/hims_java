package com.hims.service;


import com.hims.utils.HMISTransaction;

public interface TransactionSequenceService {

    String generateTransactionNumber(HMISTransaction transaction,
                                     Long hospitalId);

}
