package com.example.constructionmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.zip.Inflater;

public class NewProjectFragment extends DialogFragment {

    MainActivity ma;
    ImageButton galleryBtn, documentBtn;
    Button positiveButton;
    Boolean createFromPdf = false;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle bundle) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.newproject_fragment, null);

        builder.setView(view);

        // asetetaan dialogin viesti
        builder.setTitle(R.string.createNewProject);

        // hae mainactivity jotta sen metodit saadaan käyttöön
        ma = (MainActivity)getActivity();

        //Liitetään dialogin komponentit
        galleryBtn = (ImageButton) view.findViewById(R.id.gallery_imgBtn);
        documentBtn = (ImageButton) view.findViewById(R.id.document_imgBtn);

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ma.createFromPdf = false;
                //Lataa galleriasta pohjapiirroksen
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ma.onActivityResult.launch(intent);

                //'Luo' painike käyttöön
                createFromPdf = false;
                positiveButton.setEnabled(true);
            }
        });

        documentBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ma.createFromPdf = true;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                ma.onActivityResult.launch(intent);

                //'Luo' painike käyttöön
                createFromPdf = true;
                positiveButton.setEnabled(true);
            }
        });
        //Luo ja peruuta painikkeet

        // lisätään Add flaw painike
        builder.setPositiveButton(R.string.create,
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
                        //TODO Poista mahdollinen valittu kuva imageviewistä
                    }
                });

        return builder.create();
    }

    //Override tämä jotta voidaan estää dialogin sulkeutuminen jos kaikki tiedot ei ole täytetty
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO puutteen ominaisuuksien valitseminen & niiden lähettäminen mainactivityyn tallennusta varten
                    dismiss();
                }
            });
        }
    }

}
