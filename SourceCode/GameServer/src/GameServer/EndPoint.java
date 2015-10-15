/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

/**
 *
 * @author mark.chen
 */
public class EndPoint {

    String m_IP = "";
    int m_Port = 0;

    public EndPoint() {
    }

    public EndPoint(String ip, int port) {
        m_IP = ip;
        m_Port = port;
    }

    public EndPoint(String endPoint) {
        String[] args = endPoint.split(":");
        m_IP = args[0];
        m_Port = Integer.parseInt(args[1]);
    }

    public String GetIP() {
        return m_IP;
    }

    public int GetPort() {
        return m_Port;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", m_IP, m_Port);
    }
}
