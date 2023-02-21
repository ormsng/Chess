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

public class LossDialog extends DialogFragment {
    static GameApi gameApi;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle("You lost! \uD83D\uDE14")
                .setMessage("You lost to "+gameApi.getInstance().getEnemyName())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                })
                .create();
    }

    public static void popupLossDialog(FragmentActivity fa) {
            gameApi.getInstance().updateScore();
            FragmentTransaction ft = fa.getSupportFragmentManager().beginTransaction();
            LossDialog lossFragment = new LossDialog();
            lossFragment.show(ft, "dialog");

    }


}

