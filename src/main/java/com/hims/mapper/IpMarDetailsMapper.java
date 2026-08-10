package com.hims.mapper;

import com.hims.projection.IpMarDetailsProjection;
import com.hims.projection.MarMedicineProjection;
import com.hims.response.IpMarDetailsResponse;
import com.hims.response.MarMedicineResponse;
import org.springframework.stereotype.Component;

@Component
public class IpMarDetailsMapper {

    public IpMarDetailsResponse mapToMarDetailsResponse(IpMarDetailsProjection projection) {
        if (projection == null) {
            return null;
        }
        IpMarDetailsResponse response = new IpMarDetailsResponse();
        response.setInpatientId(projection.getInpatientId());
        response.setAdministrationTime(projection.getAdministrationTime());
        response.setItemId(projection.getItemId());
        response.setNomenclature(projection.getNomenclature());
        response.setRouteName(projection.getRouteName());
        response.setDose(projection.getDose());
        response.setAdministeredQty(projection.getAdministeredQty());
        response.setBatchNo(projection.getBatchNo());
        response.setExpiryDate(projection.getExpiryDate());
        response.setAdministeredBy(projection.getAdministeredBy());
        response.setRemarks(projection.getRemarks());
        return response;
    }

    public MarMedicineResponse mapToMarMedicineResponse(MarMedicineProjection projection) {
        if (projection == null) {
            return null;
        }
        MarMedicineResponse response = new MarMedicineResponse();
        response.setItemId(projection.getItemId());
        response.setNomenclature(projection.getNomenclature());
        return response;
    }
}
