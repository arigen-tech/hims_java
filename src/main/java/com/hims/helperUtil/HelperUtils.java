package com.hims.helperUtil;

import java.sql.Timestamp;
import java.util.Date;

public class HelperUtils {

    // FOR dev  D:\BmsBackend\webapps\bmsreport
    public static String LASTFOLDERPATH = "D:/payroll/webapps/bmsreport";
    public static String FILEPATH = "https://icg.net.in/bmsreport/";

//     For UAT
//    public static String LASTFOLDERPATH = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/bmsreport";
//    public static String FILEPATH = "https://icg.net.in/bmsreport/";

//     For PROD
//    public static String LASTFOLDERPATH = "C:/Program Files/Tomcat 9.0/webapps/cgbmsreport";
//    public static String FILEPATH = "https://icg.net.in/cgbmsreport/";

    public static String getUserId() {
        return "USR" + ConverterUtils.getRandomTimeStamp();
    }

    public static String getJoiningId() {
        return "JNG_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static String getAddressId() {
        return "AD_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static String getBankId() {
        return "BNK_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static String getEduId() {
        return "EDU_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static String getPernlId() {
        return "PER_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static String getProfId() {
        return "PRO_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static String getRollId() {
        return "ROL_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static Timestamp getCurrentTimeStamp() {
        return new Timestamp(new Date().getTime());
    }

    public static String getOtp() {
        return ConverterUtils.generateOTP();
    }


}
