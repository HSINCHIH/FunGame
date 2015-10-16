/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var GameServerIP = "127.0.0.1";
var GameServerPort = 99;
var MessageLevel = {
    Info: 1,
    Danger: 2,
    Warning: 3,
    Success: 4
};
var ServerAction =
        {
            DISCONNECTED: 0,
            PLSV_REGISTRY: 1,
            SVPL_REGISTRY: 2,
            PLSV_LOGIN: 3,
            SVPL_LOGIN: 4,
            PLSV_CREATE_ROOM: 5,
            SVPL_CREATE_ROOM: 6,
            PLSV_GET_ROOM_MEMBER: 7,
            SVPL_GET_ROOM_MEMBER: 8

        };



