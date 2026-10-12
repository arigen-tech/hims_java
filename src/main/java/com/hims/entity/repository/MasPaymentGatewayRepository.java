package com.hims.entity.repository;

import com.hims.entity.MasPaymentGateway;
import com.hims.response.MasPaymentGatewayResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasPaymentGatewayRepository extends JpaRepository<MasPaymentGateway,Long> {

     Optional<MasPaymentGateway> findByGatewayCodeIgnoreCase(String gatewayCode);

    List<MasPaymentGateway> findByStatusIgnoreCaseOrderByGatewayCodeDesc(
            String status
    );

    List<MasPaymentGateway> findByStatusInIgnoreCaseOrderByGatewayCodeDesc(
            List<String> statuses
    );
}
