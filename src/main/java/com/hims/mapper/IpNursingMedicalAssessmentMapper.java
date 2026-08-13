package com.hims.mapper;

import com.hims.entity.IpNursingMedicalAssessment;
import com.hims.response.IpNursingMedicalAssessmentResponse;
import org.springframework.stereotype.Component;

@Component
public class IpNursingMedicalAssessmentMapper {

    public IpNursingMedicalAssessmentResponse mapToResponse(IpNursingMedicalAssessment entity) {
        if (entity == null) {
            return null;
        }
        IpNursingMedicalAssessmentResponse response = new IpNursingMedicalAssessmentResponse();
        response.setAssessmentId(entity.getAssessmentId());
        
        if (entity.getInpatient() != null) {
            response.setInpatientId(entity.getInpatient().getInpatientId());
        }
        if (entity.getHospital() != null) {
            response.setHospitalId(entity.getHospital().getId());
        }

        response.setConsciousness(entity.getConsciousness());
        response.setGcsScore(entity.getGcsScore());
        response.setPainScore(entity.getPainScore());
        response.setMobilityStatus(entity.getMobilityStatus());
        response.setFallRisk(entity.getFallRisk());
        response.setPressureSoreRisk(entity.getPressureSoreRisk());
        
        response.setSkinCondition(entity.getSkinCondition());
        response.setSkinRemarks(entity.getSkinRemarks());
        
        response.setIvLinePresent(entity.getIvLinePresent());
        response.setIvSite(entity.getIvSite());
        
        response.setCatheterPresent(entity.getCatheterPresent());
        response.setCatheterType(entity.getCatheterType());
        
        response.setDrainPresent(entity.getDrainPresent());
        response.setDrainType(entity.getDrainType());
        
        response.setNutritionRisk(entity.getNutritionRisk());
        response.setNutritionRemarks(entity.getNutritionRemarks());
        
        response.setInfectionRisk(entity.getInfectionRisk());
        response.setInfectionRemarks(entity.getInfectionRemarks());
        
        response.setPatientOrientationDone(entity.getPatientOrientationDone());
        response.setRelativeOrientationDone(entity.getRelativeOrientationDone());
        
        response.setNursingCarePlan(entity.getNursingCarePlan());
        
        response.setChiefComplaint(entity.getChiefComplaint());
        response.setHistoryPresentIllness(entity.getHistoryPresentIllness());
        response.setFamilyHistory(entity.getFamilyHistory());
        response.setMedicationHistory(entity.getMedicationHistory());
        response.setAllergies(entity.getAllergies());
        
        response.setPulse(entity.getPulse());
        response.setSystolicBp(entity.getSystolicBp());
        response.setDiastolicBp(entity.getDiastolicBp());
        response.setTemperature(entity.getTemperature());
        response.setTemperatureUnit(entity.getTemperatureUnit());
        response.setRespiratoryRate(entity.getRespiratoryRate());
        response.setSpo2(entity.getSpo2());
        
        response.setGeneralExaminationNotes(entity.getGeneralExaminationNotes());
        response.setSystemRsExamination(entity.getSystemRsExamination());
        response.setSystemCvsExamination(entity.getSystemCvsExamination());
        response.setSystemPaExamination(entity.getSystemPaExamination());
        response.setSystemCnsExamination(entity.getSystemCnsExamination());
        response.setProvisionalDiagnosis(entity.getProvisionalDiagnosis());
        
        response.setStatus(entity.getStatus());
        
        return response;
    }
}
