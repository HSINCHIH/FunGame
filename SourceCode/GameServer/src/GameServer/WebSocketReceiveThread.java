/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.io.InputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Hashtable;
import sun.misc.BASE64Encoder;

/**
 *
 * @author mark.chen
 */
public class WebSocketReceiveThread extends Thread {

    Log m_Log = new Log("WebSocketReceiveThread");
    Socket m_TcpSocket = null;
    boolean m_IsRunning = true;
    InputStream m_InStream = null;
    EndPoint m_RemoteEndPoint = null;
    boolean m_IsFirst = true;
    IReceiveMsgCallBack m_CallBack = null;

    public WebSocketReceiveThread() {
    }

    public void SetSocket(Socket socket) {
        try {
            m_TcpSocket = socket;
            m_InStream = m_TcpSocket.getInputStream();
            String remoteIP = socket.getInetAddress().getHostAddress();
            int remotePort = socket.getPort();
            m_RemoteEndPoint = new EndPoint(remoteIP, remotePort);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "SetSocket", e.getMessage()));
        }
    }

    public void SetMsgCallBack(IReceiveMsgCallBack callBack) {
        m_CallBack = callBack;
    }

    @Override
    public void run() {
        while (m_IsRunning) {
            try {
                if (m_InStream.available() <= 0) {
                    Thread.sleep(10);
                    continue;
                }
                byte[] recvBuf = new byte[m_InStream.available()];
                int length = m_InStream.read(recvBuf);
                RefString refItem = new RefString();
                int decodeLength = Decode(recvBuf, refItem);
                if (refItem.value.equals("Disconnect")) {
                    Parse(String.format("%s|%d", m_RemoteEndPoint.toString(), ServerAction.DISCONNECTED));
                    Thread.sleep(10);
                    continue;
                }
                if (decodeLength <= 0) {
                    Thread.sleep(10);
                    continue;
                }
                String recvData = String.format("%s|%s", m_RemoteEndPoint.toString(), refItem.value);
                Parse(recvData);
                Thread.sleep(10);
            } catch (Exception e) {
                m_Log.Writeln(String.format("%s Exception : %s", "run", e.getMessage()));
            }
        }
        m_Log.Writeln(String.format("%s %s", "run", "stop"));
    }

    public void Abort() {
        m_IsRunning = false;
    }

    private int Decode(byte[] recvBuf, RefString recvString) {
        try {
            if (m_IsFirst) {
                String newLine = "\r\n";
                Hashtable<String, String> header = new Hashtable<>();
                String recvData = new String(recvBuf);
                String[] parameters = recvData.split("\r|\n");
                for (String parameter : parameters) {
                    int index = parameter.indexOf(":");
                    if (index > -1) {
                        String key = parameter.substring(0, index);
                        String value = parameter.substring(index + 2, parameter.length());
                        header.put(key, value);
                    }
                }
                String newKey = GetAcceptKey(header.get("Sec-WebSocket-Key"));
                String newHead = String.format("HTTP/1.1 101 Switching Protocols%s", newLine);
                newHead += String.format("Upgrade: WebSocket%s", newLine);
                newHead += String.format("Connection: Upgrade%s", newLine);
                newHead += String.format("Sec-WebSocket-Accept: %s%s%s", newKey, newLine, newLine);
                m_TcpSocket.getOutputStream().write(newHead.getBytes("UTF8"));
                m_IsFirst = false;
            } else {
                int length = recvBuf[1] & 0x7f;
                byte[] masks = new byte[4];
                byte[] dataBuf = null;
                switch (length) {
                    case 126: {
                        System.arraycopy(recvBuf, 4, masks, 0, 4);
                        short u16Length = (short) ((recvBuf[2] & 0xFF) << 8 | (recvBuf[3] & 0xFF));
                        dataBuf = new byte[u16Length];
                        System.arraycopy(recvBuf, 8, dataBuf, 0, u16Length);
                    }
                    break;
                    case 127: {
//                        System.arraycopy(recvBuf, 10, masks, 0, 4);
//                        byte[] uInt64Bytes = new byte[8];
//                        System.arraycopy(recvBuf, 2, uInt64Bytes, 0, 8);
//                        Utilities.Reverse(uInt64Bytes);
//                        BigInteger u64Length = new BigInteger(uInt64Bytes);
//                        dataBuf = new byte[u64Length];
//                        for (UInt64 i = 0; i < u64Length; i++) {
//                            dataBuf[i] = recvBuf[i + 14];
//                        }
                    }
                    break;
                    default: {
                        System.arraycopy(recvBuf, 2, masks, 0, 4);
                        dataBuf = new byte[length];
                        System.arraycopy(recvBuf, 6, dataBuf, 0, length);
                    }
                    break;
                }
                for (int i = 0; i < dataBuf.length; i++) {
                    dataBuf[i] = (byte) (dataBuf[i] ^ masks[i % 4]);
                }
                if (IsDisconnect(dataBuf)) {
                    recvString.value = "Disconnect";
                    return recvString.value.length();
                }
                recvString.value = new String(dataBuf, "UTF8").trim();
                return recvString.value.length();
            }
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Decode", e.getMessage()));
        }
        return recvString.value.length();
    }

    private String GetAcceptKey(String keySource) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] rawBuf = md.digest((keySource + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF8"));
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(rawBuf);
        } catch (Exception e) {
            m_Log.Writeln(String.format("[%s]->[%s] Exception : %s", "WebSocketReceiveThread", "GetAcceptKey", e.getMessage()));
            return "";
        }
    }

    private boolean IsDisconnect(byte[] src) {
        byte[] target = new byte[]{(byte) 3, (byte) 233};
        return Arrays.equals(src, target);
    }

    private void Parse(String recvData) {
        if (m_CallBack != null) {
            String[] args = recvData.split(";|\\|");
            String[] epArgs = args[0].split(":");
            EndPoint ep = new EndPoint(epArgs[0], Integer.parseInt(epArgs[1]));
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = Integer.parseInt(args[1]);
            for (int i = 2; i < args.length; i++) {
                newMsg.Args.add(args[i]);
            }
            m_CallBack.ReceiveMsg(newMsg, ep);
        }
    }
}
