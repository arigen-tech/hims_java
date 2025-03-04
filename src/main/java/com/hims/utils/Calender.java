package com.hims.utils;

import java.sql.Timestamp;
import java.util.Date;

public class Calender {
    public static String getCurrentTimeStamp() {
        return new String(String.valueOf(new Date().getTime()));
    }
}
