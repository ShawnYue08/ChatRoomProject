package org.westos.ui;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author lwj
 * @date 2020/6/21 9:58
 */
public class MyServer {
    public static void main(String[] args) {
        HashMap<String, ObjectOutputStream> sockets = new HashMap<>();
        ArrayList<String> strings = new ArrayList<>();
        try {
            System.out.println("请输入端口号：");
            int port = Integer.parseInt(new Scanner(System.in).next());
            ServerSocket serverSocket = new ServerSocket(port);
            int i = 1;
            System.out.println("服务器已开启，正在等待连接...");
            while (true) {
                //循环监听
                Socket socket = serverSocket.accept();
                System.out.println("第" + i++ + "个客户端已经连接上来...");
                //当有客户端连接上来时，开启注册用户、用户登录线程
                RegisterOrLoginThread registerUserThread = new RegisterOrLoginThread(sockets, socket, strings);
                new Thread(registerUserThread).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}