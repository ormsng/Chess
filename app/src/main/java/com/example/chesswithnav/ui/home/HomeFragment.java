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
    private TextView main_LBL_score;

    private AppCompatEditText main_ET_text;
    private MaterialButton main_BTN_update;
    private FragmentHomeBinding binding;
    boolean exists = false;
    public static String enemyId = "";
    public static String enemyName = "";
    boolean firstRun = true;
    public static int regCounter = 0;

    private GameApi gameApi;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        gamesRef = database.getReference("games");
        gameApi = GameApi.getInstance();
        gameApi.updateMyScore();
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
        gameApi.updateMyScore();
        main_LBL_score.setText("SCORE:" + gameApi.getMyWins() + "/" + gameApi.getMyLosses());
    }

    public void addEventListeners() {
        main_BTN_update.setOnClickListener(v -> inviteEnemy());
        main_ET_text.addTextChangedListener(new TextChangedListener<EditText>(main_ET_text) {
            @SuppressLint("RestrictedApi")
            @Override
            public void onTextChanged(EditText target, Editable s) {
                enemyName = String.valueOf(target.getText());
                if (!gameApi.isInGame()) {
                    gameApi.setEnemyName(String.valueOf(target.getText()));
                    gameApi.getIdByUserName(gameApi.getInstance().getEnemyName());
                }

            }
        });
    }

    private void findViews() {
        main_LBL_title = binding.mainLBLTitle;
        main_ET_text = binding.mainETText;
        main_BTN_update = binding.mainBTNInvites;
        main_LBL_score = binding.mainLBLScore;
    }

    public void registerToInvites() {
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("invites").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                regCounter++;
                popupInviteDialog(getActivity());
                ArrayList MyInvites = (ArrayList) snapshot.getValue();

                if (snapshot.getValue() != null) {
                    enemyName = (String) MyInvites.get(MyInvites.size() - 1);
                    gameApi.setEnemyName((String) MyInvites.get(MyInvites.size() - 1));
                }

                enemyId = gameApi.getInstance().getIdByUserName(enemyName);
                gameApi.setEnemyId(enemyId);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        firstRun = false;
    }

    public void updateScore() {
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("wins").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                gameApi.setMyWins(String.valueOf(snapshot.getValue()));
                System.out.println("wins is" + snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("losses").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                gameApi.setMyLosses(String.valueOf(snapshot.getValue()));
                System.out.println("losses is" + snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersRef.child(gameApi.getEnemyId()).child("wins").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                gameApi.setEnemyWins(String.valueOf(snapshot.getValue()));
                System.out.println("losses enemy is" + snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersRef.child(gameApi.getEnemyId()).child("losses").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                gameApi.setEnemyLosses(String.valueOf(snapshot.getValue()));
                System.out.println("losses enemy is" + snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void inviteEnemy() {

        if (gameApi.isInGame()) {
            Toast.makeText(getApplicationContext(), "Can't invite while in a game!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gameApi.getEnemyId().equals("") || main_ET_text.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "This user doesn't exist!", Toast.LENGTH_SHORT).show();
            return;
        }

        exists = true;
        DatabaseReference pendingRef = usersRef.child(gameApi.getInstance().getEnemyId()).child("invites");
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
                usersRef.child(gameApi.getInstance().getEnemyId()).child("invites").setValue(arr);
                Toast.makeText(getApplicationContext(), "Invite to " + gameApi.getEnemyName() + " sent!", Toast.LENGTH_SHORT).show();
                waitForAccept();

                gameApi.setActive(true);
                gameApi.setPassive(false);

                updateScore();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void waitForAccept() {
        gamesRef.child(gameApi.getEnemyId() + FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() { // for all time data load.
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    gameApi.setInGame(true);
                    gameApi.setActive(true);
                    gameApi.setPassive(false);
                    //finalEnemyName = gameApi.getEnemyName();

                    gameApi.setChosenEnemyName(gameApi.getEnemyName());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Canceled", "Failed to read value.", error.toException());
            }
        });
    }
}

