package com.hims.utils;

import com.hims.entity.OrderSequence;
import com.hims.entity.repository.OrderSequenceRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Service
public class RandomNumGenerator {

    private final OrderSequenceRepo repository;

    @Autowired
    public RandomNumGenerator(OrderSequenceRepo repository) {
        this.repository = repository;
    }

    @Transactional
    public String generateOrderNumber(String prefix, boolean AddYear, boolean AddMonth) {
        LocalDate now = LocalDate.now();

        String yearSuffix = "";
        String monthSuffix = "";

        if (AddYear) {
            int year = now.getYear();
            yearSuffix = String.format("%02d-%02d", year % 100, (year + 1) % 100);
        }

        if (AddMonth) {
            monthSuffix = now.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase();
        }


        StringBuilder keyBuilder = new StringBuilder(prefix);
        if (AddYear) {
            keyBuilder.append("-Y").append(yearSuffix);
        }
        if (AddMonth){
            keyBuilder.append("-M").append(monthSuffix);
        }

        String sequenceKey = keyBuilder.toString();


        OrderSequence sequence = repository.findById(sequenceKey)
                .orElseGet(() -> {
                    OrderSequence newSeq = new OrderSequence();
                    newSeq.setPrefix(sequenceKey);
                    newSeq.setLastValue(0L);
                    return newSeq;
                });

        long newValue = sequence.getLastValue() + 1;
        sequence.setLastValue(newValue);
        repository.save(sequence);

        //  final order number
        StringBuilder orderNumber = new StringBuilder();
        orderNumber.append(prefix).append("-").append(newValue);

        if (AddYear) {
            orderNumber.append("/").append(yearSuffix);
        }

        if (AddMonth) {
            orderNumber.append("/").append(monthSuffix);
        }

        return orderNumber.toString();
    }
}
