package club.learncode.minichat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import club.learncode.minichat.R;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        firebaseAuth = FirebaseAuth.getInstance();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupActionBarWithNavController(this,navController);
    }
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, (DrawerLayout) null);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logoutUser();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        gotoLoginActivity();
    }

    private void gotoLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}
