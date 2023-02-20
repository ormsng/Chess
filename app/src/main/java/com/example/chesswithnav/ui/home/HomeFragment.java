package com.example.chesswithnav.ui.home;


import static com.example.chesswithnav.api.inviteDialog.popupInviteDialog;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.chesswithnav.api.GameApi;
import com.example.chesswithnav.api.TextChangedListener;
import com.example.chesswithnav.api.inviteDialog;
import com.example.chesswithnav.databinding.FragmentHomeBinding;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private FirebaseDatabase database;
    private DatabaseReference usersRef = null;
    private static DatabaseReference gamesRef;

    private TextView main_LBL_title;
    private AppCompatEditText main_ET_text;
    private MaterialButton main_BTN_update;
    private FragmentHomeBinding binding;
    boolean exists = false;
    public static String enemyId = "";
    public static String enemyName = "";
    boolean firstRun = true;
    public static int regCounter = 0;
    public static String ID = "";
    public static boolean active = false;
    public static boolean passive = false;
    public static boolean inGame = false;
    public static String finalEnemyName;
    //private GameApi gameApi;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        gamesRef = database.getReference("games");
        //GameApi gameApi = GameApi.getInstance();

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        findViews();
        initViews();
        if (firstRun == true) registerToInvites();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViews() {
        addEventListeners();
        main_LBL_title.setText("Hi, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
    }

    public void addEventListeners() {
        main_BTN_update.setOnClickListener(v -> {
            setTitle(main_ET_text.getText().toString());
        });

        main_ET_text.addTextChangedListener(new TextChangedListener<EditText>(main_ET_text) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                ID = "";
                enemyName = String.valueOf(target.getText());
                getIdByUserName(enemyName);
                //gameApi.getIdByUserName(String.valueOf(target.getText()));
            }
        });
    }

    private void findViews() {
        main_LBL_title = binding.mainLBLTitle;
        main_ET_text = binding.mainETText;
        main_BTN_update = binding.mainBTNInvites;
    }

    public void registerToInvites() {
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("invites").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                regCounter++;
                popupInviteDialog(getActivity());
                ArrayList MyInvites = (ArrayList) snapshot.getValue();
                if (snapshot.getValue() != null)
                    enemyName = (String) MyInvites.get(MyInvites.size() - 1);
                enemyId = getIdByUserName(enemyName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        firstRun = false;
    }

    private String getIdByUserName(String enemyName) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    for (DataSnapshot a : ds.getChildren()) {
                        if (a.getValue().equals(HomeFragment.enemyName)) {
                            enemyId = (String) ds.getKey();
                            ID = (String) ds.getKey();
                        }
                    }
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return ID;

    }

    @SuppressLint("RestrictedApi")
    private void setTitle(String title) {
        enemyId = ID;
        if (ID.equals("") || main_ET_text.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "This user doesn't exist!", Toast.LENGTH_SHORT).show();
            return;
        }
        exists = true;
        DatabaseReference pendingRef = usersRef.child(ID).child("invites");
        pendingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList arr = (ArrayList) snapshot.getValue();
                if (arr != null)
                    arr.add(arr.size(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                else {
                    arr = new ArrayList<>();
                    arr.add(0, FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                }
                usersRef.child(ID).child("invites").setValue(arr);
                Toast.makeText(getApplicationContext(), "Invite to " + enemyName + " sent!", Toast.LENGTH_SHORT).show();
                waitForAccept();
                active = true;
                passive = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void waitForAccept() {
        gamesRef.child(enemyId + FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() { // for all time data load.
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    inGame = true;
                    finalEnemyName = enemyName;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Canceled", "Failed to read value.", error.toException());
            }
        });
    }

    @SuppressLint("RestrictedApi")
    public static void newGame() {
        gamesRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + enemyId).child("CHESS_MATRIX").setValue("RNBQKBNRPPPPPPPPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXpppppppprnbqkbnr");
        passive = true;
        active = false;
    }

}