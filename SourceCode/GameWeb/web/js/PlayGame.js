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
                    $("#LB_SignUp_MSG").text("signup fail, please try another user name and password");
                }
                $("#LB_SignUp_MSG").text("signup success, now you can login the game");
            }
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
            $('#gameDiv').append("<div class=\"col-md-3 col-xs-3\"><img src=\"images/" + imgList[i] + ".png\" alt=\"\" class=\"img-responsive\"/></div>");
        }
    },
    SingUp: function ()
    {
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
    Login: function ()
    {
        $("#BT_Login").prop('disabled', true);
        if (!this.m_IsConnect)
        {
            console.log("Socket no connect ");
            return;
        }
        var name = $("#TB_Login_ID").val();
        var pw = $("#TB_Login_PW").val();
        var rememberMe = $("#CKB_RememberMe").prop("checked");
    },
    OnSignUpClick: function ()
    {
        //Reset sign form
        $("#TB_SignUp_ID").val("");
        $("#TB_SignUp_PW").val("");
        $("#BT_SignUp").prop('disabled', false);
        //Toggle form
        $("#DLG_SignUp").modal("toggle");
    },
    OnLoginClick: function ()
    {
        //Reset login form
        $("#TB_Login_ID").val("");
        $("#TB_Login_PW").val("");
        $("#BT_Login").prop('disabled', false);
        $("#CKB_RememberMe").prop("checked", false);
        //Toggle form
        $("#DLG_Login").modal("toggle");
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
