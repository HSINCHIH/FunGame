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
                    this.ShowWatchRoomList(recvMsg.Args[1]);
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
    WatchRoom: function (roomNum, roomName)
    {
        var newMsg = new Message();
        newMsg.Action = ServerAction.MOSV_WATCH_ROOM;
        newMsg.Args.push(roomNum);
        this.Send(newMsg);
    },
    ShowWatchRoomList: function (rawData)
    {
        var roomList = eval(rawData);
        $('#GD_Room_List tbody tr').remove();
        for (var i = 0; i < roomList.length; i++)
        {
            $('#GD_Room_List tbody').append('<tr><td>' + roomList[i].RoomName + '</td><td><input type="button" class="btn btn-default" value="Watch" onclick="monitor.WatchRoom(' + roomList[i].RoomNum + ',\'' + roomList[i].RoomName + '\')"/></td></tr>');
        }
    },
    CreateCard: function ()
    {
        var twoSide = ["Left", "Right"];
        for (var i = 0; i < twoSide.length; i++)
        {
            $("#DIV_" + twoSide[i]).css({"border": "1px solid #F0F0F0"});
            $("#DIV_" + twoSide[i]).append('<div class="col-md-12 col-xs-12">Player : </div>');
            for (var j = 0; j < 16; j++)
            {
                var test = '<div class="col-md-3 col-xs-3"><div class="flip"><div class="card" id="' + StringFormat("card_{0}_{1}", twoSide[i].substring(0, 2), DigitFormat(j, 2)) + '" on onclick="monitor.OnCardClick(\'' + StringFormat("{0}_{1}", twoSide[i].substring(0, 2), DigitFormat(j, 2)) + '\')"><div class="face front"><img src="images/00.png" alt="" class="img-responsive center-block"/></div><div class="face back"><img src="images/00.png" alt="" id="' + StringFormat("image_{0}_{1}", twoSide[i].substring(0, 2), DigitFormat(j, 2)) + '" class="img-responsive center-block"/></div></div></div></div>';
                $("#DIV_" + twoSide[i]).append(test);
            }
        }
    },
    Init: function ()
    {
        this.CreateCard();
        this.m_Socket = new WrapWebSocket();
        this.m_Socket.m_Event.AddListener("onOpen", BindWrapper(this, this.OnOpen));
        this.m_Socket.m_Event.AddListener("onReceive", BindWrapper(this, this.OnReceive));
        this.m_Socket.m_Event.AddListener("onError", BindWrapper(this, this.OnError));
        this.m_Socket.m_Event.AddListener("onClose", BindWrapper(this, this.OnClose));
        this.Connect();
    }
};


