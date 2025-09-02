package com.example.easychat.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.easychat.model.ChatroomModel;
import com.example.easychat.model.UserModel;
import com.google.firebase.firestore.auth.User;

public class AndroidUtil {

    public static  void showToast(Context context,String message){
        try {
            if(context != null && message != null){
                Toast.makeText(context,message,Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            android.util.Log.e("AndroidUtil", "Erro ao mostrar toast: " + e.getMessage());
        }
    }

    public static void passUserModelAsIntent(Intent intent, UserModel model){
        try {
            if(intent != null && model != null){
                intent.putExtra("username",model.getUsername());
                intent.putExtra("phone",model.getPhone());
                intent.putExtra("userId",model.getUserId());
                intent.putExtra("fcmToken",model.getFcmToken());
            }
        } catch (Exception e) {
            android.util.Log.e("AndroidUtil", "Erro ao passar UserModel para Intent: " + e.getMessage());
        }
    }

    public static UserModel getUserModelFromIntent(Intent intent){
        try {
            if(intent == null){
                return null;
            }

            UserModel userModel = new UserModel();
            userModel.setUsername(intent.getStringExtra("username"));
            userModel.setPhone(intent.getStringExtra("phone"));
            userModel.setUserId(intent.getStringExtra("userId"));
            userModel.setFcmToken(intent.getStringExtra("fcmToken"));
            return userModel;
        } catch (Exception e) {
            android.util.Log.e("AndroidUtil", "Erro ao obter UserModel do Intent: " + e.getMessage());
            return null;
        }
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        try {
            if(context != null && imageUri != null && imageView != null){
                Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
            }
        } catch (Exception e) {
            android.util.Log.e("AndroidUtil", "Erro ao definir imagem de perfil: " + e.getMessage());
        }
    }
    public static void passChatroomModelAsIntent(Intent intent, ChatroomModel model){
        try {
            if(intent != null && model != null){
                intent.putExtra("chatroom_model", model);
            }
        } catch (Exception e) {
            android.util.Log.e("AndroidUtil", "Erro ao passar ChatroomModel para Intent: " + e.getMessage());
        }
    }

    public static ChatroomModel getChatroomModelFromIntent(Intent intent){
        try {
            if(intent == null){
                return null;
            }
            return (ChatroomModel) intent.getSerializableExtra("chatroom_model");
        } catch (Exception e) {
            android.util.Log.e("AndroidUtil", "Erro ao obter ChatroomModel do Intent: " + e.getMessage());
            return null;
        }
    }
}
