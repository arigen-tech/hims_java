package com.hims.projection;

import java.math.BigInteger;

public interface BloodStockSummaryProjection {
    String getBloodGroup();
    BigInteger getPrbc();
    BigInteger getPlasma();
    BigInteger getPlatelets();
    BigInteger getCryo();
    BigInteger getTotalUnits();
}