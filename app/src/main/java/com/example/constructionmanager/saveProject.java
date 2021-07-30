package com.example.constructionmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class saveProject extends Thread{
    MainActivity ma;
    Context mContext;
    Handler mHandler;
    String fileName;
    ImageView iv;
    String dir, projects;
    List<FlawInfo> flawInfoList;

    public saveProject(String fileName, Context ctx, ImageView iv, List<FlawInfo> flawInfoList, String dir, Handler handler){
        this.fileName = fileName;
        this.iv = iv;
        this.flawInfoList = flawInfoList;
        this.mContext = ctx;
        this.projects = "/projects";
        this.dir = dir;
        this.mHandler = handler;
        start();
    }

    @Override
    public void run() {
        //erota bitmap imageView:stä
        BitmapDrawable draw = (BitmapDrawable) iv.getDrawable();
        Bitmap bm = draw.getBitmap();

        //Luo tallennusolio bitmapille
        // TODO Molemmat samaan tallennustiedostoon
        SaveBitmap sb = new SaveBitmap(bm);

        //Bitmap ja puutteet tallennetaan erikseen ja omiin tiedostoihinsa koska en osannut yhdistää tallennusta
        try {
            //Bitmapin tallennus
            FileOutputStream fileOut = new FileOutputStream(dir + projects + "/" + fileName + "Bitmap.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            sb.writeObject(out);

            out.close();
            fileOut.close();

            //Puutteet
            fileOut = new FileOutputStream(dir + projects + "/" + fileName + "Save.ser");
            out = new ObjectOutputStream(fileOut);

            out.writeObject(flawInfoList);

            out.close();
            fileOut.close();
            handlerSendMessage(11);

            //ei tallentamattomia muutoksia
            //((MainActivity)ma).unsaved=false;

        } catch (IOException i) {
            i.printStackTrace();
            handlerSendMessage(10);
        }
    }

    void handlerSendMessage(int command) {
        //Kutsutaan mainactivityn handlerin kautta toastia
        Message message = mHandler.obtainMessage(command);
        message.sendToTarget();
    }
}


