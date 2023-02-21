package com.example.chesswithnav.api;

import static com.example.chesswithnav.ui.home.HomeFragment.enemyName;
import static com.example.chesswithnav.ui.home.HomeFragment.regCounter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

public class WinDialog extends DialogFragment {
    static GameApi gameApi;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle("You won! \uD83D\uDE03")
                .setMessage("You won " + gameApi.getInstance().getEnemyName())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                })
                .create();
    }

    public static void popupWinDialog(FragmentActivity fa) {
        gameApi.getInstance().updateScore();
        FragmentTransaction ft = fa.getSupportFragmentManager().beginTransaction();
        WinDialog newFragment = new WinDialog();
        newFragment.show(ft, "dialog");


    }


}

