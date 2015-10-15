/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.util.List;

/**
 *
 * @author mark.chen
 */
public class MainServer implements IReceiveMsgCallBack {

    Log m_Log = new Log("MainServer");
    String m_HostIP = "";
    WebSocketServerEP m_LocalControlEP = null;
    DataBaseHandler m_DBHandler = null;

    public MainServer() {
        m_HostIP = Utilities.GetHostIP();
        System.out.println(String.format("Host IP : %s", m_HostIP));
    }

    @Override
    public void ReceiveMsg(BaseMessage msg, EndPoint ep) {
        switch (msg.Action) {
            case ServerAction.DISCONNECTED: {
                DISCONNECTED_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_REGISTRY: {
                PLSV_REGISTRY_recv(msg, ep);
            }
            break;
            default: {
            }
            break;
        }
    }

    public boolean Start() {
        try {
            m_Log.Writeln("Start");
            //Control client server
            m_LocalControlEP = new WebSocketServerEP();
            m_LocalControlEP.SetMsgCallBack(this);
            if (!m_LocalControlEP.Start(IPAddress.Any, Settings.ServerPort)) {
                m_Log.Writeln(String.format("%s %s", "Start", "m_LocalControlEP fail"));
                return false;
            }
            //Initial database handler
            m_DBHandler = new DataBaseHandler();
            if (!m_DBHandler.Initial()) {
                m_Log.Writeln(String.format("%s %s", "Start", "m_DBHandler fail"));
                return false;
            }
            return true;
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Start", e.getMessage()));
            return false;
        }
    }

    public void Stop() {
        m_LocalControlEP.Stop();
    }

    private void DISCONNECTED_recv(BaseMessage msg, EndPoint ep) {
    }

    private void PLSV_REGISTRY_recv(BaseMessage msg, EndPoint ep) {
        try {
            String name = msg.Args.get(0);
            String pw = msg.Args.get(1);
            String sql = "";
            //Check if name is exist or not
            sql = String.format("SELECT * FROM `Player` WHERE `PlayerName`='%s';", name);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_REGISTRY;
            if (rs.size() > 0) {
                //name already in use
                newMsg.Args.add("0");
                m_LocalControlEP.Send(newMsg, ep);
            }
            sql = String.format("INSERT INTO `Player` (`PlayerName`, `PlayerPW`) VALUES ('%s', '%s');", name, pw);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Insert fail
                newMsg.Args.add("0");
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s %s fail, name : %s", "PLSV_REGISTRY_recv", "Insert", name));
                return;
            }
            //Get PlayerNum
            sql = String.format("SELECT `PlayerNum` FROM `Player` WHERE `PlayerName`='%s' AND `PlayerPW`='%s';", name, pw);
            rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                //Select fail
                newMsg.Args.add("0");
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s %s fail, name : %s", "PLSV_REGISTRY_recv", "Select", name));
                return;
            }
            newMsg.Args.add("1");
            newMsg.Args.add(rs.get(0)[0]);
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_REGISTRY_recv", e.getMessage()));
        }
    }
}
