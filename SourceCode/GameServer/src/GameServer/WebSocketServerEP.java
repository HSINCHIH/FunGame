/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author mark.chen
 */
public class WebSocketServerEP implements IBaseEP, IAcceptSocketCallBack {

    Log m_Log = new Log("WebSocketServerEP");
    TcpAcceptThread m_AcceptThread = null;
    IReceiveMsgCallBack m_CallBack = null;
    Hashtable<String, Socket> m_Sessions = null;
    Hashtable<String, WebSocketReceiveThread> m_ReceiveThreadTable = null;

    public WebSocketServerEP() {
        m_Sessions = new Hashtable<>();
        m_ReceiveThreadTable = new Hashtable<>();
    }

    @Override
    public boolean Start(String ip, int port) {
        try {
            m_AcceptThread = new TcpAcceptThread();
            if (!m_AcceptThread.Bind(ip, port)) {
                m_Log.Writeln(String.format("%s %s", "Start", "Bind fail"));
            }
            m_AcceptThread.SetAcceptSocketCallBack(this);
            m_AcceptThread.start();
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Start", e.getMessage()));
            return false;
        }
        return true;
    }

    @Override
    public void Stop() {
        try {
            if (m_AcceptThread != null) {
                if (m_AcceptThread.isAlive()) {
                    m_AcceptThread.Abort();
                }
            }
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Stop", e.getMessage()));
        }
    }

    @Override
    public boolean Send(BaseMessage msg, EndPoint endPoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void SetMsgCallBack(IReceiveMsgCallBack callback) {
        m_CallBack = callback;
    }

    @Override
    public void AcceptSocket(Socket socket) {
        try {
            String remoteIP = socket.getInetAddress().getHostAddress();
            int remotePort = socket.getPort();
            String key = String.format("%s:%d", remoteIP, remotePort);
            if (!m_Sessions.containsKey(key)) {
                m_Sessions.put(key, socket);
            }
            //Receive thread
            WebSocketReceiveThread receiveThread = new WebSocketReceiveThread();
            receiveThread.SetSocket(socket);
            receiveThread.SetMsgCallBack(m_CallBack);
            m_ReceiveThreadTable.put(key, receiveThread);
            //Start
            receiveThread.start();
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "AcceptSocket", e.getMessage()));
        }
    }

}
