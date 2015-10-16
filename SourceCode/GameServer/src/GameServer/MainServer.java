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
            case ServerAction.PLSV_LOGIN: {
                PLSV_LOGIN_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_CREATE_ROOM: {
                PLSV_CREATE_ROOM_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_GET_ROOM_MEMBER: {
                PLSV_GET_ROOM_MEMBER_recv(msg, ep);
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
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_REGISTRY_recv", sql));
                return;
            }
            sql = String.format("INSERT INTO `Player` (`PlayerName`, `PlayerPW`) VALUES ('%s', '%s');", name, pw);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Insert fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_REGISTRY_recv", sql));
                return;
            }
            newMsg.Args.add("1");
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_REGISTRY_recv", e.getMessage()));
        }
    }

    private void PLSV_LOGIN_recv(BaseMessage msg, EndPoint ep) {
        try {
            String name = msg.Args.get(0);
            String pw = msg.Args.get(1);
            String sql = "";
            //Get PlayerNum
            sql = String.format("SELECT `PlayerNum` FROM `Player` WHERE `PlayerName`='%s' AND `PlayerPW`='%s';", name, pw);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_LOGIN;
            if (rs.size() <= 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_LOGIN_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(rs.get(0)[0]);
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_LOGIN_recv", e.getMessage()));
        }
    }

    private void PLSV_CREATE_ROOM_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomName = msg.Args.get(1);
            String roomPW = msg.Args.get(2);
            String description = msg.Args.get(3);
            String sql = "";
            //Check if name is exist or not
            sql = String.format("SELECT * FROM `Room` WHERE `RoomName`='%s';", roomName);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_CREATE_ROOM;
            if (rs.size() > 0) {
                //RoomName already in use
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                return;
            }
            sql = String.format("INSERT INTO `Room` (`RoomName`, `RoomPW`,`Description`,`StartTime`, `GameNum`, `CreateBy`) VALUES ('%s', '%s', '%s', %s, %s, %s);", roomName, roomPW, description, "CURRENT_TIMESTAMP()", "1", playerNum);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Insert fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_CREATE_ROOM_recv", sql));
                return;
            }
            sql = String.format("SELECT `RoomNum` FROM `Room` WHERE `RoomName`='%s' AND `roomPW`='%s';", roomName, roomPW);
            rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_CREATE_ROOM_recv", sql));
                return;
            }
            String roomNum = rs.get(0)[0];
            sql = String.format("UPDATE `Player` SET `RoomNum` = %s WHERE `PlayerNum` = %s;", roomNum, playerNum);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Update fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_CREATE_ROOM_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(rs.get(0)[0]);
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_CREATE_ROOM_recv", e.getMessage()));
        }
    }

    private void PLSV_GET_ROOM_MEMBER_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            String sql = "";
            sql = String.format("SELECT `PlayerName` FROM `Player` WHERE `RoomNum`=%s;", roomNum);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GET_ROOM_MEMBER;
            if (rs.size() <= 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_GET_ROOM_MEMBER_recv", sql));
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String[] cols : rs) {
                sb.append(String.format("%s,", cols[0]));
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(sb.substring(0, sb.length() - 1));
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_GET_ROOM_MEMBER_recv", e.getMessage()));
        }
    }
}
