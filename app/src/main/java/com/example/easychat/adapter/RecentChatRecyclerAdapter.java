package com.example.easychat.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.easychat.ChatActivity;
import com.example.easychat.R;
import com.example.easychat.model.ChatroomModel;
import com.example.easychat.model.UserModel;
import com.example.easychat.utils.AndroidUtil;
import com.example.easychat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {

        /*** Gemini - Inicio***/
        if (model.isGroupChat()) {
            // LÓGICA PARA GRUPO
            holder.usernameText.setText(model.getGroupName());

            // Define o ícone do grupo (se houver)
            if (model.getGroupIcon() != null) {
                FirebaseUtil.getGroupIconStorageRef(model.getGroupIcon()).getDownloadUrl()
                        .addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Uri uri = t.getResult();
                                AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                            }
                        });
            }

            // Lógica da última mensagem para grupos
            if(model.getLastMessageSenderId() != null && !model.getLastMessageSenderId().isEmpty()){
                if (model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId())) {
                    holder.lastMessageText.setText("Você: " + model.getLastMessage());
                } else {
                    // Busca o nome de quem enviou a última mensagem
                    FirebaseUtil.allUserCollectionReference().document(model.getLastMessageSenderId()).get()
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    UserModel sender = task.getResult().toObject(UserModel.class);
                                    if(sender != null){
                                        holder.lastMessageText.setText(sender.getUsername() + ": " + model.getLastMessage());
                                    }
                                }
                            });
                }
            } else {
                holder.lastMessageText.setText("Toque para conversar");
            }


            holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

            holder.itemView.setOnClickListener(v -> {
                // Navegar para a ChatActivity (lógica para grupo)
                Intent intent = new Intent(context, ChatActivity.class);
                // Para grupos, passamos o próprio ChatroomModel
                AndroidUtil.passChatroomModelAsIntent(intent, model);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });

        } else {
            // LÓGICA PARA CHAT 1-PARA-1 (CÓDIGO ORIGINAL)
            if (model.getUserIds() != null && !model.getUserIds().isEmpty()) {
                FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                                if (otherUserModel != null && otherUserModel.getUserId() != null) {
                                    // Verifica se há última mensagem antes de acessar
                                    if (model.getLastMessageSenderId() != null && !model.getLastMessageSenderId().isEmpty()) {
                                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                                        if (lastMessageSentByMe) {
                                            holder.lastMessageText.setText("Você: " + (model.getLastMessage() != null ? model.getLastMessage() : ""));
                                        } else {
                                            holder.lastMessageText.setText(model.getLastMessage() != null ? model.getLastMessage() : "");
                                        }
                                    } else {
                                        holder.lastMessageText.setText("Toque para conversar");
                                    }

                                    FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                            .addOnCompleteListener(t -> {
                                                if (t.isSuccessful()) {
                                                    Uri uri = t.getResult();
                                                    AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                                }
                                            });

                                    holder.usernameText.setText(otherUserModel.getUsername());
                                    holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                                    holder.itemView.setOnClickListener(v -> {
                                        try {
                                            //navigate to chat activity
                                            Intent intent = new Intent(context, ChatActivity.class);
                                            AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);
                                        } catch (Exception e) {
                                            android.util.Log.e("RecentChatAdapter", "Erro ao abrir chat: " + e.getMessage());
                                            android.widget.Toast.makeText(context, "Erro ao abrir chat", android.widget.Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    holder.usernameText.setText("Usuário não encontrado");
                                    holder.lastMessageText.setText("Erro ao carregar dados");
                                    holder.lastMessageTime.setText("");
                                }
                            } else {
                                holder.usernameText.setText("Erro ao carregar");
                                holder.lastMessageText.setText("Falha na conexão");
                                holder.lastMessageTime.setText("");
                            }
                        });
            } else {
                holder.usernameText.setText("Erro: IDs inválidos");
                holder.lastMessageText.setText("Dados corrompidos");
                holder.lastMessageTime.setText("");
            }
        }
        /***gemini - fim***/
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}