/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 *
 * @author mark.chen
 */
public class BaseMessage {

    public int Action = 0;
    public ArrayList<String> Args = null;

    public BaseMessage() {
        this.Args = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder SB = new StringBuilder();
        SB.append(String.format("%d|", Action));
        for (String arg : Args) {
            SB.append(String.format("%s;", arg));
        }
        return SB.toString().substring(0, SB.length());
    }

    public byte[] GetBytes() {
        return this.toString().getBytes(Charset.forName("UTF-8"));
    }
}
