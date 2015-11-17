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
public class ServerAction {

    public static final int DISCONNECTED = 0;
    public static final int DUPLICATE_LOGIN = 1;
    public static final int PLSV_REGISTRY = 2;
    public static final int SVPL_REGISTRY = 3;
    public static final int PLSV_LOGIN = 4;
    public static final int SVPL_LOGIN = 5;
    public static final int PLSV_CREATE_ROOM = 6;
    public static final int SVPL_CREATE_ROOM = 7;
    public static final int PLSV_GET_ROOM_MEMBER = 8;
    public static final int SVPL_GET_ROOM_MEMBER = 9;
    public static final int PLSV_GET_ENABLE_ROOM = 10;
    public static final int SVPL_GET_ENABLE_ROOM = 11;
    public static final int PLSV_JOIN_ROOM = 12;
    public static final int SVPL_JOIN_ROOM = 13;
    public static final int PLSV_LEAVE_ROOM = 14;
    public static final int SVPL_LEAVE_ROOM = 15;
    public static final int PLSV_INITIAL_GAME = 16;
    public static final int SVPL_INITIAL_GAME = 17;
    public static final int SVPL_SETUP_GAME = 18;
    public static final int PLSV_GET_IMAGE = 19;
    public static final int SVPL_GET_IMAGE = 20;
    public static final int PLSV_GAME_READY = 21;
    public static final int SVPL_GAME_READY = 22;
    public static final int SVPL_GAME_START = 23;
    public static final int PLSV_OPERATOR = 24;
    public static final int SVPL_OPERATOR = 25;
    public static final int SVPL_WINNER = 26;
    public static final int MOSV_LOGIN = 27;
    public static final int SVMO_LOGIN = 28;
    public static final int MOSV_GET_ENABLE_ROOM = 29;
    public static final int SVMO_GET_ENABLE_ROOM = 30;
    public static final int MOSV_WATCH_ROOM = 31;
    public static final int SVMO_WATCH_ROOM = 32;
}
