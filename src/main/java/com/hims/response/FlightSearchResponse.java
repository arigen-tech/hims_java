package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlightSearchResponse {

    private ResponseDTO response;

    @Getter
    @Setter
    public static class ResponseDTO {
        private String origin;
        private String destination;
        private String provider;
        private int journeyType;
        private String traceId;
        private String isDomestic;
        private int paxCount;
        private ResultsDTO results;
        private FacetsDTO facets;
    }

    @Getter
    @Setter
    public static class ResultsDTO {
        private List<FlightDTO> outboundFlights;
        private List<FlightDTO> inboundFlights;
    }

    @Getter
    @Setter
    public static class FlightDTO {
        private boolean iR;
        private Object aR;
        private boolean iL;
        private String rI;
        private String pr;
        private int pF;
        private String cr;
        private int bF;
        private int sC;
        private int sF;
        private String pFC;
        private List<SegmentDTO> sg;
        private int sA;
        private String db;
        private int fF;
        private int tAS;
        private int fFWAM;
        private FareIdentifierDTO fareIdentifier;
        private int groupId;
    }

    @Getter
    @Setter
    public static class SegmentDTO {
        private String bg;
        private String cBg;
        private int cC;
        private AirlineDTO al;
        private int nOSA;
        private OriginDestinationDTO or;
        private OriginDestinationDTO ds;
        private int aD;
        private int dr;
        private int gT;
        private boolean sO;
        private String sP;
        private int sD;
    }

    @Getter
    @Setter
    public static class AirlineDTO {
        private String alC;
        private String alN;
        private String fN;
        private String fC;
        private String oC;
        private String fCFC;
    }

    @Getter
    @Setter
    public static class OriginDestinationDTO {
        private String aC;
        private String aN;
        private String tr;
        private String cC;
        private String cN;
        private String dT;
        private String cnN;
    }

    @Getter
    @Setter
    public static class FareIdentifierDTO {
        private String name;
        private String code;
        private String colorCode;
    }

    @Getter
    @Setter
    public static class FacetsDTO {
        private List<FareDTO> fares;
        private AirlinesDTO airlines;
    }

    @Getter
    @Setter
    public static class FareDTO {
        private String name;
        private int count;
        private String code;
        private String colorCode;
    }

    @Getter
    @Setter
    public static class AirlinesDTO {
        private List<AirlineCountDTO> inbound;
        private List<AirlineCountDTO> outbound;
    }

    @Getter
    @Setter
    public static class AirlineCountDTO {
        private String name;
        private String code;
        private int count;
    }
}
