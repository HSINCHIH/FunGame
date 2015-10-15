/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

/**
 *
 * @author mark.chen
 */
public interface IBaseEP {

    boolean Start(String ip, int port);

    void Stop();

    boolean Send(BaseMessage msg, EndPoint endPoint);

    void SetMsgCallBack(IReceiveMsgCallBack callback);
}
