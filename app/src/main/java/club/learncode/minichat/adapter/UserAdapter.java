package club.learncode.minichat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


import java.util.ArrayList;
import java.util.List;

import club.learncode.minichat.R;
import club.learncode.minichat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {
    private List<User> userList = new ArrayList<>();
    private ItemClickListener itemClickListener;

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.people_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        User user = userList.get(position);
        holder.bindTo(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final CircleImageView profileImageView;
        private final TextView nameTextView, emailTextView;

        UserHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            emailTextView = itemView.findViewById(R.id.email_text_view);

            itemView.setOnClickListener(this);
        }

        void bindTo(User user) {
            Glide.with(itemView).load(user.getPhotoUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .placeholder(R.drawable.placeholder).error(R.drawable.profile_image).into(profileImageView);
            nameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null)
                itemClickListener.onItemClicked(userList.get(getAdapterPosition()));
        }
    }

    public interface ItemClickListener {
        void onItemClicked(User user);
    }
}
