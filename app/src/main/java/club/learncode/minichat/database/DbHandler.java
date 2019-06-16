package club.learncode.minichat.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import club.learncode.minichat.listener.DataFetchListener;
import club.learncode.minichat.model.User;

public class DbHandler {
    private final DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference messageRef;
    private FirebaseUser currentUser;

    public DbHandler() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("User");
        messageRef = rootRef.child("Messages");
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
                    if (!user.getUid().equals(currentUser.getUid())) {
                        userList.add(user);
                    }
                }
                listener.onComplete(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }


    public void sendMessage(User chatUser, String message) {
        String currentUserRef = "Messages/" + currentUser.getUid() + "/" + chatUser.getUid();
        String chatUserRef = "Messages/" + chatUser.getUid() + "/" + currentUser.getUid();
        DatabaseReference userMessagePushRef = rootRef.child("Messages").child(currentUser.getUid())
                .child(chatUser.getUid()).push();
        String pushId = userMessagePushRef.getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message", message);
        hashMap.put("seen", false);
        hashMap.put("sendTime", System.currentTimeMillis());
        hashMap.put("from", currentUser.getUid());

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put(currentUserRef + "/" + pushId, hashMap);
        messageMap.put(chatUserRef + "/" + pushId, hashMap);

        rootRef.child("Chat").child(currentUser.getUid()).child(chatUser.getUid()).child("seen").setValue(true);
        rootRef.child("Chat").child(currentUser.getUid()).child(chatUser.getUid()).child("timestamp").setValue(ServerValue.TIMESTAMP);
        rootRef.child("Chat").child(currentUser.getUid()).child(chatUser.getUid()).child("userid").setValue(chatUser.getUid());

        rootRef.child("Chat").child(chatUser.getUid()).child(currentUser.getUid()).child("seen").setValue(false);
        rootRef.child("Chat").child(chatUser.getUid()).child(currentUser.getUid()).child("timestamp").setValue(ServerValue.TIMESTAMP);
        rootRef.child("Chat").child(chatUser.getUid()).child(currentUser.getUid()).child("userid").setValue(currentUser.getUid());


        rootRef.updateChildren(messageMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

            }
        });
    }
}
