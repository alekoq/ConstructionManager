package com.example.constructionmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class NewProjectFragment extends DialogFragment {

    MainActivity ma;
    ImageButton galleryBtn, documentBtn;
    Button positiveButton;
    Boolean createFromPdf = false;

    //chippien erotusmerkin (',') tarkastamiseen
    private char inputChar;
    private int chipStartIndex = 0;
    private int chipEndIndex = 0;

    private EditText newProjectChipET;

    private ArrayList<ChipDrawable> chips = new ArrayList<>();

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

        newProjectChipET = (EditText)view.findViewById((R.id.newProjectChipInputEditText));
        chipCreator(newProjectChipET);

        newProjectChipET.setText(getString(R.string.apartment));
        newProjectChipET.append(",");
        newProjectChipET.append(getString(R.string.room));
        newProjectChipET.append(",");
        newProjectChipET.append(getString(R.string.flaw));
        newProjectChipET.append(",");

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


    private void chipCreator(EditText et){

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //TODO pugi jos poistaa (pyyhkii) tekstiä
                //backspace
                if(count==0){
                    if(chipStartIndex<chipEndIndex){
                        chipEndIndex--;

                        //DEBUG
                        System.out.println("End " + chipEndIndex);
                    }
                    else if(chips.size()>0){
                        //otetaan chip-arraylistin viimeinen esiintymä (ja poistetaan se listasta)
                        ChipDrawable chip = chips.remove(chips.size()-1);
                        //vähennetään end-indeksistä poistetun chipin pituus
                        chipEndIndex -= chip.getText().length()+1;

                        //start ja end samaan
                        chipStartIndex = chipEndIndex;

                        //DEBUG
                        System.out.println("End " + chipEndIndex);
                        System.out.println("Start " + chipStartIndex);
                    }
                }
                //else if(charSequence.length()>0) {
                else{
                    chipEndIndex++;
                    inputChar = charSequence.charAt(start);

                    //DEBUG
                    System.out.println("End " + chipEndIndex);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    //jos syötetään välimerkki (ja on tekstiä) tehdään uusi chip
                    if (inputChar == ',' && chipEndIndex > chipStartIndex + 1) {
                        ChipDrawable chip = ChipDrawable.createFromResource(getActivity(), R.xml.chip);
                        chip.setText(editable.subSequence(chipStartIndex, editable.length() - 1));

                        chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());
                        ImageSpan span = new ImageSpan(chip);
                        editable.setSpan(span, chipStartIndex, editable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        //lisätään listaan
                        chips.add(chip);

                        //uusi aloituspiste
                        chipStartIndex = editable.length();
                        chipEndIndex = chipStartIndex;

                        inputChar = ' ';
                        //DEBUG
                        System.out.println("End " + chipEndIndex);
                        System.out.println("Start " + chipStartIndex);
                    }
                }
                catch(IndexOutOfBoundsException e){
                    ma.toast(getString(R.string.somethingWentWrong));
                    e.printStackTrace();
                }
            }
        });
    }
}
