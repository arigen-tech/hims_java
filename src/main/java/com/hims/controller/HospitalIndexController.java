package com.hims.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HospitalIndexController {

    private static final String hospitalPage = "<html>\n" +
            "<body>\n" +
            "  <table width=\"100%\" bgcolor=\"#ffffff\" style=\"font-family:Arial,Helvetica,sans-serif;box-sizing:border-box;font-size:17px;width:100%;background-color:#ffffff;margin:0\">\n" +
            "    <tbody>\n" +
            "      <tr>\n" +
            "        <td valign=\"top\" style=\"max-width:1000px;margin:0 auto;border:1px solid #d6d6d6;border-radius:8px;overflow:hidden\">\n" +
            "          <div style=\"padding:20px;background:#f8faff\">\n" +
            "            <table width=\"100%\">\n" +
            "              <tr>\n" +
            "                <td width=\"65%\" valign=\"top\">\n" +
            "                  <span style=\"font-size:20px;font-weight:bold;color:#1a237e;display:block;margin-bottom:5px;\">Hospital Internal Management System is running now.</span>\n" +
            "                  <span style=\"font-size:13px;color:#444;margin-bottom:15px;display:block;\">Now Release Version 2.0 — Under Testing</span>\n" +
            "                  <div style=\"background:#1976d2;color:#ffffff;border-radius:8px;padding:20px;margin-bottom:20px;\">\n" +
            "                    <span>The Hospital Internal Management System (HIMS) simplifies patient record keeping, appointment scheduling, billing, and staff coordination.</span><br/><br/>\n" +
            "                    <span>It helps hospitals and clinics streamline workflows, improve patient care, and securely manage sensitive health data in one place.</span>\n" +
            "                  </div>\n" +
            "                  <div style=\"color:#5e6c84;font-size:16px;\">\n" +
            "                    For assistance, contact our support team at <strong>+91-9999-999-999</strong> or reach us through the Help section in your HMS dashboard.\n" +
            "                  </div>\n" +
            "                </td>\n" +
            "                <td width=\"35%\" align=\"right\" valign=\"top\">\n" +
            "                  <img src=\"https://cdn-icons-png.flaticon.com/512/2966/2966485.png\" width=\"200\" alt=\"Hospital\"/>\n" +
            "                </td>\n" +
            "              </tr>\n" +
            "            </table>\n" +
            "          </div>\n" +
            "          <div style=\"background:#e3f2fd;padding:10px;text-align:center;color:#444;\">\n" +
            "            <div style=\"font-size:15px;\">\n" +
            "              <strong>Arigen Technology Private Limited</strong><br/>\n" +
            "              Innovating healthcare IT solutions since 2008<br/>\n" +
            "              <span style=\"font-size:13px;color:#777;\">Registered Office: Tower-A, I-Thum Tower, Sector-62 Noida, UP-201309</span><br/>\n" +
            "              <span style=\"font-size:13px;color:#777;\">© 2008–2025 | ArigenTechnology.com | All Rights Reserved</span>\n" +
            "            </div>\n" +
            "          </div>\n" +
            "        </td>\n" +
            "      </tr>\n" +
            "    </tbody>\n" +
            "  </table>\n" +
            "</body>\n" +
            "</html>";

    @GetMapping("/")
    public String home() {
        return hospitalPage;
    }
}
