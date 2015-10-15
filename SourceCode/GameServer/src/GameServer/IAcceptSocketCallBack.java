/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.net.Socket;

/**
 *
 * @author mark.chen
 */
public interface IAcceptSocketCallBack {

    void AcceptSocket(Socket socket);

}
