package com.example.constructionmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadProjectFragment extends DialogFragment {

    private Spinner spinner;
    private TextView warning;
    private String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/CM/projects";

    // luodaan AlertDialogi ja palautetaan se
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // luodaan dialogi
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        final View loadDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.loadproject_fragment, null);

        builder.setView(loadDialogView);

        // asetetaan dialogin viesti
        builder.setTitle(R.string.loadProject);

        spinner = (Spinner)loadDialogView.findViewById(R.id.saveSpinner);
        warning = (TextView) loadDialogView.findViewById(R.id.noSelectedWarning);

        // lisätään Add flaw painike
        builder.setPositiveButton(R.string.load,
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

        String path = dir;
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<String> spinnerArray = new ArrayList<>();
        String file;
        for (int i = 0; i < files.length; i++)
        {
            //Otetaan pelkästään Save.ser tiedostot ja pätkitään tiedostonimi helposti luettavaksi (testiSave.ser -> testi)
            file = files[i].toString().substring(dir.length());
            if(file.endsWith("e.ser")){
                spinnerArray.add(file.substring(1, file.length()-8));
            }

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        return builder.create();
    }

    //Override tämä jotta voidaan estää dialogin sulkeutuminen jos kaikki tiedot ei ole täytetty
    @Override
    public void onStart() {
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
                    String sel = spinner.getSelectedItem().toString();
                    try{
                        // hae mainactivity jotta sen metodit saadaan käyttöön
                        MainActivity ma = (MainActivity)getActivity();

                        //lataa projetki tiedostonimen perusteella (hakee sekä kuvan että muut tiedot pätkityn tiedostonimen avulla)
                        ma.loadProject(sel);
                    }
                    catch (NullPointerException i){
                        i.printStackTrace();
                        warning.setVisibility(View.VISIBLE);
                    }

                    //sulje dialogi
                    dismiss();
                }
            });
        }
    }
}
