package com.example.easychat.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.io.Serializable;

public class ChatroomModel  implements Serializable {
    String chatroomId;
    List<String> userIds;
    private transient Timestamp lastMessageTimestamp;
    String lastMessageSenderId;
    String lastMessage;

    private boolean groupChat;
    private String groupName;
    private String groupIcon;
    private List<String> adminIds;


    public ChatroomModel() {
    }

    // Construtor original para chats 1-para-1
    public ChatroomModel(String chatroomId, List<String> userIds, Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;

        this.groupChat = false;

    }

    /*** Gemini - Inicio***/
    // Novo construtor para criar GRUPOS
    public ChatroomModel(String chatroomId, String groupName, List<String> userIds, List<String> adminIds) {
        this.chatroomId = chatroomId;
        this.groupName = groupName;
        this.userIds = userIds;
        this.adminIds = adminIds;
        this.lastMessageTimestamp = Timestamp.now();
        this.groupChat = true; // Define como true para identificar que é um grupo
    }
    /***gemini - fim***/


    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    /*** Gemini - Inicio***/
    // Novos Getters e Setters para os campos de grupo
    public boolean isGroupChat() {
        return groupChat;
    }

    public void setGroupChat(boolean groupChat) {
        this.groupChat = groupChat;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public List<String> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(List<String> adminIds) {
        this.adminIds = adminIds;
    }
    /***gemini - fim***/
}