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

    public DataBaseHandler() {
    }

    private Statement GetStatement() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = String.format("jdbc:mysql://%s:%d/%s", Settings.DB_Host, Settings.DB_Port, Settings.DB_Name);
            Connection connection = DriverManager.getConnection(url, Settings.DB_USER, Settings.DB_PW);
            Statement statement = connection.createStatement();
            return statement;
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Initial", e.getMessage()));
            return null;
        }
    }

    public List<String[]> ExecuteQuery(String sql) {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            Statement statement = GetStatement();
            if (statement == null) {
                m_Log.Writeln(String.format("%s fail", "GetStatement"));
                return list;
            }
            if (!sql.toUpperCase().startsWith("SELECT")) {
                return list;
            }
            ResultSet rs = statement.executeQuery(sql);
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
        try {
            Statement statement = GetStatement();
            if (statement == null) {
                m_Log.Writeln(String.format("%s fail", "GetStatement"));
                return -1;
            }
            if (sql.toUpperCase().startsWith("SELECT")) {
                return -1;
            }
            statement.execute(sql);
            return statement.getUpdateCount();
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Initial", e.getMessage()));
            return -1;
        }
    }
}
