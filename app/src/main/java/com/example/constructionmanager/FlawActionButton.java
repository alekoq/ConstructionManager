package com.example.constructionmanager;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FlawActionButton extends FloatingActionButton {

    public FlawActionButton(@NonNull Context context) {
        super(context);
    }

    /**
     * TODO
     * Lisää Puutteiden tiedot suoraan tänne
     */
    public FlawInfo flawinfo;

    public void setFlawInfo(FlawInfo fi){
        this.flawinfo = fi;
    }

    public FlawInfo getFlawInfo(){
        return this.flawinfo;
    }


}
