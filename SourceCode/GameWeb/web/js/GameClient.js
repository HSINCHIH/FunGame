/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function GameClient() {
}
GameClient.prototype = {
    m_Socket: null,
    m_IsConnect: false,
    m_IsLogin: false,
    m_PlayerName: "",
    m_PlayerNum: "",
    m_RoomName: "",
    m_RoomNum: "",
    m_GameSettings: null,
    m_ClickInterval: 300,
    m_PrevClickTick: 0,
    m_ClickCards: null,
    m_GameOver: 0,
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
                    this.CloseLoginDialog();
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
                    this.CloseCreateRoomDialog();
                    this.OpenRoomHostDialog();
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
                    this.CloseJoinRoomDialog();
                }
                break;
            case ServerAction.SVPL_INITIAL_GAME:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_INITIAL_GAME fail");
                        return;
                    }
                }
                break;
            case ServerAction.SVPL_SETUP_GAME:
                {
                    var newMsg = new Message();
                    newMsg.Action = ServerAction.PLSV_GET_IMAGE;
                    newMsg.Args.push(this.m_PlayerNum);
                    newMsg.Args.push(this.m_RoomNum);
                    this.Send(newMsg);
                }
                break;
            case ServerAction.SVPL_GET_IMAGE:
                {
                    this.SVPL_GET_IMAGE_recv(recvMsg);
                }
                break;
            case ServerAction.SVPL_GAME_READY:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_GAME_READY fail");
                        return;
                    }
                }
                break;
            case ServerAction.SVPL_GAME_START:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_GAME_START fail");
                        return;
                    }
                }
                break;
            default:
                {
                }
                break;
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
    CreateDefaultCards: function ()
    {
        var jsonString = '({"10":{"Img":"07","Open":0,"Click":0},"11":{"Img":"06","Open":0,"Click":0},"12":{"Img":"04","Open":0,"Click":0},"13":{"Img":"03","Open":0,"Click":0},"14":{"Img":"07","Open":0,"Click":0},"15":{"Img":"03","Open":0,"Click":0},"16":{"Img":"01","Open":0,"Click":0},"01":{"Img":"08","Open":0,"Click":0},"02":{"Img":"06","Open":0,"Click":0},"03":{"Img":"01","Open":0,"Click":0},"04":{"Img":"04","Open":0,"Click":0},"05":{"Img":"05","Open":0,"Click":0},"06":{"Img":"02","Open":0,"Click":0},"07":{"Img":"08","Open":0,"Click":0},"08":{"Img":"05","Open":0,"Click":0},"09":{"Img":"02","Open":0,"Click":0}})';
        this.SetupCards(jsonString);
    },
    SetupCards: function (jsonString)
    {
        this.m_GameSettings = eval(jsonString);
        for (key in this.m_GameSettings)
        {
            var setting = this.m_GameSettings[key];
            $("#image" + key).attr("src", "images/" + setting.Img + ".png");
        }
    },
    SVPL_GET_IMAGE_recv: function (recvMsg)
    {
        if (recvMsg.Args[0] === "0")
        {
            console.log("SVPL_GET_IMAGE fail");
            return;
        }
        this.SetupCards(recvMsg.Args[1]);
        this.CloseRoomHostDialog();
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_GAME_READY;
        newMsg.Args.push(this.m_PlayerNum);
        newMsg.Args.push(this.m_RoomNum);
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
    //Sign up
    OpenSignUpDialog: function ()
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
    CloseSignUpDialog: function ()
    {
        $("#DLG_SignUp").modal("toggle");
    },
    SingUp: function ()
    {
        //Disable button
        $("#BT_SignUp").prop('disabled', true);
        if (!this.m_IsConnect)
        {
            console.log("Socket no connect");
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
    //Login
    OpenLoginDialog: function ()
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
    CloseLoginDialog: function ()
    {
        $("#DLG_Login").modal("toggle");
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
    ShowLoginMsg: function (level, msg)
    {
        this.ShowMsg("LB_Login_MSG", level, msg);
    },
    //Create room
    OpenCreateRoomDialog: function ()
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
    CloseCreateRoomDialog: function ()
    {
        $("#DLG_Create_Room").modal("toggle");
    },
    CreateRoom: function ()
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
    OpenRoomHostDialog: function ()
    {
        $("#DLG_Room_Host").modal("toggle");
    },
    CloseRoomHostDialog: function ()
    {
        $("#DLG_Room_Host").modal("toggle");
    },
    RoomHostStart: function ()
    {
        //$("#LT_Room_Host_List").append('<li class="list-group-item disabled">Apples</li>');
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_INITIAL_GAME;
        newMsg.Args.push(this.m_PlayerNum);
        newMsg.Args.push(this.m_RoomNum);
        this.Send(newMsg);
    },
    RoomHostCancel: function ()
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
    //join room
    OpenJoinRoomDialog: function ()
    {
        //Get room member
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_GET_ENABLE_ROOM;
        newMsg.Args.push(this.m_PlayerNum);
        this.Send(newMsg);
        //Toggle form
        $("#DLG_Join_Room").modal("toggle");
    },
    CloseJoinRoomDialog: function ()
    {
        $("#DLG_Join_Room").modal("toggle");
    },
    ShowJoinRoomList: function (rawData)
    {
        $("#LT_Join_Room_List").empty();
        var roomList = eval(rawData);
        for (var i = 0; i < roomList.length; i++)
        {
            $("#LT_Join_Room_List").append('<a href="#" class="list-group-item" onclick="client.OnJoinRoomJoinClick(' + roomList[i].RoomNum + ',\'' + roomList[i].RoomName + '\')">' + roomList[i].RoomName + '</a>');
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
    OnCardClick: function (id)
    {
        console.log("OnCardClick");
        //Check interval
        var curTick = (new Date()).getTime();
        //console.log(curTick - this.m_PrevClickTick);
        if (curTick - this.m_PrevClickTick < this.m_ClickInterval)
        {
            console.log("return");
            return;
        }
        console.log("run");
        if (this.m_ClickCards.length === 2)
        {
            console.log("already open 2 cards");
            return;
        }
        //Check card open or not
        if (this.m_GameSettings[id].Open === 1)
        {
            console.log("card already open");
            return;
        }
        //Check card click or not
        if (this.m_GameSettings[id].Click === 1)
        {
            console.log("card already click");
            return;
        }
        this.m_ClickCards[this.m_ClickCards.length] = id;
        this.m_GameSettings[id].Click = 1;
        this.m_GameSettings[id].Open = 1;
        //flip card to front
        $("#card" + id).closest('.card').css('-webkit-transform', 'rotatey(-180deg)');
        $("#card" + id).closest('.card').css('transform', 'rotatey(-180deg)');
        if (this.m_ClickCards.length === 2)
        {
            setTimeout(BindWrapper(this, this.CheckCard), 500);
        }
        this.m_PrevClickTick = curTick;
    },
    CheckCard: function ()
    {
        var card1 = this.m_GameSettings[this.m_ClickCards[0]].Img;
        var card2 = this.m_GameSettings[this.m_ClickCards[1]].Img;
        if (card1 === card2)
        {
            for (var i = 0; i < this.m_ClickCards.length; i++)
            {
                $("#card" + this.m_ClickCards[i]).fadeTo(400, 0.1).delay(300).fadeTo(400, 1, BindWrapper(this, function () {
                    if (this.m_GameOver === 1)
                    {
                        return;
                    }
                    var openCard = 0;
                    for (key in this.m_GameSettings)
                    {
                        var setting = this.m_GameSettings[key];
                        console.log(key + " : " + setting.Open);
                        openCard += setting.Open;
                    }
                    if (openCard >= 16)
                    {
                        alert("You win the game !!!");
                        this.m_GameOver = 1;
                    }
                }));
            }
        }
        else
        {
            this.CloseCard();
        }
        //reset array
        this.m_ClickCards = [];
    },
    CloseCard: function ()
    {
        for (var i = 0; i < this.m_ClickCards.length; i++)
        {
            //flip cards to back
            $("#card" + this.m_ClickCards[i]).closest('.card').css('-webkit-transform', 'rotatey(0deg)');
            $("#card" + this.m_ClickCards[i]).closest('.card').css('transform', 'rotatey(0deg)');
            //reset status to 0
            this.m_GameSettings[this.m_ClickCards[i]].Click = 0;
            this.m_GameSettings[this.m_ClickCards[i]].Open = 0;
        }
    },
    Init: function () {
        this.m_ClickCards = [];
        this.CreateDefaultCards();
        this.m_Socket = new WrapWebSocket();
        this.m_Socket.m_Event.AddListener("onOpen", BindWrapper(this, this.OnOpen));
        this.m_Socket.m_Event.AddListener("onReceive", BindWrapper(this, this.OnReceive));
        this.m_Socket.m_Event.AddListener("onError", BindWrapper(this, this.OnError));
        this.m_Socket.m_Event.AddListener("onClose", BindWrapper(this, this.OnClose));
        this.Connect();
    }
};
