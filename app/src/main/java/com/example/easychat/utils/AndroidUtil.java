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
       Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public static void passUserModelAsIntent(Intent intent, UserModel model){
       intent.putExtra("username",model.getUsername());
       intent.putExtra("phone",model.getPhone());
       intent.putExtra("userId",model.getUserId());
        intent.putExtra("fcmToken",model.getFcmToken());

    }

    public static UserModel getUserModelFromIntent(Intent intent){
        UserModel userModel = new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setFcmToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }
    public static void passChatroomModelAsIntent(Intent intent, ChatroomModel model){
        intent.putExtra("chatroom_model", model);
    }

    public static ChatroomModel getChatroomModelFromIntent(Intent intent){
        return (ChatroomModel) intent.getSerializableExtra("chatroom_model");
    }
}
