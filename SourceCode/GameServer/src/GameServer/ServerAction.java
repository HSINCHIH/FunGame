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
}
