function StringFormat()
{
    if (arguments.length <= 0 || arguments[0] == "")
        return;
    var result = arguments[0];
    for (var i = 1; i < arguments.length; i++)
    {
        var str = "\\{" + (i - 1) + "\\}";
        var pattern = new RegExp(str);
        result = result.replace(pattern, arguments[i]);
    }
    return result;
}
function DigitFormat(value, digit)
{
    var valueLength = value.toString().length;
    var result = "";
    if (digit > valueLength)
    {
        for (var i = 0; i < digit - valueLength; i++)
        {
            result += "0";
        }
        result = result.concat(value.toString());
    }
    else
        result = value.toString();
    return result;
}
function GetCurrentTime(format)
{
    var now = new Date();
    var yyyy, MM, dd, hh, mm, ss, mis;
    yyyy = now.getFullYear();
    MM = digitFormat("00", now.getMonth() + 1);
    dd = digitFormat("00", now.getDate());
    hh = digitFormat("00", now.getHours());
    mm = digitFormat("00", now.getMinutes());
    ss = digitFormat("00", now.getSeconds());
    mis = digitFormat("000", now.getMilliseconds());
    format = String(format).replace(/yyyy/, yyyy);
    format = String(format).replace(/MM/, MM);
    format = String(format).replace(/dd/, dd);
    format = String(format).replace(/hh/, hh);
    format = String(format).replace(/mm/, mm);
    format = String(format).replace(/ss/, ss);
    format = String(format).replace(/mis/, mis);
    return format;
}
function GetUUID()
{
    var guid = "";
    for (var i = 1; i <= 32; i++)
    {
        var n = Math.floor(Math.random() * 16.0).toString(16);
        guid += n;
        if ((i == 8) || (i == 12) || (i == 16) || (i == 20))
            guid += "-";
    }
    return guid;
}
function ArrayCopy(input)
{
    var result = [];
    for (var i = 0; i < input.length; i++)
    {
        result.push(input[i]);
    }
    return result;
}
function UseFloor(min, max)
{
    return Math.floor(Math.random() * (max - min + 1) + min);
}
function BindWrapper(object, method)
{
    return function ()
    {
        return method.apply(object, arguments);
    };
}
var Screen =
        {
            Width: document.documentElement.clientWidth,
            Height: document.documentElement.clientHeight
        }