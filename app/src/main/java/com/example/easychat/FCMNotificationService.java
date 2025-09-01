package com.example.easychat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMNotificationService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "chat_notifications";
    private static final String CHANNEL_NAME = "Chat Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for chat messages";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Verifica se a mensagem contém dados
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            String senderId = remoteMessage.getData().get("senderId");
            String chatroomId = remoteMessage.getData().get("chatroomId");

            // Cria e mostra a notificação
            createNotification(title, message, senderId, chatroomId);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // O token será atualizado automaticamente pelo MainActivity
    }

    private void createNotification(String title, String message, String senderId, String chatroomId) {
        // Cria o canal de notificação para Android 8.0+
        createNotificationChannel();

        // Intent para abrir o chat quando a notificação for tocada
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatroom_id", chatroomId);
        intent.putExtra("other_user_id", senderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Som padrão do sistema
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Constrói a notificação
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ícone da notificação
                .setContentTitle(title != null ? title : "Nova mensagem")
                .setContentText(message != null ? message : "Você recebeu uma mensagem")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // Mostra a notificação
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
