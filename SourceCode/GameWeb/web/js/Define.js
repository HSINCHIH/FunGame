/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var Version = "0.0.0.2";
var GameServerIP = "127.0.0.1";
var GameServerPort = 99;
var MessageLevel = {
    Info: 1,
    Danger: 2,
    Warning: 3,
    Success: 4
};
var ServerAction = {
    DISCONNECTED: 0,
    DUPLICATE_LOGIN: 1,
    PLSV_REGISTRY: 2,
    SVPL_REGISTRY: 3,
    PLSV_LOGIN: 4,
    SVPL_LOGIN: 5,
    PLSV_CREATE_ROOM: 6,
    SVPL_CREATE_ROOM: 7,
    PLSV_GET_ROOM_MEMBER: 8,
    SVPL_GET_ROOM_MEMBER: 9,
    PLSV_GET_ENABLE_ROOM: 10,
    SVPL_GET_ENABLE_ROOM: 11,
    PLSV_JOIN_ROOM: 12,
    SVPL_JOIN_ROOM: 13,
    PLSV_LEAVE_ROOM: 14,
    SVPL_LEAVE_ROOM: 15,
    PLSV_INITIAL_GAME: 16,
    SVPL_INITIAL_GAME: 17,
    SVPL_SETUP_GAME: 18,
    PLSV_GET_CARD_STATE: 19,
    SVPL_GET_CARD_STATE: 20,
    PLSV_GAME_READY: 21,
    SVPL_GAME_READY: 22,
    SVPL_GAME_START: 23,
    PLSV_OPERATOR: 24,
    SVPL_OPERATOR: 25,
    SVPL_WINNER: 26,
    MOSV_LOGIN: 27,
    SVMO_LOGIN: 28,
    MOSV_GET_ENABLE_ROOM: 29,
    SVMO_GET_ENABLE_ROOM: 30,
    MOSV_WATCH_ROOM: 31,
    SVMO_WATCH_ROOM: 32,
    MOSV_LOAD_ROOM_STATE: 33,
    SVMO_LOAD_ROOM_STATE: 34,
    SVMO_SETP: 35,
    SVMO_GAME_READY: 36,
    SVMO_GAME_START: 37,
    PLSV_RESUME_GAME: 38,
    SVPL_RESUME_GAME: 39,
    MOSV_GAME_HISTORY: 40,
    SVMO_GAME_HISTORY: 41,
    MOSV_GET_REPLAY_ROOM: 42,
    SVMO_GET_REPLAY_ROOM: 43,
    MOSV_GET_REPLAY_DATA: 44,
    SVMO_GET_REPLAY_DATA: 45
};
var GameLevel = ["Normal", "Nightmare", "Hell"];



