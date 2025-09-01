package com.example.easychat.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class ChatMessageModel implements Serializable {
    private String message;
    private String senderId;
    private Timestamp timestamp;

    /*** Gemini - Inicio***/
    private String messageType; // Pode ser "TEXT", "IMAGE", ou "VIDEO"
    private String messageStatus; // Pode ser "sent", "delivered", ou "read"
    /***gemini - fim***/

    public ChatMessageModel() {
    }

    // Construtor modificado
    public ChatMessageModel(String message, String senderId, Timestamp timestamp, String messageType, String messageStatus) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        /*** Gemini - Inicio***/
        this.messageType = messageType;
        this.messageStatus = messageStatus;
        /***gemini - fim***/
    }

    // ... (Getters e Setters existentes para message, senderId, timestamp)

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /*** Gemini - Inicio***/
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }
    /***gemini - fim***/
}