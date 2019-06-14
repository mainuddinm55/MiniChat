package club.learncode.minichat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.w3c.dom.Text;

import java.util.List;

import club.learncode.minichat.R;
import club.learncode.minichat.database.DbHandler;
import club.learncode.minichat.listener.DataFetchListener;
import club.learncode.minichat.model.Conversion;
import club.learncode.minichat.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_USER = "club.learncode.minichat.activity.EXTRA_USER";
    private User chatUser;
    private CircleImageView profileImageView;
    private TextView nameTextView;

    private ProgressBar progressBar;
    private TextView startChatTextView;
    private EditText messageEditText;

    private DbHandler dbHandler;

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

        profileImageView = findViewById(R.id.profile_image_view);
        nameTextView = findViewById(R.id.name_text_view);
        RecyclerView chatRecyclerView = findViewById(R.id.chat_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        startChatTextView = findViewById(R.id.no_conversion_text_view);
        messageEditText = findViewById(R.id.message_edit_text);
        ImageButton sendBtn = findViewById(R.id.send_btn);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chatUser = (User) bundle.getSerializable(EXTRA_USER);
            assert chatUser != null;
            updateUI(chatUser);
        }
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
        getConversions(chatUser);
    }

    private void getConversions(final User chatUser) {
        dbHandler.getConversions(chatUser, new DataFetchListener<List<Conversion>>() {
            @Override
            public void showProgress() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(List<Conversion> conversions) {
                progressBar.setVisibility(View.GONE);
                if (conversions.size() > 0) {
                    startChatTextView.setVisibility(View.GONE);
                } else {
                    startChatTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
