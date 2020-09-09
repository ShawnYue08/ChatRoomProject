package org.westos.ui;

import org.westos.bean.ChatMsgBean;
import org.westos.config.Config;
import org.westos.utils.InputNumUtil;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author lwj
 * @date 2020/6/21 9:45
 */
public class MyClient {
    private static ObjectOutputStream out;
    private static Scanner sc;
    private static Socket socket;

    public static void main(String[] args) {
        try {
            System.out.println("请输入服务器IP：");
            sc = new Scanner(System.in);
            String ip = sc.next();
            System.out.println("请输入端口号：");
            int port = Integer.parseInt(sc.next());
            socket = new Socket(ip, port);

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            //in 反序列化流
            out = new ObjectOutputStream(socket.getOutputStream());

            while (true) {
                sc = new Scanner(System.in);
                System.out.println("请选择：1、登录 2、注册");
                int i = InputNumUtil.scannerNum();
                if (!(i == 1 || i == 2)) {
                    System.out.println("输入错误，重新输入");
                    continue;
                }
                byte[] bytes = new byte[1024];
                ChatMsgBean chatMsgBean = null;
                ChatMsgBean bean = null;
                if (i == 1) {
                    System.out.println("开始登录...");
                    sc = new Scanner(System.in);
                    System.out.println("请输入用户名：");
                    String name = sc.next();
                    System.out.println("请输入密码：");
                    String password = sc.next();
                    chatMsgBean = new ChatMsgBean(null, name, name + "#" + password, "0", Config.MESSAGE_LOGIN);
                    //发送的登陆消息对象 receiver,sender,content,time,type
                    out.writeObject(chatMsgBean);
                    out.flush();

                    bean = (ChatMsgBean) in.readObject();
                    String content = bean.content;
                    if ("登录成功。".equals(content)) {
                        //登录成功
                        System.out.println("登录成功。");
                        break;
                    } else {
                        System.out.println(content);
                        //重新选择
                    }
                } else {
                    System.out.println("开始注册...");
                    sc = new Scanner(System.in);
                    System.out.println("请输入用户名：");
                    String username = sc.next();
                    System.out.println("请输入密码：");
                    String pwd = sc.next();
                    chatMsgBean = new ChatMsgBean(null, username, username + "#" + pwd, "0", Config.MESSAGE_REGISTER);
                    out.writeObject(chatMsgBean);
                    out.flush();

                    //接收RegisterOrLoginThread线程的反馈
                    bean = (ChatMsgBean) in.readObject();
                    String msg = bean.content;
                    if ("yes".equals(msg)) {
                        //注册成功
                        System.out.println("您已注册成功，下一步请选择登录：");
                    } else {
                        System.out.println("注册失败，请重新注册");
                    }
                }
            }


            ClientThread clientThread = new ClientThread(in);
            Thread thread = new Thread(clientThread);
            thread.start();
            //当登录完成后，便可以开始聊天，开启子线程接收消息，in参数是当前客户端和服务端之间的输入流通道

            boolean flag = true;
            while (flag) {
                //可以循环选择聊天方式：公聊，私聊，获取在线列表等
                System.out.println("请选择：1.私聊 2.公聊 3.在线列表 4.下线 5.发送文件 6.隐身/上线 7.查询聊天记录 ");
                int num = InputNumUtil.scannerNum();
                switch (num) {
                    case 1:
                        privateChat();
                        break;
                    case 2:
                        publicChat();
                        break;
                    case 3:
                        getOnlineList();
                        break;
                    case 4:
                        exitChat();
                        flag = false;
                        // 客户端要做什么？1.发送下线指令到服务端，2.关闭客户端的Socket 3.停掉客户端的读取消息线程。
                        // 服务端要做什么？1.给其他人发送下线提醒。2.服务器要关闭客户端的管道，3.还要移除这个人。
                        break;
                    case 5:
                        sendFile();
                        break;
                    case 6:
                        hiddenOrOnline();
                        break;
                    case 7:
                        inquireMsg();
                        break;
                    case 8:
                        clientThread.setF(true);
                        //这样while循环可以break
                        break;
                    case 9:
                        clientThread.setSave(false);
                        //这样while循环为false，不进入循环
                        break;
                    default:
                        exitChat();
                        //默认也让用户下线
                        flag = false;
                        break;
                }
            }

            //当flag==false时，退出主线程的while循环，停掉客户端的读取消息线程
            thread.stop();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                    //关闭客户端主线程中的socket
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void inquireMsg() throws IOException {
        System.out.println("你想查询？(1代表公聊，2代表私聊)");
        ChatMsgBean chatMsgBean = null;
        while (true) {
            int num = InputNumUtil.scannerNum();
            if (!(num == 1 || num == 2)) {
                System.out.println("您的输出不正确，请重新输入。");
                continue;
            }
            if (num == 1) {
                chatMsgBean = new ChatMsgBean(null, null, null, "0", Config.MESSAGE_PUBLIC_REQUIRE);
            } else {
                chatMsgBean = new ChatMsgBean(null, null, null, "0", Config.MESSAGE_PRIVATE_REQUIRE);
            }
            out.writeObject(chatMsgBean);
            out.flush();
            break;
        }
    }

    /**
     * 上线/隐身状态的切换
     * @throws IOException
     */
    private static void hiddenOrOnline() throws IOException {
        ChatMsgBean chatMsgBean = new ChatMsgBean(null, null, null, "0", Config.MESSAGE_HIDDEN);
        out.writeObject(chatMsgBean);
    }


    /**
     * 发送文件
     */
    private static void sendFile() throws IOException {
        getOnlineList();
        sc = new Scanner(System.in);
        System.out.println("请输入接收者：");
        String receiver = sc.next();
        System.out.println("请输入文件的路径：");
        sc = new Scanner(System.in);
        String filePath = sc.nextLine();
        //这里用nextLine()，否则如果文件名存在空格，那么将会报FileNotFoundException
        File file = new File(filePath);

        System.out.println(file.exists());

        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024 * 8];
        int len = 0;
        while ((len = fis.read(bytes)) != -1) {
            bos.write(bytes, 0, len);
        }
        byte[] fileBytes = bos.toByteArray();
        ChatMsgBean chatMsgBean = new ChatMsgBean(receiver, null, null, "0", Config.MESSAGE_SEND_FILE,
                file.getName(), file.length(), fileBytes);
        out.writeObject(chatMsgBean);
        out.flush();
    }

    private static void exitChat() throws IOException {
        ChatMsgBean chatMsgBean = new ChatMsgBean(null, null, null, "0", Config.MESSAGE_EXIT);
        out.writeObject(chatMsgBean);
        out.flush();
    }

    /**
     * 获取当前在线用户列表
     */
    private static void getOnlineList() throws IOException {
        //接收者###消息内容###消息类型
        ChatMsgBean chatMsgBean = new ChatMsgBean(null, null, null, "0", Config.MESSAGE_ONLINE_LIST);
        out.writeObject(chatMsgBean);
        out.flush();
    }


    /**
     * 公聊功能
     */
    private static void publicChat() throws IOException {
        while (true) {
            System.out.println("[你当前处于公聊模式] 请输入要发送的消息，如果想要退出公聊模式，请按q键");
            sc = new Scanner(System.in);
            String msg = sc.nextLine();
            if (msg.equals("q")) {
                break;
            }
            //公聊的消息格式：接收者###消息内容###消息类型
            //公聊不需要指定接收者
            ChatMsgBean chatMsgBean = new ChatMsgBean(null, null, msg, "0", Config.MESSAGE_PUBLIC);
            out.writeObject(chatMsgBean);
            out.flush();
        }
    }

    /**
     * 私聊功能
     */
    private static void privateChat() throws IOException {
        getOnlineList();
        //获取在线列表
        sc = new Scanner(System.in);
        System.out.println("[你当前处于私聊模式] 请选择一个私聊对象，如果想要退出私聊模式，请按q键");
        String username = sc.nextLine();
        if (username.equals("q")) {
            return;
        }
        while (true) {
            System.out.println("请输入待发的消息，如果想要退出，请按q键");
            String msg = sc.next();
            if (msg.equals("q")) {
                break;
            }
            //私聊的消息格式：接收者###消息内容###消息类型
            ChatMsgBean chatMsgBean = new ChatMsgBean(username, null, msg, "0", Config.MESSAGE_PRIVATE);
            out.writeObject(chatMsgBean);
            out.flush();
        }
    }
}