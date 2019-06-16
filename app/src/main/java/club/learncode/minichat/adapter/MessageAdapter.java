package club.learncode.minichat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import club.learncode.minichat.R;
import club.learncode.minichat.model.Message;
import club.learncode.minichat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {
    private static final int MESSAGE_TYPE_LEFT = 1;
    private static final int MESSAGE_TYPE_RIGHT = 2;
    private FirebaseUser currentUser;
    private User chatUser;
    private List<Message> messageList = new ArrayList<>();


    public MessageAdapter(FirebaseUser currentUser, User chatUser) {
        this.currentUser = currentUser;
        this.chatUser = chatUser;
    }


    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == MESSAGE_TYPE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
        } else if (viewType == MESSAGE_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
        }
        assert view != null;
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bindTo(message);
    }

    @Override
    public int getItemViewType(int position) {
        if (currentUser != null) {
            if (messageList.get(position).getFrom().equals(currentUser.getUid())) {
                return MESSAGE_TYPE_RIGHT;
            } else {
                return MESSAGE_TYPE_LEFT;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged();
    }

    class MessageHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImageView;
        TextView messageTextView, msgSeenTextView;

        MessageHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            messageTextView = itemView.findViewById(R.id.show_message);
            msgSeenTextView = itemView.findViewById(R.id.msg_seen_text_view);

        }

        void bindTo(Message message) {
            messageTextView.setText(message.getMessage());
            String url = null;
            if (getItemViewType() == MESSAGE_TYPE_RIGHT) {
                url = currentUser.getPhotoUrl()!=null?currentUser.getPhotoUrl().toString():null;
            } else {
                url = chatUser.getPhotoUrl()!=null?chatUser.getPhotoUrl():null;
            }
            Glide.with(itemView.getContext()).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.placeholder).error(R.drawable.profile_image).into(profileImageView);

            if (getAdapterPosition() == (messageList.size() - 1)) {
                if (messageList.get(getAdapterPosition()).isSeen()) {
                    msgSeenTextView.setText("seen");
                } else {
                    msgSeenTextView.setText("delivered");
                }
            } else {
                msgSeenTextView.setVisibility(View.GONE);
            }
        }
    }

}