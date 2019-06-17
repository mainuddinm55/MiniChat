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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import club.learncode.minichat.R;
import club.learncode.minichat.activity.ChatActivity;
import club.learncode.minichat.adapter.ChatAdapter;
import club.learncode.minichat.model.Chat;
import club.learncode.minichat.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements ChatAdapter.ItemClickListener {

    private DatabaseReference chatRef;

    private List<Chat> chatList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private Context context;
    private ProgressBar progressBar;
    private TextView noChatTextView;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        chatRef = rootRef.child("Chat").child(currentUser.getUid());

        RecyclerView chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        noChatTextView = view.findViewById(R.id.no_user_text_view);
        chatRecyclerView.setHasFixedSize(true);
        chatAdapter = new ChatAdapter();
        chatRecyclerView.setAdapter(chatAdapter);
        chatAdapter.setChatList(chatList);
        chatAdapter.setItemClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        chatList.clear();
        progressBar.setVisibility(View.VISIBLE);
        Query query = chatRef.orderByChild("timestamp");

        query.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                chatList.add(chat);
                chatAdapter.notifyDataSetChanged();
                toggleNoData();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void toggleNoData() {
        progressBar.setVisibility(View.GONE);
        if (chatList.size() > 0) {
            noChatTextView.setVisibility(View.GONE);
        } else {
            noChatTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClicked(User user) {
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_USER, user);
        startActivity(chatIntent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }
}
