package org.westos.bean;

import java.io.Serializable;

/**
 * @author lwj
 * @date 2020/6/26 12:24
 */
public class ChatMsgBean implements Serializable {
    private static final long serialVersionUID = 5472471558866377344L;
    public String receiver;//接收者
    public String sender;//发送者
    public String content;//消息内容
    public String time;//时间
    public int msgType;//消息类型

    public String fileName;//文件名
    public long fileLength;//文件大小
    public byte[] fileData;//文件的字节数据

    //普通消息的构造方法
    public ChatMsgBean(String receiver, String sender, String content,
                       String time, int msgType) {
        super();
        this.receiver = receiver;
        this.sender = sender;
        this.content = content;
        this.time = time;
        this.msgType = msgType;
    }

    //文件的构造方法
    public ChatMsgBean(String receiver, String sender, String content,
                       String time, int msgType, String fileName, long fileLength,
                       byte[] fileData) {
        super();
        this.receiver = receiver;
        this.sender = sender;
        this.content = content;
        this.time = time;
        this.msgType = msgType;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.fileData = fileData;
    }
}
