package dk.stbn.testts;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.*;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    String baseUrlVideo = "http://m.tegnsprog.dk/video/mobil/t-webm/";
    String søgeurl1 = "http://tegnsprog.dk/#|tegn|386|soeg|/'tekst/'";
    String søgeurl2 = "%7Cresultat%7C10%7Ctrestjerner%7C1";
    String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
    String baseUrlArtikler = "http://m.tegnsprog.dk/artikler/"; //+artNr+".htm"		kaffe = 386
    String baseUrlBillede = "http://tegnsprog.dk/billede_t/"; //+"f_"+bNr+".jpg"	kaffe = 314
    String kaffe = baseUrlVideo + "t_317.mp4";
    String velkommen = "t_2079.mp4";

    //Eksempel: http://www.tegnsprog.dk/video/t/t_2079.mp4

    // - Tilstand
    boolean tomsøg = true;
    boolean liggendeVisning;
    boolean aktGenstartet = false;
    String søgeordVedMistetForbindelse = "";

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
        sp = a.sp;

        vent = (ProgressBar) findViewById(R.id.progressBar);
        vent.setVisibility(View.GONE);
        søgeknap = (ImageButton) findViewById(R.id.mainButton);
        søgeknap.setEnabled(false);

        hovedlisten = (RecyclerView) findViewById(R.id.hovedlisten);

        //hovedlisten.setHasFixedSize(true);

        adapter = new Hovedliste_adapter(a.søgeresultat, this);
        hovedlisten.setAdapter(adapter);
        hovedlisten.setLayoutManager(new LinearLayoutManagerWrapper(this));
        liggendeVisning = liggendeVisning();

        //mere = (ImageView) findViewById(R.id.mere);

        //mere.bringToFront();
        //mere.invalidate();

        //mere.setAlpha(0);
        fl = (FrameLayout) findViewById(R.id.fl);
        fl.bringToFront();
        fl.invalidate();

        fl.setAlpha(0);
        flereFund = (TextView) findViewById(R.id.antalFund);


        søgefelt = (AutoCompleteTextView) findViewById(R.id.søgefelt);
        loop = (TextView) findViewById(R.id.looptv);
        loopcb = (CheckBox) findViewById(R.id.loopcb);
        loopcb.setChecked(a.loop);
        langsomcb = (CheckBox) findViewById(R.id.langsomcb);
        langsom = (TextView) findViewById(R.id.langsomtv);
        langsomcb.setChecked(a.slowmotion);
        logo = (ImageView) findViewById(R.id.overskriftLogo);

        sætLyttere();

        if (savedInstanceState != null || aktGenstartet) {

            grunddataHentet();
            p("Startet ved skærmvending. Eller akt har været lukket. Initialiserer autocomplete-listen (sæt adapter)");

            //viserposition = sp.getInt("position", 0);

            //p("Viser position: "+viserposition);
        }

        //Viser brugeren en dialog hvis han/hun kører en nyligt opdateret verison af appen
        String gemtVersionsNr = a.sp.getString("versionsnr", "helt ny");
        String versionsnummer = a.versionsnr();
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
            //viserposition = 0;
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
            afsp.setPlaybackParameters(new PlaybackParameters(hast, 1));
            a.opdaterHastighed();
        } else if (klikket == søgefelt) {
            søgefelt.setText("");
        } else if (a.test && (klikket == logo)) testSøgning();

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

    //En slags hack hvis setPlayWhenReady(true/false) ikke kommer til at virke ordentligt
    void pauseVideo(boolean pause) {

        float valgtHast = (a.slowmotion) ? 0.25f : 1.0f;

        float hast = (pause) ? 0.0f : valgtHast;
        afsp.setPlaybackParameters(new PlaybackParameters(hast, 1));
    }


    private String forberedSøgning() {

        skjulTastatur();
        String søgeordet = søgefelt.getText().toString().toLowerCase().trim();
        p("forberedSøgning søgeord: " + søgeordet);

        søgefelt.setText("");
        if (søgeordet.equals("")) søgeordet = søgefelt.getHint().toString();

        søgefelt.setHint(søgeordet);

        if (søgefelt.getText().toString().equals(søgefelt.getHint().toString()))
            return ""; //Der blev trykket "Søg" uden at søgeordet var ændret
        søgeknap.setEnabled(false);
        //a.søgeresultat.clear();
        return søgeordet.toLowerCase();
    }


    private void opdaterUI(boolean tomSøgning, String søgeordInd) {

        p("opdaterUI kaldt! Var søgningen tom?  " + tomSøgning);
        tomsøg = tomSøgning;
        if (tomSøgning) {

            tomsøgning(søgeordInd);
        } else {

            //-- Opdaterer synligheden for pilen "vis mere"
            if (a.søgeresultat.size() < 2 || !a.visPil) {
                //mere.setAlpha(0);
                fl.setAlpha(0);

            } else {
                //mere.setAlpha(100);
                fl.setAlpha(0.9f);
                a.visPil = false;
            }
            flereFund.setText("Antal fund: " + a.søgeresultat.size());

            //Først spilles det første fund i listen
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

            //Derefter initialiseres alle andre afspillere i listen
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
        a.nystartet = false;
        if (!a.harNetværk) {
            manglerNetværk();
            søgeordVedMistetForbindelse = søgeordInd;
            //netværksdialog = infodialog("Tjek dine netværksindstillinger", "Ingen netværksforbindelse");
            return;
        }
        p("søg(" + søgeordInd + ")");

        //a.antalSøgninger++; // Bruges til at tjekke om onScroll er blevet kaldt når lytteren sættes eller om brugeren rent faktisk har scrollet (alternativ til onTouch)
        final String søgeord = søgeordInd.trim();
        a.aktueltSøgeord = søgeord;
        if (søgeord.equalsIgnoreCase(getString(R.string.hint))) {
            tomsøgning("");
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
                    Fund f = a.hentArtikel(s);//baseUrlArtikler+s+".html");
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
                //else tomsøgning(søgeord);

            }
        }.execute();


    }

    void tomsøgning(String søgeord) {
        //t("Din søgning på: '"+søgeord+ "' gav ikke noget resultat");  //resultat.setText("Ordet \""+søgeordF+ "\" findes ikke i ordbogen");

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
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view == null) {
            //p("skjulTastatur(), view var null");
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

        søgeknap.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                testDialog("Vil du starte test/debug-skærmen?", "TEST/DEBUG");
                return false;
            }
        });

        if (a.test) logo.setOnClickListener(this);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        hovedlisten.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //mere.setAlpha(0);
                fl.setAlpha(0);
            }
        });
        //}


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
            opdaterUI(false, s);
        } else tomsøgning(a.aktueltSøgeord);

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
            //vent.setVisibility(View.VISIBLE);
            //netværksdialog = infodialog("Tjek dine netværksindstillinger. Tryk evt. på Søg-knappen for at prøve igen", "Ingen netværksforbindelse");
            manglerNetværk();


        } else {
            if (netværksdialog != null) netværksdialog.dismiss();
            if (!a.nystartet) t("Nu forbundet til netværk");
            if (a.nystartet && a.dataHentet) grunddataHentet(); //ikke så pænt at aktivere lytteren herfra...

            søgeknap.setEnabled(true);
            if (pDialog != null)  pDialog.dismiss();
            p(a.aktueltSøgeord);

            //Hvis brugeren har forsøgt at søge mens forbindelsen var nede: lav ny søgning
            if (!a.nystartet && !søgeordVedMistetForbindelse.equals(a.aktueltSøgeord)) {
                søg(søgeordVedMistetForbindelse);
                p("Genetableret forbindelse. Søger på: "+ søgeordVedMistetForbindelse);
            }


        }


    }

    void manglerNetværk(){
        pDialog = new ProgressDialog(ctx);
        pDialog.setMessage("Ingen netværksforbindelse...");
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Prøv igen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (a.netværksstatus == null) a.init("Main (brugeren har trykket");
                //Andet?
            }
        });
        pDialog.show();

    }


    /*
        @Override
        public boolean onLongClick(View p1) {
            //Intent i = new Intent(this, Test.class);
            //startActivity(i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                //afspView.animate().scaleX(0.5f).scaleY(0.5f);
            }

            return true;
        }
    */

    //////////////////////////_______________________ADAPTER_______________________/////////////////////////////////

    public class Hovedliste_adapter extends RecyclerView.Adapter<Hovedliste_adapter.ViewHolder> {

        ArrayList<Fund> data;
        Context c;
        Appl a = Appl.a;


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CardView c;
            com.google.android.exoplayer2.ui.SimpleExoPlayerView playerv;
            TextView overskrift;
            TextView fundtekst;

            public ViewHolder(View v) {
                super(v);
                c = (CardView) v.findViewById(R.id.kort);
                playerv = (SimpleExoPlayerView) v.findViewById(R.id.afspillerview);
                playerv.setOnClickListener(this); //Virker ikke
                overskrift = (TextView) v.findViewById(R.id.fundtekstOverskrift);
                fundtekst = (TextView) v.findViewById(R.id.fundtekst);
                fundtekst.setOnClickListener(this);
                //playerv.setControllerShowTimeoutMs(1500);
                //playerv.hideController();
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

                playerv.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                            //pauser hvis den spiller, spiller hvis den er pauset
                            SimpleExoPlayer p = playerv.getPlayer();
                            boolean pause = p.getPlayWhenReady();
                            p.setPlayWhenReady(!pause);
                        }


                        return true; //Sender ikke touch videre
                    }
                });
            }

            @Override
            public void onClick(View view) {
                final int position = getAdapterPosition();
                Toast.makeText(getApplicationContext(), "Tegn nr " + position + " klikket. \nFunktionen er endnu ikke implemteret", Toast.LENGTH_LONG).show();

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
            holder.overskrift.setText(f.nøgle);
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

    boolean klikket = false;

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


    void p(Object o) {
        Utill.p("Main." + o);
    }

    void t(String s) {

        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    void ts(String s) {

        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


}
