package com.example.chesswithnav.api;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.example.chesswithnav.ui.home.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameApi {

    public boolean active = false;
    public boolean passive = false;
    public boolean inGame = false;
    public boolean enemyExists = false;

    public String ID;
    public String enemyId;

    public String enemyName;
    public String chosenEnemyName;
    public String chosenEnemyId;

    public String enemyLosses;

    public String getMyLosses() {
        return myLosses;
    }

    public void setMyLosses(String myLosses) {
        this.myLosses = myLosses;
    }

    public String getMyWins() {
        return myWins;
    }

    public void setMyWins(String myWins) {
        this.myWins = myWins;
    }

    public String enemyWins;

    public String myLosses;
    public String myWins;

    public String getEnemyWins() {
        return enemyWins;
    }

    public void setEnemyWins(String enemyWins) {
        this.enemyWins = enemyWins;
    }

    public String getEnemyLosses() {
        return enemyLosses;
    }

    public void setEnemyLosses(String enemyLosses) {
        this.enemyLosses = enemyLosses;
    }

    public String getChosenEnemyId() {
        return chosenEnemyId;
    }

    public void setChosenEnemyId(String chosenEnemyId) {
        this.chosenEnemyId = chosenEnemyId;
    }

    public String getChosenEnemyName() {
        return chosenEnemyName;
    }

    public void setChosenEnemyName(String chosenEnemyName) {
        this.chosenEnemyName = chosenEnemyName;
    }

    public String getEnemyName() {
        return enemyName;
    }

    public void setEnemyName(String enemyName) {
        this.enemyName = enemyName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    private FirebaseDatabase database;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public String getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(String enemyId) {
        this.enemyId = enemyId;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    private DatabaseReference usersRef = null;
    private static DatabaseReference gamesRef;

    // Static variable reference of single_instance
    // of type Singleton
    private static GameApi single_instance = null;

    // Declaring a variable of type String
    public String s;

    // Constructor
    // Here we will be creating private constructor
    // restricted to this class itself
    private GameApi() {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        gamesRef = database.getReference("games");
        setEnemyId("");
        setMyWins("0");
        setMyLosses("0");
        s = "Hello I am a string part of Singleton class";
    }

    // Static method
    // Static method to create instance of Singleton class
    public static GameApi getInstance() {
        if (single_instance == null)
            single_instance = new GameApi();

        return single_instance;
    }

    public boolean isEnemyExists() {
        return enemyExists;
    }

    public void setEnemyExists(boolean enemyExists) {
        this.enemyExists = enemyExists;
    }

    public String getIdByUserName(String enemyName) {
        setEnemyExists(false);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    for (DataSnapshot a : ds.getChildren()) {
                        if (a.getValue().equals(enemyName)) {
                            //enemyId = (String) ds.getKey();
                            setEnemyId((String) ds.getKey());
                            setEnemyExists(true);
                        }
                    }
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        if (!isEnemyExists())
            setEnemyId("");
        setEnemyExists(false);
        return ID;
    }

    public void newGame() {
        updateScore();
        gamesRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + getInstance().getEnemyId()).child("CHESS_MATRIX").setValue("RNBQKBNRPPPPPPPPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXpppppppprnbqkbnr");
        getInstance().setPassive(true);
        getInstance().setActive(false);

    }

    public void updateMyScore(){
            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("wins").addValueEventListener(new ValueEventListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    getInstance().setMyWins(String.valueOf(snapshot.getValue()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("losses").addValueEventListener(new ValueEventListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    getInstance().setMyLosses(String.valueOf(snapshot.getValue()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

    public void updateScore() {
        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("wins").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                getInstance().setMyWins(String.valueOf(snapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("losses").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                getInstance().setMyLosses(String.valueOf(snapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersRef.child(getInstance().getEnemyId()).child("wins").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                getInstance().setEnemyWins(String.valueOf(snapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersRef.child(getInstance().getEnemyId()).child("losses").addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                getInstance().setEnemyLosses(String.valueOf(snapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }




}