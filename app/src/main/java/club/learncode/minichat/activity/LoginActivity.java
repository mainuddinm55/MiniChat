package club.learncode.minichat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import club.learncode.minichat.R;
import club.learncode.minichat.database.DbHandler;
import club.learncode.minichat.listener.DataFetchListener;
import club.learncode.minichat.model.User;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private static final int RC_GOOGLE_SIGN_IN = 100;
    private FirebaseAuth firebaseAuth;
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginBtn;
    private TextView singUpTextView;
    private ImageButton googleBtn, twitterBtn, facebookBtn;

    private GoogleSignInClient mGoogleSignInClient;

    private static final String EMAIL = "email";
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private AlertDialog alertDialog;
    private DbHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        initView();
        dbHandler = new DbHandler();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        AppEventsLogger.activateApp(getApplication());
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        emailEditText.addTextChangedListener(this);
        passwordEditText.addTextChangedListener(this);

        loginBtn.setOnClickListener(this);
        singUpTextView.setOnClickListener(this);
        facebookBtn.setOnClickListener(this);
        googleBtn.setOnClickListener(this);
        twitterBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initView() {
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginBtn = findViewById(R.id.login_button);
        singUpTextView = findViewById(R.id.signup_text_view);
        facebookBtn = findViewById(R.id.facebook_btn);
        googleBtn = findViewById(R.id.google_btn);
        twitterBtn = findViewById(R.id.twitter_btn);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                if (isValid()) {
                    loginUser();
                }
                break;
            case R.id.signup_text_view:
                gotoRegisterActivity();
                break;
            case R.id.facebook_btn:
                loginManager.logInWithReadPermissions(this, Arrays.asList(EMAIL));
                break;
            case R.id.google_btn:
                googleSignIn();
                break;
            case R.id.twitter_btn:
                break;
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(emailEditText.getText())) {
            emailLayout.setError("Email Required");
            emailLayout.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
            emailLayout.setError("Email Invalid");
            emailLayout.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(passwordEditText.getText())) {
            passwordLayout.setError("Password Required");
            passwordLayout.requestFocus();
            return false;
        }
        return true;
    }

    private void loginUser() {
        showDialog();
        firebaseAuth.signInWithEmailAndPassword(
                emailEditText.getText().toString(),
                passwordEditText.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dismissDialog();
                if (task.isSuccessful()) {
                    Log.e("Username", task.getResult().getUser().getDisplayName());
                    Toasty.success(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                    gotoMainActivity();
                } else {
                    String message = "Email or Password Invalid";
                    if (task.getException() != null) {
                        message = task.getException().getLocalizedMessage();
                    }
                    Toasty.error(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        showDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString());
                            dbHandler.addUser(user, new DataFetchListener<Task<Void>>() {
                                @Override
                                public void showProgress() {

                                }

                                @Override
                                public void onComplete(Task<Void> voidTask) {
                                    dismissDialog();
                                    if (voidTask.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toasty.success(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                        gotoMainActivity();
                                    } else {
                                        if (voidTask.getException() instanceof FirebaseAuthUserCollisionException) {
                                            Toasty.error(LoginActivity.this, "An account already exists with the same email address", Toast.LENGTH_LONG).show();
                                        } else {
                                            voidTask.getException().printStackTrace();
                                            Toasty.error(LoginActivity.this, "Some error occurred, try again", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    dismissDialog();
                                    Toasty.error(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                                }
                            });
                        } else {
                            dismissDialog();
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toasty.error(LoginActivity.this, "An account already exists with the same email address", Toast.LENGTH_LONG).show();
                            } else {
                                task.getException().printStackTrace();
                                Toasty.error(LoginActivity.this, "Some error occurred, try again", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("Facebook Token", "handleFacebookAccessToken:" + token);
        showDialog();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            User user = new User(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl().toString());
                            dbHandler.addUser(user, new DataFetchListener<Task<Void>>() {
                                @Override
                                public void showProgress() {

                                }

                                @Override
                                public void onComplete(Task<Void> voidTask) {
                                    dismissDialog();
                                    if (voidTask.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toasty.success(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                        gotoMainActivity();
                                    } else {
                                        if (voidTask.getException() instanceof FirebaseAuthUserCollisionException) {
                                            Toasty.error(LoginActivity.this, "An account already exists with the same email address", Toast.LENGTH_LONG).show();
                                        } else {
                                            voidTask.getException().printStackTrace();
                                            Toasty.error(LoginActivity.this, "Some error occurred, try again", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    dismissDialog();
                                    Toasty.error(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            dismissDialog();
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toasty.error(LoginActivity.this, "An account already exists with the same email address", Toast.LENGTH_LONG).show();
                            } else {
                                task.getException().printStackTrace();
                                Toasty.error(LoginActivity.this, "Some error occurred, try again", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void gotoMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void gotoRegisterActivity() {
        Intent registerIntent = new Intent(this, RegistrationActivity.class);
        startActivity(registerIntent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void showDialog() {
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Loading....")
                .build();
        alertDialog.show();
    }

    private void dismissDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}
