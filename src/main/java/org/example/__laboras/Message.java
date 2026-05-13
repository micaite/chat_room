package org.example.__laboras;

import java.io.Serializable;

public class Message implements Serializable {                                                   //serializable, kad objectOutputStream galetu paversti baitais ir siusti per tinkla
    public enum Type {
        PUBLIC,
        PRIVATE,
        CREATE_ROOM,
        JOIN_ROOM,
        ROOM_LIST,
        JOIN_OK,
        USER_LIST,
        GET_ROOMS,
        GET_USERS
    }

    private String sender;
    private String receiver; // null- visiems
    private String content;
    private Type type;
    private String room; // null- asmenine

    public Message(Type type, String sender, String receiver, String content, String room) {
        this.type     = type;
        this.sender   = sender;
        this.receiver = receiver;
        this.content  = content;
        this.room     = room;
    }

    public Type   getType()      { return type; }
    public String getSender()    { return sender; }
    public String getReceiver()  { return receiver; }
    public String getContent()   { return content; }
    public String getRoom()      { return room; }
}
