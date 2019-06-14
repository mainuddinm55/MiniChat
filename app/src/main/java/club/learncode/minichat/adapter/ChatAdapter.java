package club.learncode.minichat.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseUser;

import club.learncode.minichat.R;
import club.learncode.minichat.model.Conversion;
import club.learncode.minichat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends PagedListAdapter<Conversion, ChatAdapter.MessageHolder> {
    private static final int MESSAGE_TYPE_LEFT = 1;
    private static final int MESSAGE_TYPE_RIGHT = 2;
    private FirebaseUser currentUser;
    private User chatUser;

    private static DiffUtil.ItemCallback<Conversion> DIFF_UTIL = new DiffUtil.ItemCallback<Conversion>() {
        @Override
        public boolean areItemsTheSame(@NonNull Conversion oldItem, @NonNull Conversion newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Conversion oldItem, @NonNull Conversion newItem) {
            return oldItem.getMessage().equals(newItem.getMessage());
        }
    };


    public ChatAdapter(FirebaseUser currentUser, User chatUser) {
        super(DIFF_UTIL);
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
        Conversion conversion = getItem(position);
        holder.bindTo(conversion);
    }

    @Override
    public int getItemViewType(int position) {
        if (currentUser != null) {
            if (getItem(position).getSenderId().equals(currentUser.getUid())) {
                return MESSAGE_TYPE_RIGHT;
            } else {
                return MESSAGE_TYPE_LEFT;
            }
        }
        return -1;
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

        void bindTo(Conversion conversion) {
            messageTextView.setText(conversion.getMessage());
            String url;
            if (getItemViewType() == MESSAGE_TYPE_RIGHT) {
                url = currentUser.getPhotoUrl().toString();
            } else {
                url = chatUser.getPhotoUrl();
            }
            Glide.with(itemView.getContext()).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.placeholder).error(R.drawable.profile_image).into(profileImageView);

            if (getAdapterPosition() == (getCurrentList().size() - 1)) {
                if (getItem(getAdapterPosition()).isSeen()) {
                    msgSeenTextView.setText("seen");
                } else {
                    msgSeenTextView.setText("delivered");
                }
            } else {
                msgSeenTextView.setVisibility(View.GONE);
            }
        }
    }

    public interface ItemClickedListener {
        void onDownloadItemClicked(String url);
    }
}