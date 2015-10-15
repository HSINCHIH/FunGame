/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var GlobalGameServerIP = "127.0.0.1";
var GlobalGameServerPort = 99;
var GlobalWebSocket = new WrapWebSocket();
function SignUp()
{
    var userName = $("#TB_SignUp_ID").val();
}
function Login()
{
    var userName = $("#TB_Login_ID").val();
    var rememberMe = $("#CKB_RememberMe").prop("checked");
}
function CreateMap()
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
}
