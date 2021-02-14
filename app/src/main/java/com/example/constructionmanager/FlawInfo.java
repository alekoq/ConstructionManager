package com.example.constructionmanager;

import android.widget.RelativeLayout;

import java.io.Serializable;

public class FlawInfo implements Serializable {
    String apartment;
    String room;
    String flaw;
    int counter;
    int leftMargin, topMargin;

    public FlawInfo(String a, String r, String f, int c){
        apartment = a;
        room = r;
        flaw = f;
        counter = c;
    }

    public FlawInfo(String a, String r, String f, int c, int lm, int tm){
        apartment = a;
        room = r;
        flaw = f;
        counter = c;
        leftMargin = lm;
        topMargin = tm;
    }

    public String getApartment(){
        return this.apartment;
    }

    public void setApartment(String apartment){
        this.apartment = apartment;
    }

    //lol
    public String getRoom(){
        return this.room;
    }

    public void setRoom(String room){
        this.room = room;
    }

    public String getFlaw(){
        return this.flaw;
    }

    public void setFlaw(String flaw){
        this.flaw = flaw;
    }

    public int getCounter(){ return this.counter;}

    //onko syytä muokata? Ehkä jos Puutteita poistetaan
    private void setCounter(int counter){
        this.counter = counter;
    }

    public int getLeftMargin(){
        return this.leftMargin;
    }
    public void setLeftMargin(int leftMargin){
        this.leftMargin = leftMargin;
    }
    public int getTopMargin(){
        return this.topMargin;
    }
    public void setTopMargin(int topMargin){
        this.topMargin = topMargin;
    }


}
