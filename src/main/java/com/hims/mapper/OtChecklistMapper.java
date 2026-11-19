package com.hims.mapper;

import com.hims.entity.OtChecklist;
import com.hims.entity.OtChecklistItem;
import com.hims.response.OtChecklistSubmitResponse;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class OtChecklistMapper {
    public OtChecklistSubmitResponse toResponse(OtChecklist header, List<OtChecklistItem> items) {
        OtChecklistSubmitResponse response = new OtChecklistSubmitResponse();
        response.setOtChecklistId(header.getOtChecklistId());
        response.setOtBookingId(header.getOtBooking().getOtBookingId());
        response.setChecklistStage(header.getChecklistStage());
        response.setChecklistStatus(header.getChecklistStatus());
        response.setRemarks(header.getRemarks());
        response.setVerifiedBy(header.getVerifiedBy());
        response.setVerifiedDatetime(header.getVerifiedDatetime());
        response.setStatus(header.getStatus());
        response.setLastChgBy(header.getLastChgBy());
        response.setLastChgDate(header.getLastChgDate());
        response.setItems(items.stream().map(this::toItemResponse).toList());
        return response;
    }

    private OtChecklistSubmitResponse.ItemResponse toItemResponse(OtChecklistItem item) {
        var master = item.getChecklist();
        var response = new OtChecklistSubmitResponse.ItemResponse();
        response.setOtChecklistItemId(item.getOtChecklistItemId());
        response.setChecklistId(master.getChecklistId());
        response.setChecklistCode(master.getChecklistCode());
        response.setChecklistName(master.getChecklistName());
        response.setChecklistStage(master.getChecklistStage());
        response.setIsMandatory(master.getIsMandatory());
        response.setAllowNa(master.getAllowNa());
        response.setDisplayOrder(master.getDisplayOrder());
        response.setVerificationStatus(item.getVerificationStatus());
        response.setRemarks(item.getRemarks());
        response.setVerifiedBy(item.getVerifiedBy());
        response.setVerifiedDatetime(item.getVerifiedDatetime());
        return response;
    }
}
