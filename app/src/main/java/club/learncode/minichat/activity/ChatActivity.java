package club.learncode.minichat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udevel.widgetlab.TypingIndicatorView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;

import club.learncode.minichat.R;
import club.learncode.minichat.adapter.MessageAdapter;
import club.learncode.minichat.database.DbHandler;
import club.learncode.minichat.model.Message;
import club.learncode.minichat.model.User;
import club.learncode.minichat.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_USER = "club.learncode.minichat.activity.EXTRA_USER";
    private User chatUser;
    private CircleImageView profileImageView;
    private TextView nameTextView;
    private RecyclerView chatRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ProgressBar progressBar;
    private TextView startChatTextView;
    private EditText messageEditText;
    private DbHandler dbHandler;

    private MessageAdapter messageAdapter;
    private DatabaseReference messageRef;
    private ArrayList<Message> messageList = new ArrayList<>();

    private final int ITEM_LOAD_COUNT = 10;
    private int currentPage = 1;
    private String lastKey = "", prevKey = "";
    private FirebaseUser currentUser;
    private int itemPos = 0;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(null);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHandler = new DbHandler();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        messageRef = rootRef.child("Messages");
        userRef = rootRef.child("User");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileImageView = findViewById(R.id.profile_image_view);
        nameTextView = findViewById(R.id.name_text_view);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        chatRecyclerView.setHasFixedSize(true);
        final TextView lastSeenTextView = findViewById(R.id.last_seen_text_view);
        final TypingIndicatorView typingIndicatorView = findViewById(R.id.typing_indicator);
        typingIndicatorView.startDotAnimation();

        progressBar = findViewById(R.id.progress_bar);
        startChatTextView = findViewById(R.id.no_conversion_text_view);

        messageEditText = findViewById(R.id.message_edit_text);
        ImageButton sendBtn = findViewById(R.id.send_btn);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chatUser = (User) bundle.getSerializable(EXTRA_USER);
            assert chatUser != null;
            updateUI(chatUser);
            messageAdapter = new MessageAdapter(FirebaseAuth.getInstance().getCurrentUser(), chatUser);
            chatRecyclerView.setAdapter(messageAdapter);
            messageAdapter.setMessageList(messageList);

            userRef.child(chatUser.getUid()).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user.getTypingTo() != null && user.getTypingTo().equals(currentUser.getUid())) {
                        typingIndicatorView.startDotAnimation();
                        typingIndicatorView.setVisibility(View.VISIBLE);
                    } else {
                        typingIndicatorView.stopDotAnimation();
                        typingIndicatorView.setVisibility(View.GONE);
                    }
                    String online = user.getOnlineStatus();
                    if (online.equals("online")) {
                        lastSeenTextView.setText("Online");
                    } else {
                        long lastTime = Long.parseLong(online);
                        String lastSeen = TimeUtils.getTimeAgo(lastTime, getApplicationContext());
                        Log.e("Onlinestatus", "onDataChange: " + lastTime + " , " + lastSeen);
                        lastSeenTextView.setText(lastSeen);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(messageEditText.getText())) {
                    dbHandler.sendMessage(chatUser, messageEditText.getText().toString());
                    messageEditText.setText(null);
                }
            }
        });

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(chatUser.getUid());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkTypingStatus("noOne");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finishAfterTransition();
        return super.onOptionsItemSelected(item);
    }

    private void updateUI(User chatUser) {
        Glide.with(this).load(chatUser.getPhotoUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .placeholder(R.drawable.placeholder).error(R.drawable.profile_image).into(profileImageView);
        nameTextView.setText(chatUser.getDisplayName());
        getMessages(chatUser);

    }


    private void getMessages(final User chatUser) {

        final Query query = messageRef.child(currentUser.getUid())
                .child(chatUser.getUid())
                .limitToLast(currentPage * ITEM_LOAD_COUNT);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                itemPos++;
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
                String messageKey = dataSnapshot.getKey();
                if (itemPos == 1) {
                    lastKey = messageKey;
                    prevKey = messageKey;
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                messageList.remove(messageList.size() - 1);
                messageList.add(dataSnapshot.getValue(Message.class));
                messageAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
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

        messageRef.child(chatUser.getUid()).child(chatUser.getUid()).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                String messageKey = data.getKey();
                                messageRef.child(currentUser.getUid()).child(chatUser.getUid()).child(messageKey).child("seen").setValue(true);
                                messageRef.child(chatUser.getUid()).child(currentUser.getUid()).child(messageKey).child("seen").setValue(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getMoreMessages(User chatUser) {
        Query query = messageRef.child(currentUser.getUid())
                .child(chatUser.getUid())
                .orderByKey()
                .endAt(lastKey)
                .limitToLast(ITEM_LOAD_COUNT);
        Log.e("getMoreMessages: ", lastKey);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                String messageKey = dataSnapshot.getKey();

                if (!prevKey.equals(messageKey)) {
                    messageList.add(itemPos++, message);
                } else {
                    prevKey = messageKey;
                }

                if (itemPos == 1) {
                    lastKey = messageKey;
                }
                messageAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition((currentPage * ITEM_LOAD_COUNT) - ITEM_LOAD_COUNT);
                swipeRefreshLayout.setRefreshing(false);
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
    }

    private void checkTypingStatus(String type) {
        userRef.child(currentUser.getUid()).child("typingTo").setValue(type);
    }

    @Override
    public void onRefresh() {
        currentPage++;
        itemPos = 0;
        getMoreMessages(chatUser);
    }
}

