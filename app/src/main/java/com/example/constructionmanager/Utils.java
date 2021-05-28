package com.example.constructionmanager;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

public class Utils {
    public static void hideKeyboard(@NonNull Activity activity){
        // Check if no view has focus:
        System.out.println(activity);
        View view = activity.getCurrentFocus();
        if (view != null) {
            System.out.println("Täällä");
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
