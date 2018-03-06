package dk.stbn.testts;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.*;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import android.view.inputmethod.*;
import android.content.*;


import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.ui.*;

import android.os.AsyncTask;
import android.support.v7.app.*;

import java.util.ArrayList;
import java.util.Random;

import dk.stbn.testts.lytter.Lytter;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.Logger;


public class MainActivity extends AppCompatActivity implements OnClickListener, AdapterView.OnItemClickListener, PlaybackControlView.VisibilityListener, Lytter {

    // -- Views mm
    SimpleExoPlayer afsp;
    ImageButton søgeknap;
    TextView loop, langsom, flereFund;
    CheckBox loopcb, langsomcb;
    AutoCompleteTextView søgefelt;
    ImageView mere, logo;
    ArrayAdapter autoSuggest;
    FrameLayout fl;
    LinearLayout søgebar;

    private RecyclerView hovedlisten;
    private RecyclerView.Adapter adapter;

    AlertDialog netværksdialog;
    ProgressBar vent;
    ProgressDialog pDialog;

    // -- Sys
    Appl a;
    SharedPreferences sp;
    Context ctx;

    // -- Data
    //String baseUrlVideo = "http://tegnsprog.dk/video/t/"; //+" t_"+vNr+".mp4"		kaffe = 317
/*
    String baseUrlVideo = "http://m.tegnsprog.dk/video/mobil/t-webm/";
    String søgeurl1 = "http://tegnsprog.dk/#|tegn|386|soeg|/'tekst/'";
    String søgeurl2 = "%7Cresultat%7C10%7Ctrestjerner%7C1";
    String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
    String baseUrlArtikler = "http://m.tegnsprog.dk/artikler/"; //+artNr+".htm"		kaffe = 386
    String baseUrlBillede = "http://tegnsprog.dk/billede_t/"; //+"f_"+bNr+".jpg"	kaffe = 314
    String kaffe = baseUrlVideo + "t_317.mp4";
    String velkommen = "t_2079.mp4";

    //Eksempel: http://www.tegnsprog.dk/video/t/t_2079.mp4
*/
    // - Tilstand
    boolean tomsøg = true;
    boolean liggendeVisning;
    boolean aktGenstartet = false;
    String søgeordVedMistetForbindelse = ""; //ikke nødvendigvis det samme som a.akuteltsøgeord
    boolean søgebarLille = false;

    //int viserposition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        p("ONCREATE");
        setContentView(R.layout.main);
        a = Appl.a;

        ctx = this;
        a.lyttere.add(this); //registrerer aktiviteten som lytter
        aktGenstartet = a.dataKlar; //Hvis aktiviteteten lukkes og åbnes igen er data klar og vi skal køre run() for at sætte adapteren på autocompletelisten
        sp = a.sp; //SharefPreferences

        //Views mm
        vent = findViewById(R.id.progressBar);
        vent.setVisibility(View.GONE);
        søgeknap = findViewById(R.id.mainButton);
        søgeknap.setEnabled(false);
        hovedlisten = findViewById(R.id.hovedlisten);
        søgebar = findViewById(R.id.søgebar);
        adapter = new Hovedliste_adapter(a.søgeresultat, this);
        hovedlisten.setAdapter(adapter);
        hovedlisten.setLayoutManager(new LinearLayoutManagerWrapper(this));
        liggendeVisning = liggendeVisning();
        fl = findViewById(R.id.fl);
        fl.bringToFront();
        fl.invalidate();
        fl.setAlpha(0);
        flereFund = findViewById(R.id.antalFund);
        søgefelt = findViewById(R.id.søgefelt);
        loop = findViewById(R.id.looptv);
        loopcb = findViewById(R.id.loopcb);
        loopcb.setChecked(a.loop);
        langsomcb = findViewById(R.id.langsomcb);
        langsom = findViewById(R.id.langsomtv);
        langsomcb.setChecked(a.slowmotion);
        logo = findViewById(R.id.overskriftLogo);

        sætLyttere();

        if (savedInstanceState != null || aktGenstartet) {

            grunddataHentet();
            p("Startet ved skærmvending. Eller akt har været lukket. Initialiserer autocomplete-listen (sæt adapter)");

        }

        //--Viser en dialog hvis brugeren kører en nyligt opdateret version af appen
        String gemtVersionsNr = a.sp.getString("versionsnr", "helt ny");
        String versionsnummer = a.versionsnr();
        p("Gemt versionsnr: "+ gemtVersionsNr + "  Aktuelt versioinsnr: "+versionsnummer);
        if (gemtVersionsNr.equals("helt ny")) a.sp.edit().putString("versionsnr", versionsnummer).commit();
        else if(!versionsnummer.equals(gemtVersionsNr) ) {
            infodialog("Nyeste ændringer: \n"+ Utill.changelog, "Du har netop installeret den nyeste version: "+a.versionsnr());
            a.sp.edit().putString("versionsnr", versionsnummer).commit();
        }

        skjulTastatur();

        p("onCreate færdig");

    }

    @Override
    public void onClick(View klikket) {

        if (klikket == søgeknap) {
            String søgeordF = forberedSøgning();
            søg(søgeordF);
        } else if (klikket == loopcb) {
            p("Loop-checkbox klikket");

            sp.edit().putBoolean("loop", loopcb.isChecked()).commit();
            a.loop = loopcb.isChecked();
            if (afsp == null) return;
            a.position = afsp.getCurrentPosition();
            p("position: " + a.position);
            if (a.loop) {
                afsp.setRepeatMode(Player.REPEAT_MODE_ONE);
                afsp.seekTo(0);
                afsp.setPlayWhenReady(true);

            } else afsp.setRepeatMode(Player.REPEAT_MODE_OFF);
            p("Repeatmode: " + afsp.getRepeatMode());
            a.opdaterLoop();

        } else if (klikket == langsomcb || klikket == loop) {
            a.slowmotion = !a.slowmotion;
            langsomcb.setChecked(a.slowmotion);
            float hast = (a.slowmotion) ? 0.25f : 1.0f;
            if (afsp != null)
                afsp.setPlaybackParameters(new PlaybackParameters(hast, 1));
            else { //Vi prøver en gang til om 150ms
                final float hast1 = hast;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (afsp != null) afsp.setPlaybackParameters(new PlaybackParameters(hast1, 1));
                        else p("Fejl: i onClick langsom/loop: Afspiller var null");
                    }
                }, 150);
            }
            a.opdaterHastighed();
        } else if (klikket == søgefelt) {
            søgefelt.setText("");
        } else if (a.test && (klikket == logo)) testSøgning(); //Til abetest

    }


    //** Til abetest
    private void testSøgning() {
        int længde = a.tilAutoComplete.size();
        if (længde <= 0) return;
        int i = new Random().nextInt(længde);
        String tilfældigtOrd = a.tilAutoComplete.get(i);
        søgefelt.setText(tilfældigtOrd);
        søgeknap.performClick();
    }

    //Flow: forberedSøgning -> søg() -> opdaterUI()
    private String forberedSøgning() {

        skjulTastatur();
        String søgeordet = søgefelt.getText().toString().toLowerCase().trim();
        p("forberedSøgning søgeord: " + søgeordet);

        søgefelt.setText("");
        if (søgeordet.equals("")) søgeordet = søgefelt.getHint().toString();

        søgefelt.setHint(søgeordet);

        //--Der blev trykket "Søg" uden at søgeordet var ændret
        if (søgefelt.getText().toString().equals(søgefelt.getHint().toString())){
            t("Skriv/vælg noget i søgefeltet");
            return "";
        }

        søgeknap.setEnabled(false);

        //--Viser progress mens der søges
        pDialog = new ProgressDialog(ctx);
        pDialog.setMessage("Søger...");
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Annuller", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pDialog.dismiss();
                søgeknap.setEnabled(true);
                //todo...
            }
        });
        pDialog.show();

        return søgeordet.toLowerCase();
    }


    private void opdaterUI(boolean tomSøgning, String søgeordInd) {

        p("opdaterUI kaldt! Var søgningen tom?  " + tomSøgning);
        tomsøg = tomSøgning;
        dismisSøgDialog ();
        if (tomSøgning) {
            p("Tomsøgning");
            tomsøgning(søgeordInd);
        } else {

            //-- Opdaterer synligheden for pilen "vis mere" og antal fund
            if (a.søgeresultat.size() < 2 || !a.visPil) {
               fl.setAlpha(0);

            } else {
                fl.setAlpha(0.9f);
                a.visPil = false;
            }
            flereFund.setText("" + a.søgeresultat.size());

            //--Først spilles det første fund i listen
            p("Søgeresultat size1: " + a.søgeresultat.size());

            if (a.søgeresultat != null && a.søgeresultat.size() > 0) {
                p("Søgeresultat size2: " + a.søgeresultat.size());
                Fund f = a.søgeresultat.get(0);
                f.initAfsp(this);

                afsp = f.afsp;
                if (a.loop) afsp.setRepeatMode(Player.REPEAT_MODE_ONE);
                float hast = (a.slowmotion) ? 0.25f : 1.0f;
                afsp.setPlaybackParameters(new PlaybackParameters(hast, 1));
                afsp.setPlayWhenReady(true);
            } else {
                p("Fejl: Ikke tom søgning, men søgeresultat var tomt!!!!!");
                søgeknap.setEnabled(true);
                return;
            }

            //--Derefter initialiseres alle andre afspillere i listen
            if (a.søgeresultat.size() > 1) {
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        for (int i = 1; i < a.søgeresultat.size(); i++)
                            a.søgeresultat.get(i).initAfsp(getApplicationContext());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        a.opdaterLoop();
                        a.opdaterHastighed();
                        p("Resultatliste længde: " + a.søgeresultat.size());

                    }
                }.execute();
            }
            hovedlisten.getRecycledViewPool().clear();
            adapter.notifyItemRangeChanged(0, a.søgeresultat.size() - 1);
            adapter.notifyDataSetChanged();
            søgeknap.setEnabled(true);
        }

    }

    //-- Starter i velkomst-tilstand og viser videoen med tegnet "Velkommen"
    void velkommen() {
        søgefelt.setHint(getString(R.string.hint));
        forberedSøgning();
        søg("velkommen");
    }

    void søg(String søgeordInd) {
        a.visPil = true;

        if (!a.harNetværk) {
            manglerNetværk();
            søgeordVedMistetForbindelse = søgeordInd;
            //netværksdialog = infodialog("Tjek dine netværksindstillinger", "Ingen netværksforbindelse");
            return;
        }
        p("søg(" + søgeordInd + ")");

        final String søgeord = søgeordInd.trim();
        a.aktueltSøgeord = søgeord;
        if (søgeord.equalsIgnoreCase(getString(R.string.hint))) {
            tomsøgning("");
            dismisSøgDialog();
            return;
        }

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                //Kan helt klart optimeres!

                a.søgeresultat.clear();
                Indgang fundet = null;
                for (int i = 0; i < a.søgeindeks.size(); i++) {
                    fundet = a.søgeindeks.get(i);
                    if (søgeord.equalsIgnoreCase(fundet.søgeord)) break;

                }
                if (fundet == null || !fundet.søgeord.equalsIgnoreCase(søgeord)) {
                    if (fundet == null) p("fundet var null");
                    else p("søgning var tom: " + fundet.søgeord);
                    return true;
                }

                p("søg() ordet: " + søgeord + " blev fundet i søgeindeks");
                p("søg(): Indgang fundet: " + fundet + "  index: " + fundet.index.size());
                for (String s : fundet.index) {
                    p("   index: " + s);
                    Fund f = a.hentArtikel(s);
                    f.nøgle = fundet.getSøgeord();
                    f.index = s;
                    a.søgeresultat.add(f);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                boolean tomsøgning = (boolean) o;

                opdaterUI(tomsøgning, søgeord);
                if (!tomsøgning) for (Fund f : a.søgeresultat) p("Tjekker fund: " + f);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        a.nystartet = false;
                    }
                }, 50);
                //;
            }
        }.execute();
    }

    void dismisSøgDialog () {
        if (pDialog == null)  {
            p("pdialog var null");
            //pDialog.dismiss();
        }
        else if (pDialog.isShowing()) {
            p("pdialog var isshowing");
            pDialog.dismiss();
        }
    }

    void tomsøgning(String søgeord) {
        a.søgeresultat.clear();
        Fund tom = new Fund(null, null);
        tom.nøgle = "Søgning på: '" + søgeord + "' gav 0 fund";
        a.søgeresultat.add(tom);
        hovedlisten.getRecycledViewPool().clear();
        adapter.notifyItemRangeChanged(0, 1);
        if (afsp != null) afsp.release();
        søgeknap.setEnabled(true);
    }

    void skjulTastatur() {
        View view = this.getCurrentFocus();
        if (view == null) {
            søgefelt.requestFocus();
            view = søgefelt;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    void sætLyttere() {

        søgeknap.setOnClickListener(this);
        loopcb.setOnClickListener(this);
        loop.setOnClickListener(this);
        langsomcb.setOnClickListener(this);
        langsom.setOnClickListener(this);
        søgefelt.setOnClickListener(this);

        //-- Søgeknappen på soft-keyboardet
        søgefelt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    søgeknap.performClick();
                    return true;
                }
                return false;
            }
        });

        søgefelt.setOnItemClickListener(this); //kun til autocomplete

        //Aktiverer debug-skærm
        søgeknap.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                testDialog("Vil du starte test/debug-skærmen?", "TEST/DEBUG");
                return false;
            }
        });

        //Til abetest
        if (a.test) logo.setOnClickListener(this);

        //Fjerner den gule boble med antal fund
        hovedlisten.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                fl.animate().alpha(0).setDuration(700);
            }
        });



    }// END sætLyttere()


    //-- Kun til autocomplete
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView t = (TextView) view;
        String s = forberedSøgning();
        p("onItemClick: fra TV: " + t.getText().toString() + "  |  Fra forbered: " + s);
        søg(t.getText().toString());
    }

    private boolean liggendeVisning() {
        int højde = Resources.getSystem().getDisplayMetrics().heightPixels;
        int bredde = Resources.getSystem().getDisplayMetrics().widthPixels;
        return (højde < bredde);
    }

    @Override
    public void onVisibilityChange(int p1) {
        p("Visibility " + p1);
    }

    @Override
    protected void onDestroy() {
        a.releaseAlle();
        a.lyttere.remove(this);// afregistrerer lytter
        a.nystartet = true; //Når Appl overlever, kaldes init ikke igen
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String s = "";
        if (søgefelt.getHint() != null) s = søgefelt.getHint().toString();
        //t("onsaveinstancestate: "+ s);
        outState.putString("søgeord", s);
        //outState.putInt("position", viserposition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        p("ONRESTOREINSTANCESTATE");
        //////////////////////  HUSK TJEK FOR OM TOM SØGNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
        String s = savedInstanceState.getString("søgeord");
        //viserposition = savedInstanceState.getInt("position");
        if (getString(R.string.hint).equalsIgnoreCase(s))
            s = "velkommen";
        if (s != null) {
            søgefelt.setHint(s);
            søg(s);
        }
        else tomsøgning(a.aktueltSøgeord);

    }

    @Override
    protected void onStop() {
        dismisSøgDialog();
        super.onStop();

    }

    //-- Eget lytter-inteface
    @Override
    public void grunddataHentet() {
        p("Grunddata hentet");
        søgeknap.setEnabled(true);
        autoSuggest = new ArrayAdapter(this, android.R.layout.simple_list_item_1, a.tilAutoComplete);
        søgefelt.setAdapter(autoSuggest);

        if (a.genstartetFraTestAkt) {
            a.genstartetFraTestAkt = false;
            søg(a.aktueltSøgeord);
        } else velkommen();
    }

    @Override
    public void logOpdateret() {

    }

    @Override
    public void netværksændring(boolean forbundet) {
        if (!forbundet) {
           if  (!a.nystartet) manglerNetværk();
        }
        else {
            if (netværksdialog != null) netværksdialog.dismiss();
            if (!a.nystartet) t("Nu forbundet til netværk");
            if (a.nystartet && a.dataHentet) grunddataHentet(); //ikke så pænt at aktivere lytteren herfra...

            søgeknap.setEnabled(true);

            p(a.aktueltSøgeord);

            //Hvis brugeren har forsøgt at søge mens forbindelsen var nede: lav ny søgning
            if (!a.nystartet && !søgeordVedMistetForbindelse.equals(a.aktueltSøgeord)) {
                søg(søgeordVedMistetForbindelse);
                p("Genetableret forbindelse. Søger på: "+ søgeordVedMistetForbindelse);
            }
        }
    }

    @Override
    public void fejlmeddelelse(String besked) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.mainlayout), besked, Snackbar.LENGTH_LONG);
        mySnackbar.setAction("genopfrisk", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                a.nulstilTilstandHeavy(); //ikke optimalt
            }
        });
        mySnackbar.show();
    }

    void manglerNetværk(){

        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.mainlayout),"Ingen neværksforbindelse", Snackbar.LENGTH_LONG);
        mySnackbar.setAction("prøv igen", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (a.netværksstatus == null) {
                    a.sætNetværkslytter();
                    a.init("Main snackbar (brugeren har trykket)");
                }
            }
        });
        mySnackbar.show();
    }


    boolean klikket = false; //Bruges ikke (endnu)

    private void testDialog(String besked, String overskrift) {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(overskrift);
        alertDialogBuilder
                .setMessage(besked)
                .setCancelable(true)
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        t("dialog");
                        klikket = true;
                        startActivity(new Intent(ctx, Test.class));
                        dialog.cancel();
                        finish();
                    }
                })
                .setNegativeButton("Nej", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        klikket = true;
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        if (klikket) {
            //doKeepDialog(alertDialogBuilder);
            klikket = false;
        }
        alertDialog.show();
    }

    private AlertDialog infodialog(String besked, String overskrift) {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(overskrift);
        alertDialogBuilder
                .setMessage(besked)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return alertDialog;
    }

    //Bevarer dialog ved skærmvending tilpasset fra http://stackoverflow.com/questions/8537518/the-method-getwindow-is-undefined-for-the-type-alertdialog-builder
    private static void doKeepDialog(AlertDialog.Builder dialog) {
        AlertDialog dlg = dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dlg.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dlg.getWindow().setAttributes(lp);
    }

    //--Convenience

    void p(Object o) {
        Utill.p("Main." + o);
    }

    void t(String s) {

        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    void ts(String s) {

        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////_______________________ADAPTER_______________________/////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class Hovedliste_adapter extends RecyclerView.Adapter<Hovedliste_adapter.ViewHolder> {

        ArrayList<Fund> data;
        Context c;
        Appl a = Appl.a;


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CardView c;
            com.google.android.exoplayer2.ui.SimpleExoPlayerView playerv;
            TextView overskrift;
            TextView fundtekst;
            boolean udfoldet = false;

            public ViewHolder(View v) {
                super(v);
                c = v.findViewById(R.id.kort);
                playerv = v.findViewById(R.id.afspillerview);
                playerv.setOnClickListener(this); //Virker ikke
                overskrift = v.findViewById(R.id.fundtekstOverskrift);
                fundtekst = v.findViewById(R.id.fundtekst);
                fundtekst.setOnClickListener(this);
                playerv.setControllerAutoShow(false);

                //Deaktiverer controls
                playerv.hideController();
                playerv.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
                    @Override
                    public void onVisibilityChange(int i) {
                        if (i == 0) {
                            playerv.hideController();
                        }
                    }
                });

                //Håndterer "klik" på video
                playerv.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                            //pauser hvis den spiller, spiller hvis den er pauset
                            SimpleExoPlayer p = playerv.getPlayer();
                            if (p == null) p("FEJL: Player var null i onTouch");
                            else {
                                boolean pause = p.getPlayWhenReady();
                                p.setPlayWhenReady(!pause);
                            }
                        }
                        return true; //Sender ikke touch videre
                    }
                });
            }

            //Håndterer klik på cardview
            @Override
            public void onClick(View view) {
                final int position = getAdapterPosition();

                //TODO: skelne mellem stående og liggende visning

                //TODO: gøre det samme ved scroll

                float højde = (float) Resources.getSystem().getDisplayMetrics().heightPixels/10;
                int tid = 400;

                udfoldet = !udfoldet;

                if(udfoldet) {
                    søgebar.animate().scaleY(0.0f).setDuration(tid);
                    hovedlisten.animate().translationY(-højde).setDuration(tid).start();//  translationY(0.5f);
                    t("Kommer snart: Detaljeret visning/fuld artikel");



                }
                else {

                    søgebar.animate().scaleY(1.0f).setDuration(tid);
                    hovedlisten.animate().translationY(0.0f).setDuration(tid).start();//  translationY(0.5f);

/*
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition((CardView) findViewById(R.id.kort));
                        ConstraintSet constraintSet1 = new ConstraintSet();
                        constraintSet1.clone(ctx,R.id.kort_lille);

                        ConstraintSet constraintSet2 = new ConstraintSet();
                        constraintSet1.clone(ctx,R.id.kort_stor);
                        if ()
                        ConstraintSet constraint = if (changed) constraintSet1 else constraintSet2
                        constraint.applyTo(constraintLayout)

                    }

*/


                }

                //søgebarLille = !søgebarLille;
            }
        }

        public Hovedliste_adapter(ArrayList søgeresultater, Context ctx) {
            data = søgeresultater;
            c = ctx;
        }

        @Override
        public Hovedliste_adapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {

            CardView rod = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.kort, parent, false);

            ViewHolder vh = new ViewHolder(rod);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            p("onBindViewHolder pos " + pos);
            Fund f = data.get(pos);
            holder.playerv.setPlayer(f.afsp);

            int parentes = f.nøgle.indexOf("(");
            if (parentes >0) {
                String overskriftDel1 = f.nøgle.substring(0, parentes).toUpperCase();
                String overskriftDel2 = f.nøgle.substring(parentes);
                holder.overskrift.setText(overskriftDel1 + overskriftDel2);
            }
            else holder.overskrift.setText(f.nøgle.toUpperCase());
            if (f.index != null) {
                holder.overskrift.append(" (" + f.index + ")");
                holder.fundtekst.setText(f.getTekst());
            } else holder.fundtekst.setText("");


        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    //fra https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
    public class LinearLayoutManagerWrapper extends LinearLayoutManager {

        public LinearLayoutManagerWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                p("Fejl: LLManagerWrapper IndexOutOfBoundsException " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
