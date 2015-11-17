/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function GameMonitor() {
    this.Init();
}
GameMonitor.prototype = {
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
    }
};


