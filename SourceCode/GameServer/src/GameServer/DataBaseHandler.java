/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mark.chen
 */
public class DataBaseHandler {

    Log m_Log = new Log("DataBaseHandler");
    Connection m_Connection = null;
    Statement m_Statement = null;
    boolean m_IsConnect = false;

    public DataBaseHandler() {
    }

    public boolean Initial() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = String.format("jdbc:mysql://%s:%d/%s", Settings.DB_Host, Settings.DB_Port, Settings.DB_Name);
            m_Connection = DriverManager.getConnection(url, Settings.DB_USER, Settings.DB_PW);
            m_Statement = m_Connection.createStatement();
            m_IsConnect = true;
            return true;
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Initial", e.getMessage()));
            return false;
        }
    }

    public List<String[]> ExecuteQuery(String sql) {
        ArrayList<String[]> list = new ArrayList<>();
        if (!m_IsConnect) {
            return list;
        }
        try {
            if (!sql.toUpperCase().startsWith("SELECT")) {
                return list;
            }
            ResultSet rs = m_Statement.executeQuery(sql);
            ResultSetMetaData rsMetaData = rs.getMetaData();
            String[] strArray = null;
            while (rs.next()) {
                strArray = new String[rsMetaData.getColumnCount()];
                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    strArray[i - 1] = rs.getString(i);
                }
                list.add(strArray);
            }
            return list;
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Initial", e.getMessage()));
            return list;
        }
    }

    public int Execute(String sql) {
        if (!m_IsConnect) {
            return -1;
        }
        try {
            if (sql.toUpperCase().startsWith("SELECT")) {
                return -1;
            }
            m_Statement.execute(sql);
            return m_Statement.getUpdateCount();
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Initial", e.getMessage()));
            return -1;
        }
    }
}
