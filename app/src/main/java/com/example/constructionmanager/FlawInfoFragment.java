package com.example.constructionmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

public class FlawInfoFragment  extends DialogFragment {

    private TextInputLayout apartmentTI;
    private TextInputLayout roomTI;
    private TextInputLayout flawTI;
    MainActivity ma;

    String apartmentInput;
    String roomInput;
    String flawInput;

    FlawInfo fi;

    //Mahdollistaa objektin (fabin flawInfo) lähettämisen
    public static FlawInfoFragment newInstance(int arg, FlawInfo fi) {
        FlawInfoFragment frag = new FlawInfoFragment();
        Bundle args = new Bundle();
        args.putInt("count", arg);
        frag.setArguments(args);
        frag.setFlawInfo(fi);
        return frag;
    }

    public void setFlawInfo(FlawInfo fi){
        this.fi = fi;
    }

    // luodaan Dialogi
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // luodaan dialogi
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        final View infoDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.flawinfo_fragment, null);
        // hae mainactivity jotta sen metodit saadaan käyttöön
        ma = (MainActivity)getActivity();

        builder.setView(infoDialogView); // lisätään GUI dialogiin

        // asetetaan dialogin viesti
        builder.setTitle(R.string.title_info_dialog);

        // liitetään textInputit:t
        apartmentTI = (TextInputLayout) infoDialogView.findViewById(R.id.apartmentInfoTextInput);
        roomTI = (TextInputLayout) infoDialogView.findViewById(R.id.roomInfoTextInput);
        flawTI = (TextInputLayout) infoDialogView.findViewById(R.id.flawInfoTextInput);

        /**
         * Näytä oikeat tiedot
         */
        apartmentTI.getEditText().setText(fi.getApartment()); //Bundlella apartmentTI.getEditText().setText(getArguments().getStringArray("flawinfo")[0]
        roomTI.getEditText().setText(fi.getRoom());
        flawTI.getEditText().setText(fi.getFlaw());

        // lisätään Add flaw painike
        builder.setPositiveButton(R.string.save,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Täällä ei tehdä mitään koska funktio overridattu onStartissa jotta ei sulkeudu, silloin jos kaikkia kenttiä ei ole täytetty
                    }
                });

        // lisätään peruuta-painike
        builder.setNegativeButton(R.string.ret,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
        });

        builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Täällä ei tehdä mitään koska funktio overridattu onStartissa
                }
        });

        //TODO
        //Estä näppäimistön automaattinen ilmestyminen

        return builder.create();
    }


    // TODO Näppiksen piilottamiseen. EI TOIMI
    @Override
    public void onResume()
    {
        InputMethodManager imm =
                (InputMethodManager)apartmentTI.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.toggleSoftInput(0, InputMethodManager.RESULT_HIDDEN);

        super.onResume();
    }


    //Override tämä jotta voidaan estää dialogin sulkeutuminen jos kaikki tiedot ei ole täytetty
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            final Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);

            positiveButton.setEnabled(false);   //oletuksena pois käytöstä, kun muokataan annetaan tallennusmahdollisuus
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Jos kaikissa edelleen tekstiä päivitetään tiedot
                    if(confirmInput()) {
                        // TODO Päivitä vain ne kentät joita on päivitetty
                        fi.setApartment(apartmentInput);
                        fi.setRoom(roomInput);
                        fi.setFlaw(flawInput);

                        dismiss();
                    }
                }
            });

            Button editButton = (Button) d.getButton(Dialog.BUTTON_NEUTRAL);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO
                    //Tallenna-nappi käyttöön vasta kun jotain tietoja on jo muokattu
                    positiveButton.setEnabled(true);

                    apartmentTI.setEnabled(true);
                    roomTI.setEnabled(true);
                    flawTI.setEnabled(true);
                }
            });
        }

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
}
