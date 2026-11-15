
package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ot_pac_instruction",
        schema = "public"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtPacInstruction {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "pac_instruction_id")
 private Long pacInstructionId;

 @JsonIgnore
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "pac_assessment_id", nullable = false)
 private OtPacAssessment pacAssessment;

 @Column(name = "instruction_text", nullable = false)
 private String instructionText;

 @Column(name = "last_chg_by", length = 200)
 private String lastChgBy;

 @Column(name = "last_chg_date", nullable = false)
 private LocalDateTime lastChgDate;
}
