package org.westos.ui;

import org.westos.bean.ChatMsgBean;
import org.westos.config.Config;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;


/**
 * @author lwj
 * @date 2020/6/21 11:11
 */
public class RegisterOrLoginThread implements Runnable {
    private HashMap<String, ObjectOutputStream> sockets;
    private Socket socket;
    private ArrayList<String> list;
    private volatile static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(new FileReader(new File("ChatRoomServer/src/org/westos/resource/user.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RegisterOrLoginThread(HashMap<String, ObjectOutputStream> sockets, Socket socket, ArrayList<String> strings) {
        this.sockets = sockets;
        this.socket = socket;
        this.list = strings;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            String username;
            while (true) {
                ChatMsgBean chatMsgBean = (ChatMsgBean) in.readObject();
                username = chatMsgBean.sender;
                int messageType = chatMsgBean.msgType;
                String content = chatMsgBean.content;
                String[] split = content.split("#");
                String name = split[0];
                String password = split[1];
                if (messageType == Config.MESSAGE_REGISTER) {
                    Set<String> strings = properties.stringPropertyNames();
                    if (strings.contains(username)) {
                        chatMsgBean.content = "no";
                    } else {
                        properties.setProperty(username, password);
                        synchronized (this) {
                            //持久化
                            properties.store(new FileWriter(new File("ChatRoomServer/src/org/westos/resource/user.properties")), "add a user");
                        }
                        chatMsgBean.content = "yes";
                    }
                    out.writeObject(chatMsgBean);
                    out.flush();
                } else if (messageType == Config.MESSAGE_LOGIN) {
                    //如果是登录消息
                    Set<String> strings = properties.stringPropertyNames();
                    if (strings.contains(username)) {
                        if (!sockets.containsKey(username)) {
                            String property = properties.getProperty(username);
                            if (property.equals(password)) {
                                sockets.put(username, out);
                                list.add(username);
                                chatMsgBean.content = "登录成功。";
                                out.writeObject(chatMsgBean);
                                out.flush();
                                break;
                            } else {
                                chatMsgBean.content = "密码输入错误，请重新选择。";
                                out.writeObject(chatMsgBean);
                                out.flush();
                            }
                        } else {
                            chatMsgBean.content = "该用户已经登录，不能重复登录。";
                            out.writeObject(chatMsgBean);
                            out.flush();
                        }
                    } else {
                        //如果不包含该用户
                        chatMsgBean.content = "该用户还未注册，请先去注册。";
                        out.writeObject(chatMsgBean);
                        out.flush();
                    }
                }
            }


            //登录成功，向其他在线成员发送上线提醒
            Set<String> strings = sockets.keySet();
            for (String string : strings) {
                if (!string.equals(username)) {
                    ChatMsgBean bean = new ChatMsgBean(string, username, "上线了", getCurrentTime(), Config.MESSAGE_ONLINE);
                    //receiver=string,其他在线用户
                    //sender=username
                    //content:"上线了"
                    sockets.get(string).writeObject(bean);
                }
            }

            //如果登录成功，开启聊天线程
            new Thread(new ServerThread(sockets, in, username, list)).start();
            //把HashMap、当前用户、通道、list都传递给通信线程
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentTime() {
        return LocalDateTime.now().
                format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
