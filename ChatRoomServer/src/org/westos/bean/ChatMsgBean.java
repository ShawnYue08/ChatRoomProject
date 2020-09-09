package org.westos.bean;

import java.io.Serializable;

/**
 * @author lwj
 * @date 2020/6/26 12:24
 */
public class ChatMsgBean implements Serializable {
    private static final long serialVersionUID = 5472471558866377344L;
    public String receiver;
    public String sender;
    public String content;
    public String time;
    public int msgType;

    public String fileName;
    public long fileLength;
    public byte[] fileData;


    public ChatMsgBean(String receiver, String sender, String content,
                       String time, int msgType) {
        this.receiver = receiver;
        this.sender = sender;
        this.content = content;
        this.time = time;
        this.msgType = msgType;
    }


    public ChatMsgBean(String receiver, String sender, String content,
                       String time, int msgType, String fileName, long fileLength,
                       byte[] fileData) {
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
