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
    public static final int PLSV_REGISTRY = 1;
    public static final int SVPL_REGISTRY = 2;
    public static final int PLSV_LOGIN = 3;
    public static final int SVPL_LOGIN = 4;
    public static final int PLSV_CREATE_ROOM = 5;
    public static final int SVPL_CREATE_ROOM = 6;
    public static final int PLSV_GET_ROOM_MEMBER = 7;
    public static final int SVPL_GET_ROOM_MEMBER = 8;
    public static final int PLSV_GET_ENABLE_ROOM = 9;
    public static final int SVPL_GET_ENABLE_ROOM = 10;
    public static final int PLSV_JOIN_ROOM = 11;
    public static final int SVPL_JOIN_ROOM = 12;
    public static final int PLSV_INITIAL_GAME = 13;
    public static final int SVPL_INITIAL_GAME = 14;
    public static final int SVPL_SETUP_GAME = 15;
    public static final int PLSV_GET_IMAGE = 16;
    public static final int SVPL_GET_IMAGE = 17;
    public static final int PLSV_GAME_READY = 18;
    public static final int SVPL_GAME_READY = 19;
    public static final int SVPL_GAME_START = 20;

}
