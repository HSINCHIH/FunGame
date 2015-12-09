/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function GameMonitor() {
    this.Init();
}
GameMonitor.prototype = {
    m_Socket: null,
    m_IsLogin: false,
    m_WatchRoom: -1,
    m_RoomState: null,
    m_PlayerList: null,
    m_ReplayStep: null,
    m_ReplayIndex: 0,
    m_ReplayHandle: null,
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
            case ServerAction.SVMO_LOGIN:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        this.ShowLoginMsg(MessageLevel.Danger, "Login fail, please try again!!");
                        return;
                    }
                    this.m_IsLogin = true;
                    this.CloseLoginDialog();
                }
                break;
            case ServerAction.SVMO_GET_ENABLE_ROOM:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log(StringFormat("{0} fail", "SVMO_GET_ENABLE_ROOM"));
                        return;
                    }
                    this.LoadWatchRoom(recvMsg);
                }
                break;
            case ServerAction.SVMO_WATCH_ROOM:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log(StringFormat("{0} fail", "SVMO_WATCH_ROOM"));
                        return;
                    }
                    this.m_WatchRoom = recvMsg.Args[1];
                    this.CloseWatchRoomDialog();
                    this.m_PlayerList = [];
                    var newMsg = new Message();
                    newMsg.Action = ServerAction.MOSV_LOAD_ROOM_STATE;
                    newMsg.Args.push(this.m_WatchRoom);
                    this.Send(newMsg);
                }
                break;
            case ServerAction.SVMO_LOAD_ROOM_STATE:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log(StringFormat("{0} fail", "SVMO_LOAD_ROOM_STATE"));
                        return;
                    }
                    this.LoadRoomState(recvMsg.Args);
                }
                break;
            case ServerAction.SVMO_SETP:
                {
                    this.ApplyStep(recvMsg.Args[0], recvMsg.Args[1]);
                }
                break;
            case ServerAction.SVMO_GAME_READY:
                {
                    var newMsg = new Message();
                    this.m_PlayerList = [];
                    newMsg.Action = ServerAction.MOSV_LOAD_ROOM_STATE;
                    newMsg.Args.push(this.m_WatchRoom);
                    this.Send(newMsg);
                }
                break;
            case ServerAction.SVMO_GAME_START:
                {
                    this.OpenCountDownDialog();
                }
                break;
            case ServerAction.SVMO_GAME_HISTORY:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log(StringFormat("{0} fail", "SVMO_GAME_HISTORY"));
                        return;
                    }
                    this.LoadGameHistory(recvMsg);
                }
                break;
            case ServerAction.SVMO_GET_REPLAY_ROOM:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log(StringFormat("{0} fail", "SVMO_GET_REPLAY_ROOM"));
                        return;
                    }
                    this.LoadReplayRoom(recvMsg);
                }
                break;
            case ServerAction.SVMO_GET_REPLAY_DATA:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log(StringFormat("{0} fail", "SVMO_GET_REPLAY_DATA"));
                        return;
                    }
                    var playerCount = parseInt(recvMsg.Args[1]);
                    var stateInfo = [];
                    stateInfo.push(recvMsg.Args[0]);
                    for (var i = 2; i < 2 + playerCount * 4; i++)
                    {
                        stateInfo.push(recvMsg.Args[i]);
                    }
                    for (var i = 2 + playerCount * 4; i < recvMsg.Args.length; i++)
                    {
                        this.m_ReplayStep.push(recvMsg.Args[i]);
                    }
                    this.LoadRoomState(stateInfo);
                    this.m_ReplayHandle = setInterval(BindWrapper(this, this.ReplayGame), 1000);
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
        newMsg.Action = ServerAction.MOSV_LOGIN;
        newMsg.Args.push(name);
        newMsg.Args.push(pw);
        this.Send(newMsg);
    },
    ShowLoginMsg: function (level, msg)
    {
        this.ShowMsg("LB_Login_MSG", level, msg);
    },
    OpenWatchRoomDialog: function ()
    {
        var newMsg = new Message();
        newMsg.Action = ServerAction.MOSV_GET_ENABLE_ROOM;
        this.Send(newMsg);
        //Toggle form
        $("#DLG_Watch_Room").modal("toggle");
    },
    CloseWatchRoomDialog: function ()
    {
        //Toggle form
        $("#DLG_Watch_Room").modal("toggle");
    },
    LoadWatchRoom: function (recvMsg)
    {
        $('#GD_Room_List tbody tr').remove();
        for (var i = 1; i < recvMsg.Args.length; i += 3)
        {
            $('#GD_Room_List tbody').append('<tr><td>' + recvMsg.Args[i + 1] + '</td><td>' + recvMsg.Args[i + 2] + '</td><td><input type="button" class="btn btn-default" value="Watch" onclick="monitor.WatchRoom(' + recvMsg.Args[i + 0] + ',\'' + recvMsg.Args[i + 1] + '\')"/></td></tr>');
        }
    },
    WatchRoom: function (roomNum, roomName)
    {
        this.m_RoomState = [];
        $("#DIV_Game").empty();
        var newMsg = new Message();
        newMsg.Action = ServerAction.MOSV_WATCH_ROOM;
        newMsg.Args.push(roomNum);
        this.Send(newMsg);
    },
    LoadRoomState: function (args)
    {
        for (var i = 1; i < args.length; i += 4)
        {
            var playerNum = args[i + 0];
            var playerName = args[i + 1];
            var step = args[i + 2];
            var state = args[i + 3];
            var cardState = eval(state);
            $("#DIV_Game").append('<div id="' + StringFormat("DIV_{0}", playerNum) + '" class="col-md-6 col-xs-6"></div>');
            $("#DIV_" + playerNum).css({"border": "1px solid #F0F0F0"});
            $("#DIV_" + playerNum).append('<div class="col-md-12 col-xs-12">Player : ' + playerName + '</div>');
            for (var j = 0; j < cardState.length; j++)
            {
                var item = cardState[j];
                var test = '<div class="col-md-3 col-xs-3"><div class="flip"><div class="card" id="' + StringFormat("card_{0}_{1}", playerNum, item.Card) + '" on onclick="monitor.OnCardClick(\'' + StringFormat("{0}_{1}", playerNum, item.Card) + '\')"><div class="face front"><img src="images/00.png" alt="" class="img-responsive center-block"/></div><div class="face back"><img src="images/' + StringFormat("{0}.png", item.Img) + '" alt="" id="' + StringFormat("image_{0}_{1}", playerNum, item.Card) + '" class="img-responsive center-block"/></div></div></div></div>';
                $("#DIV_" + playerNum).append(test);
            }
            this.m_RoomState[playerNum] = {ClickCards: [], State: cardState};
            this.ApplyState(playerNum, cardState);
            this.ApplyStep(playerNum, step);
        }
    },
    ApplyState: function (playerNum, cardState)
    {
        this.m_PlayerList.push(playerNum);
        for (var i = 0; i < cardState.length; i++)
        {
            var item = cardState[i];
            var card = $(StringFormat("#card_{0}_{1}", playerNum, item.Card));
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
                this.m_RoomState[playerNum].ClickCards.push(item.Card);
            }
        }
    },
    ApplyStep: function (playerNum, step)
    {
        if (step === "empty")
            return;
        var selectCard = $(StringFormat("#card_{0}_{1}", playerNum, step));
        this.m_RoomState[playerNum].ClickCards.push(step);
        selectCard.data("Open", 1);
        selectCard.data("Click", 1);
        //flip card to front
        selectCard.closest('.card').css('-webkit-transform', 'rotatey(-180deg)');
        selectCard.closest('.card').css('transform', 'rotatey(-180deg)');

        var self = this;
        if (this.m_RoomState[playerNum].ClickCards.length === 2)
        {
            setTimeout(function () {
                var selectCard1 = $(StringFormat("#card_{0}_{1}", playerNum, self.m_RoomState[playerNum].ClickCards[0]));
                var selectCard2 = $(StringFormat("#card_{0}_{1}", playerNum, self.m_RoomState[playerNum].ClickCards[1]));
                if (selectCard1.data("Content") === selectCard2.data("Content"))
                {
                    selectCard1.fadeTo(400, 0.1).delay(300).fadeTo(400, 1);
                    selectCard2.fadeTo(400, 0.1).delay(300).fadeTo(400, 1, function () {
                        selectCard1.data("Click", 0);
                        selectCard2.data("Click", 0);
                        //reset array
                        self.m_RoomState[playerNum].ClickCards = [];
                        if (self.IsWinner(playerNum))
                        {
                            self.SetWinner(parseInt(playerNum));
                        }
                    });
                }
                else
                {
                    for (var i = 0; i < self.m_RoomState[playerNum].ClickCards.length; i++)
                    {
                        var selectCard = $(StringFormat("#card_{0}_{1}", playerNum, self.m_RoomState[playerNum].ClickCards[i]));
                        //flip cards to back
                        selectCard.closest('.card').css('-webkit-transform', 'rotatey(0deg)');
                        selectCard.closest('.card').css('transform', 'rotatey(0deg)');
                        //reset status to 0
                        selectCard.data("Click", 0);
                        selectCard.data("Open", 0);
                    }
                    //reset array
                    self.m_RoomState[playerNum].ClickCards = [];
                    if (self.IsWinner(playerNum))
                    {
                        self.SetWinner(parseInt(playerNum));
                    }
                }
            }, 500);
        }
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
                    for (var i = 0; i < self.m_PlayerList.length; i++)
                    {
                        for (var j = 0; j <= 16; j++)
                        {
                            //flip card to front
                            $(StringFormat("#card_{0}_{1}", self.m_PlayerList[i], DigitFormat(j, 2))).closest('.card').css('-webkit-transform', 'rotatey(-180deg)');
                            $(StringFormat("#card_{0}_{1}", self.m_PlayerList[i], DigitFormat(j, 2))).closest('.card').css('transform', 'rotatey(-180deg)');
                        }
                    }
                    setTimeout(function () {
                        for (var i = 0; i < self.m_PlayerList.length; i++)
                        {
                            for (var j = 0; j <= 16; j++)
                            {
                                //flip card to front
                                $(StringFormat("#card_{0}_{1}", self.m_PlayerList[i], DigitFormat(j, 2))).closest('.card').css('-webkit-transform', 'rotatey(0deg)');
                                $(StringFormat("#card_{0}_{1}", self.m_PlayerList[i], DigitFormat(j, 2))).closest('.card').css('transform', 'rotatey(0deg)');
                            }
                        }
                    }, 1000);
                });
            });
        });
    },
    OpenGameHistoryDialog: function ()
    {
        var newMsg = new Message();
        newMsg.Action = ServerAction.MOSV_GAME_HISTORY;
        this.Send(newMsg);
        //Toggle form
        $("#DLG_Game_History").modal("toggle");
    }, LoadGameHistory: function (recvMsg)
    {
        $('#GD_Game_History tbody tr').remove();
        for (var i = 1; i < recvMsg.Args.length; i += 4)
        {
            $('#GD_Game_History tbody').append('<tr><td>' + recvMsg.Args[i + 0] + '</td><td>' + recvMsg.Args[i + 1] + '</td><td>' + recvMsg.Args[i + 2] + '</td><td>' + recvMsg.Args[i + 3] + '</td></tr>');
        }
    },
    OpenReplayRoomDialog: function ()
    {
        var newMsg = new Message();
        newMsg.Action = ServerAction.MOSV_GET_REPLAY_ROOM;
        this.Send(newMsg);
        //Toggle form
        $("#DLG_Replay_Room").modal("toggle");
    },
    CloseReplayRoomDialog: function ()
    {
        //Toggle form
        $("#DLG_Replay_Room").modal("toggle");
    },
    LoadReplayRoom: function (recvMsg)
    {
        $('#GD_Replay_Room tbody tr').remove();
        for (var i = 1; i < recvMsg.Args.length; i += 3)
        {
            $('#GD_Replay_Room tbody').append('<tr><td>' + recvMsg.Args[i + 1] + '</td><td>' + recvMsg.Args[i + 2] + '</td><td><input type="button" class="btn btn-default" value="Replay" onclick="monitor.GetReplayData(' + recvMsg.Args[i + 0] + ',\'' + recvMsg.Args[i + 1] + '\')"/></td></tr>');
        }
    },
    GetReplayData: function (roomNum, roomName)
    {
        this.m_RoomState = [];
        this.m_PlayerList = [];
        this.m_ReplayStep = [];
        this.m_ReplayIndex = 0;
        $("#DIV_Game").empty();
        this.CloseReplayRoomDialog();
        var newMsg = new Message();
        newMsg.Action = ServerAction.MOSV_GET_REPLAY_DATA;
        newMsg.Args.push(roomNum);
        this.Send(newMsg);
    },
    ReplayGame: function ()
    {
        console.log("ReplayGame");
        if (this.m_ReplayIndex >= (this.m_ReplayStep.length / 2))
        {
            clearInterval(this.m_ReplayHandle);
            return;
        }
        var playerNum = parseInt(this.m_ReplayStep[this.m_ReplayIndex * 2 + 0]);
        if (this.m_RoomState[playerNum].ClickCards.length === 2)
        {
            return;
        }
        this.ApplyStep(this.m_ReplayStep[this.m_ReplayIndex * 2 + 0], this.m_ReplayStep[this.m_ReplayIndex * 2 + 1]);
        this.m_ReplayIndex++;
        console.log("this.m_ReplayIndex : " + this.m_ReplayIndex);
        if (this.IsWinner(playerNum))
        {
            this.SetWinner(playerNum);
        }
    },
    IsWinner: function (playerNum)
    {
        var openCount = 0;
        for (var i = 0; i < 16; i++)
        {
            var selectCard = $(StringFormat("#card_{0}_{1}", playerNum, DigitFormat(i, 2)));
            openCount += parseInt(selectCard.data("Open"));
        }
        return openCount === 16;
    },
    SetWinner: function (playerNum)
    {
        $(".flip").each(function () {
            $(this).hide();
        });
        for (var i = 0; i < this.m_PlayerList.length; i++)
        {
            var tempNum = parseInt(this.m_PlayerList[i]);
            if (tempNum === playerNum)
            {
                $("#DIV_" + tempNum).append('<img src="images/you_win.jpg" alt="" class="img-responsive center-block"/>');
            }
            else
            {
                $("#DIV_" + tempNum).append('<img src="images/you_lose.jpg" alt="" class="img-responsive center-block"/>');
            }
        }
    },
    Init: function ()
    {
        this.m_Socket = new WrapWebSocket();
        this.m_Socket.m_Event.AddListener("onOpen", BindWrapper(this, this.OnOpen));
        this.m_Socket.m_Event.AddListener("onReceive", BindWrapper(this, this.OnReceive));
        this.m_Socket.m_Event.AddListener("onError", BindWrapper(this, this.OnError));
        this.m_Socket.m_Event.AddListener("onClose", BindWrapper(this, this.OnClose));
        this.Connect();
    }
};


