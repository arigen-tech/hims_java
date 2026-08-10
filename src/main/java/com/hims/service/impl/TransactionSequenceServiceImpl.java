package com.hims.service.impl;

import com.hims.constants.AppConstants;
import com.hims.entity.TransactionSequence;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.TransactionSequenceRepository;
import com.hims.service.TransactionSequenceService;
import com.hims.utils.AuthUtil;
import com.hims.utils.HMISTransaction;
import com.hims.utils.HMISUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class TransactionSequenceServiceImpl implements TransactionSequenceService {

    @Autowired
    private TransactionSequenceRepository transactionSequenceRepository;

    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired
    private AuthUtil  authUtil;

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
                .orElseGet(() -> {
                    TransactionSequence newSequence = new TransactionSequence();

                    newSequence.setTransactionName(transactionType.getTransactionName());
                    newSequence.setTransactionPrefix(transactionType.getPrefix());
                    newSequence.setHospital(
                            masHospitalRepository.findById(hospitalId)
                                    .orElseThrow(() -> new RuntimeException("Hospital not found"))
                    );
                    newSequence.setFinancialYear(financialYear);
                    newSequence.setCurrentSequence(0L);
                    newSequence.setStatus(AppConstants.STATUS_Y.toLowerCase());
                    newSequence.setLastChgBy(authUtil.getCurrentUser().getUserId());
                    newSequence.setLastChgDate(LocalDateTime.now());
                    return transactionSequenceRepository.save(newSequence);
                });

        Long nextSequence = sequence.getCurrentSequence() + 1;

        sequence.setCurrentSequence(nextSequence);

        return HMISUtil.formatTransactionNumber(
                transactionType,
                financialYear,
                nextSequence);
    }
}