package com.example.constructionmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
/**
public class saveAsFragment extends DialogFragment {

    TextInputLayout saveTI;
    MainActivity ma;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // luodaan dialogi
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        final View saveDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.saveas_fragment, null);
        // hae mainactivity jotta sen metodit saadaan käyttöön
        ma = (MainActivity)getActivity();

        builder.setView(saveDialogView); // lisätään GUI dialogiin

        // asetetaan dialogin viesti
        builder.setTitle(R.string.saveas);

        // liitetään textInputit:t
        saveTI = (TextInputLayout) saveDialogView.findViewById(R.id.saveAsTextInput);

        // lisätään Add flaw painike
        builder.setPositiveButton(R.string.save,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Tallennetaan annetulla nimellä (tarkastetaan ettei ole tyhjä)
                    if (confirmInput()) {
                        ma.checkPermission(bitmap
                    }
                }

            });

        }
    }

        private boolean validateSave() {
            //get edited text
            String saveInput = saveTI.getEditText().getText().toString().trim();

            //check if empty and return that value
            return (validateString(saveTI, saveInput));
        }

        private boolean validateString(TextInputLayout ti, String input) {
            if (input.isEmpty()) {
                ti.setError(getString(R.string.error_empty));
                return false;
            } else {
                //input.setError(null);
                return true;
            }
        }

        public boolean confirmInput() {
            //if not filled everything
            if (!validateSave()) {
                return false;
            }

            return true;
        }
}
**/