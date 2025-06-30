package com.hims.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Data
@Table(name="randomnum_seq")
public class OrderSequence {
    @Id
    private String prefix;
    private Long lastValue;

}
