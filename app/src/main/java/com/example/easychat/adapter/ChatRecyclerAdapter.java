package com.example.easychat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.easychat.ChatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.easychat.R;
import com.example.easychat.model.ChatMessageModel;
import com.example.easychat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    Context context;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {

        /*** Gemini - Inicio***/
        // Lógica principal para separar mensagens de texto e de imagem
        boolean isSentByMe = model.getSenderId().equals(FirebaseUtil.currentUserId());

        if (isSentByMe) {
            // Se a mensagem foi enviada por mim (lado direito)
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            handleSentMessage(holder, model);
        } else {
            // Se a mensagem foi recebida (lado esquerdo)
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            handleReceivedMessage(holder, model);
        }
        /***gemini - fim***/
    }

    /*** Gemini - Inicio***/
    private void handleSentMessage(ChatModelViewHolder holder, ChatMessageModel model) {
        // Log para debug - verificar o modelo recebido
        Log.d("ChatAdapter", "handleSentMessage - Model data:");
        Log.d("ChatAdapter", "  - message: " + model.getMessage());
        Log.d("ChatAdapter", "  - messageType: " + model.getMessageType());
        Log.d("ChatAdapter", "  - senderId: " + model.getSenderId());

        // Mostra a mensagem de acordo com o tipo (texto ou imagem)
        if ("TEXT".equals(model.getMessageType())) {
            holder.rightChatTextview.setVisibility(View.VISIBLE);
            holder.rightChatImageView.setVisibility(View.GONE);
            holder.rightChatTextview.setText(model.getMessage());
        } else { // IMAGE ou VIDEO
            holder.rightChatTextview.setVisibility(View.GONE);
            holder.rightChatImageView.setVisibility(View.VISIBLE);

            // Log para debug
            String imageUrl = model.getMessage();
            Log.d("ChatAdapter", "Loading sent image: " + imageUrl);

            Glide.with(context).load(imageUrl)
                    .apply(new RequestOptions()
                            .override(800, 800)
                            .placeholder(null) // Remove placeholder
                            .error(null) // Remove error image
                            .timeout(30000) // 30 segundos de timeout
                            .skipMemoryCache(false) // Usa cache de memória
                    )
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("ChatAdapter", "Failed to load sent image: " + imageUrl, e);
                            // Mostra um ícone de erro em vez da bolinha azul
                            holder.rightChatImageView.setImageResource(android.R.drawable.ic_menu_report_image);
                            return true; // Consome o erro
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("ChatAdapter", "Sent image loaded successfully: " + imageUrl);
                            return false;
                        }
                    })
                    .into(holder.rightChatImageView);
        }

        // Lógica para os Ticks de status
        holder.statusIcon.setVisibility(View.VISIBLE);
        if (model.getMessageStatus() != null) {
            switch (model.getMessageStatus()) {
                case "read":
                    holder.statusIcon.setImageResource(R.drawable.ic_check_read);
                    break;
                case "delivered":
                    holder.statusIcon.setImageResource(R.drawable.ic_check_delivered);
                    break;
                default: // "sent"
                    holder.statusIcon.setImageResource(R.drawable.ic_check_sent);
                    break;
            }
        }
    }

    private void handleReceivedMessage(ChatModelViewHolder holder, ChatMessageModel model) {
        // Log para debug - verificar o modelo recebido
        Log.d("ChatAdapter", "handleReceivedMessage - Model data:");
        Log.d("ChatAdapter", "  - message: " + model.getMessage());
        Log.d("ChatAdapter", "  - messageType: " + model.getMessageType());
        Log.d("ChatAdapter", "  - senderId: " + model.getSenderId());

        // Mostra a mensagem de acordo com o tipo (texto ou imagem)
        if ("TEXT".equals(model.getMessageType())) {
            holder.leftChatTextview.setVisibility(View.VISIBLE);
            holder.leftChatImageView.setVisibility(View.GONE);
            holder.leftChatTextview.setText(model.getMessage());
        } else { // IMAGE ou VIDEO
            holder.leftChatTextview.setVisibility(View.GONE);
            holder.leftChatImageView.setVisibility(View.VISIBLE);

            // Log para debug
            String imageUrl = model.getMessage();
            Log.d("ChatAdapter", "Loading image: " + imageUrl);

            Glide.with(context).load(imageUrl)
                    .apply(new RequestOptions()
                            .override(800, 800)
                            .placeholder(null) // Remove placeholder
                            .error(null) // Remove error image
                            .timeout(30000) // 30 segundos de timeout
                            .skipMemoryCache(false) // Usa cache de memória
                    )
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("ChatAdapter", "Failed to load image: " + imageUrl, e);
                            // Mostra um ícone de erro em vez da bolinha azul
                            holder.leftChatImageView.setImageResource(android.R.drawable.ic_menu_report_image);
                            return true; // Consome o erro
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("ChatAdapter", "Image loaded successfully: " + imageUrl);
                            return false;
                        }
                    })
                    .into(holder.leftChatImageView);
        }
        // Esconde o ícone de status para mensagens recebidas
        holder.statusIcon.setVisibility(View.GONE);
    }
    /***gemini - fim***/

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextview, rightChatTextview;
        ImageView statusIcon;

        /*** Gemini - Inicio***/
        ImageView leftChatImageView, rightChatImageView;
        /***gemini - fim***/

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            statusIcon = itemView.findViewById(R.id.message_status_icon);

            /*** Gemini - Inicio***/
            leftChatImageView = itemView.findViewById(R.id.left_chat_imageview);
            rightChatImageView = itemView.findViewById(R.id.right_chat_imageview);
            /***gemini - fim***/
        }
    }
}