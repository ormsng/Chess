package com.example.chesswithnav.ui.dashboard;

import static com.example.chesswithnav.ui.home.HomeFragment.active;
import static com.example.chesswithnav.ui.home.HomeFragment.enemyId;
import static com.example.chesswithnav.ui.home.HomeFragment.enemyName;
import static com.example.chesswithnav.ui.home.HomeFragment.passive;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chesswithnav.databinding.FragmentDashboardBinding;
import com.example.chesswithnav.ui.home.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment {

    static Map<String, String> value = new HashMap<String, String>();
    private static TextView[][] CHESS_MATRIX;
    private static String[][] CHESS_MATRIX_STR = {{"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}, {"x", "x", "x", "x", "x", "x", "x", "x"}};
    private static int[] move_from = {-1, -1};
    private static int[] move_to = {-1, -1};
    static HashMap<String, String> capitalCities = new HashMap<String, String>();
    public boolean inGame = HomeFragment.inGame;
    private FragmentDashboardBinding binding;
    public boolean active = HomeFragment.active;
    public boolean passive = HomeFragment.passive;
    public String enemyName = HomeFragment.enemyName;
    public String finalEnemyName = HomeFragment.finalEnemyName;
    public String realEnemyName;

    @SuppressLint("Range")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Add keys and values (Country, City)
        capitalCities.put("p", "♟");
        capitalCities.put("P", "♟");

        capitalCities.put("K", "♚");
        capitalCities.put("k", "♚");

        capitalCities.put("Q", "♛");
        capitalCities.put("q", "♛");

        capitalCities.put("N", "♞");
        capitalCities.put("n", "♞");

        capitalCities.put("B", "♝");
        capitalCities.put("b", "♝");

        capitalCities.put("R", "♜");
        capitalCities.put("r", "♜");

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
        if (inGame) {
            binding.mainLBLEnemyName.setText(finalEnemyName);
            binding.mainLBLEnemyScore.setVisibility(View.VISIBLE);
            binding.mainLBLEnemyScore.setText("SCORE: 2/5");
            getBoard();
        }
        if (passive)
            binding.chessBoard.setRotationX(180);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public HashMap<String, String> getBoard() {
        DatabaseReference usersRef = null;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (active)
            usersRef = database.getReference("games").child(enemyId + FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CHESS_MATRIX");
        else
            usersRef = database.getReference("games").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + enemyId).child("CHESS_MATRIX");

        DatabaseReference finalUsersRef = usersRef;
        usersRef.addValueEventListener(new ValueEventListener() { // for all time data load.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String res = (String) dataSnapshot.getValue();
                int k = 0;
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (res == null) {
                            res = "RNBQKBNRPPPPPPPPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXpppppppprnbqkbnr";
                        }
                        CHESS_MATRIX_STR[i][j] = String.valueOf(res.charAt(k));
                        if (res.charAt(k) == 'X')
                            CHESS_MATRIX[i][j].setText(" ");
                        else {
                            CHESS_MATRIX[i][j].setText(capitalCities.get(String.valueOf(res.charAt(k))));
                            if (passive)
                                CHESS_MATRIX[i][j].setRotationX(180);
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
                                if (inGame == false)
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
                                    //Two good cells selected
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
//                                        if (validateChessMove(temp, new String(a)) == true)
//                                            System.out.println("Valid move!")g
//                                        else
//                                            System.out.println("Not valid!!");

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

        return (HashMap<String, String>) value;
    }

    static char[] swap(String str, int i, int j) {
        char ch[] = str.toCharArray();
        char temp = ch[i];
        ch[i] = ch[j];
        ch[j] = temp;
        return ch;
    }

    public static boolean validateChessMove(String currentBoard, String newBoard) {
        // Find the starting and ending squares of the move
        int start = -1, end = -1;
        for (int i = 0; i < currentBoard.length(); i++) {
            char cur = currentBoard.charAt(i);
            char next = newBoard.charAt(i);
            if (cur != next) {
                if (start == -1) {
                    start = i;
                } else {
                    end = i;
                    break;
                }
            }
        }
        if (start == -1 || end == -1) {
            return false; // No move found
        }

        // Get the piece and destination square from the boards
        char piece = Character.toUpperCase(currentBoard.charAt(start));
        char dest = newBoard.charAt(end);

        // Check if the destination square is empty
        if (dest != 'X') {
            return false;
        }

        // Check if the move is valid based on the piece type
        int x1 = start % 8, y1 = start / 8;
        int x2 = end % 8, y2 = end / 8;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        switch (piece) {
            case 'P':
            case 'p':
                // Check if this is a valid pawn move
                int forwardDir = isUpperCase(piece) ? -1 : 1;
                if (dx == 0 && (y2 - y1 == forwardDir || (y1 == 6 && y2 == 4) || (y1 == 1 && y2 == 3))) {
                    // Valid pawn move: one square forward
                    return true;
                } else if (dx == 1 && y2 - y1 == forwardDir) {
                    // Valid pawn capture
                    return true;
                }
                return false; // Invalid pawn move

            case 'R':
            case 'r':
                // Check if this is a valid rook move
                if (dx == 0 || dy == 0) {
                    int dir = dx != 0 ? dx / Math.abs(dx) : dy / Math.abs(dy);
                    for (int i = 1; i < Math.max(dx, dy); i++) {
                        char c = currentBoard.charAt(start + i * dir * (dx != 0 ? 1 : 8));
                        if (c != 'X') {
                            return false; // Invalid rook move: obstruction in path
                        }
                    }
                    return true; // Valid rook move: horizontal or vertical
                }
                return false; // Invalid rook move

            case 'N':
            case 'n':
                // Check if this is a valid knight move
                if ((dx == 1 && dy == 2) || (dx == 2 && dy == 1)) {
                    return true; // Valid knight move
                }
                return false; // Invalid knight move

            case 'B':
            case 'b':
                // Check if this is a valid bishop move
                if (dx == dy) {
                    int xDir = dx / Math.abs(dx);
                    int yDir = dy / Math.abs(dy);
                    for (int i = 1; i < dx; i++) {
                        char c = currentBoard.charAt(start + i * (8 * yDir + xDir));
                        if (c != 'X') {
                            return false; // Invalid bishop

                        }
                    }
                    return true; // Valid bishop move: diagonal
                }
                return false; // Invalid bishop move

            case 'Q':
            case 'q':
                // Check if this is a valid queen move
                if (dx == 0 || dy == 0 || dx == dy) {
                    int xDir = dx != 0 ? dx / Math.abs(dx) : 0;
                    int yDir = dy != 0 ? dy / Math.abs(dy) : 0;
                    int dir = dx != 0 ? xDir : yDir;
                    for (int i = 1; i < Math.max(dx, dy); i++) {
                        char c = currentBoard.charAt(start + i * dir * (dx != 0 ? 1 : 8 + xDir));
                        if (c != 'X') {
                            return false; // Invalid queen move: obstruction in path
                        }
                    }
                    return true; // Valid queen move: horizontal, vertical, or diagonal
                }
                return false; // Invalid queen move

            case 'K':
            case 'k':
                // Check if this is a valid king move
                if ((dx == 1 && dy == 0) || (dx == 0 && dy == 1) || (dx == 1 && dy == 1)) {
                    return true; // Valid king move
                }
                return false; // Invalid king move

            default:
                return false; // Invalid piece
        }
    }


}