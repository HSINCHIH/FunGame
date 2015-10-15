/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var GlobalGameServerIP = "127.0.0.1";
var GlobalGameServerPort = 99;
function SignUp()
{
    var userName = $("#TB_SignUp_ID").val();
}

function Login()
{
    var userName = $("#TB_Login_ID").val();
    var rememberMe = $("#CKB_RememberMe").prop("checked");
}

function PlayGame() {
}
PlayGame.prototype = {
    m_Socket: null,
    OnOpen: function ()
    {
        console.log("OnOpen");
    },
    OnReceive: function ()
    {
        console.log("OnReceive");
    },
    OnError: function ()
    {
        console.log("OnError");
    },
    OnClose: function ()
    {
        console.log("OnClose");
    },
    Connect: function ()
    {
        this.m_Socket.Connect("127.0.0.1:99");
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
