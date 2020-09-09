package org.westos.ui;


import com.sun.security.ntlm.Server;
import org.westos.bean.ChatMsgBean;
import org.westos.config.Config;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author lwj
 * @date 2020/6/21 10:10
 */
public class ServerThread implements Runnable {
    private HashMap<String, ObjectOutputStream> sockets;
    private ObjectInputStream in;
    private String username;
    private ArrayList<String> list;
    private boolean isHidden;
    private static BufferedWriter pri_bw;
    private static BufferedWriter pub_bw;

    static {
        try {
            pri_bw = new BufferedWriter(new FileWriter(new File("私聊.txt"), true));
            pub_bw = new BufferedWriter(new FileWriter(new File("公聊.txt"), true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerThread(HashMap<String, ObjectOutputStream> sockets, ObjectInputStream in, String username, ArrayList<String> list) {
        this.sockets = sockets;
        this.in = in;
        this.username = username;
        this.list = list;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ChatMsgBean chatMsgBean = (ChatMsgBean) in.readObject();
                //填充字段
                chatMsgBean.sender = username;
                chatMsgBean.time = RegisterOrLoginThread.getCurrentTime();
                String receiver = chatMsgBean.receiver;
                int messageType = chatMsgBean.msgType;

                if (messageType == Config.MESSAGE_ONLINE_LIST) {
                    StringBuilder sb = new StringBuilder("在线列表：");
                    int i = 1;
                    for (String s : list) {
                        if (!s.equals(username)) {
                            sb.append(i++).append("、").append(s).append("\t");
                        }
                    }
                    chatMsgBean.content = sb.toString();
                    sockets.get(username).writeObject(chatMsgBean);
                    //receiver=username,获取username的out通道
                } else if (messageType == Config.MESSAGE_PRIVATE) {
                    if (sockets.containsKey(receiver)) {
                        synchronized (ServerThread.class) {
                            pri_bw.write(chatMsgBean.time + "\t\t" + chatMsgBean.sender + "-->" + receiver + "\t\t" +
                                    chatMsgBean.content);
                            pri_bw.newLine();
                            pri_bw.flush();
                        }
                        sockets.get(receiver).writeObject(chatMsgBean);
                        //receiver=receiver,获取receiver的out通道
                    } else {
                        chatMsgBean.content = "当前用户不在线，请稍后再聊";
                        sockets.get(username).writeObject(chatMsgBean);
                        //receiver=username,获取username的out通道
                    }
                } else if (messageType == Config.MESSAGE_PUBLIC) {
                    Set<String> strings = sockets.keySet();
                    synchronized (ServerThread.class) {
                        pub_bw.write(chatMsgBean.time + "\t\t" + chatMsgBean.sender + "-->" + "所有人" + "\t\t"
                                + chatMsgBean.content);
                        pub_bw.newLine();;
                        pub_bw.flush();
                    }
                    for (String string : strings) {
                        if (!string.equals(username)) {
                            sockets.get(string).writeObject(chatMsgBean);
                            //receiver=当前Sockets集合中的用户,即使隐身也可以接收消息
                        }
                    }
                } else if (messageType == Config.MESSAGE_EXIT) {
                    Set<String> strings = sockets.keySet();
                    for (String string : strings) {
                        chatMsgBean.content = "下线了";
                        if (!string.equals(username)) {
                            sockets.get(string).writeObject(chatMsgBean);
                            //receiver=当前Sockets集合中的用户
                        }
                    }
                    break;
                } else if (messageType == Config.MESSAGE_SEND_FILE) {
                    sockets.get(receiver).writeObject(chatMsgBean);
                    //receiver=receiver,向接收方发送消息
                } else if (messageType == Config.MESSAGE_HIDDEN) {
                    if (!isHidden) {
                        //隐身
                        list.remove(username);
                    } else {
                        //上线
                        list.add(username);
                        for (String string : list) {
                            if (string.equals(username)) {
                                continue;
                            }
                            ChatMsgBean bean = new ChatMsgBean(string, username, "上线了",
                                    RegisterOrLoginThread.getCurrentTime(), Config.MESSAGE_ONLINE);
                            sockets.get(string).writeObject(bean);
                        }
                    }
                    isHidden = !isHidden;
                } else if (messageType == Config.MESSAGE_PUBLIC_REQUIRE) {
                    BufferedReader reader = new BufferedReader(new FileReader(new File("公聊.txt")));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    String s = null;
                    while ((s = reader.readLine()) != null) {
                        bos.write(s.getBytes());
                        bos.write("\n".getBytes());
                    }
                    chatMsgBean = new ChatMsgBean(username, null, new String(bos.toByteArray()), RegisterOrLoginThread.getCurrentTime(),
                            Config.MESSAGE_PUBLIC_REQUIRE);
                    sockets.get(username).writeObject(chatMsgBean);
                } else if (messageType == Config.MESSAGE_PRIVATE_REQUIRE) {
                    BufferedReader reader = new BufferedReader(new FileReader(new File("私聊.txt")));
                    String s = null;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while ((s = reader.readLine()) != null) {
                        String[] split = s.split("[\\-\\->]{3}");
                        if (split[0].contains(username) || split[1].contains(username)) {
                            bos.write(s.getBytes());
                            bos.write("\n".getBytes());
                        }
                    }
                    chatMsgBean = new ChatMsgBean(username, null, new String(bos.toByteArray()), RegisterOrLoginThread.getCurrentTime(),
                            Config.MESSAGE_PRIVATE_REQUIRE);
                    sockets.get(username).writeObject(chatMsgBean);
                }
            }
            sockets.get(username).close();
            sockets.remove(username);
            list.remove(username);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
