package club.learncode.minichat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import club.learncode.minichat.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            gotoMainActivity();
        } else {
            gotoLoginActivity();
        }
    }

    private void gotoLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void gotoMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
