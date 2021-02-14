package com.example.constructionmanager;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;


//EI KÄYTÖSSÄ KOSKA LAITTEEN KÄÄNÄTMINEN ESTETTY

public class ImageRetainingFragment extends Fragment {
    private Bitmap selectedImage;

    public ImageRetainingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setImage(Bitmap selectedImage) {
        this.selectedImage = selectedImage;
    }
    public Bitmap getImage() {
        return this.selectedImage;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_retaining, container, false);
    }
}