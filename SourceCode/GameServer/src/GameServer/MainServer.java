/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.util.ArrayList;
import java.util.HashMap;
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
            case ServerAction.PLSV_GET_ENABLE_ROOM: {
                PLSV_GET_ENABLE_ROOM_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_JOIN_ROOM: {
                PLSV_JOIN_ROOM_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_LEAVE_ROOM: {
                PLSV_LEAVE_ROOM_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_INITIAL_GAME: {
                PLSV_INITIAL_GAME_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_GET_IMAGE: {
                PLSV_GET_IMAGE_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_GAME_READY: {
                PLSV_GAME_READY_recv(msg, ep);
            }
            break;
            default: {
                m_Log.Writeln(String.format("%s UnKnown message : %s", "ReceiveMsg", msg.toString()));
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
        String sql = String.format("UPDATE `Player` SET `Online` = %d WHERE `EndPoint` = '%s';", 0, ep.toString());
        if (m_DBHandler.Execute(sql) <= 0) {
            //Update fail
            m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_LOGIN_recv", sql));
        }
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
            sql = String.format("UPDATE `Player` SET `EndPoint` = '%s', `Online` = %d WHERE `PlayerNum` = %s;", ep.toString(), 1, rs.get(0)[0]);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Update fail
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
            //Check if name is exist or not
            String sql = String.format("SELECT * FROM `Room` WHERE `RoomName`='%s';", roomName);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_CREATE_ROOM;
            if (rs.size() > 0) {
                //RoomName already in use
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                return;
            }
            sql = String.format("INSERT INTO `Room` (`RoomName`, `RoomPW`,`Description`, `GameNum`, `CreateBy`) VALUES ('%s', '%s', '%s', %s, %s);", roomName, roomPW, description, "1", playerNum);
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
            sql = String.format("INSERT INTO `RoomPlayer` (`PlayerNum`, `RoomNum`) VALUES (%s, %s);", playerNum, roomNum);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Insert fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_CREATE_ROOM_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(roomNum);
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_CREATE_ROOM_recv", e.getMessage()));
        }
    }

    private void PLSV_GET_ROOM_MEMBER_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            String sql = String.format("SELECT T1.PlayerName FROM `Player` AS T1,`RoomPlayer` AS T2 WHERE T1.Online = 1 AND T1.PlayerNum = T2.PlayerNum AND T2.RoomNum = %s;", roomNum);
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

    private void PLSV_GET_ENABLE_ROOM_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String sql = String.format("SELECT `RoomNum`,`RoomName` FROM `Room` WHERE `RoomState` = %d;", 1);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GET_ENABLE_ROOM;
            StringBuilder sb = new StringBuilder();
            newMsg.Args.add("1");//Success
            for (String[] cols : rs) {
                sb.append(String.format("{RoomNum:\"%s\",RoomName:\"%s\"},", cols[0], cols[1]));
            }
            newMsg.Args.add(String.format("[%s]", sb.toString().substring(0, sb.length() - 1)));
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_GET_ENABLE_ROOM_recv", e.getMessage()));
        }
    }

    private void PLSV_JOIN_ROOM_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            String roomName = msg.Args.get(2);
            String roomPW = msg.Args.get(3);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_JOIN_ROOM;
            //Check room password
            String sql = String.format("SELECT `RoomName` FROM `Room` WHERE `RoomNum` = %s AND `RoomPW` = '%s';", roomNum, roomPW);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_JOIN_ROOM_recv", sql));
                return;
            }
            //Check if user already in room
            sql = String.format("SELECT `MemberID` FROM `RoomPlayer` WHERE `PlayerNum` = %s AND `RoomNum` = %s AND `Enable` = %d;", playerNum, roomNum, 1);
            rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() > 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_JOIN_ROOM_recv", sql));
                return;
            }
            sql = String.format("INSERT INTO `RoomPlayer` (`PlayerNum`, `RoomNum`) VALUES (%s, %s);", playerNum, roomNum);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Insert fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_JOIN_ROOM_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(roomNum);
            newMsg.Args.add(roomName);
            m_LocalControlEP.Send(newMsg, ep);
            //Get room host endpoint
            sql = String.format("SELECT T1.PlayerName,T1.EndPoint FROM `Player` AS T1,`Room` AS T2 WHERE T2.RoomNum = %s AND T2.CreateBy = T1.PlayerNum ;", roomNum);
            rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_JOIN_ROOM_recv", sql));
                return;
            }
            newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GET_ROOM_MEMBER;
            EndPoint hostEP = new EndPoint(rs.get(0)[1]);
            //Get room players
            sql = String.format("SELECT T1.PlayerName FROM `Player` AS T1,`RoomPlayer` AS T2 WHERE T1.Online = 1 AND T1.PlayerNum = T2.PlayerNum AND T2.RoomNum = %s AND T2.Enable = %d;", roomNum, 1);
            rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_JOIN_ROOM_recv", sql));
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String[] cols : rs) {
                sb.append(String.format("%s,", cols[0]));
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(sb.substring(0, sb.length() - 1));
            m_LocalControlEP.Send(newMsg, hostEP);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_JOIN_ROOM_recv", e.getMessage()));
        }
    }

    private void PLSV_LEAVE_ROOM_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            String roomName = msg.Args.get(2);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_LEAVE_ROOM;
            String sql = String.format("UPDATE `RoomPlayer` SET `Enable` = %d WHERE `PlayerNum` = %s AND `RoomNum` = %s;", 0, playerNum, roomNum);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Insert fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_LEAVE_ROOM_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            m_LocalControlEP.Send(newMsg, ep);
            //Get room host endpoint
            sql = String.format("SELECT T1.PlayerName,T1.EndPoint FROM `Player` AS T1,`Room` AS T2 WHERE T2.RoomNum = %s AND T2.CreateBy = T1.PlayerNum ;", roomNum);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_JOIN_ROOM_recv", sql));
                return;
            }
            newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GET_ROOM_MEMBER;
            EndPoint hostEP = new EndPoint(rs.get(0)[1]);
            //Get room players
            sql = String.format("SELECT T1.PlayerName FROM `Player` AS T1,`RoomPlayer` AS T2 WHERE T1.Online = 1 AND T1.PlayerNum = T2.PlayerNum AND T2.RoomNum = %s AND T2.Enable = %d;", roomNum, 1);
            rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_JOIN_ROOM_recv", sql));
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String[] cols : rs) {
                sb.append(String.format("%s,", cols[0]));
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(sb.substring(0, sb.length() - 1));
            m_LocalControlEP.Send(newMsg, hostEP);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_LEAVE_ROOM_recv", e.getMessage()));
        }
    }

    private void PLSV_INITIAL_GAME_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_INITIAL_GAME;
            String sql = String.format("SELECT `RoomNum` FROM `Room` WHERE `RoomNum` = %s AND `CreateBy` = %s;", roomNum, playerNum);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_INITIAL_GAME_recv", sql));
                return;
            }
            sql = String.format("UPDATE `Room` SET `StartTime` = CURRENT_TIMESTAMP() , `RoomState` = %d WHERE `RoomNum` = %s;", 2, roomNum);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Update fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_INITIAL_GAME_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            m_LocalControlEP.Send(newMsg, ep);
            //Send image order to player
            newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_SETUP_GAME;
            sql = String.format("SELECT T1.EndPoint FROM `Player` AS T1,`RoomPlayer` AS T2 WHERE T1.Online = %d AND T1.PlayerNum = T2.PlayerNum AND T2.RoomNum = %s;", 1, roomNum);
            rs = m_DBHandler.ExecuteQuery(sql);
            for (String[] cols : rs) {
                EndPoint sendEp = new EndPoint(cols[0]);
                m_LocalControlEP.Send(newMsg, sendEp);
            }
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_INITIAL_GAME_recv", e.getMessage()));
        }
    }

    private String GetImageSetting() {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            temp.add((i % 8) + 1);
        }
        ArrayList<Integer> cloneTemp = (ArrayList<Integer>) temp.clone();
        StringBuilder sb = new StringBuilder();
        int orderIndex = 0;
        while (!cloneTemp.isEmpty()) {
            int itemIndex = (int) (Math.random() * cloneTemp.size());
            sb.append(String.format("\"%02d\":{\"Img\":\"%02d\",\"Open\":0,\"Click\":0},", orderIndex, cloneTemp.get(itemIndex)));
            cloneTemp.remove(itemIndex);
            orderIndex++;
        }
        return String.format("({%s})", sb.toString().substring(0, sb.length() - 1));
    }

    private void PLSV_GET_IMAGE_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            String jsonSetting = GetImageSetting();
            String sql = String.format("INSERT INTO `Record` (`RoomNum`, `PlayerNum`, `Command`, `RecordTime`) VALUES (%s, %s, '%s', %s);", roomNum, playerNum, jsonSetting, "CURRENT_TIMESTAMP()");
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GET_IMAGE;
            if (m_DBHandler.Execute(sql) <= 0) {
                //Update fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_GET_IMAGE_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(jsonSetting);
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_GET_IMAGE_recv", e.getMessage()));
        }
    }

    private void PLSV_GAME_READY_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GAME_READY;
            String sql = String.format("UPDATE `RoomPlayer` SET `Status` = %d WHERE `RoomNum` = %s AND `PlayerNum` = %s;", 1, roomNum, playerNum);
            if (m_DBHandler.Execute(sql) <= 0) {
                //Update fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_GAME_READY_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            m_LocalControlEP.Send(newMsg, ep);
            sql = String.format("SELECT COUNT(*) = SUM(`Status`) FROM `RoomPlayer` WHERE `RoomNum` = %s;", roomNum);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_GAME_READY_recv", sql));
                return;
            }
            if (rs.get(0)[0].equals("0")) {
                return;
            }
            newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GAME_START;
            sql = String.format("SELECT T1.EndPoint FROM `Player` AS T1,`RoomPlayer` AS T2 WHERE T1.Online = %d AND T1.PlayerNum = T2.PlayerNum AND T2.RoomNum = %s AND T2.Enable = %d;", 1, roomNum, 1);
            rs = m_DBHandler.ExecuteQuery(sql);
            for (String[] cols : rs) {
                EndPoint sendEp = new EndPoint(cols[0]);
                m_LocalControlEP.Send(newMsg, sendEp);
            }
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_GAME_READY_recv", e.getMessage()));
        }
    }
}
