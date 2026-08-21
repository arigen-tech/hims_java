package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_anaesthesia_instruction", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasAnaesthesiaInstruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anaesthesia_instruction_id")
    private Long anaesthesiaInstructionId;

    @Column(name = "instruction_type", nullable = false, length = 10)
    private String instructionType;

    @Column(name = "instruction", nullable = false, length = 500)
    private String instruction;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
