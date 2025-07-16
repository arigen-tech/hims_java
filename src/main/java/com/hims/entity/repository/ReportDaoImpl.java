package com.hims.entity.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ReportDaoImpl implements ReportDao {

    @Autowired
    private DataSource dataSource;

    @Override
    public Map<String, Object> getConnectionForReport() {
        System.out.println("inside connection report method");
        Map<String, Object> map = new HashMap<String, Object>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            map.put("conn", conn);
        }catch(Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> getConnectionForReportMis() {
        System.out.println("inside Mis connection report method");
        Map<String, Object> map = new HashMap<String, Object>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            map.put("conn", conn);
        }catch(Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return map;
    }
}
