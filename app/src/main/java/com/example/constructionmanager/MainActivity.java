package com.example.constructionmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    //säilyttää kuvan kun laitetta käännetään
    //private static final String FRAGMENT_NAME = "imageFragment";
    //private ImageRetainingFragment imageRetainingFragment;

    //private static final int GALLERY_REQUEST_CODE = 123;

    public String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/CM";
    public String images = "/images";
    public String csvs = "/csvs";
    public String projects = "/projects";

    private boolean blueprintLoaded = false;
    public Uri imageData;
    private boolean isEditable = false;
    private boolean check = true;

    public int counter;  //laskee virheiden määrän
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    ImageView imageView;
    Drawable icon;
    RelativeLayout layout;

    final int RQS_IMAGE1 = 1;
    final int RQS_DATA = 2;

    Bitmap bitmapMaster;
    Canvas canvasMaster;

    public int prvX, prvY;

    Paint paintDraw;
    Paint paintText;
    int centerText = 45;

    //Lista kaikista FloatingActionButtoneista
    ArrayList<FlawActionButton> fabList = new ArrayList<>();



    //COLORS FOR FAB
    int[][] states = new int[][] {
            new int[] { android.R.attr.state_enabled}, // enabled
            //new int[] {-android.R.attr.state_enabled}, // disabled
            //new int[] {-android.R.attr.state_checked}, // unchecked
            //new int[] { android.R.attr.state_pressed}  // pressed
    };

    int[] colors = new int[] {
            Color.CYAN,
            //Color.RED,
            //Color.GREEN,
            //Color.RED
    };

    int[] testcolors = new int[] {
            Color.BLACK,
    };

    ColorStateList myList = new ColorStateList(states, colors);
    ColorStateList testList = new ColorStateList(states, testcolors);
    

    //Lista puutteista
    List<FlawInfo> flawInfoList = new ArrayList<FlawInfo>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.blueprint);
        icon = getResources().getDrawable(R.drawable.ic_baseline_add_24);

        layout = findViewById(R.id.imageRelativeLayout);

        //kuvan koon hallintaan
        final float dpi = getResources().getDisplayMetrics().density;
        //lataa kuvan takaisin kun laitetta käännetään. EI TOIMINNASSA
        //initializeImageRetainingFragment();
        //tryLoadImage();

        isEditable = imageView.getDrawable() != null;

        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.STROKE);
        paintDraw.setColor(Color.RED);
        paintDraw.setStrokeWidth(5);


        paintText = new Paint();
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.RED);
        paintText.setTextSize(32);

        //Zoom ei toimi (gradle PhotoView)
        //PhotoViewAttacher pAttacher;
        //pAttacher = new PhotoViewAttacher(imageView);
        //pAttacher.update();

        //Luo sovellukselle oman kansion jos sitä ei jo ole
        createAppDir();


        //näytön koko
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        final int displayHeight = displayMetrics.heightPixels;
        final int displayWidth = displayMetrics.widthPixels;


        layout.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                //jos kuva lisätty, voidaan lisätä myös merkintöjä
                if(isEditable){
                    int action = event.getAction();
                    prvX = (int) event.getX();
                    prvY = (int) event.getY();

                    //kuvan reunat
                    float xl, xr;
                    xl = (displayWidth - canvasMaster.getWidth())/2; //(displayWidth*dpi - imageView.getWidth()*dpi)/2;
                    xr = xl + canvasMaster.getWidth(); //xl + imageView.getWidth()*dpi;

                    System.out.println("prvX: " + prvX + "\nprvY: " + prvY + "\nkuvan vasen reuna: " + xl + "\nkuvan oikea reuna: " + xr );

                    //tarkistetaan että ollaan kuvan sisällä
                    if(action == MotionEvent.ACTION_DOWN && prvY < displayHeight){

                        //Dialogi jossa täytetään tiedot, ja painamalla Add pirretään ympyrä ja tallennetaan tiedot
                        showFlawFragment();
                    }
                }

                return true;
            }
        });
    }

    //method to convert your text to image
    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        //ympyrän ääriviivan piirtäminen täällä ei oikein onnistu
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public void newFab(FlawInfo fi){
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = (int) prvX-centerText;
        lp.topMargin = (int) prvY-centerText;

        //Luo fabin ominaisuudet
        newFab(fi, lp);

        flawInfoList.add(fi);
    }

    public void newFab(FlawInfo fi, RelativeLayout.LayoutParams lp) {
        final FlawActionButton fab = new FlawActionButton(this);

        //Klikkaaminen näyttää fabin tiedot ja mahdollistaa niiden muokkaamisen
        fab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                showInfoFragment(fab.getFlawInfo());
            }
        });


        //Kun painetaan pitkään voidaan merkintää siirtää
        fab.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                v.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_MOVE:
                                //Arvot saatu puhtaasti testaamalla
                                view.setX(event.getRawX() - imageView.getWidth()/50); //Puhelin : (event.getRawX() - imageView.getWidth()/30)
                                view.setY(event.getRawY() - imageView.getHeight()/6 ); //Puhelin: (event.getRawY() - imageView.getHeight()/4 - imageView.getHeight()/30)
                                break;
                            case MotionEvent.ACTION_UP:
                                view.setOnTouchListener(null);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                return true;
            }


        });

        //Painikkeen asetukset
        fab.setAlpha(0.65f);
        fab.setBackgroundColor(Color.RED);
        fab.setBackgroundTintList(testList);
        //DrawableCompat.setTintList(DrawableCompat.wrap(fab.getBackground()), testList);
        //fab.setBackgroundColor(Color.TRANSPARENT);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        fab.setImageBitmap(textAsBitmap(Integer.toString(counter), 40, Color.WHITE));
        fab.setFlawInfo(fi);
        fab.setLayoutParams(lp);

        //Määritetään fabin FlawInfo-luokalle xy-koordinaatit niiden selaamista varten
        fab.getFlawInfo().setLeftMargin(lp.leftMargin);
        fab.getFlawInfo().setTopMargin(lp.topMargin);

        //Lisää painike listaan, jotta sitä voidaan myöhemmin käsitellä
        fabList.add(fab);

        //Lisää painike näkymään
        layout.addView(fab);
        counter++;
    }


    //funktio avaa flawFragmentin kun lisätään uutta Puutetta
    public void showFlawFragment(){

        //Luo uuden flawFragmentin ja näyttää sen
        AddFlawFragment flawFragment = new AddFlawFragment();
        flawFragment.show(getSupportFragmentManager(), "flawFragment");
    }

    //funktio avaa flawinfoFragmentin kun tarkastellaan vanhaa Puutetta
    public void showInfoFragment(FlawInfo fi){

        //Luo uuden infoFlawFragmentin ja lähettää sille fabin flawInfon argumenttina
        FlawInfoFragment infoFragment = FlawInfoFragment.newInstance(0, fi);

        infoFragment.show(getSupportFragmentManager(), "infoFragment");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    //Valitaan haluttu toiminto valitun option-elementin avulla
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addFlaw:
                if (blueprintLoaded) {
                    isEditable = !isEditable;
                    if(isEditable){
                        icon.setColorFilter(getResources().getColor(R.color.iconActivated), PorterDuff.Mode.SRC_ATOP);
                    }
                    else{
                        icon.setColorFilter(getResources().getColor(R.color.iconUnActivated), PorterDuff.Mode.SRC_ATOP);
                    }
                    invalidateOptionsMenu();
                }
                return true;
            case R.id.addImage:
                //checks if permissions are okay, then opens menu where image can be loaded
                if(checkPermission()){
                    loadImage();
                }
                return true;
            case R.id.save:
                if (blueprintLoaded) {
                    if(bitmapMaster != null && checkPermission()){
                        //Luo alertdialogin jossa kysytään millä nimellä tallennetaan. Tallentaa samalla nimellä sekä kuvan että csv:n
                        showSaveAsDialog(true);
                    }
                }
                return true;
            case R.id.saveProject:
                if(flawInfoList.size()>0 && checkPermission())
                    showSaveAsDialog(false);
                return true;
            case R.id.loadProject:
                if(flawInfoList.size()>0){
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainActivity.this);
                        final View dialogView = MainActivity.this.getLayoutInflater().inflate(
                                R.layout.areyousure_fragment, null);

                        builder.setView(dialogView);

                        // lisätään Add flaw painike
                        builder.setPositiveButton(R.string.cont,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (checkPermission()){
                                            loadData();
                                        }
                                    }
                                });

                        // lisätään peruuta-painike
                        builder.setNegativeButton(R.string.button_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        ;

                    // näytetään dialogi
                    builder.create().show();
                }
                else if (checkPermission()){
                    loadData();
                }

                return true;
            case R.id.info:
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MainActivity.this);

                // asetetaan ilmoituksen viesti
                builder.setMessage(R.string.info);

                final View infoDialogView = MainActivity.this.getLayoutInflater().inflate(
                        R.layout.info_fragment, null);

                builder.setView(infoDialogView);

                TextView saveLoc = (TextView) infoDialogView.findViewById(R.id.saveLocationTextView);

                try {
                    saveLoc.setText(dir);
                } catch(Exception e) {
                    e.printStackTrace();
                    saveLoc.setText(getString(R.string.error));
                }



                // lisätään dialogiin OK painike
                builder.setPositiveButton(R.string.back,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );

                // näytetään dialogi
                builder.create().show();
        }

        return super.onOptionsItemSelected(item);
    }



    //Ikonin väärin muutos (kun valittuna) vaatii tämän
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.addFlaw);
        item.setIcon(icon);

        return true;
    }

    //Määrittää valitun kuvan ja asettaa sen imageViewiin
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap tempBitmap;

        if (resultCode == RESULT_OK  && data != null) {

            if (requestCode == RQS_IMAGE1) {
                    imageData = data.getData();

                    try {

                        tempBitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(imageData));

                        Bitmap.Config config;
                        if (tempBitmap.getConfig() != null) {
                            config = tempBitmap.getConfig();
                        } else {
                            config = Bitmap.Config.ARGB_8888;
                        }


                        bitmapMaster = Bitmap.createBitmap(
                                tempBitmap.getWidth(),
                                tempBitmap.getHeight(),
                                config);



                        canvasMaster = new Canvas(bitmapMaster);
                        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);

                        imageView.setImageBitmap(bitmapMaster);

                        //alustetaan tietyt muuttujat
                        initialize();

                        //Tarvitaan kuvan säilyttämiseen kun laitetta käännetään. EI TOIMINNASSA
                        //imageRetainingFragment.setImage(bitmapMaster);

                        blueprintLoaded = true;
                    //jos kuva ei löydy
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
            }

        }
    }


    //varmistaa että on asetettu laitteesta lupa lataamiseen/tallentamiseen
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && check) {

            // näyttää selityksen miksi lupaa vaaditaan
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MainActivity.this);

                // asetetaan ilmoituksen viesti
                builder.setMessage(R.string.permission_explanation);

                // lisätään dialogiin OK painike
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // pyydetään lupa
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                                check = false;
                                checkPermission();
                            }
                        }
                );

                // näytetään dialogi
                builder.create().show();

            } else {
                // pyydetään lupaa
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);
            }


        } else {
            return true;
        }

        return false;
    }


    /**
     * Apufunktioita tallennukseen ja lataamiseen
     ************************************************************************************************************
     */
    //Kutsuu muita tallennusfunkioita bitmapin ja puutteiden tallentamiseen
    private void save(String fileName){
        //jos lisätty puutteita
        if(!flawInfoList.isEmpty()) {
            saveBitmap(fileName);
            saveFlaws(fileName);
        }
        else{
            // näytetään viesti virheestä
            toast(getString(R.string.noChanges));
        }
    }

    //Lataa galleriasta pohjapiirroksen
    private void loadImage(){
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RQS_IMAGE1);
    }

    //Luo uuden LoadProjectFragmentin jossa voi valita ladattavan projektin. Kutsuu tiedostonimen perusteella loadProject()
    private void loadData(){
        LoadProjectFragment loadFragment = new LoadProjectFragment();

        loadFragment.show(getSupportFragmentManager(), "loadFragment");
    }

    //Tallentaa nimellä joko projektin: finalSave=false, tai kuvan ja puutteet: finalSave=true
    public void showSaveAsDialog(final boolean finalSave){
        //TODO Tälle oma luokka

        // luodaan dialogi
        AlertDialog.Builder builder =
                new AlertDialog.Builder(MainActivity.this);
        final View saveDialogView = MainActivity.this.getLayoutInflater().inflate(
                R.layout.saveas_fragment, null);

        builder.setView(saveDialogView); // lisätään GUI dialogiin

        // asetetaan dialogin viesti
        //builder.setTitle(R.string.saveas);

        // liitetään textInputit:t
        final TextInputLayout saveTI = (TextInputLayout) saveDialogView.findViewById(R.id.saveAsTextInput);
        TextView tv = (TextView) saveDialogView.findViewById(R.id.saveAsTextView);

        if(finalSave){
            tv.setText(R.string.saveimageas);
        }
        else{
            tv.setText(R.string.saveprojectas);
        }

        // lisätään Add flaw painike
        builder.setPositiveButton(R.string.save,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(checkPermission()){
                            //Kuvan ja puutteiden tallennus
                            if(finalSave) {
                                save(saveTI.getEditText().getText().toString());
                            }
                            //Projektin tallennus
                            else{
                                saveProject(saveTI.getEditText().getText().toString());
                            }
                        }
                    }
                });

        // näytetään dialogi
        builder.create().show();
    }

    /************************************************************************************************************
     */

    /**
     * VARSINAISIA TALLENNUS JA LATAUSFUNKTIOITA
     * ***********************************************************************************************************
     */

    private void saveBitmap(String fileName) {

        // käytetään kuvan nimenä "ConstructionManager" ja kellonaika
        final String name = dir + images + "/" + fileName + ".png";


        //screenshot tallennusratkaisu jossa kaikki lisätyt FABitkin tulee mukaan
        View v1 = getWindow().getDecorView().findViewById(R.id.imageRelativeLayout);
        v1.setDrawingCacheEnabled(true);
        Bitmap screenshot = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        try (FileOutputStream out = new FileOutputStream(name)) {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, out);
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void saveFlaws(String fileName) {

        String file = dir + csvs + "/" +  fileName + ".csv";
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(file));
            //otsikko
            outputStream.write((getString(R.string.header_number) + getString(R.string.sep)).getBytes());
            outputStream.write((getString(R.string.apartment) + getString(R.string.sep)).getBytes());
            outputStream.write((getString(R.string.room) + getString(R.string.sep)).getBytes());
            outputStream.write((getString(R.string.flaw) + "\n").getBytes());

            // flawInfoListasta kaikki objektit
            for (FlawInfo fi : flawInfoList) {
                outputStream.write((Integer.toString(fi.getCounter()) + getString(R.string.sep)).getBytes());
                outputStream.write((fi.getApartment() + getString(R.string.sep)).getBytes());
                outputStream.write((fi.getRoom() + getString(R.string.sep)).getBytes());
                outputStream.write((fi.getFlaw() + "\n").getBytes());
            }

            // näytetään viesti tallennuksesta
            toast(getString(R.string.message_csv_saved) +"\n" + file);


            //Tulostaa polun. Voi etsiä tiedoston koska en pääse emulaattorin tiedostoihin käsiksi muuten kuin File explorerin avulla
            System.out.println(file);

        } catch(Exception e) {
            e.printStackTrace();

            toast(getString(R.string.message_error_savingCSV));

        }

    }

    private void saveProject(String fileName) {
        //erota bitmap imageView:stä
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
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
            toast(getString(R.string.projectSaved));
        } catch (IOException i) {
            i.printStackTrace();
            toast(getString(R.string.projectSaveError));
        }


    }


    public void loadProject(String fileName) {
        SaveBitmap sb = new SaveBitmap();

        Bitmap tempBitmap;

        try {
            initialize();

            FileInputStream fileIn = new FileInputStream(dir + projects + "/" + fileName + "Bitmap.ser");
            //FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            sb.readObject(in);

            in.close();
            fileIn.close();

            tempBitmap = sb.getBm();

            Bitmap.Config config;
            config = tempBitmap.getConfig();

            bitmapMaster = Bitmap.createBitmap(
                    tempBitmap.getWidth(),
                    tempBitmap.getHeight(),
                    config);


            canvasMaster = new Canvas(bitmapMaster);
            canvasMaster.drawBitmap(tempBitmap, 0, 0, null);

            imageView.setImageBitmap(bitmapMaster);

            //Puutteet
            fileIn = new FileInputStream(dir + projects + "/" + fileName + "Save.ser");
            in = new ObjectInputStream(fileIn);

            flawInfoList = (List<FlawInfo>) in.readObject();

            fabList.clear();
            counter=1;

            blueprintLoaded = true;

        } catch (IOException | ClassNotFoundException i) {
            i.printStackTrace();
            toast(getString(R.string.projectLoadError));
        }

        //Luodaan flawActionButtonit uudestaan
        for(FlawInfo fi : flawInfoList){
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.leftMargin=fi.getLeftMargin();
            lp.topMargin=fi.getTopMargin();

            //Luo fabin ominaisuudet
            newFab(fi, lp);
        }

    }

    /************************************************************************************************************
     */


    /**
        Pienempiä apufunktioita
     */


    //alustaa muuttujat jos on tehty muutoksia ja ladataankin uusi kuva
    private void initialize(){
        //asetetaan numerointi oikein
        counter = 1;

        //tyhjennetään lista
        if (!flawInfoList.isEmpty()){
            flawInfoList.clear();
            System.out.println("new list");
        }

        //Poistaa FABit näytöltä
        for(FlawActionButton fab:fabList){
            layout.removeView(fab);
        }
    }

    //Luo ensimmäisellä kerralla projektikansion
    private void createAppDir(){
        File root = new File(dir);

        if(!root.exists()){
            if(!root.mkdirs()){
                // näytetään viesti virheestä
                toast(getString(R.string.dir_error));
            }
            File csvdir = new File(root.getPath()+csvs);
            csvdir.mkdir();
            File projectdir = new File(root.getPath()+projects);
            projectdir.mkdir();
            File imagedir = new File(root.getPath()+images);
            imagedir.mkdir();
        }
    }

    //Funktio ponnahdusilmoituksen esittämiseen
    private void toast(String s){
        Toast message = Toast.makeText(MainActivity.this, s,
                Toast.LENGTH_LONG);
        message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                message.getYOffset() / 4);
        message.show();
    }

}