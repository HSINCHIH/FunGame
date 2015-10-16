/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function PlayGame() {
}
PlayGame.prototype = {
    m_Socket: null,
    m_IsConnect: false,
    m_IsLogin: false,
    m_PlayerName: "",
    m_PlayerNum: "",
    m_RoomName: "",
    m_RoomNum: "",
    OnOpen: function (event)
    {
        console.log("OnOpen");
        this.m_IsConnect = true;
    },
    OnReceive: function (event)
    {
        console.log("OnReceive");
        var recvMsg = this.ParseData(event.data);
        switch (recvMsg.Action)
        {
            case ServerAction.SVPL_REGISTRY:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        this.ShowSignUpMsg(MessageLevel.Danger, "Sign up fail, please try another user name and password");
                        return;
                    }
                    this.ShowSignUpMsg(MessageLevel.Success, "Sign up success, now you can login the game");
                }
                break;
            case ServerAction.SVPL_LOGIN:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        this.ShowLoginMsg(MessageLevel.Danger, "Login fail, please try again!!");
                        return;
                    }
                    this.m_PlayerNum = recvMsg.Args[1];
                    this.m_PlayerName = $("#TB_Login_ID").val();
                    this.ShowLoginMsg(MessageLevel.Success, "Login success!!");
                    this.ShowPlayer(this.m_PlayerName);
                    this.m_IsLogin = true;
                    $("#DLG_Login").modal("toggle");
                    this.ShowWelcome(this.m_PlayerName);
                }
                break;
            case ServerAction.SVPL_CREATE_ROOM:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        this.ShowCreateRoomMsg(MessageLevel.Danger, "Create room fail, please try again!!");
                        return;
                    }
                    this.ShowCreateRoomMsg(MessageLevel.Success, "Create room success");
                    this.m_RoomName = $("#TB_Create_Room_Name").val();
                    this.m_RoomNum = recvMsg.Args[1];
                    this.ShowRoom(this.m_RoomName);
                    this.ShowHostRoomName(this.m_RoomName);
                    //Get room member
                    var newMsg = new Message();
                    newMsg.Action = ServerAction.PLSV_GET_ROOM_MEMBER;
                    newMsg.Args.push(this.m_PlayerNum);
                    newMsg.Args.push(this.m_RoomNum);
                    this.Send(newMsg);
                    //Toggle form
                    $("#DLG_Create_Room").modal("toggle");
                    $("#DLG_Room_Host").modal("toggle");
                }
                break;
            case ServerAction.SVPL_GET_ROOM_MEMBER:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_GET_ROOM_MEMBER fail");
                        return;
                    }
                    this.ShowHostRoomList(recvMsg.Args[1]);
                }
                break;
            case ServerAction.SVPL_GET_ENABLE_ROOM:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_GET_ENABLE_ROOM fail");
                        return;
                    }
                    this.ShowJoinRoomList(recvMsg.Args[1]);
                }
                break;
            case ServerAction.SVPL_JOIN_ROOM:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_JOIN_ROOM fail");
                        return;
                    }
                    this.m_RoomNum = recvMsg.Args[1];
                    this.m_RoomName = recvMsg.Args[2];
                    this.ShowRoom(this.m_RoomName);
                    //Toggle form
                    $("#DLG_Join_Room").modal("toggle");
                }
                break
            default:
                {
                }
                break
        }
    },
    OnError: function (event)
    {
        console.log("OnError");
    },
    OnClose: function (event)
    {
        console.log("OnClose");
    },
    Send: function (msg)
    {
        this.m_Socket.Send(msg.GetString());
    },
    ParseData: function (recvString)
    {
        var args = recvString.split(/[\|;]/);
        var msg = new Message();
        msg.Action = parseInt(args[0]);
        for (var i = 1; i < args.length; i++)
        {
            msg.Args.push(args[i]);
        }
        return msg;
    },
    Connect: function ()
    {
        this.m_Socket.Connect(GameServerIP + ":" + GameServerPort);
    },
    CreateMap: function ()
    {
        var imgList = [];
        for (var i = 0; i < 16; i++)
        {
            imgList.push("0" + (i % 8 + 1));
        }
        for (var i = 0; i < imgList.length; i++)
        {
            $('#gameDiv').append('<div class="col-md-3 col-xs-3"><img src="images/' + imgList[i] + '.png" alt="" class="img-responsive"/></div>');
        }
    },
    SingUp: function ()
    {
        //Disable button
        $("#BT_SignUp").prop('disabled', true);
        if (!this.m_IsConnect)
        {
            console.log("Socket no connect ");
            return;
        }
        var name = $("#TB_SignUp_ID").val();
        var pw = $("#TB_SignUp_PW").val();
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_REGISTRY;
        newMsg.Args.push(name);
        newMsg.Args.push(pw);
        this.Send(newMsg);
    },
    ShowSignUpMsg: function (level, msg)
    {
        this.ShowMsg("LB_SignUp_MSG", level, msg);
    },
    OnSignUpClick: function ()
    {
        //Reset sign form
        $("#TB_SignUp_ID").val("");
        $("#TB_SignUp_PW").val("");
        $("#LB_SignUp_MSG").text("");
        $("#LB_SignUp_MSG").hide();
        $("#BT_SignUp").prop('disabled', false);
        //Toggle form
        $("#DLG_SignUp").modal("toggle");
    },
    Login: function ()
    {
        //Disable button
        $("#BT_Login").prop('disabled', true);
        if (!this.m_IsConnect)
        {
            console.log("Socket no connect ");
            return;
        }
        var name = $("#TB_Login_ID").val();
        var pw = $("#TB_Login_PW").val();
        var rememberMe = $("#CKB_RememberMe").prop("checked");
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_LOGIN;
        newMsg.Args.push(name);
        newMsg.Args.push(pw);
        this.Send(newMsg);
    },
    ShowMsg: function (item, level, msg)
    {
        var selectItem = $("#" + item);
        selectItem.show();
        selectItem.removeClass();
        selectItem.addClass("alert");
        switch (level)
        {
            case MessageLevel.Info:
                {
                    selectItem.addClass("alert-info");
                }
                break;
            case MessageLevel.Danger:
                {
                    selectItem.addClass("alert-danger");
                }
                break;
            case MessageLevel.Warning:
                {
                    selectItem.addClass("alert-warning");
                }
                break;
            case MessageLevel.Success:
                {
                    selectItem.addClass("alert-success");
                }
                break;
            case MessageLevel.Info:
                {
                    selectItem.addClass("alert-info");
                }
                break;
        }
        selectItem.text(msg);
    },
    ShowLoginMsg: function (level, msg)
    {
        this.ShowMsg("LB_Login_MSG", level, msg);
    },
    OnLoginClick: function ()
    {
        //Reset login form
        $("#TB_Login_ID").val("");
        $("#TB_Login_PW").val("");
        $("#CKB_RememberMe").prop("checked", false);
        $("#LB_Login_MSG").text("");
        $("#LB_Login_MSG").hide();
        $("#BT_Login").prop('disabled', false);
        //Toggle form
        $("#DLG_Login").modal("toggle");
    },
    OnCreateRoomClick: function ()
    {
        //Reset create room modal
        $("#TB_Create_Room_Name").val("");
        $("#TB_Create_Room_PW").val("");
        $("#TB_Create_Room_Description").val("");
        $("#LB_Create_Room_MSG").text("");
        $("#LB_Create_Room_MSG").hide();
        $("#BT_Create_Room_Create").prop('disabled', false);
        //Toggle form
        $("#DLG_Create_Room").modal("toggle");
    },
    OnCreateRoomCreateClick: function ()
    {
        //Disable button
        $("#BT_Create_Room_Create").prop('disabled', true);
        var roomName = $("#TB_Create_Room_Name").val();
        var roomPW = $("#TB_Create_Room_PW").val();
        var description = $("#TB_Create_Room_Description").val();
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_CREATE_ROOM;
        newMsg.Args.push(this.m_PlayerNum);
        newMsg.Args.push(roomName);
        newMsg.Args.push(roomPW);
        newMsg.Args.push(description);
        this.Send(newMsg);
    },
    ShowCreateRoomMsg: function (level, msg)
    {
        this.ShowMsg("LB_Create_Room_MSG", level, msg);
    },
    OnRoomHostStartClick: function ()
    {
        $("#LT_Room_Host_List").append('<li class="list-group-item disabled">Apples</li>');
    },
    OnRoomHostCancelClick: function ()
    {
        $("#LT_Room_Host_List").empty();
    },
    ShowWelcome: function (name)
    {
        $("#DLG_Welcome").modal("toggle");
        $("#LB_Welcome_MSG").text("Welcome back " + name);
    },
    ShowPlayer: function (playerName)
    {
        $("#LB_PLAYER").text("Player : " + playerName);
    },
    ShowRoom: function (roomName)
    {
        $("#LB_ROOM").text("Room : " + roomName);
    },
    ShowHostRoomName: function (roomName)
    {
        $("#LB_Room_Host_Name").text(roomName);
    },
    ShowHostRoomList: function (rawData)
    {
        $("#LT_Room_Host_List").empty();
        var args = rawData.split(/[,]/);
        for (var i = 0; i < args.length; i++)
        {
           $("#LT_Room_Host_List").append('<li class="list-group-item disabled">' + args[i] + '</li>');
        }
    },
    OnJoinRoomClick: function ()
    {
        //Get room member
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_GET_ENABLE_ROOM;
        newMsg.Args.push(this.m_PlayerNum);
        this.Send(newMsg);
        //Toggle form
        $("#DLG_Join_Room").modal("toggle");
    },
    ShowJoinRoomList: function (rawData)
    {
        $("#LT_Join_Room_List").empty();
        var roomList = eval(rawData);
        for (var i = 0; i < roomList.length; i++)
        {
            $("#LT_Join_Room_List").append('<a href="#" class="list-group-item" onclick="game.OnJoinRoomJoinClick(' + roomList[i].RoomNum + ',\'' + roomList[i].RoomName + '\')">' + roomList[i].RoomName + '</a>');
        }
    },
    OnJoinRoomJoinClick: function (roomNum, roomName)
    {
        //Join room
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_JOIN_ROOM;
        newMsg.Args.push(this.m_PlayerNum);
        newMsg.Args.push(roomNum);
        newMsg.Args.push(roomName);
        this.Send(newMsg);
    },
    Init: function () {
        this.CreateMap();
        this.m_Socket = new WrapWebSocket();
        this.m_Socket.m_Event.AddListener("onOpen", BindWrapper(this, this.OnOpen));
        this.m_Socket.m_Event.AddListener("onReceive", BindWrapper(this, this.OnReceive));
        this.m_Socket.m_Event.AddListener("onError", BindWrapper(this, this.OnError));
        this.m_Socket.m_Event.AddListener("onClose", BindWrapper(this, this.OnClose));
        this.Connect();
    }
};
