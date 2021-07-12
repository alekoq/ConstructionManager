package com.example.constructionmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
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
import android.graphics.RectF;
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
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

//import com.sun.pdfview.PDFFile;
//import com.sun.pdfview.PDFPage;



public class MainActivity extends AppCompatActivity{

    //säilyttää kuvan kun laitetta käännetään
    //private static final String FRAGMENT_NAME = "imageFragment";
    //private ImageRetainingFragment imageRetainingFragment;

    //private static final int GALLERY_REQUEST_CODE = 123;

    // Millä encodella tehdään csv. Jos tämä on false, excel ei tunnista ääkkösiä
    private boolean ansi=true;

    public String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/CM";
    public String images = "/images";
    public String csvs = "/csvs";
    public String projects = "/projects";

    //tähän tallennetaan tallennuksen nimi, kun on ladattu tallennus
    public String loadedSave = "";
    MenuItem saveProjectMenuItem;
    boolean spmiEnabled=false; //pakollinen apumuuttuja koska menu ladataan uudestaan useasti (spmi=SaveProjectMenuItem)

    //tallentamattomia muutoksia
    public boolean unsaved=false;

    private boolean blueprintLoaded = false;
    public Uri fileData;
    private boolean isEditable = false;
    private boolean check = true;

    public int counter;  //laskee virheiden määrän
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    ImageView imageView;
    Drawable icon;
    RelativeLayout layout;

    final int RQS_IMAGE1 = 1;
    private static final int CHOOSE_FILE_REQUESTCODE = 8777;
    private static final int PICKFILE_RESULT_CODE = 8778;

    Bitmap bitmapMaster;
    Canvas canvasMaster;

    public int prvX, prvY;

    Paint paintDraw;
    Paint paintText;
    int centerText = 45;

    //Lista kaikista FloatingActionButtoneista
    ArrayList<FlawActionButton> fabList = new ArrayList<>();

    //apumuttuja fabin irti päästämiseen kun siirretään
    private boolean isLongPressed=false;
    //apumuuttuja poista valikon näkymiseen
    private boolean showDelete=false;



    //COLORS FOR FAB
    int[][] states = new int[][] {
            new int[] { android.R.attr.state_enabled}, // enabled
            //new int[] {-android.R.attr.state_enabled}, // disabled
            //new int[] {-android.R.attr.state_checked}, // unchecked
            //new int[] { android.R.attr.state_pressed}  // pressed
    };


    int[] colors = new int[] {
            Color.BLACK
            //Korjaus valmis esim. Color.LTGRAY
    };

    ColorStateList colorList = new ColorStateList(states, colors);
    

    //Lista puutteista
    List<FlawInfo> flawInfoList = new ArrayList<FlawInfo>();

    PopupWindow popUp;

    // tallentaa viimeisimmän kosketuksen koordinaatit. Hyödynnetään popUpWindowissa
    private float[] lastTouchDownXY = new float[2];

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

        //poistamiseen popUp
        popUp = new PopupWindow(this);


        //näytön koko
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        layout.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //jos kuva lisätty, voidaan lisätä myös merkintöjä
                if(isEditable){
                    int action = event.getAction();
                    prvX = (int) event.getX();
                    prvY = (int) event.getY();

                    //tarkistetaan että ollaan kuvan sisällä
                    if(action == MotionEvent.ACTION_DOWN){

                        //Dialogi jossa täytetään tiedot, ja painamalla Add lisätään fab ja tallennetaan tiedot
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
                showInfoFragment(fab);
            }
        });


        //Kun painetaan pitkään voidaan merkintää siirtää
        fab.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                isLongPressed=true;

                /*
                //Avaa poistovalikko
                showDelete = true;

                // retrieve the stored coordinates
                int x = (int)fab.getX();
                int y = (int)fab.getY();
                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                deletePopup dp = new deletePopup(imageView, x,y,width,height);
*/

                

                v.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_MOVE:
                                //Poista merkintä häviää
                                if(showDelete){
                                    showDelete = false;
                                    //popup.dismiss();
                                }



                                //Arvot saatu puhtaasti testaamalla
                                view.setX(event.getRawX() - imageView.getWidth()/50); //Puhelin : (event.getRawX() - imageView.getWidth()/30)
                                view.setY(event.getRawY() - imageView.getHeight()/6 ); //Puhelin: (event.getRawY() - imageView.getHeight()/4 - imageView.getHeight()/30)
                                break;
                            case MotionEvent.ACTION_UP:
                                // Painalluksen loppuessa asetetaan fabin uuden sijainnin arvo
                                if(isLongPressed){
                                    //Määritetään fabin FlawInfo-luokalle xy-koordinaatit niiden selaamista varten
                                    int[] loc = new int[2];
                                    view.getLocationOnScreen(loc);
                                    fab.getFlawInfo().setLeftMargin(loc[0]);
                                    //menubar ilmeisesti laittaa tämän liian alas. Korjaan kokeilemalla arvoja koska en tiedä mistä saa menun korkeuden
                                    fab.getFlawInfo().setTopMargin(loc[1]-175);
                                    //päästettiin irti
                                    isLongPressed=false;

                                    //tallentamattomia muutoksia
                                    unsaved=true;
                                }
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

        //Jos puutteen tietojen counter on suurempi kuin yleiscounter, päivitetään yleiscounter
        // Tilanne voi olla tämä jos ladataan vanha projekti josta on poistettu puutteita
        if(fi.getCounter() > counter){
            counter = fi.getCounter();
        }

        //Painikkeen asetukset
        fab.setAlpha(0.65f);
        fab.setBackgroundColor(Color.RED);
        fab.setBackgroundTintList(colorList);
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
        //tallentamattomia muutoksia
        unsaved=true;
    }


    //funktio avaa flawFragmentin kun lisätään uutta Puutetta
    public void showFlawFragment(){
        //Luo uuden flawFragmentin ja näyttää sen
        AddFlawFragment flawFragment = new AddFlawFragment();
        flawFragment.show(getSupportFragmentManager(), "flawFragment");

    }

    //funktio avaa flawinfoFragmentin kun tarkastellaan vanhaa Puutetta
    public void showInfoFragment(FlawActionButton fab){

        //Luo uuden infoFlawFragmentin ja lähettää sille fabin flawInfon argumenttina
        FlawInfoFragment infoFragment = FlawInfoFragment.newInstance(0, fab);

        infoFragment.show(getSupportFragmentManager(), "infoFragment");
    }

    //fabin poistamiseen
    public void deleteFAB(FlawActionButton fab){
        flawInfoList.remove(fab.getFlawInfo());
        layout.removeView(fab);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        saveProjectMenuItem = menu.findItem(R.id.saveProject);
        saveProjectMenuItem.setEnabled(spmiEnabled);
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
            case R.id.newProject:
                NewProjectFragment npf = new NewProjectFragment();
                npf.show(getSupportFragmentManager(), "newProject");
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
                if(checkPermission())
                    saveProject(loadedSave);
                return true;
            case R.id.saveAsProject:
                if(flawInfoList.size()>0 && checkPermission())
                    showSaveAsDialog(false);
                return true;
            case R.id.loadProject:
                if(unsaved){
                    confirmContinue("data");
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

    public void confirmContinue(final String functionCall){
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
                        if(checkPermission()){
                            if(functionCall=="data"){
                                loadData();
                            }
                            else if(functionCall=="image"){
                                loadImage();
                            }
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

    //Tarkistaa onko tallentamattomia muutoksia, sitten avaa gallerian
    public void openGallery(){
        if(unsaved) {
            confirmContinue("image");
        }
        else if (checkPermission()){
            loadImage();
        }
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


        if (resultCode == RESULT_OK  && data != null) {

            if (requestCode == RQS_IMAGE1) {
                    fileData = data.getData();
                    //Asettaa valitun kuvan bitmappiin/canvakselle
                    configBitmap();

            }
            else if(requestCode == PICKFILE_RESULT_CODE){
                fileData = data.getData();
                pdfToImage();
                //TODO PDF to image
            }
        }
    }

    //Asettaa valitun kuvan bitmappiin/canvakselle
    private void configBitmap(){
        Bitmap tempBitmap;

        try {
            tempBitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(fileData));

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

    private void pdfToImage(){
        byte[] bytes;
        try {

            File file = new File(fileData.getPath());
            FileInputStream is = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();
            bytes = new byte[(int) length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }

/**
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            String data = Base64.encodeToString(bytes, Base64.DEFAULT);
            PDFFile pdf_file = new PDFFile(buffer);
            PDFPage page = pdf_file.getPage(2);

            RectF rect = new RectF(0, 0, (int) page.getBBox().width(),
                    (int) page.getBBox().height());
            //  Bitmap bufferedImage = Bitmap.createBitmap((int)rect.width(), (int)rect.height(),
            //        Bitmap.Config.ARGB_8888);

            Bitmap image = page.getImage((int)rect.width(), (int)rect.height(), rect);
            FileOutputStream os = new FileOutputStream(this.getFilesDir().getAbsolutePath()+"/pdf.jpg");
            image.compress(Bitmap.CompressFormat.JPEG, 80, os);
*/
            //((ImageView) findViewById(R.id.testView)).setImageBitmap(image);

        } catch (Exception e) {
            e.printStackTrace();
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

    public void loadPDF(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
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


        /*
        TODO Tarkista onko tiedosto olemassa ja kysy käyttäjältä jatkosta

        File f = new File(file);
        if(f.exists()){

        }
        */

        try {
            //Kaksi eri tallennusformaattia. Ansi mahdollistaa ääkköset (jos ei ansi, excel ei ainakaan tunnista ääkkösiä)
            if(ansi){
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true),
                    "windows-1252");
            writer.append(getString(R.string.header_number) + getString(R.string.sep));
            writer.append(getString(R.string.apartment) + getString(R.string.sep));
            writer.append(getString(R.string.room) + getString(R.string.sep));
            writer.append(getString(R.string.flaw) + "\n");

            // flawInfoListasta kaikki objektit
            for (FlawInfo fi : flawInfoList) {
                writer.append((fi.getCounter() + getString(R.string.sep)));
                writer.append(fi.getApartment() + getString(R.string.sep));
                writer.append((fi.getRoom() + getString(R.string.sep)));
                writer.append((fi.getFlaw() + "\n"));
            }

            writer.close();
            }

            else {
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
            }
            // näytetään viesti tallennuksesta
            toast(getString(R.string.message_csv_saved) +"\n" + file);

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

            //ei tallentamattomia muutoksia
            unsaved=false;

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

            //Asetetaan ladattu tallennuspolku (tai tiedostonimi) ja avataan menuitem saveProject käyttöön
            loadedSave=fileName;
            spmiEnabled=true;
            saveProjectMenuItem.setEnabled(true);

            //Luodaan flawActionButtonit uudestaan
            for(FlawInfo fi : flawInfoList){
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.leftMargin=fi.getLeftMargin();
                lp.topMargin=fi.getTopMargin();

                //Luo fabin ominaisuudet
                newFab(fi, lp);
            }

            //ei vielä tallentamattomia muutoksia
            unsaved=false;

        } catch (IOException | ClassNotFoundException i) {
            i.printStackTrace();
            toast(getString(R.string.projectLoadError));
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

        //Nollataan tämä ettei voi tallentaa aiemmin ladatun tiedoston päälle
        loadedSave="";
        spmiEnabled=false;
        saveProjectMenuItem.setEnabled(false);
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

    //Näppäimistön piilottamiseen
    public void hideKeyboard(View view) {
        InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //Poistamisen yhteydessä päivitetään counter, jos poistetaan viimeisin merkintä.
    //Eli jos mennään merkinnässä 5, poistetaan se merkintä, ei jatketa 6:sta vaan 5:sta
    public void updateCounterValue(int fabCounter){
        if(fabCounter == counter-1){
            counter--;
        }
    }

    /**
     ****************************************** Sovelluksesta poistuminen ************************************************
     */

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                //.setTitle("Closing Activity")
                .setMessage(R.string.closingWarning)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton(R.string.ret, null)
                .show();
    }

}