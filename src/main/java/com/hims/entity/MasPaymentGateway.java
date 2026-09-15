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
@Table(name = "mas_payment_gateway")
public class MasPaymentGateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_gateway_id")
    private Long gatewayId;

    @Column(name = "gateway_code",nullable = false, unique = true,length = 30)
    private String gatewayCode;

    @Column(name = "gateway_name",nullable = false,length = 100)
    private String gatewayName;

    @Column(name = "status",nullable = false,length = 1)
    private String status;
}
