package com.example.constructionmanager;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SaveBitmap implements java.io.Serializable{

    /**
    Jotta voi serializoida:
     -Luokan pitää implemnetoida Serializable
     -Kaikkien tallennettavien luokkien pitää implementoida Serializable

     Eli pitää varmaan tallentaa tiedot pienemmissä osissa ja ladattaessa luoda kaikki uudestaan.
     Kuvien tallennus joko:
     -Polun perusteella (Tieto häviää jos alkuperäinen kuva poistetaan/siirretään)
     -Kopioida käytettävä kuva projektikansioon ja hakea se sieltä
     */

    //Bitmap
    Bitmap bm;

    //Lista puutteista
    //List<FlawInfo> flawInfoList = new ArrayList<FlawInfo>();

    public SaveBitmap(){
    }

    public SaveBitmap(Bitmap bm){
        this.bm=bm;
    }



    public void writeObject(ObjectOutputStream out) throws IOException{
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.bm.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        out.writeInt(byteArray.length);
        out.write(byteArray);
    }

    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{

        int bufferLength = in.readInt();

        byte[] byteArray = new byte[bufferLength];

        int pos = 0;
        do {
            int read = in.read(byteArray, pos, bufferLength - pos);

            if (read != -1) {
                pos += read;
            } else {
                break;
            }

        } while (pos < bufferLength);

        this.bm = BitmapFactory.decodeByteArray(byteArray, 0, bufferLength);
    }

    public Bitmap getBm() {
        return this.bm;
    }


}
