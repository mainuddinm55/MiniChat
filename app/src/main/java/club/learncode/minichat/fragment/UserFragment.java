package club.learncode.minichat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import club.learncode.minichat.R;
import club.learncode.minichat.activity.ChatActivity;
import club.learncode.minichat.adapter.UserAdapter;
import club.learncode.minichat.database.DbHandler;
import club.learncode.minichat.listener.DataFetchListener;
import club.learncode.minichat.model.User;


public class UserFragment extends Fragment implements UserAdapter.ItemClickListener {
    private TextView noUserTextView;
    private ProgressBar progressBar;
    private DbHandler dbHandler;
    private UserAdapter userAdapter;
    private Context context;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        noUserTextView = view.findViewById(R.id.no_user_text_view);
        RecyclerView userListRecyclerView = view.findViewById(R.id.user_list_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);

        dbHandler = new DbHandler();
        userAdapter = new UserAdapter();
        userAdapter.setItemClickListener(this);

        userListRecyclerView.setHasFixedSize(true);
        userListRecyclerView.setAdapter(userAdapter);

    }

    private void getUsers() {
        dbHandler.getUsers(new DataFetchListener<List<User>>() {
            @Override
            public void showProgress() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(List<User> users) {
                progressBar.setVisibility(View.GONE);
                userAdapter.setUserList(users);
                if (users.size() > 0) {
                    noUserTextView.setVisibility(View.GONE);
                } else {
                    noUserTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getUsers();
    }

    @Override
    public void onItemClicked(User user) {
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_USER, user);
        startActivity(chatIntent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}
