package club.learncode.minichat.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import club.learncode.minichat.listener.DataFetchListener;
import club.learncode.minichat.model.Conversion;
import club.learncode.minichat.model.Friendship;
import club.learncode.minichat.model.User;

public class DbHandler {
    private DatabaseReference userRef;
    private DatabaseReference conversionRef;
    private FirebaseUser currentUser;

    public DbHandler() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("User");
        conversionRef = rootRef.child("Conversion");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void addUser(User user, final DataFetchListener<Task<Void>> listener) {
        listener.showProgress();
        userRef.child(user.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onComplete(task);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailure(e);
            }
        });
    }

    public void getUsers(final DataFetchListener<List<User>> listener) {
        final List<User> userList = new ArrayList<>();
        listener.showProgress();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot data : dataSnapshot.getChildren()) {
                    final User user = data.getValue(User.class);
                    userList.add(user);
                }
                listener.onComplete(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public void getConversions(User chatUser, final DataFetchListener<List<Conversion>> listener) {
        final List<Conversion> conversionList = new ArrayList<>();
        listener.showProgress();
        conversionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Conversion conversion = data.getValue(Conversion.class);
                    conversionList.add(conversion);
                }
                listener.onComplete(conversionList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }
}
