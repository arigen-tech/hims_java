package com.hims.helperUtil;


import com.hims.exception.SDDException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class ConverterUtils {


    private static final String[] tensNames = {
            "",
            " Ten",
            " Twenty",
            " Thirty",
            " Forty",
            " Fifty",
            " Sixty",
            " Seventy",
            " Eighty",
            " Ninety"
    };
    private static final String[] numNames = {
            "",
            " One",
            " Two",
            " Three",
            " Four",
            " Five",
            " Six",
            " Seven",
            " Eight",
            " Nine",
            " Ten",
            " Eleven",
            " Twelve",
            " Thirteen",
            " Fourteen",
            " Fifteen",
            " Sixteen",
            " Seventeen",
            " Eighteen",
            " Nineteen"
    };

    public static String generateOTP() {
        int otpLength = 6;
        StringBuilder otp = new StringBuilder(otpLength);
        Random random = new Random();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public static String getRandomString(MultipartFile file) {
        try {
            Thread.sleep(10);
            return System.currentTimeMillis() + "";
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return null + "";
    }

    public static String getRandomString(String fileName) {
        try {
            Thread.sleep(10);
            return fileName + System.currentTimeMillis();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return fileName;
    }

    public static String getRandomTimeStamp() {
        try {
            Thread.sleep(10);
            return System.currentTimeMillis() + "";
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return null + "";
    }

    public static String convertDate(Timestamp dateStart) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String string = dateFormat.format(dateStart);
            return string;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return "";

    }

    public static long timeDiffer(Timestamp dateStart, Timestamp enddate) {


        Date d1 = null;
        Date d2 = null;
        try {
            d1 = new Date(dateStart.getTime());
            d2 = new Date(enddate.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        long diff = d2.getTime() - d1.getTime();

        long diffDays = diff / (60 * 60 * 1000 * 24);
//        long diffSeconds = diff / 1000;
//        long diffMinutes = diff / (60 * 1000);
//        long diffHours = diff / (60 * 60 * 1000);
//        System.out.println("Time in seconds: " + diffSeconds + " seconds.");
//        System.out.println("Time in minutes: " + diffMinutes + " minutes.");
//        System.out.println("Time in hours: " + diffHours + " hours.");
//        System.out.println("Time in Day: " + diffDays + " hours.");
        return diffDays;

    }

    public static File getComplaintPathOnly(String fileExtn, String fileName, String fileDestPath) {
        Path path = Paths.get(fileDestPath);
        File targetFile = new File(path + "/" + fileName + fileExtn);
        return targetFile;
    }

    public static String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        return (dtf.format(now));
    }

    public static String addDecimalPoint(String number) {
        try {
            if (number == null) {
                return "0.0000";
            }

            String amoumt = String.format("%.4f", Double.parseDouble(number));
            DecimalFormat df = new DecimalFormat("#.####");
            String dat23 = df.format(Double.parseDouble(amoumt));

            if (!(dat23.contains("."))) {
                dat23 = dat23 + ".0000";
            }
            String amoumt11 = String.format("%.4f", Double.parseDouble(dat23));
            return amoumt11;
        } catch (Exception e) {
            return number;
        }
    }

    public static String addDecimal2Point(String number) {
        try {
            if (number == null) {
                return "0.00";
            }

            String amoumt = String.format("%.2f", Double.parseDouble(number));
            DecimalFormat df = new DecimalFormat("#.##");
            String dat23 = df.format(Double.parseDouble(amoumt));

            if (!(dat23.contains("."))) {
                dat23 = dat23 + ".00";
            }
            String amoumt11 = String.format("%.2f", Double.parseDouble(dat23));
            return amoumt11;
        } catch (Exception e) {
            return number;
        }
    }

    public static Boolean isNumber(String number) {
        try {
            Float.parseFloat(number);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String addSpacaeInString(String number, int lenghtData) {

        String addSpace = "";
        for (Integer n = number.length(); n < lenghtData; n++) {
            addSpace = addSpace + " ";
        }
        if (number.length() == lenghtData) {
            return number + addSpace;
        } else {
            return number + addSpace + " ";
        }
    }

    public static String addSpacaeInStringInWord(String number, int lenghtData) {

        String addSpace = "";
        for (Integer n = number.length(); n < lenghtData; n++) {
            addSpace = addSpace + "   ";
        }
        if (number.length() == lenghtData) {
            return number + addSpace;
        } else {
            return number + addSpace + "  ";
        }
    }

    public static int getMaximumLength(int number1, int number2) {
        if (number1 > number2) {
            return number1;
        } else {
            return number2;
        }
    }

    public static String checkDateIsvalidOrNor(String dt) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(dt));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "INVALID DATE FORMAT");
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        return sdf1.format(c.getTime());
    }

    public static String conVertDateTimeFormat(String dt) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();

        if (dt == null) {
            return null;
        }
        try {
            c.setTime(sdf.parse(dt));
        } catch (ParseException e) {
            e.printStackTrace();
            return dt;
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        return sdf1.format(c.getTime());
    }

    public static Timestamp convertDateTotimeStamp(String inDate) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return new Timestamp(df.parse(inDate).getTime());
        } catch (Exception ie) {
            System.out.println("Problem creating directory " + ie);
            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "Date Format not correct. Use this yyyy-MM-dd.");
        }
    }

    private static String convertLessThanOneThousand(int number) {
        String soFar;

        if (number % 100 < 20) {
            soFar = numNames[number % 100];
            number /= 100;
        } else {
            soFar = numNames[number % 10];
            number /= 10;

            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) return soFar;
        return numNames[number] + " Hundred" + soFar;
    }

    public static String convert(long number) {
        // 0 to 999 999 999 999
        if (number == 0) {
            return "zero";
        }

        String snumber = Long.toString(number);

        // pad with "0"
        String mask = "000000000000";
        DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);

        // XXXnnnnnnnnn
        int billions = Integer.parseInt(snumber.substring(0, 3));
        // nnnXXXnnnnnn
        int millions = Integer.parseInt(snumber.substring(3, 6));
        // nnnnnnXXXnnn
        int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
        // nnnnnnnnnXXX
        int thousands = Integer.parseInt(snumber.substring(9, 12));

        String tradBillions;
        switch (billions) {
            case 0:
                tradBillions = "";
                break;
            case 1:
                tradBillions = convertLessThanOneThousand(billions)
                        + " Billion ";
                break;
            default:
                tradBillions = convertLessThanOneThousand(billions)
                        + " Billion ";
        }
        String result = tradBillions;

        String tradMillions;
        switch (millions) {
            case 0:
                tradMillions = "";
                break;
            case 1:
                tradMillions = convertLessThanOneThousand(millions)
                        + " Million ";
                break;
            default:
                tradMillions = convertLessThanOneThousand(millions)
                        + " Million ";
        }
        result = result + tradMillions;

        String tradHundredThousands;
        switch (hundredThousands) {
            case 0:
                tradHundredThousands = "";
                break;
            case 1:
                tradHundredThousands = "One Thousand ";
                break;
            default:
                tradHundredThousands = convertLessThanOneThousand(hundredThousands)
                        + " Thousand ";
        }
        result = result + tradHundredThousands;

        String tradThousand;
        tradThousand = convertLessThanOneThousand(thousands);
        result = result + tradThousand;

        // remove extra spaces!
        return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
    }

    public static long timeDifferTimeStamp(Timestamp dateStart, Timestamp dateEnd) {


        Date d1 = null;
        Date d2 = null;
        try {
            d1 = new Date(dateStart.getTime());
            d2 = new Date(dateEnd.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        long diff = d1.getTime() - d2.getTime();
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        System.out.println("Time in seconds: " + diffSeconds + " seconds.");
        System.out.println("Time in minutes: " + diffMinutes + " minutes.");
        System.out.println("Time in hours: " + diffHours + " hours.");
        long diffday = diff / (60 * 60 * 1000 * 24);
        return diffday;

    }

    public boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }
    public static String ageCalculator(LocalDate dt) {
        // DOB in String format
        String dobString =dt.toString();
        // Define formatter for input date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // Parse string to LocalDate
        LocalDate dob = LocalDate.parse(dobString, formatter);
        // Current date
        LocalDate today = LocalDate.now();
        // Calculate age
        Period age = Period.between(dob, today);
      //  System.out.println("Age is: " + age.getYears() + " years");
        return(age.getYears() + "Y"+" "+ age.getMonths()+ "M"+" "+age.getDays()+"D");
//        LocalDate dob = LocalDate.of(1990, 01, 01);  // YYYY, MM, DD   1990, 01, 01
//        // Current date
//        LocalDate today = LocalDate.now();
//        // Calculate age
//        Period age = Period.between(dob, today);
//      //  System.out.println("Age is: " + age.getYears() + " years"+" Month "+age.getMonths()+" Day"+age.getDays());
//        return "Age is: " + age.getYears() + " years"+" Month "+age.getMonths()+" Day"+age.getDays();
    }

}
