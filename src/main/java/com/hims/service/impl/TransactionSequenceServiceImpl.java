package com.hims.service.impl;

import com.hims.entity.TransactionSequence;
import com.hims.entity.repository.TransactionSequenceRepository;
import com.hims.service.TransactionSequenceService;
import com.hims.utils.HMISTransaction;
import com.hims.utils.HMISUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
//
@Service
public class TransactionSequenceServiceImpl implements TransactionSequenceService {

    @Autowired
    private TransactionSequenceRepository transactionSequenceRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public String generateTransactionNumber(HMISTransaction transactionType,
                                            Long hospitalId) {

        String financialYear = HMISUtil.getCurrentFinancialYear();

        TransactionSequence sequence = transactionSequenceRepository
                .findForUpdate(
                        transactionType.getTransactionName(),
                        hospitalId,
                        financialYear)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Transaction sequence not configured for "
                                        + transactionType.getTransactionName()
                                        + " and Financial Year "
                                        + financialYear));

        Long nextSequence = sequence.getCurrentSequence() + 1;

        sequence.setCurrentSequence(nextSequence);

        return HMISUtil.formatTransactionNumber(
                transactionType,
                financialYear,
                nextSequence);
    }
}