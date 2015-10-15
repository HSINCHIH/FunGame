/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author mark.chen
 */
public class Log {

    String m_ClassName = "";
    String m_LogPath = "";

    public Log(String className) {
        this(className, Paths.get(System.getProperty("user.dir"), "Log.txt").toString());
    }

    public Log(String className, String logPath) {
        m_ClassName = className;
        m_LogPath = logPath;
    }

    public void Delete() {
        File f = new File(m_LogPath);
        if (!f.exists()) {
            return;
        }
        f.delete();
    }

    public void Writeln(String msg) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String newMsg = String.format("[%s] \"%s\" %s\r\n", dateFormat.format(new Date()), m_ClassName, msg);
        Write(newMsg);
    }

    public void Write(String msg) {
        try {
            File logFile = new File(m_LogPath);
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.write(msg);
            writer.close();
            System.out.print(msg);
        } catch (Exception e) {
            System.out.println(String.format("%s -> %s Exception : %s", "Log", "Write", e.getMessage()));
        }
    }
}
