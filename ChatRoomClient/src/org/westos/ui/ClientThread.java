package org.westos.ui;

import org.westos.bean.ChatMsgBean;
import org.westos.config.Config;

import java.io.*;


/**
 * 客户端读取消息
 * @author lwj
 * @date 2020/6/21 10:09
 */
public class ClientThread implements Runnable {
    private ObjectInputStream in;
    private volatile boolean isSave = true;
    private volatile boolean f = false;

    public ClientThread(ObjectInputStream in) {
        this.in = in;
    }

    public boolean isSave() {
        return isSave;
    }

    public void setSave(boolean save) {
        isSave = save;
    }

    public boolean isF() {
        return f;
    }

    public void setF(boolean f) {
        this.f = f;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ChatMsgBean bean = (ChatMsgBean) in.readObject();
                String sender = bean.sender;
                String content = bean.content;
                int messageType = bean.msgType;
                String time = bean.time;
                if (messageType == Config.MESSAGE_ONLINE) {
                    //如果是上线提醒消息
                    System.out.println(time + "\t\t" + sender + ":" + content);
                } else if (messageType == Config.MESSAGE_ONLINE_LIST) {
                    //如果是在线用户列表消息
                    System.out.println(time + "\t\t" + content);
                } else if (messageType == Config.MESSAGE_PRIVATE) {
                    //如果是私聊消息
                    System.out.println(time + "\t\t" + sender + " 对你说：" + content);
                } else if (messageType == Config.MESSAGE_PUBLIC) {
                    //如果是公聊消息
                    System.out.println(time + "\t\t" + sender + " 对大家说：" + content);
                } else if (messageType == Config.MESSAGE_EXIT) {
                    //如果是下线消息
                    System.out.println(time + "\t\t" + sender + ":" + content);
                } else if (messageType == Config.MESSAGE_SEND_FILE) {
                    //如果是文件消息
                    System.out.println(time + "\t\t" + sender + " 给你发来一个文件，名称为: " + bean.fileName +
                            "，大小" + (bean.fileLength / 1024 / 1024.0) + "MB");

                    byte[] fileData = bean.fileData;

                    System.out.println("你是否要接收？y(输入8代表接收)/n(输入9代表不接收)");
                    
                    while (isSave) {
                        if (f) {
                            break;
                        }
                    }

                    if (isSave) {
                        //如果选择保存文件
                        try {
                            FileOutputStream fis = new FileOutputStream(new File("chatroom",
                                    System.currentTimeMillis() + "-" + bean.fileName));
                            //保证文件不重名，不被覆盖
                            fis.write(fileData);
                            fis.flush();
                            System.out.println("文件保存成功");
                        } catch (IOException e) {
                            System.out.println("文件保存失败。");
                        }
                    } else {
                        ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
                        //读取管道中的文件数据
                        bis.reset();
                    }

                    //重置
                    isSave = true;
                    f = false;
                } else if (messageType == Config.MESSAGE_HIDDEN) {
                    //如果是上线消息
                    System.out.println(time + "\t\t" + sender + ":" + content);
                } else if (messageType == Config.MESSAGE_PRIVATE_REQUIRE) {
                    //如果是查询私聊消息
                    System.out.println(content);
                } else if (messageType == Config.MESSAGE_PUBLIC_REQUIRE) {
                    //如果是查询公聊消息
                    System.out.println(content);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
