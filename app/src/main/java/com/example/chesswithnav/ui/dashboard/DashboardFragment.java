package com.example.chesswithnav.ui.dashboard;

import static com.example.chesswithnav.api.LossDialog.popupLossDialog;
import static com.example.chesswithnav.api.WinDialog.popupWinDialog;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chesswithnav.api.GameApi;
import com.example.chesswithnav.databinding.FragmentDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class DashboardFragment extends Fragment {

    private static TextView[][] CHESS_MATRIX;
    private static String[][] CHESS_MATRIX_STR = {{"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}};
    private static int[] move_from = {-1, -1};
    private static int[] move_to = {-1, -1};
    static HashMap<String, String> charToSymbol = new HashMap<String, String>();
    private FragmentDashboardBinding binding;
    private GameApi gameApi;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        gameApi = GameApi.getInstance();

        charToSymbol.put("p", "♟");
        charToSymbol.put("P", "♟");
        charToSymbol.put("K", "♚");
        charToSymbol.put("k", "♚");
        charToSymbol.put("Q", "♛");
        charToSymbol.put("q", "♛");
        charToSymbol.put("N", "♞");
        charToSymbol.put("n", "♞");
        charToSymbol.put("B", "♝");
        charToSymbol.put("b", "♝");
        charToSymbol.put("R", "♜");
        charToSymbol.put("r", "♜");
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        CHESS_MATRIX = new TextView[][]{
                {binding.gameTXTR0c0, binding.gameTXTR0c1, binding.gameTXTR0c2, binding.gameTXTR0c3, binding.gameTXTR0c4, binding.gameTXTR0c5, binding.gameTXTR0c6, binding.gameTXTR0c7},
                {binding.gameTXTR1c0, binding.gameTXTR1c1, binding.gameTXTR1c2, binding.gameTXTR1c3, binding.gameTXTR1c4, binding.gameTXTR1c5, binding.gameTXTR1c6, binding.gameTXTR1c7},
                {binding.gameTXTR2c0, binding.gameTXTR2c1, binding.gameTXTR2c2, binding.gameTXTR2c3, binding.gameTXTR2c4, binding.gameTXTR2c5, binding.gameTXTR2c6, binding.gameTXTR2c7},
                {binding.gameTXTR3c0, binding.gameTXTR3c1, binding.gameTXTR3c2, binding.gameTXTR3c3, binding.gameTXTR3c4, binding.gameTXTR3c5, binding.gameTXTR3c6, binding.gameTXTR3c7},
                {binding.gameTXTR4c0, binding.gameTXTR4c1, binding.gameTXTR4c2, binding.gameTXTR4c3, binding.gameTXTR4c4, binding.gameTXTR4c5, binding.gameTXTR4c6, binding.gameTXTR4c7},
                {binding.gameTXTR5c0, binding.gameTXTR5c1, binding.gameTXTR5c2, binding.gameTXTR5c3, binding.gameTXTR5c4, binding.gameTXTR5c5, binding.gameTXTR5c6, binding.gameTXTR5c7},
                {binding.gameTXTR6c0, binding.gameTXTR6c1, binding.gameTXTR6c2, binding.gameTXTR6c3, binding.gameTXTR6c4, binding.gameTXTR6c5, binding.gameTXTR6c6, binding.gameTXTR6c7},
                {binding.gameTXTR7c0, binding.gameTXTR7c1, binding.gameTXTR7c2, binding.gameTXTR7c3, binding.gameTXTR7c4, binding.gameTXTR7c5, binding.gameTXTR7c6, binding.gameTXTR7c7}
        };

        binding.mainLBLYourName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        if (gameApi.isInGame()) {
            binding.mainLBLEnemyName.setText(gameApi.getChosenEnemyName());
            binding.mainLBLEnemyScore.setVisibility(View.VISIBLE);
            binding.mainLBLEnemyScore.setText("SCORE:" + gameApi.getEnemyWins() + "/" + gameApi.getEnemyLosses());
            getBoard();
        }
        if (gameApi.isPassive()) {
            binding.chessBoard.setRotationX(180);
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    CHESS_MATRIX[i][j].setRotationX(180);
                }
            }
        }
        if (gameApi.isActive()) {
            binding.chessBoard.setRotationX(0);
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    CHESS_MATRIX[i][j].setRotationX(0);
                }
            }
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void getBoard() {
        if (!gameApi.isInGame())
            return;
        DatabaseReference usersRef = null;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (gameApi.isPassive())
            usersRef = database.getReference("games").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + gameApi.getEnemyId()).child("CHESS_MATRIX");
        else if (gameApi.isActive())
            usersRef = database.getReference("games").child(gameApi.getEnemyId() + FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CHESS_MATRIX");
        DatabaseReference finalUsersRef = usersRef;
        DatabaseReference finalUsersRef1 = usersRef;
        usersRef.addValueEventListener(new ValueEventListener() { // for all time data load.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference tempRef = database.getReference("users");
                String res = (String) dataSnapshot.getValue();
                if (res != null && res.equals("RNBXKXNRPPPPXPPPXXXXXXXXXXBXPXXXXXXXpXXXXXnXXnXXppppXQpprXbqkbXr")) {
                    System.out.println("getactive: " + gameApi.isActive() + "getpassive:" + gameApi.isPassive());
                    if (gameApi.isPassive()) {
                        tempRef.child(gameApi.getEnemyId()).child("wins").setValue(1);
                        popupWinDialog(getActivity());
                    }
                    if (gameApi.isActive()) {
                        tempRef.child(gameApi.getEnemyId()).child("losses").setValue(1);
                        popupLossDialog(getActivity());
                    }
                    gameApi.setInGame(false);
                    gameApi.setPassive(false);
                    gameApi.setActive(false);
                }
                int k = 0;
                if (res == null) {
                    res = "RNBQKBNRPPPPPPPPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXpppppppprnbqkbnr";
                }
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        CHESS_MATRIX_STR[i][j] = String.valueOf(res.charAt(k));
                        if (res.charAt(k) == 'X')
                            CHESS_MATRIX[i][j].setText(" ");
                        else {
                            CHESS_MATRIX[i][j].setText(charToSymbol.get(String.valueOf(res.charAt(k))));
                        }
                        if (isLowerCase(res.charAt(k)) == true)
                            CHESS_MATRIX[i][j].setTextColor(Color.WHITE);
                        else
                            CHESS_MATRIX[i][j].setTextColor(Color.BLACK);
                        k++;
                        int finalI = i;
                        int finalJ = j;
                        String finalRes = res;
                        CHESS_MATRIX[i][j].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View viewIn) {
                                if (CHESS_MATRIX_STR[finalI][finalJ].equals("X") && move_from[0] == -1)
                                    return;
                                char[] a = finalRes.toCharArray();
                                if (gameApi.isInGame() == false)
                                    return;
                                try {
                                    String temp = finalRes;
                                    CHESS_MATRIX[finalI][finalJ].setTextColor(Color.RED);
                                    if (move_to[0] == -1 && move_from[0] != -1) {
                                        move_to = new int[]{finalI, finalJ};

                                        if (a[8 * move_from[0] + move_from[1]] == 'X') {
                                            move_from = new int[]{-1, -1};
                                            move_to = new int[]{-1, -1};
                                            return;
                                        }
                                        //FFD1DC
                                    }
                                    if (move_from[0] == -1)
                                        move_from = new int[]{finalI, finalJ};
                                    if (move_from[0] != -1 && move_to[0] != -1) {
                                        if (move_from[0] == move_to[0] && move_from[1] == move_to[1]) {
                                            CHESS_MATRIX[finalI][finalJ].setTextColor(isLowerCase(a[8 * move_to[0] + move_to[1]]) ? Color.WHITE : Color.BLACK);
                                            move_from = new int[]{-1, -1};
                                            move_to = new int[]{-1, -1};
                                            return;
                                        }
                                        if (a[8 * move_from[0] + move_from[1]] == 'X') {
                                            move_from = new int[]{-1, -1};
                                            move_to = new int[]{-1, -1};
                                            return;
                                        }
                                        if (isLowerCase(a[8 * move_to[0] + move_to[1]]) && isLowerCase(a[8 * move_from[0] + move_from[1]]) || (
                                                !CHESS_MATRIX_STR[move_to[0]][move_to[1]].equals("X") &&
                                                        isUpperCase(a[8 * move_to[0] + move_to[1]]) && isUpperCase(a[8 * move_from[0] + move_from[1]]))) {
                                            CHESS_MATRIX[move_from[0]][move_from[1]].setTextColor(isLowerCase(a[8 * move_to[0] + move_to[1]]) ? Color.WHITE : Color.BLACK);
                                            move_from = move_to;
                                            move_to = new int[]{-1, -1};
                                            CHESS_MATRIX[move_from[0]][move_from[1]].setTextColor(Color.RED);
                                            return;
                                        }
                                        a[8 * move_to[0] + move_to[1]] = a[8 * move_from[0] + move_from[1]];
                                        a[8 * move_from[0] + move_from[1]] = 'X';

                                        finalUsersRef.setValue(new String(a));
                                        move_from = new int[]{-1, -1};
                                        move_to = new int[]{-1, -1};
                                    }
                                } catch (Exception except) {


                                }
                            }
                        });
                    }
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