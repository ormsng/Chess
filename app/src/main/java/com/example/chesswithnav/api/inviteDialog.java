package com.example.chesswithnav.api;

import static com.example.chesswithnav.ui.home.HomeFragment.enemyName;
import static com.example.chesswithnav.ui.home.HomeFragment.newGame;
import static com.example.chesswithnav.ui.home.HomeFragment.regCounter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

public class inviteDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("New Invite")
                .setMessage(enemyName + " has invited you to a game!")
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Invitation declined
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Invitation accepted
                        newGame();
                        boolean inGame = true;
                    }
                })
                .create();
    }

    public static void popupInviteDialog(FragmentActivity fa) {
        if (regCounter > 1) {
            FragmentTransaction ft = fa.getSupportFragmentManager().beginTransaction();
            inviteDialog newFragment = new inviteDialog();
            newFragment.show(ft, "dialog");
        }
    }


}
