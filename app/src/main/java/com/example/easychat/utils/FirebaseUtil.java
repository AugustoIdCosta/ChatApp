package com.example.easychat.utils;

import android.content.Intent;

import com.example.easychat.model.ChatroomModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {

    public static String currentUserId(){
        try {
            return FirebaseAuth.getInstance().getUid();
        } catch (Exception e) {
            android.util.Log.e("FirebaseUtil", "Erro ao obter currentUserId: " + e.getMessage());
            return null;
        }
    }

    public static boolean isLoggedIn(){
        try {
            String userId = currentUserId();
            return userId != null && !userId.isEmpty();
        } catch (Exception e) {
            android.util.Log.e("FirebaseUtil", "Erro ao verificar login: " + e.getMessage());
            return false;
        }
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static String getChatroomId(String userId1,String userId2){
        try {
            if(userId1 == null || userId2 == null || userId1.isEmpty() || userId2.isEmpty()){
                return null;
            }
            
            if(userId1.hashCode()<userId2.hashCode()){
                return userId1+"_"+userId2;
            }else{
                return userId2+"_"+userId1;
            }
        } catch (Exception e) {
            android.util.Log.e("FirebaseUtil", "Erro ao gerar chatroomId: " + e.getMessage());
            return null;
        }
    }

    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds){
        if(userIds == null || userIds.size() < 2){
            return null;
        }
        
        String currentUserId = currentUserId();
        if(currentUserId == null){
            return null;
        }
        
        if(userIds.get(0).equals(currentUserId)){
            return allUserCollectionReference().document(userIds.get(1));
        }else{
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    public static String timestampToString(Timestamp timestamp){
        try {
            if (timestamp == null) {
                return ""; // Retorna uma string vazia se o timestamp for nulo
            }
            return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
        } catch (Exception e) {
            android.util.Log.e("FirebaseUtil", "Erro ao converter timestamp: " + e.getMessage());
            return "";
        }
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }
    public static StorageReference getChatImageStorageRef(String chatroomId, String fileName) {
        // Garante que o caminho seja válido
        if (chatroomId == null || chatroomId.isEmpty()) {
            chatroomId = "default";
        }
        return FirebaseStorage.getInstance().getReference().child("chat_media")
                .child(chatroomId).child(fileName);
    }

    public static StorageReference  getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(FirebaseUtil.currentUserId());
    }

    public static StorageReference  getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(otherUserId);
    }
    public static StorageReference  getGroupIconStorageRef(String chatroomId){
        return FirebaseStorage.getInstance().getReference().child("group_icons")
                .child(chatroomId);
    }
    public static void passChatroomModelAsIntent(Intent intent, ChatroomModel model){
        intent.putExtra("chatroom_model", model);
    }

    public static ChatroomModel getChatroomModelFromIntent(Intent intent){
        try {
            if(intent == null){
                return null;
            }
            return (ChatroomModel) intent.getSerializableExtra("chatroom_model");
        } catch (Exception e) {
            android.util.Log.e("FirebaseUtil", "Erro ao obter ChatroomModel do Intent: " + e.getMessage());
            return null;
        }
    }


}










