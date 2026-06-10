package com.hims.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
public class OpdObgDetailsRequest {
    private Long patientId;
    private Long visitId;
    private LocalDate opdDate;
    private OBGDetails obgDetails;
    private GynaecologyHistory gynaecologyHistory;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GynaecologyHistory {
        private String abdomenPalpation;
        private Integer ageOfMenarche;
        private String bimanualExamination;
        private String gynFlow;
        private String gynCycle;
        private LocalDate lastMenstrualPeriod;
        private String localExamination;
        private String menstrualPattern;
        private String gynObstetricHistory;
        private String papSmear;
        private String perAbdomenInspection;
        private String perSpeculum;
        private String sterilisation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OBGDetails {
        private String obstetricHistory;
        private ObstetricScore obstetricScore;
        private String conception;
        private String marriedLife;
        private String consanguinity;
        private String booked;
        private String immunised;
        private String trimester;
        private String gestationalCalculation;
        private String paPalpation;
        private String perExamination;
        private String tetanusToxoid;
        private String fetalHeartRate;
        private String presentation;
        private String palpation;
        private String pv;
        private String inspectionHeightOfUterus;
        private String specify;
        private String Remarks;
        private MenstrualHistory menstrualHistory;
        private SystemicExamination systemicExamination;
        private CardiovascularSystem cardiovascularSystem;
        private PerVaginalExamination perVaginalExamination;



        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ObstetricScore {
            private String gravida;
            private String para;
            private String abortion;
            private String livingChildren;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MenstrualHistory {
            private Integer ageOfMenarche;
            private String cycles;
            private Integer rangeDays;
            private String interval;
            private String flow;
            private String menstrualPause;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SystemicExamination{
            private String respiratorySystem;
            private String breathSounds;
        }


        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CardiovascularSystem{
            private String s1;
            private String s2;
            private String murmurs;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PerVaginalExamination{
            private String osDilatation;
            private String effacement;
            private String membrane;
            private String liquor;
            private String consistency;
            private String position;
            private String length;
            private String stationOfPresentingPart;
            private String head;
            private String pelvis;
        }

    }


}