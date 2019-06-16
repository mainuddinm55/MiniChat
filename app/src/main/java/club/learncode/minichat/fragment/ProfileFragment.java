package club.learncode.minichat.fragment;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import club.learncode.minichat.R;
import club.learncode.minichat.activity.LoginActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private static final int GALLERY_PICK = 10;
    private boolean isEditing = false;
    private Context context;
    private FirebaseUser user;
    private AlertDialog alertDialog;
    private ImageButton editBtn;
    private EditText nameEditText;
    private TextView nameTextView;
    private TextView emailTextView;
    private CircleImageView profileImageView;

    public ProfileFragment() {
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        profileImageView = view.findViewById(R.id.profile_image_view);
        nameTextView = view.findViewById(R.id.name_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        ImageButton uploadImageBtn = view.findViewById(R.id.upload_image_btn);
        nameEditText = view.findViewById(R.id.name_edit_text);
        editBtn = view.findViewById(R.id.edit_btn);
        Button logoutBtn = view.findViewById(R.id.logout_btn);

        user = FirebaseAuth.getInstance().getCurrentUser();
        String url = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
        Glide.with(view.getContext()).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.placeholder).error(R.drawable.profile_image).into(profileImageView);
        nameTextView.setText(user.getDisplayName());
        nameEditText.setText(user.getDisplayName());
        emailTextView.setText(user.getEmail());

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditing) {
                    isEditing = false;
                    nameEditText.setEnabled(false);
                    editBtn.setImageResource(R.drawable.ic_edit_black_24dp);
                    if (TextUtils.isEmpty(nameEditText.getText())) {
                        nameEditText.setError("Enter name");
                        nameEditText.requestFocus();
                        return;
                    }
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nameEditText.getText().toString()).build();
                    showDialog();
                    user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideDialog();
                            if (task.isSuccessful()) {
                                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                DatabaseReference userRef = rootRef.child("User").child(user.getUid());
                                userRef.child("photoUrl").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                                updateUI();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideDialog();
                        }
                    });
                } else {
                    isEditing = true;
                    nameEditText.setEnabled(true);
                    nameEditText.setSelection(nameEditText.getText().length());
                    editBtn.setImageResource(R.drawable.ic_check_black_24dp);
                }
            }
        });


        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void logoutUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        gotoLoginActivity();
    }

    private void gotoLoginActivity() {
        Intent loginIntent = new Intent(context, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void updateUI() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        String url = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
        Glide.with(context).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.placeholder).error(R.drawable.profile_image).into(profileImageView);
        nameTextView.setText(user.getDisplayName());
        nameEditText.setText(user.getDisplayName());
        emailTextView.setText(user.getEmail());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(context, this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                final Uri resultUri = result.getUri();
                final File filePath = new File(resultUri.getPath());

                Bitmap bitmap = null;
                try {
                    bitmap = new Compressor(context)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] bytes = baos.toByteArray();
                final StorageReference imagePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(user.getUid() + ".jpeg");
                showDialog();
                imagePath.putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isComplete() && task.isSuccessful()) {
                            Log.e("File Upload", "onComplete: " + imagePath.getDownloadUrl());
                            imagePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri uri = task.getResult();
                                    Log.e("After complete", "onComplete: " + uri);
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri).build();
                                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            hideDialog();
                                            if (task.isSuccessful()) {
                                                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                                DatabaseReference userRef = rootRef.child("User").child(user.getUid());
                                                userRef.child("photoUrl").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                                                updateUI();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            hideDialog();
                                        }
                                    });
                                }
                            });
                        } else {
                            hideDialog();
                        }
                    }
                });

            }

        }
    }


    private void showDialog() {
        alertDialog = new SpotsDialog.Builder()
                .setContext(context)
                .setMessage("Loading...")
                .build();
        alertDialog.show();
    }

    private void hideDialog() {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
