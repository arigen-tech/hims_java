package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mas_result_flag")
public class MasResultFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_flag_id")
    private Long resultFlagId;

    @Column(name = "flag_code", length = 20, nullable = false, unique = true)
    private String flagCode;

    @Column(name = "flag_name", length = 50, nullable = false)
    private String flagName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", length = 1)
    private String status = "Y";
}