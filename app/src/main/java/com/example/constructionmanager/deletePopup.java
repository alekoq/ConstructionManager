package com.example.constructionmanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;


public class deletePopup extends Activity {
    ImageButton deleteBtn;
    int width;
    int height;
    int x, y;
    View v;

    public deletePopup(View v, int x, int y, int width, int height){
        this.v = v;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.delete_popup, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, x-28, y+45); //sijainti kokeilemalla

        //poistopainike
        deleteBtn = popupView.findViewById(R.id.delete);

        deleteBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // varmista poistamisesta
                // poista listasta (flawInfoList, fabList)
                //fabList.remove(fab);
                // poista fab
                //layout.removeView(fab);
                // tarkista numerointi
                System.out.println("test");
                popupWindow.dismiss();
        }});



    }
}
