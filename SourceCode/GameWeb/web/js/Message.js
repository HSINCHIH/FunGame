/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function Message()
{
    this.Initial();
}
Message.prototype =
        {
            Action: null,
            Args: null,
            GetString: function ()
            {
                var command = this.Action.toString() + "|";
                for (var i = 0; i < this.Args.length; i++)
                {
                    command += this.Args[i].toString() + ";";
                }
                command = command.substring(0, command.length - 1);
                return command;
            },
            Initial: function ()
            {
                this.Args = new Array();
            }
        }

