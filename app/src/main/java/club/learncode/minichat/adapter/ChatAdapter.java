package club.learncode.minichat.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import club.learncode.minichat.R;
import club.learncode.minichat.model.Chat;
import club.learncode.minichat.model.Message;
import club.learncode.minichat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    private List<Chat> chatList = new ArrayList<Chat>();
    private DatabaseReference userRef;
    private DatabaseReference messageRef;
    private ItemClickListener itemClickListener;

    public ChatAdapter() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("User");
        messageRef = rootRef.child("Messages").child(currentUser.getUid());
    }

    @NonNull
    @Override

    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.people_row_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull final ChatHolder holder, int position) {
        final Chat chat = chatList.get(position);
        messageRef.child(chat.getUserid()).limitToLast(1)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        holder.setMessage(message);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        userRef.child(chat.getUserid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                holder.setUser(user);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemClickListener != null)
                            itemClickListener.onItemClicked(user);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        notifyDataSetChanged();
    }

    public void setChatList(List<Chat> chatList) {
        this.chatList = chatList;
        notifyDataSetChanged();
    }

    class ChatHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImageView;
        private final TextView nameTextView, emailTextView;
        private final ImageView onlineImageView;

        ChatHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            emailTextView = itemView.findViewById(R.id.email_text_view);
            onlineImageView = itemView.findViewById(R.id.online_image_view);
        }

        void setMessage(Message message) {
            emailTextView.setText(message.getMessage());
            if (!message.isSeen()) {
                emailTextView.setTypeface(emailTextView.getTypeface(), Typeface.BOLD);
            } else {
                emailTextView.setTypeface(emailTextView.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setUser(User user) {
            Glide.with(itemView).load(user.getPhotoUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .placeholder(R.drawable.placeholder).error(R.drawable.profile_image).into(profileImageView);
            nameTextView.setText(user.getDisplayName());
            if (user.getOnlineStatus().equals("online")) {
                onlineImageView.setVisibility(View.VISIBLE);
            } else {
                onlineImageView.setVisibility(View.GONE);
            }
        }

    }

    public interface ItemClickListener {
        void onItemClicked(User user);
    }
}
