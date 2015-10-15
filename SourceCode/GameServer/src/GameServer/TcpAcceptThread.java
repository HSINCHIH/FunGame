/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author mark.chen
 */
public class TcpAcceptThread extends Thread {

    Log m_Log = new Log("TcpAcceptThread");
    ServerSocket m_ServerSocket = null;
    boolean m_IsRunning = true;
    IAcceptSocketCallBack m_CallBack = null;

    public TcpAcceptThread() {
    }

    public void SetAcceptSocketCallBack(IAcceptSocketCallBack callback) {
        m_CallBack = callback;
    }

    public boolean Bind(String ip, int port) {
        try {
            m_ServerSocket = new ServerSocket();
            m_ServerSocket.bind(new InetSocketAddress(ip, port));
            m_ServerSocket.setSoTimeout(1000);
            return true;
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Bind", e.getMessage()));
            return false;
        }
    }

    @Override
    public void run() {
        while (m_IsRunning) {
            try {
                Socket remoteSocket = m_ServerSocket.accept();
                if (m_CallBack == null) {
                    continue;
                }
                m_CallBack.AcceptSocket(remoteSocket);
            } catch (Exception e) {
                if (!e.getMessage().equals("Accept timed out")) {
                    m_Log.Writeln(String.format("%s Exception : %s", "run", e.getMessage()));
                }
            }
        }
        m_Log.Writeln(String.format("%s  %s", "run", "stop"));
    }

    public void Abort() {
        m_IsRunning = false;
    }
}
