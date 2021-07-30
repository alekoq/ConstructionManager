package com.example.constructionmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.rendering.*;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Base64;

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

    //NewProjectFAB aloitusnäyttöön
    Button newProjectBtn;

    //Lista kaikista FloatingActionButtoneista
    ArrayList<FlawActionButton> fabList = new ArrayList<>();

    //apumuttuja fabin irti päästämiseen kun siirretään
    private boolean isLongPressed=false;
    //apumuuttuja poista valikon näkymiseen
    private boolean showDelete=false;

    //apumuuttuja kuvan/pdf:n valitsemiseen (koska requestCode ei toimi, eikä intent.identifieriä voi käyttää
    boolean createFromPdf = false;



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

    //toastit thread classista tulee tämän kautta
    Handler mHandler;



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

        //Määrittää aloitusnäytön painikkeen
        setNewProjectBtn();

        isEditable = imageView.getDrawable() != null;

        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.STROKE);
        paintDraw.setColor(Color.RED);
        paintDraw.setStrokeWidth(5);

        paintText = new Paint();
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.RED);
        paintText.setTextSize(32);

        //Luo sovellukselle oman kansion jos sitä ei jo ole
        createAppDir();

        //poistamiseen popUp
        popUp = new PopupWindow(this);



        //näytön koko (Ei tarvita tällä hetkellä?)
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

        //Toastit toisesta threadista tämän kautta
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                if(msg.what == 11){
                    toast(getString(R.string.projectSaved));

                    //kaikki tallennettu
                    unsaved=false;
                }
                else if(msg.what == 10){
                    toast(getString(R.string.projectSaveError));
                }
            }
        };
    }

    private void setNewProjectBtn(){
        newProjectBtn = findViewById(R.id.newProject_btn);

        newProjectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(unsaved){
                    confirmContinue("project");
                }
                createNewProject();
            }
        });
    }

    private void createNewProject(){
        NewProjectFragment npf = new NewProjectFragment();
        npf.show(getSupportFragmentManager(), "newProject");
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
                if(unsaved){
                    confirmContinue("project");
                }
                else{
                    createNewProject();
                }
                return true;
            case R.id.save:
                if (blueprintLoaded && checkPermission()) {
                        //Luo alertdialogin jossa kysytään millä nimellä tallennetaan. Tallentaa samalla nimellä sekä kuvan että csv:n
                        showSaveAsDialog(true);
                }
                else
                    toast(getString(R.string.noChanges));
                return true;
            case R.id.saveProject:
                if(checkPermission())
                    saveProject(loadedSave);
                else
                    toast(getString(R.string.noPermission));
                return true;
            case R.id.saveAsProject:
                if(flawInfoList.size()>0 && checkPermission())
                    showSaveAsDialog(false);
                else
                    toast(getString(R.string.noChanges));
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
                            else if(functionCall=="project"){
                                createNewProject();
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

    //Ikonin värin muutos (kun valittuna) vaatii tämän
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.addFlaw);
        item.setIcon(icon);

        return true;
    }

    //onActivityResult (vanha ratkaisu) on vanhentunut (deprecated)
    ActivityResultLauncher<Intent> onActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK && result.getData() != null){
                        fileData = result.getData().getData();

                        //Jos valittu kuva eikä pdf:ää
                        if(!createFromPdf){
                            configBitmap();
                        }
                        else{
                            createFromPdf();
                        }
                    }
                }
            }
    );

    public void disableNewProjectBtn(){
        //Otetaan aloituspainike pois käytöstä ja näkyvistä
        newProjectBtn.setVisibility(View.GONE);
    }

    //Asettaa valitun kuvan bitmappiin/canvakselle
    public void configBitmap(){

        try {
            bitmapMaster = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(fileData));

            //imageView.setImageBitmap(bitmapMaster);
            setBitmap(bitmapMaster);

            //alustetaan tietyt muuttujat
            initialize();

            //Tarvitaan kuvan säilyttämiseen kun laitetta käännetään. EI TOIMINNASSA
            //imageRetainingFragment.setImage(bitmapMaster);

            //jos kuva ei löydy
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            toast(getString(R.string.fileNotFound));
        }
    }

    public void createFromPdf(){
        String filename = fileData.getLastPathSegment();

        try {
            renderPdf(filename);
        }catch (IOException e){
            e.printStackTrace();
            toast(getString(R.string.fileNotFound));
        }

    }

    //Näyttää oletuksena pdf:n 1. sivun imageViewissä TOIMII. Kysyy sivusta jos useampia.
    // Sivun valitsemisen takia pilkottu funktio kahteen osaan. Jälkimmäinen showPDF näyttää valitun sivun ja sulkee ParcelFileDescriptorin ja PdfRendererin
    private void renderPdf(String filename) throws IOException{
        int pageCount;

        //Tehdään välimuistiin kopio pdf-tiedostosta (koska polkua tiedostoon ei ole saatavilla ja fileDescriptor tarvitsee sen)
        File filecopy = new File(getCacheDir(), filename);
        if(!filecopy.exists()){
            //luetaan inputStreamilla tiedosto ja kirjoitetaan välimuistiin kopio
            InputStream inputStream = getContentResolver().openInputStream(fileData);
            FileOutputStream output = new FileOutputStream(filecopy);

            final byte[] buffer = new byte[1024];
            int size;
            while ((size = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            inputStream.close();
            output.close();
        }

        //Avataan kopio välimuistista
        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(filecopy, ParcelFileDescriptor.MODE_READ_ONLY);

        // create a new renderer
        if (fileDescriptor != null) {
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

            //tarkastetaan sivumäärä. Jos useampia, käyttäjä voi valita haluamansa sivun.
            pageCount = pdfRenderer.getPageCount();

            if(pageCount > 1){
                selectPdfPageDialogOpener(pdfRenderer, fileDescriptor, pageCount);
            }

            else {
                showPDF(pdfRenderer, fileDescriptor, 0);
            }
        }
    }

    private void showPDF(PdfRenderer pdfRenderer, ParcelFileDescriptor fileDescriptor, int pageToOpen) throws IOException{
        PdfRenderer.Page page = pdfRenderer.openPage(pageToOpen);

        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                Bitmap.Config.ARGB_8888);

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        //asetetaan luettu pdf (bitmap) imageViewiin
        setBitmap(bitmap);

        page.close();
        pdfRenderer.close();
        fileDescriptor.close();

        initialize();
        blueprintLoaded = true;
    }

    //Sivunumeron valitsemiseen jos PDF:ssä useita sivuja. HUOM! Jos tästä poistuu muuten kuin OK:lla jää renderit sulkematta
    private void selectPdfPageDialogOpener(final PdfRenderer pdfRenderer, final ParcelFileDescriptor fileDescriptor, int pageCount){
        // luodaan dialogi
        AlertDialog.Builder builder =
                new AlertDialog.Builder(MainActivity.this);
        final View selectPageDialogView = MainActivity.this.getLayoutInflater().inflate(
                R.layout.select_page, null);

        builder.setView(selectPageDialogView); // lisätään GUI dialogiin
        builder.setTitle(R.string.pdf);         //otsikko

        builder.setCancelable(false);

        //pudotusvalikko
        final Spinner spinner = (Spinner)selectPageDialogView.findViewById(R.id.pageSpinner);

        //ladataan sivunumerot pudotusvalikkoon
        List<String> spinnerArray = new ArrayList<>();

        for (int i = 1; i <= pageCount; i++)
        {
                spinnerArray.add(Integer.toString(i));
        }

        //android.R.layout.simple_spinner_item
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_text, spinnerArray);

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        spinner.setAdapter(adapter);


        // lisätään Add flaw painike
        builder.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int page = Integer.parseInt(spinner.getSelectedItem().toString());
                        try {
                            showPDF(pdfRenderer, fileDescriptor, page-1);
                        } catch (IOException e){
                            e.printStackTrace();
                            toast(getString(R.string.pdfFail));
                        }
                    }
                });

        // näytetään dialogi
        builder.create().show();
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
     * FlawActionButtoneihin FAB liittyvät funktiot
     * ***********************************************************************************************************
     */

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
        fab.setBackgroundResource(0);
        //fab.setBackgroundColor(Color.RED);
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

    //fabin poistamiseen
    public void deleteFAB(FlawActionButton fab){
        flawInfoList.remove(fab.getFlawInfo());
        layout.removeView(fab);
    }
    /**
     -------------------------------------------------------------------------------------------------------------
     */

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

    /**
     -------------------------------------------------------------------------------------------------------------
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

        //TODO Tarkista onko tiedosto olemassa ja kysy käyttäjältä jatkosta

        /*
        File f = new File(file);
        if(f.exists()){
            toast(getString(R.string.fileExists));
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
        saveProject sp = new saveProject(fileName, this, imageView, flawInfoList, dir, mHandler);
    }


    public void loadProject(String fileName) {
        SaveBitmap sb = new SaveBitmap();

        try {
            initialize();

            FileInputStream fileIn = new FileInputStream(dir + projects + "/" + fileName + "Bitmap.ser");
            //FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            sb.readObject(in);

            in.close();
            fileIn.close();

            bitmapMaster = sb.getBm();

            setBitmap(bitmapMaster);

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

    public void setBitmap(Bitmap bm){
        //asetetaan bitmap
        imageView.setImageBitmap(bm);
        //boolean bitmapin asettamisen tarkistamiseksi (ei voida esim tallentaa jos tämä false)
        blueprintLoaded = true;
        //otetaan aloituspainike pois käytöstä
        disableNewProjectBtn();
    }

    //alustaa muuttujat jos on tehty muutoksia ja ladataankin uusi kuva
    public void initialize(){
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
    public void toast(String s){
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