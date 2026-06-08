package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OpdEntDetailsResponse {

    private Long entId;

    private Long patientId;
    private Long visitId;
    private LocalDate opdDate;
    private String rightPinna;
    private String leftPinna;
    private String rightEarCanal;
    private String leftEarCanal;
    private String rightTmStatus;
    private String leftTmStatus;
    private String rinneTest;
    private String weberTest;
    private String abcTest;
    private String audiometryFindings;
    private String externalNose;
    private String nasalMucosa;
    private String septum;
    private String turbinates;
    private String nasalPolyp;
    private String nasalDischarge;
    private String maxillaryTenderness;
    private String frontalTenderness;
    private String oralCavity;
    private String tonsilGrade;
    private String tonsilCongestion;
    private String tonsilFollicles;
    private String tonsilMembrane;
    private String peritonsillarAbscess;
    private String pharynx;
    private String uvula;
    private String voiceQuality;
    private String thyroidEnlargement;
    private String cervicalNodes;
    private String neckMass;
    private String neckOtherFindings;
}