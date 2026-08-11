package com.hims.mapper;

import com.hims.entity.IpProcedureTxn;
import com.hims.response.IpProcedureTxnResponse;
import org.springframework.stereotype.Component;

@Component
public class IpProcedureTxnMapper {

    public IpProcedureTxnResponse mapToDTO(IpProcedureTxn entity) {
        if (entity == null) {
            return null;
        }
        IpProcedureTxnResponse response = new IpProcedureTxnResponse();
        response.setProcedureTxnId(entity.getProcedureTxnId());
        response.setInpatientId(entity.getInpatient() != null ? entity.getInpatient().getInpatientId() : null);
        response.setProcedureName(entity.getProcedureName());
        response.setProcedureDatetime(entity.getProcedureDatetime());
        response.setPerformedBy(entity.getPerformedBy());
        response.setRemarks(entity.getRemarks());
        return response;
    }
}
