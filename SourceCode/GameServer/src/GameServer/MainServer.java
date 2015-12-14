/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mark.chen
 */
public class MainServer implements IReceiveMsgCallBack {

    Log m_Log = new Log("MainServer");
    String m_HostIP = "";
    WebSocketServerEP m_LocalControlEP = null;
    DataBaseHandler m_DBHandler = null;
    EndPoint m_MonitorEP = null;
    String m_WatchRoom = "-1";

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
            case ServerAction.PLSV_GET_CARD_STATE: {
                PLSV_GET_CARD_STATE_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_GAME_READY: {
                PLSV_GAME_READY_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_OPERATOR: {
                PLSV_OPERATOR_recv(msg, ep);
            }
            break;
            case ServerAction.PLSV_RESUME_GAME: {
                PLSV_RESUME_GAME_recv(msg, ep);
            }
            break;
            case ServerAction.MOSV_LOGIN: {
                MOSV_LOGIN_recv(msg, ep);
            }
            break;
            case ServerAction.MOSV_GET_ENABLE_ROOM: {
                MOSV_GET_ENABLE_ROOM_recv(msg, ep);
            }
            break;
            case ServerAction.MOSV_WATCH_ROOM: {
                MOSV_WATCH_ROOM_recv(msg, ep);
            }
            break;
            case ServerAction.MOSV_LOAD_ROOM_STATE: {
                MOSV_LOAD_ROOM_STATE_recv(msg, ep);
            }
            break;
            case ServerAction.MOSV_GAME_HISTORY: {
                MOSV_GAME_HISTORY_recv(msg, ep);
            }
            break;
            case ServerAction.MOSV_GET_REPLAY_ROOM: {
                MOSV_GET_REPLAY_ROOM_recv(msg, ep);
            }
            break;
            case ServerAction.MOSV_GET_REPLAY_DATA: {
                MOSV_GET_REPLAY_DATA_recv(msg, ep);
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

    private String CreateCardState(int level) {
        ArrayList<String> sourceItems = new ArrayList<>();
        sourceItems.addAll(Arrays.asList("RS30", "CP60", "CP55", "CP50", "9700", "9200", "8600", "8400", "8300", "8200", "8000", "1000", "1100", "1500", "1704", "1861"));
        ArrayList<String> pickItems = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            int index = (int) (Math.random() * sourceItems.size());
            String item = sourceItems.get(index);
            switch (level) {
                case 0: {
                    pickItems.add(String.format("%s_%d", item, 0));
                    pickItems.add(String.format("%s_%d", item, 0));
                }
                break;
                case 1: {
                    pickItems.add(String.format("%s_%d", item, 1));
                    pickItems.add(String.format("%s_%d", item, 1));
                }
                break;
                case 2: {
                    pickItems.add(String.format("%s_%d", item, 0));
                    pickItems.add(String.format("%s_%d", item, 1));
                }
                break;
                default: {
                }
                break;
            }
            sourceItems.remove(index);
        }
        StringBuilder sb = new StringBuilder();
        int orderIndex = 0;
        while (!pickItems.isEmpty()) {
            int index = (int) (Math.random() * pickItems.size());
            //int index = 0;
            sb.append(String.format("{\"Card\":\"%02d\",\"Img\":\"%s\",\"Open\":0,\"Click\":0,\"Content\":\"%s\"},", orderIndex, pickItems.get(index), pickItems.get(index).split("_")[0]));
            pickItems.remove(index);
            orderIndex++;
        }
        return String.format("[%s]", sb.toString().substring(0, sb.length() - 1));
    }

    private void DISCONNECTED_recv(BaseMessage msg, EndPoint ep) {
        if (m_MonitorEP != null && ep.toString().equals(m_MonitorEP.toString())) {
            m_MonitorEP = null;
        } else {
            String sql = String.format("SELECT `PlayerNum` FROM `Player` WHERE `EndPoint` = '%s';", ep.toString());
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() > 0) {
                sql = String.format("UPDATE `Player` SET `Online` = %d WHERE `EndPoint` = '%s';", 0, ep.toString());
                if (m_DBHandler.Execute(sql) <= 0) {
                    //Update fail
                    m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_LOGIN_recv", sql));
                }
            }
        }
        m_LocalControlEP.Remove(ep);
    }

    //Player command
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
            String gameLevel = msg.Args.get(3);
            String description = msg.Args.get(4);
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
            sql = String.format("INSERT INTO `Room` (`RoomName`, `RoomPW`, `GameLevel`, `Description`, `GameNum`, `CreateBy`) VALUES ('%s', '%s', %s, '%s', %s, %s);", roomName, roomPW, gameLevel, description, "1", playerNum);
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
            String sql = String.format("SELECT `RoomNum`, `RoomName`, `GameLevel` FROM `Room` WHERE `RoomState` = %d;", 1);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GET_ENABLE_ROOM;
            newMsg.Args.add("1");//Success
            for (String[] cols : rs) {
                newMsg.Args.add(cols[0]);
                newMsg.Args.add(cols[1]);
                newMsg.Args.add(cols[2]);
            }
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

    private void PLSV_GET_CARD_STATE_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            String sql = String.format("SELECT `GameLevel` FROM `Room` WHERE `RoomNum` = %s;", roomNum);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            int gameLevel = Integer.parseInt(rs.get(0)[0]);
            String jsonSetting = CreateCardState(gameLevel);
            sql = String.format("INSERT INTO `Record` (`RoomNum`, `PlayerNum`, `State`, `RecordTime`) VALUES (%s, %s, '%s', %s);", roomNum, playerNum, jsonSetting, "CURRENT_TIMESTAMP()");
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GET_CARD_STATE;
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
            //Notify Monitor
            if (m_WatchRoom.equals(roomNum)) {
                newMsg = new BaseMessage();
                newMsg.Action = ServerAction.SVMO_GAME_READY;
                newMsg.Args.add(roomNum);
                m_LocalControlEP.Send(newMsg, m_MonitorEP);
            }
            newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_GAME_START;
            sql = String.format("SELECT T1.EndPoint FROM `Player` AS T1,`RoomPlayer` AS T2 WHERE T1.Online = %d AND T1.PlayerNum = T2.PlayerNum AND T2.RoomNum = %s AND T2.Enable = %d;", 1, roomNum, 1);
            rs = m_DBHandler.ExecuteQuery(sql);
            for (String[] cols : rs) {
                EndPoint sendEp = new EndPoint(cols[0]);
                m_LocalControlEP.Send(newMsg, sendEp);
            }
            //Notify Monitor
            if (m_WatchRoom.equals(roomNum)) {
                newMsg = new BaseMessage();
                newMsg.Action = ServerAction.SVMO_GAME_START;
                m_LocalControlEP.Send(newMsg, m_MonitorEP);
            }
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_GAME_READY_recv", e.getMessage()));
        }
    }

    private void PLSV_OPERATOR_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            String roomNum = msg.Args.get(1);
            String step = msg.Args.get(2);
            String state = msg.Args.get(3);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_OPERATOR;
            String sql = String.format("INSERT INTO `Record` (`RoomNum`, `PlayerNum`, `Step`, `State`, `RecordTime`) VALUES (%s, %s, '%s', '%s', %s);", roomNum, playerNum, step, state, "CURRENT_TIMESTAMP()");
            if (m_DBHandler.Execute(sql) <= 0) {
                //Update fail
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_OPERATOR_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            m_LocalControlEP.Send(newMsg, ep);
            //Check send monitor or not
            if (m_MonitorEP != null && m_WatchRoom.equals(roomNum)) {
                //Send to monitor
                newMsg = new BaseMessage();
                newMsg.Action = ServerAction.SVMO_SETP;
                newMsg.Args.add(playerNum);
                newMsg.Args.add(step);
                newMsg.Args.add(state);
                m_LocalControlEP.Send(newMsg, m_MonitorEP);
            }
            //Check game over or not 
            String patternStr = "(\"Open\":[1])";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(state);
            int totalOpen = 0;
            while (matcher.find()) {
                String data = matcher.group(0);
                totalOpen++;
            }
            if (totalOpen == 17) {
                //Check winner
                sql = String.format("SELECT PlayerNum FROM `RoomPlayer` WHERE `RoomNum` = %s AND `Winner` = %d;", roomNum, 1);
                List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
                if (rs.size() > 0) {
                    return;
                }
                //Update roomplayer
                sql = String.format("UPDATE `RoomPlayer` SET `Winner` = %d WHERE `PlayerNum` = %s AND `RoomNum` = %s;", 1, playerNum, roomNum);
                if (m_DBHandler.Execute(sql) <= 0) {
                    //Update fail
                    m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_OPERATOR_recv", sql));
                }
                //Update room
                sql = String.format("UPDATE `Room` SET `StopTime` = %s, `RoomState` = %d WHERE `RoomNum` = %s;", "CURRENT_TIMESTAMP()", 3, roomNum);
                if (m_DBHandler.Execute(sql) <= 0) {
                    //Update fail
                    m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_OPERATOR_recv", sql));
                }
                //Send winner to player
                newMsg = new BaseMessage();
                newMsg.Action = ServerAction.SVPL_WINNER;
                newMsg.Args.add(roomNum);
                newMsg.Args.add(playerNum);
                sql = String.format("SELECT T1.EndPoint FROM `Player` AS T1,`RoomPlayer` AS T2 WHERE T1.Online = %d AND T1.PlayerNum = T2.PlayerNum AND T2.RoomNum = %s AND T2.Enable = %d;", 1, roomNum, 1);
                rs = m_DBHandler.ExecuteQuery(sql);
                for (String[] cols : rs) {
                    EndPoint sendEp = new EndPoint(cols[0]);
                    m_LocalControlEP.Send(newMsg, sendEp);
                }
            }
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_OPERATOR_recv", e.getMessage()));
        }
    }

    private void PLSV_RESUME_GAME_recv(BaseMessage msg, EndPoint ep) {
        try {
            String playerNum = msg.Args.get(0);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVPL_RESUME_GAME;
            String sql = String.format("SELECT T1.RoomNum, T2.RoomName, T1.Step, T1.State FROM (SELECT * FROM `Record` WHERE `PlayerNum` = %s ORDER BY `RecordNum` DESC LIMIT 1) AS T1 INNER JOIN (SELECT * FROM `Room` WHERE `RoomState` < 3) AS T2 ON T1.RoomNum = T2.RoomNum;", playerNum);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "PLSV_RESUME_GAME_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(rs.get(0)[0]);
            newMsg.Args.add(rs.get(0)[1]);
            newMsg.Args.add(rs.get(0)[2]);
            newMsg.Args.add(rs.get(0)[3]);
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_RESUME_GAME_recv", e.getMessage()));
        }
    }

    //Monitor command
    private void MOSV_LOGIN_recv(BaseMessage msg, EndPoint ep) {
        try {
            String name = msg.Args.get(0);
            String pw = msg.Args.get(1);
            BaseMessage newMsg = new BaseMessage();
            if (!name.equals("monitor") || !pw.equals("monitor")) {
                return;
            }
            if (m_MonitorEP != null) {
                newMsg.Action = ServerAction.DUPLICATE_LOGIN;
                m_LocalControlEP.Send(newMsg, m_MonitorEP);
                m_LocalControlEP.Remove(m_MonitorEP);
            }
            m_MonitorEP = new EndPoint(ep.toString());
            newMsg.Action = ServerAction.SVMO_LOGIN;
            newMsg.Args.add("1");//Success
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_LOGIN_recv", e.getMessage()));
        }
    }

    private void MOSV_GET_ENABLE_ROOM_recv(BaseMessage msg, EndPoint ep) {
        try {
            String sql = String.format("SELECT `RoomNum`, `RoomName`, `GameLevel` FROM `Room` WHERE `RoomState` < %d;", 3);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVMO_GET_ENABLE_ROOM;
            newMsg.Args.add("1");//Success
            for (String[] cols : rs) {
                newMsg.Args.add(cols[0]);
                newMsg.Args.add(cols[1]);
                newMsg.Args.add(cols[2]);
            }
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_GET_ENABLE_ROOM_recv", e.getMessage()));
        }
    }

    private void MOSV_WATCH_ROOM_recv(BaseMessage msg, EndPoint ep) {
        try {
            String roomNum = msg.Args.get(0);
            m_WatchRoom = roomNum;
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVMO_WATCH_ROOM;
            newMsg.Args.add("1");//Success
            newMsg.Args.add(roomNum);
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_WATCH_ROOM_recv", e.getMessage()));
        }
    }

    private void MOSV_LOAD_ROOM_STATE_recv(BaseMessage msg, EndPoint ep) {
        try {
            String roomNum = msg.Args.get(0);
            String sql = String.format("SELECT T1.PlayerNum, T3.PlayerName, T1.Step, T1.State FROM `Record` AS T1 JOIN (SELECT MAX(`RecordNum`) AS `LatestNum`,PlayerNum FROM `Record` WHERE `RoomNum` = %s GROUP BY `PlayerNum`) AS T2 ON T1.RecordNum = T2.LatestNum  LEFT JOIN `Player` AS T3 on T2.PlayerNum = T3.PlayerNum;", roomNum);
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVMO_LOAD_ROOM_STATE;
            StringBuilder sb = new StringBuilder();
            newMsg.Args.add("1");//Success
            for (String[] cols : rs) {
                newMsg.Args.add(cols[0]);
                newMsg.Args.add(cols[1]);
                newMsg.Args.add(cols[2]);
                newMsg.Args.add(cols[3]);
            }
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_LOAD_ROOM_STATE_recv", e.getMessage()));
        }
    }

    private void MOSV_GAME_HISTORY_recv(BaseMessage msg, EndPoint ep) {
        try {
            String sql = String.format("SELECT T3.RoomName, T3.GameLevel, T4.PlayerName AS Winner, TIMESTAMPDIFF(SECOND,MIN(T1.RecordTime),MAX(T1.RecordTime)) AS `UseSeconds` FROM `Record` AS T1 INNER JOIN (SELECT `PlayerNum`, `RoomNum` FROM `RoomPlayer` WHERE `Winner` > 0) AS T2 INNER JOIN `Room` AS T3 INNER JOIN `Player` AS T4 ON T1.PlayerNum = T2.PlayerNum AND T1.RoomNum = T2.RoomNum AND T1.RoomNum = T3.RoomNum AND T1.PlayerNum = T4.PlayerNum GROUP BY T1.RoomNum;");
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVMO_GAME_HISTORY;
            newMsg.Args.add("1");//Success
            for (String[] cols : rs) {
                newMsg.Args.add(cols[0]);
                newMsg.Args.add(cols[1]);
                newMsg.Args.add(cols[2]);
                newMsg.Args.add(cols[3]);
            }
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_GAME_RESULT_recv", e.getMessage()));
        }
    }

    private void MOSV_GET_REPLAY_ROOM_recv(BaseMessage msg, EndPoint ep) {
        try {
            String sql = String.format("SELECT `RoomNum`, `RoomName`, `GameLevel` FROM `Room` WHERE `RoomState` = 3;");
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVMO_GET_REPLAY_ROOM;
            newMsg.Args.add("1");//Success
            for (String[] cols : rs) {
                newMsg.Args.add(cols[0]);
                newMsg.Args.add(cols[1]);
                newMsg.Args.add(cols[2]);
            }
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_GET_REPLAY_ROOM_recv", e.getMessage()));
        }
    }

    private void MOSV_GET_REPLAY_DATA_recv(BaseMessage msg, EndPoint ep) {
        try {
            String roomNum = msg.Args.get(0);
            String sql = String.format("SELECT T1.PlayerNum, T3.PlayerName, T1.Step, T1.State FROM `Record` AS T1 JOIN (SELECT MIN(`RecordNum`) AS `LatestNum`,PlayerNum FROM `Record` WHERE `RoomNum` = %s GROUP BY `PlayerNum`) AS T2 ON T1.RecordNum = T2.LatestNum  LEFT JOIN `Player` AS T3 on T2.PlayerNum = T3.PlayerNum;", roomNum);
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVMO_GET_REPLAY_DATA;
            List<String[]> rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "MOSV_GET_REPLAY_DATA_recv", sql));
                return;
            }
            newMsg.Args.add("1");//Success
            newMsg.Args.add(String.format("%d", rs.size()));
            for (String[] cols : rs) {
                newMsg.Args.add(cols[0]);
                newMsg.Args.add(cols[1]);
                newMsg.Args.add(cols[2]);
                newMsg.Args.add(cols[3]);
            }
            sql = String.format("SELECT `PlayerNum`, `Step` FROM `Record` WHERE `RoomNum` = %s  AND `Step` <> 'empty' ORDER BY `RecordNum`;", roomNum);
            rs = m_DBHandler.ExecuteQuery(sql);
            if (rs.size() <= 0) {
                newMsg.Args.clear();
                newMsg.Args.add("0");//Fail
                m_LocalControlEP.Send(newMsg, ep);
                m_Log.Writeln(String.format("%s fail, sql : %s", "MOSV_GET_REPLAY_DATA_recv", sql));
                return;
            }
            for (String[] cols : rs) {
                newMsg.Args.add(cols[0]);
                newMsg.Args.add(cols[1]);
            }
            m_LocalControlEP.Send(newMsg, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_GET_REPLAY_DATA_recv", e.getMessage()));
        }
    }
}
