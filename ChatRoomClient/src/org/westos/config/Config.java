package org.westos.config;

/**
 * @author lwj
 * @date 2020/6/21 14:08
 */
public interface Config {
    //客户端向服务端发送注册/登录类型消息
    public static final int MESSAGE_REGISTER = 100;
    public static final int MESSAGE_LOGIN = 200;

    //服务端向客户端发送上线提醒类型消息
    public static final int MESSAGE_ONLINE = 300;
    //客户端向服务端发送在线用户消息
    public static final int MESSAGE_ONLINE_LIST = 400;

    //私聊消息
    public static final int MESSAGE_PRIVATE = 500;
    //公聊消息
    public static final int MESSAGE_PUBLIC = 600;

    //客户端向客户端发送下线的消息
    public static final int MESSAGE_EXIT = 700;

    //客户端向客户端发送文件的消息
    public static final int MESSAGE_SEND_FILE = 800;

    //隐身/上线
    public static final int MESSAGE_HIDDEN = 900;

    //查询公聊消息
    public static final int MESSAGE_PUBLIC_REQUIRE = 1000;

    //查询私聊消息
    public static final int MESSAGE_PRIVATE_REQUIRE = 1100;
}
