/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 *
 * @author mark.chen
 */
public class Utilities {

    public static String GetHostIP() {
        String result = "";
        try {
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements();) {
                NetworkInterface e = n.nextElement();
                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements();) {
                    InetAddress addr = a.nextElement();
                    if (addr instanceof Inet4Address) {
                        if (addr.getHostAddress().equals("127.0.0.1")) {
                            continue;
                        }
                        result = addr.getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(String.format("[%s] Exception : %s", "GetHostIP", e.getMessage()));
        }
        return result;
    }
}
