package com.example.constructionmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddFlawFragment extends DialogFragment {

    private TextInputLayout apartmentTI;
    private TextInputLayout roomTI;
    private TextInputLayout flawTI;
    MainActivity ma;

    String apartmentInput;
    String roomInput;
    String flawInput;

    // luodaan AlertDialogi ja palautetaan se
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // luodaan dialogi
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        final View flawDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.addflaw_fragment, null);
        // hae mainactivity jotta sen metodit saadaan käyttöön
        ma = (MainActivity)getActivity();

        builder.setView(flawDialogView); // lisätään GUI dialogiin

        // asetetaan dialogin viesti
        builder.setTitle(R.string.title_flaw_dialog);

        //Kun painetaan muualta kuin tekstikentistä laitetaan näppäimistö piiloon
        flawDialogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ei tarvita?
                //flawDialogView.requestFocus();

                hideKeyboard(flawDialogView);
            }
        });


        // liitetään textInputit:t
        apartmentTI = (TextInputLayout) flawDialogView.findViewById(R.id.apartmentTextInput);
        roomTI = (TextInputLayout) flawDialogView.findViewById(R.id.roomTextInput);
        flawTI = (TextInputLayout) flawDialogView.findViewById(R.id.flawTextInput);


        // lisätään Add flaw painike
        builder.setPositiveButton(R.string.button_add_flaw,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Täällä ei tehdä mitään koska funktio overridattu onStartissa jotta ei sulkeudu, silloin jos kaikkia kenttiä ei ole täytetty
                    }
                });

        // lisätään peruuta-painike
        builder.setNegativeButton(R.string.button_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        flawTI.getEditText().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    if(confirmInput()) {
                        addFab();
                    }
                    return true;
                }
                return false;
            }
        });

        return builder.create();
    }

    //Override tämä jotta voidaan estää dialogin sulkeutuminen jos kaikki tiedot ei ole täytetty
    @Override
    public void onStart()
    {
        super.onStart();
        AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Lisää tiedot listaan ja suljetaan dialogi jos kaikki tiedot on täytetty
                    if(confirmInput()) {
                        addFab();
                    }
                }
            });
        }
    }

    private void addFab(){
        //if everything is filled save information
        FlawInfo flawInfo = new FlawInfo(apartmentInput, roomInput, flawInput, ma.counter);

        //Luo uuden fabin joka sisältää myös flawInfon
        ma.newFab(flawInfo);

        //sulje dialogi
        dismiss();
    }


    public boolean validateApartment() {
        //get edited text
        apartmentInput = apartmentTI.getEditText().getText().toString().trim();

        //check if empty and return that value
        return(validateString(apartmentTI, apartmentInput));
    }

    private boolean validateRoom() {
        //get edited text
        roomInput = roomTI.getEditText().getText().toString().trim();

        //check if empty and return that value
        return(validateString(roomTI, roomInput));
    }

    private boolean validateFlaw() {
        //get edited text
        flawInput = flawTI.getEditText().getText().toString().trim();

        //check if empty and return that value
        return(validateString(flawTI, flawInput));
    }

    private boolean validateString(TextInputLayout ti, String input){
        if(input.isEmpty()){
            ti.setError(getString(R.string.error_empty));
            return false;
        }
        else{
            //input.setError(null);
            return true;
        }
    }

    public boolean confirmInput(){
        //if not filled everything
        if(!validateApartment() | !validateRoom() | !validateFlaw()){
            return false;
        }

        return true;
    }

    public void hideKeyboard(View view){
        InputMethodManager mInputMethodManager = (InputMethodManager) ma.getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
