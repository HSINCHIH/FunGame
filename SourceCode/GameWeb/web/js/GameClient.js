/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function GameClient() {
    this.Init();
}
GameClient.prototype = {
    m_Socket: null,
    m_IsConnect: false,
    m_IsLogin: false,
    m_PlayerName: "",
    m_PlayerNum: "",
    m_RoomName: "",
    m_RoomNum: "",
    m_GameLevel: -1,
    m_ClickInterval: 300,
    m_PrevClickTick: 0,
    m_ClickCards: null,
    m_CanClickCard: false,
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
                    //Resume Game
                    var newMsg = new Message();
                    newMsg.Action = ServerAction.PLSV_RESUME_GAME;
                    newMsg.Args.push(this.m_PlayerNum);
                    this.Send(newMsg);
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
                        this.ShowJoinRoomMsg(MessageLevel.Danger, "Join room fail, please try again");
                        return;
                    }
                    this.m_RoomNum = recvMsg.Args[1];
                    this.m_RoomName = recvMsg.Args[2];
                    this.ShowRoom(this.m_RoomName);
                    this.ShowJoinRoomMsg(MessageLevel.Danger, "Join room success");
                    this.CloseJoinRoomDialog();
                }
                break;
            case ServerAction.SVPL_LEAVE_ROOM:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_LEAVE_ROOM fail");
                        return;
                    }
                    this.m_RoomNum = "";
                    this.m_RoomName = "";
                    this.ShowRoom("");
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
                    newMsg.Action = ServerAction.PLSV_GET_CARD_STATE;
                    newMsg.Args.push(this.m_PlayerNum);
                    newMsg.Args.push(this.m_RoomNum);
                    this.Send(newMsg);
                }
                break;
            case ServerAction.SVPL_GET_CARD_STATE:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_GET_CARD_STATE fail");
                        return;
                    }
                    this.CleanState();
                    this.LoadRoomState(recvMsg.Args[1]);
                    var newMsg = new Message();
                    newMsg.Action = ServerAction.PLSV_GAME_READY;
                    newMsg.Args.push(this.m_PlayerNum);
                    newMsg.Args.push(this.m_RoomNum);
                    this.Send(newMsg);
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
                    this.OpenCountDownDialog();
                }
                break;
            case ServerAction.SVPL_OPERATOR:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_OPERATOR fail");
                        return;
                    }
                }
                break;
            case ServerAction.SVPL_WINNER:
                {
                    if (recvMsg.Args[0] !== this.m_RoomNum)
                    {
                        console.log(recvMsg.GetString());
                        return;
                    }
                    this.OpenGameResultDialog(recvMsg.Args[1] === this.m_PlayerNum);
                }
                break;
            case ServerAction.SVPL_RESUME_GAME:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("SVPL_RESUME_GAME fail");
                        return;
                    }
                    this.CleanState();
                    this.m_RoomNum = recvMsg.Args[1];
                    this.m_RoomName = recvMsg.Args[2];
                    this.ShowRoom(this.m_RoomName);
                    this.LoadRoomState(recvMsg.Args[4]);
                    this.ApplyStep(recvMsg.Args[3]);
                    this.m_CanClickCard = true;
                }
                break;
            default:
                {
                    console.log("unknow message : " + recvMsg.GetString());
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
    SelectGameLevel: function (level)
    {
        this.m_GameLevel = parseInt(level);
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
        newMsg.Args.push(this.m_GameLevel);
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
        this.CloseRoomHostDialog();
        //$("#LT_Room_Host_List").append('<li class="list-group-item disabled">Apples</li>');
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_INITIAL_GAME;
        newMsg.Args.push(this.m_PlayerNum);
        newMsg.Args.push(this.m_RoomNum);
        this.Send(newMsg);
    },
    RoomHostCancel: function ()
    {
        this.CloseRoomHostDialog();
        $("#LT_Room_Host_List").empty();
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
        var roomList = eval(rawData);
        $('#GRID_ROOM_LIST tbody tr').remove();
        for (var i = 0; i < roomList.length; i++)
        {
            $('#GRID_ROOM_LIST tbody').append('<tr><td>' + roomList[i].RoomName + '</td><td><input type="password" id="TB_ROOM_' + roomList[i].RoomNum + '_PW" class="btn btn-default"/></td><td><input type="button" class="btn btn-default" value="Join" onclick="client.JoinRoom(' + roomList[i].RoomNum + ',\'' + roomList[i].RoomName + '\')"/></td></tr>');
        }
    },
    JoinRoom: function (roomNum, roomName)
    {
        var roomPW = $("#TB_ROOM_" + roomNum + "_PW").val();
        //Join room
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_JOIN_ROOM;
        newMsg.Args.push(this.m_PlayerNum);
        newMsg.Args.push(roomNum);
        newMsg.Args.push(roomName);
        newMsg.Args.push(roomPW);
        this.Send(newMsg);
    },
    ShowJoinRoomMsg: function (level, msg)
    {
        this.ShowMsg("LB_JOIN_ROOM_MSG", level, msg);
    },
    OpenCountDownDialog: function ()
    {
        var self = this;
        $("#DLG_Count_Down").modal("toggle");
        $("#IMG_CountDown").attr("src", "images/num03.jpg");
        $("#IMG_CountDown").css({opacity: 1});
        $("#IMG_CountDown").fadeTo(1000, 0.01, function () {
            $("#IMG_CountDown").attr("src", "images/num02.jpg");
            $("#IMG_CountDown").css({opacity: 1});
            $("#IMG_CountDown").fadeTo(1000, 0.01, function () {
                $("#IMG_CountDown").attr("src", "images/num01.jpg");
                $("#IMG_CountDown").css({opacity: 1});
                $("#IMG_CountDown").fadeTo(1000, 0.01, function () {
                    $("#DLG_Count_Down").modal("toggle");
                    for (var i = 0; i <= 16; i++)
                    {
                        //flip card to front
                        $(StringFormat("#card_{0}", DigitFormat(i, 2))).closest('.card').css('-webkit-transform', 'rotatey(-180deg)');
                        $(StringFormat("#card_{0}", DigitFormat(i, 2))).closest('.card').css('transform', 'rotatey(-180deg)');
                    }
                    setTimeout(function () {
                        for (var i = 0; i <= 16; i++)
                        {
                            //flip card to front
                            $(StringFormat("#card_{0}", DigitFormat(i, 2))).closest('.card').css('-webkit-transform', 'rotatey(0deg)');
                            $(StringFormat("#card_{0}", DigitFormat(i, 2))).closest('.card').css('transform', 'rotatey(0deg)');
                        }
                        self.m_CanClickCard = true;
                    }, 1000);
                });
            });
        });
    },
    LeaveRoom: function ()
    {
        //leave room
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_LEAVE_ROOM;
        newMsg.Args.push(this.m_PlayerNum);
        newMsg.Args.push(this.m_RoomNum);
        newMsg.Args.push(this.m_RoomName);
        this.Send(newMsg);
    },
    CreateDefaultCards: function ()
    {
        var jsonString = '[{"Card":"00","Img":"1500_0","Open":0,"Click":0,"Content":"1500"},{"Card":"01","Img":"8400_1","Open":0,"Click":0,"Content":"8400"},{"Card":"02","Img":"1500_1","Open":0,"Click":0,"Content":"1500"},{"Card":"03","Img":"9600_0","Open":0,"Click":0,"Content":"9600"},{"Card":"04","Img":"CP50_1","Open":0,"Click":0,"Content":"CP50"},{"Card":"05","Img":"9700_1","Open":0,"Click":0,"Content":"9700"},{"Card":"06","Img":"9600_1","Open":0,"Click":0,"Content":"9600"},{"Card":"07","Img":"1704_1","Open":0,"Click":0,"Content":"1704"},{"Card":"08","Img":"9700_0","Open":0,"Click":0,"Content":"9700"},{"Card":"09","Img":"8300_1","Open":0,"Click":0,"Content":"8300"},{"Card":"10","Img":"1560_0","Open":0,"Click":0,"Content":"1560"},{"Card":"11","Img":"1704_0","Open":0,"Click":0,"Content":"1704"},{"Card":"12","Img":"1560_1","Open":0,"Click":0,"Content":"1560"},{"Card":"13","Img":"8400_0","Open":0,"Click":0,"Content":"8400"},{"Card":"14","Img":"8300_0","Open":0,"Click":0,"Content":"8300"},{"Card":"15","Img":"CP50_0","Open":0,"Click":0,"Content":"CP50"}]';
        //this.SetupCards(jsonString);
        this.LoadRoomState(jsonString);
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
    LoadRoomState: function (state)
    {
        var playerNum = this.m_PlayerNum;
        var cardState = eval(state);
        $("#DIV_Game").append('<div id="' + StringFormat("DIV_{0}", playerNum) + '" class="col-md-12 col-xs-12"></div>');
        $("#DIV_" + playerNum).css({"border": "1px solid #F0F0F0"});
        for (var j = 0; j < cardState.length; j++)
        {
            var item = cardState[j];
            var test = '<div class="col-md-3 col-xs-3"><div class="flip"><div class="card" id="' + StringFormat("card_{0}", item.Card) + '" on onclick="client.ApplyStep(\'' + item.Card + '\')"><div class="face front"><img src="images/00.png" alt="" class="img-responsive center-block"/></div><div class="face back"><img src="images/' + StringFormat("{0}.png", item.Img) + '" alt="" id="' + StringFormat("image_{0}", item.Card) + '" class="img-responsive center-block"/></div></div></div></div>';
            $("#DIV_" + playerNum).append(test);
        }
        this.ApplyState(cardState);
    },
    ApplyState: function (cardState)
    {
        for (var i = 0; i < cardState.length; i++)
        {
            var item = cardState[i];
            var card = $(StringFormat("#card_{0}", item.Card));
            card.data("Card", item.Card);
            card.data("Open", item.Open);
            card.data("Click", item.Click);
            card.data("Img", item.Img);
            card.data("Content", item.Content);
            if (item.Open)
            {
                //flip card to front
                card.closest('.card').css('-webkit-transform', 'rotatey(-180deg)');
                card.closest('.card').css('transform', 'rotatey(-180deg)');
            }
            if (item.Click)
            {
                this.m_ClickCards[this.m_ClickCards.length] = item.Card;
            }
        }
    },
    ApplyStep: function (step)
    {
        console.log("ApplyStep");
        if (!this.m_CanClickCard)
        {
            return;
        }
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
        var selectCard = $(StringFormat("#card_{0}", step));
        //Check card open or not
        if (selectCard.data("Open") === 1)
        {
            console.log("card already open");
            return;
        }
        //Check card click or not
        if (selectCard.data("Click") === 1)
        {
            console.log("card already click");
            return;
        }
        //Send setp and state to server
        this.NotifyServer(step);
        this.m_ClickCards[this.m_ClickCards.length] = step;
        selectCard.data("Click", 1);
        selectCard.data("Open", 1);
        //flip card to front
        selectCard.closest('.card').css('-webkit-transform', 'rotatey(-180deg)');
        selectCard.closest('.card').css('transform', 'rotatey(-180deg)');
        var self = this;
        if (this.m_ClickCards.length === 2)
        {
            this.m_CanClickCard = false;
            setTimeout(function () {
                var selectCard1 = $(StringFormat("#card_{0}", self.m_ClickCards[0]));
                var selectCard2 = $(StringFormat("#card_{0}", self.m_ClickCards[1]));
                if (selectCard1.data("Content") === selectCard2.data("Content"))
                {
                    selectCard1.fadeTo(400, 0.1).delay(300).fadeTo(400, 1);
                    selectCard2.fadeTo(400, 0.1).delay(300).fadeTo(400, 1, function () {
                        self.m_CanClickCard = true;
                        selectCard1.data("Click", 0);
                        selectCard2.data("Click", 0);
                    });
                }
                else
                {
                    for (var i = 0; i < self.m_ClickCards.length; i++)
                    {
                        //flip cards to back
                        var selectCard = $(StringFormat("#card_{0}", self.m_ClickCards[i]));
                        selectCard.closest('.card').css('-webkit-transform', 'rotatey(0deg)');
                        selectCard.closest('.card').css('transform', 'rotatey(0deg)');
                        //reset status to 0
                        selectCard.data("Click", 0);
                        selectCard.data("Open", 0);
                    }
                    self.m_CanClickCard = true;
                }
                //reset array
                self.m_ClickCards = [];
            }, 500);
        }
        this.m_PrevClickTick = curTick;
    },
    CleanState: function ()
    {
        $("#DIV_Game").empty();
        self.m_CanClickCard = false;
        this.m_ClickCards = [];
    },
    GetState: function ()
    {
        var cardState = [];
        $(".card").each(function () {
            var item = $(this);
            var id = item.attr('id').substring(4, 7);
            var info = {"Card": item.data("Card"), "Img": item.data("Img"), "Open": item.data("Open"), "Click": item.data("Click"), "Content": item.data("Content")};
            cardState.push(info);
        });
        return JSON.stringify(cardState);
    },
    NotifyServer: function (cardID)
    {
        var newMsg = new Message();
        newMsg.Action = ServerAction.PLSV_OPERATOR;
        newMsg.Args.push(this.m_PlayerNum);
        newMsg.Args.push(this.m_RoomNum);
        newMsg.Args.push(cardID);
        newMsg.Args.push(this.GetState());
        this.Send(newMsg);
    },
    OpenGameResultDialog: function (isWin)
    {
        var src = isWin ? "images/you_win.jpg" : "images/you_lose.jpg";
        $("#IMG_Game_Result").attr("src", src);
        $("#IMG_Game_Result").css({height: "1%", width: "1%"});
        $("#DLG_Game_Result").modal("toggle");
        $("#IMG_Game_Result").animate({height: "50%", width: "50%"}, 500);
    },
    Init: function () {
        this.m_ClickCards = [];
        //this.CreateDefaultCards();
        this.m_Socket = new WrapWebSocket();
        this.m_Socket.m_Event.AddListener("onOpen", BindWrapper(this, this.OnOpen));
        this.m_Socket.m_Event.AddListener("onReceive", BindWrapper(this, this.OnReceive));
        this.m_Socket.m_Event.AddListener("onError", BindWrapper(this, this.OnError));
        this.m_Socket.m_Event.AddListener("onClose", BindWrapper(this, this.OnClose));
        this.Connect();
    }
};
