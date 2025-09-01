package com.example.easychat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.example.easychat.adapter.ChatRecyclerAdapter;
import com.example.easychat.model.ChatMessageModel;
import com.example.easychat.model.ChatroomModel;
import com.example.easychat.model.UserModel;
import com.example.easychat.utils.AndroidUtil;
import com.example.easychat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageException;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    /*** Gemini - Inicio - Variáveis que faltavam***/
    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;
    ImageButton attachFileBtn;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri selectedImageUri;
    /***gemini - fim***/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inicializa o launcher para pegar a imagem da galeria
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            sendMessageWithImage(selectedImageUri);
                        }
                    }
                }
        );

        /*** Gemini - Inicio - Inicialização das Views que faltavam***/
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);
        attachFileBtn = findViewById(R.id.attach_file_btn);
        /***gemini - fim***/

        backBtn.setOnClickListener((v) -> {
            onBackPressed();
        });

        // Lógica para diferenciar chat de grupo e individual
        chatroomModel = AndroidUtil.getChatroomModelFromIntent(getIntent());

        if (chatroomModel != null && chatroomModel.isGroupChat()) {
            // É UM GRUPO
            chatroomId = chatroomModel.getChatroomId();
            otherUsername.setText(chatroomModel.getGroupName());

            if(chatroomModel.getGroupIcon() != null){
                FirebaseUtil.getGroupIconStorageRef(chatroomModel.getChatroomId()).getDownloadUrl()
                        .addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Uri uri = t.getResult();
                                AndroidUtil.setProfilePic(this, uri, imageView);
                            }
                        });
            }
        } else {
            // É UM CHAT 1-PARA-1
            otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
            chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());
            otherUsername.setText(otherUser.getUsername());

            FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                    .addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            Uri uri = t.getResult();
                            AndroidUtil.setProfilePic(this, uri, imageView);
                        }
                    });
        }

        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty())
                return;
            sendMessageToUser(message);
        }));

        attachFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        getOrCreateChatroom();
        setupChatRecyclerView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
        markMessagesAsRead();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    void setupChatRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter = new ChatRecyclerAdapter(options, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    void sendMessageToUser(String message) {
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel, SetOptions.merge());

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now(), "TEXT", "sent");
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageInput.setText("");
                        // Notificação automática via LocalNotificationService
                    }
                });
    }

    void sendMessageWithImage(Uri imageUri) {
        String fileName = "img_" + System.currentTimeMillis();
        AndroidUtil.showToast(this, "Enviando imagem...");

        // Verifica se o chatroomId é válido
        if (chatroomId == null || chatroomId.isEmpty()) {
            AndroidUtil.showToast(this, "Erro: ID do chat não válido.");
            return;
        }

        StorageReference storageReference = FirebaseUtil.getChatImageStorageRef(chatroomId, fileName);

        storageReference.putFile(imageUri)
                .addOnProgressListener(snapshot -> {
                    // Mostra progresso do upload
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    AndroidUtil.showToast(this, "Enviando: " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Upload bem-sucedido
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // Log para debug
                        Log.d("ChatActivity", "Image upload successful. URL: " + downloadUrl);
                        Log.d("ChatActivity", "Chatroom ID: " + chatroomId);

                        ChatMessageModel chatMessageModel = new ChatMessageModel(downloadUrl, FirebaseUtil.currentUserId(), Timestamp.now(), "IMAGE", "sent");

                        // Log para debug - verificar o modelo antes de salvar
                        Log.d("ChatActivity", "ChatMessageModel created:");
                        Log.d("ChatActivity", "  - message: " + chatMessageModel.getMessage());
                        Log.d("ChatActivity", "  - messageType: " + chatMessageModel.getMessageType());
                        Log.d("ChatActivity", "  - senderId: " + chatMessageModel.getSenderId());

                        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        Log.d("ChatActivity", "Image message saved to Firestore successfully");
                                        chatroomModel.setLastMessageTimestamp(Timestamp.now());
                                        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
                                        chatroomModel.setLastMessage("📷 Foto");
                                        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel, SetOptions.merge());
                                        AndroidUtil.showToast(this, "Imagem enviada com sucesso!");

                                        // Notificação automática via LocalNotificationService
                                    } else {
                                        Log.e("ChatActivity", "Failed to save image message to Firestore", task1.getException());
                                        AndroidUtil.showToast(this, "Erro ao salvar mensagem da imagem.");
                                    }
                                });
                    }).addOnFailureListener(e -> {
                        AndroidUtil.showToast(this, "Erro ao obter URL da imagem: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    // Trata diferentes tipos de erro
                    String errorMessage = "Falha no envio da imagem.";
                    if (e instanceof StorageException) {
                        StorageException storageException = (StorageException) e;
                        switch (storageException.getErrorCode()) {
                            case StorageException.ERROR_OBJECT_NOT_FOUND:
                                errorMessage = "Local de armazenamento não encontrado.";
                                break;
                            case StorageException.ERROR_QUOTA_EXCEEDED:
                                errorMessage = "Quota de armazenamento excedida.";
                                break;
                            case StorageException.ERROR_INVALID_CHECKSUM:
                                errorMessage = "Arquivo corrompido.";
                                break;
                            default:
                                errorMessage = "Erro de armazenamento: " + storageException.getErrorCode();
                                break;
                        }
                    }
                    AndroidUtil.showToast(this, errorMessage);
                    e.printStackTrace();
                });
    }

    void getOrCreateChatroom() {
        if (chatroomModel == null) {
            chatroomModel = new ChatroomModel(
                    chatroomId,
                    Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                    Timestamp.now(),
                    ""
            );
        }
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel, SetOptions.merge());
    }

    void markMessagesAsRead() {
        if(otherUser != null){
            Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                    .whereEqualTo("senderId", otherUser.getUserId())
                    .whereNotEqualTo("messageStatus", "read");

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        document.getReference().update("messageStatus", "read");
                    }
                }
            });
        }
    }

        // Notificações agora são automáticas via LocalNotificationService
    // Não precisamos mais enviar manualmente
    void callApi(JSONObject jsonObject){
        //...
    }
}