//package com.hims.utils;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.Period;
//import java.time.YearMonth;
//import java.time.ZoneId;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Enumeration;
//import javax.servlet.ServletContext;
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.swing.*;
//
//import org.json.JSONObject;
//import org.springframework.web.bind.ServletRequestUtils;
//
//import net.sf.jasperreports.engine.JRException;
//import net.sf.jasperreports.engine.JasperFillManager;
//import net.sf.jasperreports.engine.JasperPrint;
//import net.sf.jasperreports.engine.JasperPrintManager;
//import net.sf.jasperreports.engine.JasperReport;
//import net.sf.jasperreports.engine.JasperRunManager;
//import net.sf.jasperreports.engine.util.JRLoader;
//
//public class ReportUtils extends ServletRequestUtils {
//    public static int calculateAgeNoOfYear(Date dob ) {
//
//        Calendar lCal = Calendar.getInstance();
//        lCal.setTime(dob);
//        int yr=lCal.get(Calendar.YEAR);
//        int mn=lCal.get(Calendar.MONTH) + 1;
//        int dt=lCal.get(Calendar.DATE);
//        LocalDate today = LocalDate.now();
//
//        //System.out.println("today"+today);//Today's date
//        LocalDate birthday = LocalDate.of(yr,mn,dt) ; //Birth date
//        //System.out.println("birthday"+birthday);
//        Period p = Period.between(birthday, today);
//        //System.out.println("Period : "+p);
//        return p.getYears();
//    }
//
//    public static String getProperties(String fileName, String propName){
//        String propertyValue = null;
//        try{
//            URL resourcePath = Thread.currentThread().getContextClassLoader()
//                    .getResource(fileName);
//            Properties properties= new Properties();
//            properties.load(resourcePath.openStream());
//            propertyValue = properties.getProperty(propName);
//        }catch(Exception e){e.printStackTrace();}
//        return propertyValue;
//    }
//
//    public static Box getBox(HttpServletRequest request) {
//        Box box = new Box("requestbox");
//        Enumeration e = request.getParameterNames();
//        while (e.hasMoreElements()) {
//            String key = (String) e.nextElement();
//            box.put(key, request.getParameterValues(key));
//        }
//        return box;
//    }
//
//    public synchronized static void generateReport(String jasper_filename, String actualFileName, Map parameters,
//                                                   Connection conn, HttpServletResponse response,
//                                                   ServletContext context) {
//        byte bytes[] = null;
//        try {
//            bytes = JasperRunManager.runReportToPdf(getCompiledReport(context,
//                    jasper_filename), parameters, conn);
//
//
//            if(!conn.isClosed())
//                conn.close();
//        } catch (JRException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        response.setHeader("Content-Disposition", "attachment; filename="
//                + actualFileName + ".pdf");
//        response.setContentLength(bytes.length);
//        ServletOutputStream ouputStream;
//        try {
//            ouputStream = response.getOutputStream();
//            ouputStream.write(bytes, 0, bytes.length);
//            ouputStream.flush();
//            ouputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public static JasperReport getCompiledReport(ServletContext context,
//                                                 String fileName) throws JRException {
//        File reportFile = new File(context.getRealPath("/reports/" + fileName
//                + ".jasper"));
//        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportFile.getPath());
//        return jasperReport;
//    }
//
//    public static Date dateFormatteryyyymmdd(String stringDate) throws Exception {
//        SimpleDateFormat dateFormatterYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            return (dateFormatterYYYYMMDD.parse(stringDate));
//        } catch (ParseException e) {
//            e.printStackTrace();
//            throw e;
//        }
//
//    }
//
//    public static Date convertStringTypeDateToDateType(String date) {
//        Date orderDateTime = null;
//
//        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
//        if (date != null) {
//            try {
//                orderDateTime = df.parse(date);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return orderDateTime;
//    }
//
//    public static void generateReportDirectPrint(String jasper_filename, Map parameters,
//                                                 Connection conn, HttpServletResponse response,
//                                                 ServletContext context) {
//        byte bytes[] = null;
//        try {
//		/*	bytes = JasperRunManager.runReportToPdf(getCompiledReport(context,
//					jasper_filename), parameters, conn);*/
//
//            JasperPrint jp = JasperFillManager.fillReport(getCompiledReport(context,
//                    jasper_filename), parameters, conn);
//            JasperPrintManager.printReport(jp,false);
//            /*JasperViewer.viewReport(jp, false);*/
//
//            if(!conn.isClosed())
//                conn.close();
//        } catch (JRException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//		/*response.setHeader("Content-Disposition", "attachment; filename="
//				+ jasper_filename + ".pdf");
//		response.setContentLength(bytes.length);
//		ServletOutputStream ouputStream;*/
//	/*	try {
//			ouputStream = response.getOutputStream();
//			ouputStream.write(bytes, 0, bytes.length);
//			ouputStream.flush();
//			ouputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//*/
//    }
//
//    public static JasperPrint generateReportDirectPrintClientSide(String jasper_filename, Map parameters,
//                                                                  Connection conn, HttpServletResponse response,
//                                                                  ServletContext context) {
//        byte bytes[] = null;
//        JasperPrint jp = new JasperPrint();
//        try {
//		/*	bytes = JasperRunManager.runReportToPdf(getCompiledReport(context,
//					jasper_filename), parameters, conn);*/
//
//            jp = JasperFillManager.fillReport(getCompiledReport(context,
//                    jasper_filename), parameters, conn);
//            /* JasperPrintManager.printReport(jp,false);*/
//            /*JasperViewer.viewReport(jp, false);*/
//
//            if(!conn.isClosed())
//                conn.close();
//        } catch (JRException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//		/*response.setHeader("Content-Disposition", "attachment; filename="
//				+ jasper_filename + ".pdf");
//		response.setContentLength(bytes.length);
//		ServletOutputStream ouputStream;*/
//	/*	try {
//			ouputStream = response.getOutputStream();
//			ouputStream.write(bytes, 0, bytes.length);
//			ouputStream.flush();
//			ouputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//*/
//        return jp;
//    }
//
//
//    public static String calculateAgeYearOrMonthOrDays(Date birthDate) {
//        // get todays date
//        Calendar now = Calendar.getInstance();
//        // get a calendar representing their birth date
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(birthDate);
//
//        // calculate age as the difference in years.
//        @SuppressWarnings("unused")
//        int calculatedDays, calculatedMonth, calculatedYear;
//        int currentDays = now.get(Calendar.DATE);
//        int birthDays = cal.get(Calendar.DATE);
//        int currentMonth = now.get(Calendar.MONTH);
//        int birthMonth = cal.get(Calendar.MONTH);
//        int currentYear = now.get(Calendar.YEAR);
//        int birthYear = cal.get(Calendar.YEAR);
//
//
//        if (currentDays < birthDays) {
//            currentDays = currentDays + 30;
//            calculatedDays = currentDays - birthDays;
//            currentMonth = currentMonth - 1;
//        } else {
//            calculatedDays = currentDays - birthDays;
//        }
//
//        if (currentMonth < birthMonth) {
//            currentMonth = currentMonth + 12;
//            calculatedMonth = currentMonth - birthMonth;
//            currentYear = currentYear - 1;
//        } else {
//            calculatedMonth = currentMonth - birthMonth;
//        }
//
//        int age = currentYear - birthYear;
//        String patientAge = "";
//
//        if (age == 0 && calculatedMonth != 0 && calculatedDays != 0) {
//            patientAge = calculatedMonth + " Months ";
//        } else if (age == 0 && calculatedMonth == 0 && calculatedDays != 0) {
//            patientAge = calculatedDays + "  Days";
//        }
//        else if (age == 0 && calculatedMonth != 0 && calculatedDays == 0) {
//            patientAge = calculatedMonth + " Months ";
//        }
//        else if (age == 0 && calculatedMonth == 0 && calculatedDays == 0) {
//            patientAge = "1 Days";
//        }
//        else {
//            patientAge = age + " Years ";
//        }
//        return patientAge;
//    }
//
//
//    public static String calculateAge(Date birthDate) {
//
//        int years = 0;
//        int months = 0;
//        int days = 0;
//
//        //create calendar object for birth day
//        Calendar birthDay = Calendar.getInstance();
//        birthDay.setTimeInMillis(birthDate.getTime());
//
//        //create calendar object for current day
//        long currentTime = System.currentTimeMillis();
//        Calendar now = Calendar.getInstance();
//        now.setTimeInMillis(currentTime);
//
//        //Get difference between years
//        years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
//        int currMonth = now.get(Calendar.MONTH) + 1;
//        int birthMonth = birthDay.get(Calendar.MONTH) + 1;
//
//        //Get difference between months
//        months = currMonth - birthMonth;
//
//        //if month difference is in negative then reduce years by one
//        //and calculate the number of months.
//        if (months < 0)
//        {
//            years--;
//            months = 12 - birthMonth + currMonth;
//            if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
//                months--;
//        } else if (months == 0 && now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
//        {
//            years--;
//            months = 11;
//        }
//
//        //Calculate the days
//        if (now.get(Calendar.DATE) > birthDay.get(Calendar.DATE))
//            days = now.get(Calendar.DATE) - birthDay.get(Calendar.DATE);
//        else if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
//        {
//            int today = now.get(Calendar.DAY_OF_MONTH);
//            now.add(Calendar.MONTH, -1);
//            days = now.getActualMaximum(Calendar.DAY_OF_MONTH) - birthDay.get(Calendar.DAY_OF_MONTH) + today;
//        }
//        else
//        {
//            days = 0;
//            if (months == 12)
//            {
//                years++;
//                months = 0;
//            }
//        }
//        return years + " Years, " + months + " Months, " + days + " Days";
//    }
//
//    public static String convertNullToEmptyString(Object obj) {
//        return (obj == null) ? "" : obj.toString();
//    }
//
//    public static String convertDateToStringFormat(Date date, String format){
//        String dateFormat="";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
//        if(date != null) {
//            dateFormat=simpleDateFormat.format(date);
//        }
//        return dateFormat;
//    }
//
//    public static String getReplaceString(String replaceValue) {
//
//        String stringReplace=replaceValue.replaceAll("[\\[\\]]", "");
//        return  stringReplace.replaceAll("^\"|\"$", "");
//
//    }
//
//    public static String textToHtml(HttpServletRequest request, String data) {
//
//        JSONObject input = new JSONObject(data);
//        Box box = ReportUtils.getBox(request);
//        JSONObject json = new JSONObject(box);
//        String templateData = input.getString("resultEntry");
//        InputStream fis1 = null;
//        try {
//            fis1 = new FileInputStream(request.getServletContext()
//                    .getRealPath("resources/html/appendingHtml.html"));
//        } catch (FileNotFoundException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//        File temprory2 = new File(request.getServletContext().getRealPath(
//                "resources/html/appendingHtml.html"));
//
//        byte[] b1 = new byte[(int) temprory2.length()];
//        int offset1 = 0;
//        int numRead1 = 0;
//        try {
//            while ((offset1 < b1.length)
//                    && ((numRead1 = fis1.read(b1, offset1, b1.length
//                    - offset1)) >= 0)) {
//
//                offset1 += numRead1;
//
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        try {
//            fis1.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        String appendedHtml = new String(b1);
//        String finalFile = appendedHtml + templateData + "</body></html>";
//        return finalFile;
//    }
//
//    public static String changeDateToddMMyyyy(Date dbDate) {
//        String strDate = dbDate.toString();
//        String strNewDate = "", year = "", dt = "", month = "";
//        year = strDate.substring(0, 4);
//        month = strDate.substring(5, 7);
//        dt = strDate.substring(8, 10);
//        strNewDate = (dt + "/" + month + "/" + year);
//        return strNewDate;
//    }
//
//    public synchronized static void generateReportInPopUp(String jasper_filename, String actualFileName, Map parameters,
//                                                          Connection conn, HttpServletResponse response,
//                                                          ServletContext context) {
//        byte bytes[] = null;
//        try {
//            bytes = JasperRunManager.runReportToPdf(getCompiledReport(context,
//                    jasper_filename), parameters, conn);
//
//
//            if(!conn.isClosed())
//                conn.close();
//        } catch (JRException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", String.format("inline; filename="+ actualFileName + ".pdf"));
//
//        //response.setHeader("Content-Disposition", "inline; filename="
//        //	+ actualFileName + ".pdf");
//
//        response.setContentLength(bytes.length);
//        ServletOutputStream ouputStream;
//        try {
//            ouputStream = response.getOutputStream();
//            ouputStream.write(bytes, 0, bytes.length);
//            ouputStream.flush();
//            ouputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    public static Date getStartDate(int year, int month) {
//        //Date orderDateTime = null;
//        LocalDate date = LocalDate.of(year, month, 1);
//        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
//    }
//
//    public static Date getEndDate(int year, int month) {
//        YearMonth yearMonth = YearMonth.of(year, month);
//        LocalDate date = yearMonth.atEndOfMonth();
//        // Convert LocalDate to java.util.Date
//        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
//    }
//}
