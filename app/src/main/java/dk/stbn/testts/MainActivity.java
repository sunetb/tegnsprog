package dk.stbn.testts;

import android.content.res.Resources;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.*;

import android.net.*;
import android.view.View.*;
import android.view.*;
import android.view.inputmethod.*;
import android.content.*;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.ui.*;
import com.google.android.exoplayer2.trackselection.*;
import com.google.android.exoplayer2.upstream.*;
import com.google.android.exoplayer2.util.*;
import com.google.android.exoplayer2.extractor.*;
import com.google.android.exoplayer2.source.*;

import android.os.AsyncTask;
import android.support.v7.app.*;
import android.widget.AbsListView.*;



public class MainActivity extends AppCompatActivity implements OnClickListener, AdapterView.OnItemClickListener, com.google.android.exoplayer2.ui.PlaybackControlView.VisibilityListener, OnLongClickListener, Runnable{

	// -- Views mm
	SimpleExoPlayer player;
	SimpleExoPlayerView v;
	ImageButton søgeknap;
	TextView  loop;
	CheckBox loopcb, langsomcb;
	ListView resultatliste;
	AutoCompleteTextView søgefelt;
	ImageView mere;
	ArrayAdapter autoSuggest, resultaterListeAdapter;

	// -- Sys
	Appl a;
	SharedPreferences sp;

	// -- Data
	String baseUrlVideo = "http://tegnsprog.dk/video/t/"; //+" t_"+vNr+".mp4"		kaffe = 317
	String søgeurl1 = "http://tegnsprog.dk/#|tegn|386|soeg|/'tekst/'";
	String søgeurl2 = "%7Cresultat%7C10%7Ctrestjerner%7C1";
    String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
	String baseUrlArtikler ="http://m.tegnsprog.dk/artikler/"; //+artNr+".htm"		kaffe = 386
	String baseUrlBillede = "http://tegnsprog.dk/billede_t/"; //+"f_"+bNr+".jpg"	kaffe = 314
	String kaffe  = baseUrlVideo +"t_317.mp4";
	String velkommen = "t_2079.mp4";

	// - Tilstand
	boolean tomsøg = true;
	boolean liggendeVisning;
	int viserposition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        a = Appl.a;
		a.main = this; //registrerer aktiviteten som lytter
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		v = (SimpleExoPlayerView) findViewById(R.id.mainVideoView);
		player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(new AdaptiveVideoTrackSelection.Factory(new DefaultBandwidthMeter())), new DefaultLoadControl());
		v.setPlayer(player);
		v.setControllerShowTimeoutMs(1);

		søgeknap = (ImageButton) findViewById(R.id.mainButton);
		søgeknap.setEnabled(false);

		liggendeVisning = liggendeVisning();
		resultatliste = (ListView) findViewById(R.id.fundliste);

		int listelayout = R.layout.listelayout;

		if (liggendeVisning) listelayout = R.layout.listelayout_land;

		//-- Listen med søgeresultater
		resultaterListeAdapter = new ArrayAdapter(this, listelayout, R.id.tekst, a.søgeresultat){

			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

				View rod = super.getView(position, convertView, parent);

				p("getview resultatlisteadapter pos"+position);
				ImageView iv = (ImageView) rod.findViewById(R.id.billede);
				iv.setImageResource(R.drawable.kaffef314);
				TextView t = (TextView) rod.findViewById(R.id.tekst);
				t.setText(a.søgeresultat.get(position).toString());//søgeresultat.get(position).getTekst());
				Button b = (Button) rod.findViewById(R.id.knap);
				b.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View p1) //-- Kan også aktiveres med langt klik
						{
							Intent i = new Intent (MainActivity.this, FuldArtikel_akt.class);
							startActivity(i);
						}
						
						
					});
				return rod;
			}

			@Override
			public int getCount() {
				return a.søgeresultat.size();
			}
		};
		resultatliste.setAdapter(resultaterListeAdapter);

		//-- Den lille pil som indikerer at der er flere resultater
		mere = (ImageView) findViewById(R.id.mere);
		mere.setAlpha(0);

		søgefelt = (AutoCompleteTextView) findViewById(R.id.søgefelt);
		loop = (TextView) findViewById(R.id.looptv);
		loopcb = (CheckBox) findViewById(R.id.loopcb);
		loopcb.setChecked(a.loop);
		langsomcb = (CheckBox) findViewById(R.id.langsomcb);

		sætLyttere();

		if (savedInstanceState != null) {

			this.run(); p("Startet ved skærmvending. Initialiserer autocomplete-listen (sæt adapter)");
			viserposition = sp.getInt("position", 0);

			p("Viser position: "+viserposition);


		}
		else velkommen();

		skjulTastatur();

		p("onCreate færdig");
    }

	@Override
	public void onClick(View klikket) {

		if (klikket == søgeknap) {
			viserposition = 0;
			String søgeordF = forberedSøgning();
			boolean søgeResultat = søg(søgeordF);
            if (!søgeResultat) {
                //resultat.setText("Ordet \""+søgeordF+ "\" findes ikke i ordbogen");
            }
		}
		else if (klikket == loopcb ) {
			p("Loop-checkbox klikket");
			sp.edit().putBoolean("loop", loopcb.isChecked()).commit();

			if (loopcb.isChecked())
				opdaterUI(false, "whatever", viserposition);
				//player.setPlayWhenReady(true); //Virker rigtig dårligt!!!!
			else
				opdaterUI(false, "whatever", viserposition);
				//player.setPlayWhenReady(false);
		}
		else if (klikket == langsomcb)ts("Ikke implementeret endnu");
		else if (klikket == søgefelt) {
			p("søgefelt klikket");
			søgefelt.setText("");
		}
		
	}


	private String forberedSøgning(){

        v.setControllerShowTimeoutMs(1200); /// tiden før knapperne skjules automatisk
        skjulTastatur();
        String søgeordet = søgefelt.getText().toString().toLowerCase().trim();
        p("forberedSøgning søgeord: " + søgeordet);

        søgefelt.setText("");
        if (søgeordet.equals("")) søgeordet = søgefelt.getHint().toString();

        søgefelt.setHint(søgeordet);

        if (søgefelt.getText().toString().equals(søgefelt.getHint().toString())) return "";
        søgeknap.setEnabled(false);
        a.søgeresultat.clear();
        return søgeordet.toLowerCase();
	}


	private void opdaterUI (boolean tomSøgning, String søgeordInd, int pos){

		p("opdaterUI kaldt! Var søgningen tom?  "+ tomSøgning);
		tomsøg = tomSøgning;
        if (tomSøgning) {
			tomsøgning(søgeordInd);
            player.setPlayWhenReady(false);
        }
        else {
			resultaterListeAdapter.notifyDataSetChanged();

			//-- Opdaterer synligheden for pilen "vis mere"
			if (a.søgeresultat.size() < 2 || !a.visPil) {
				mere.setAlpha(0);
			}

			else	{
				mere.setAlpha(100);
				a.visPil = false;
			}

            try {
				MediaSource ms1;
				if (a.loop)
                ms1 = lavLoopKilde(a.søgeresultat.get(pos).videourl);
				else
				ms1 = lavKilde(a.søgeresultat.get(pos).videourl);
                player.prepare(ms1);
                player.setPlayWhenReady(true);
				v.setVisibility(View.VISIBLE);

			}
            catch (Exception e){
                e.printStackTrace();
			}
        }
		søgeknap.setEnabled(true);
		skjulTastatur();

	}

	//-- Starter i velkomst-tilstand og viser videoen med tegnet "Velkommen"
	void velkommen (){
		søgefelt.setHint(getString(R.string.hint)); //Hvorfor har jeg udkommmenteret den?
		forberedSøgning();
		søg("velkommen");
	}


	

	boolean søg (String søgeordInd){
		a.visPil = true;
		p("søg("+søgeordInd+")");
		//a.antalSøgninger++; // Bruges til at tjekke om onScroll er blevet kaldt når lytteren sættes eller om brugeren rent faktisk har scrollet (alternativ til onTouch)
		final String søgeord = søgeordInd.trim();

		if (søgeord.equalsIgnoreCase(getString(R.string.hint))) {
			tomsøgning(søgeord);
			return true;
		}

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                //Kan helt klart optimeres!
                boolean tomsøgning = true;

				Indgang fundet = null;
                for (int i = 0; i < a.søgeindeks.size(); i++) {
                    fundet = a.søgeindeks.get(i);
                    if (søgeord.equalsIgnoreCase(fundet.søgeord)) break;

                }
                if (fundet == null || !fundet.søgeord.equalsIgnoreCase(søgeord)) {
                    if (fundet == null) p("fundet var null");
                    else p("søgning var tom: " + fundet.søgeord);
                    return tomsøgning;
                } else {
					tomsøgning = false;
                    p("ordet: " + søgeord + " blev fundet i søgeindeks");
                    p("Indgang fundet: " + fundet);
                    for (String s : fundet.index) {
                        p("   index: " + s);

                        //s= "1186";

                        Fund f = a.hentArtikel(s);//baseUrlArtikler+s+".html");
                        f.nøgle =fundet.getSøgeord();
                        a.søgeresultat.add(f);
                    }
                }
                return tomsøgning;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

				boolean tomsøgning2 = (boolean) o;

                opdaterUI(tomsøgning2, søgeord, viserposition);
                p("Tjekker søgeresultat: ");
                if (!tomsøgning2) for (Fund f : a.søgeresultat) p(f);

            }
        }.execute();
		return (a.søgeresultat.size() > 0);

	}

	//-- En mediasource bruges som input til mediaplayeren
	private LoopingMediaSource lavLoopKilde (Uri u){

		DataSource.Factory kilde = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "TsTest"), null);
		MediaSource ms = new ExtractorMediaSource(
				u,
				kilde,
				new DefaultExtractorsFactory(), null, null);

		return new LoopingMediaSource(ms);
	}

	void tomsøgning (String søgeord){

		a.søgeresultat.clear();
		Fund tom = new Fund(null,null);
		tom.nøgle = "Din søgning gav ikke noget resultat";
		a.søgeresultat.add(tom);
		resultaterListeAdapter.notifyDataSetChanged();
	}

	void skjulTastatur(){
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

	MediaSource lavKilde (Uri s){

		HttpDataSource.Factory kilde = new DefaultHttpDataSourceFactory("mig", new TransferListener<DataSource>() {
			@Override
			public void onTransferStart(DataSource source, DataSpec dataSpec) {
			}

			@Override
			public void onBytesTransferred(DataSource source, int bytesTransferred) {

			}

			@Override
			public void onTransferEnd(DataSource source) {

			}
		});

		MediaSource ms = new ExtractorMediaSource(
				s,
				kilde,
				new DefaultExtractorsFactory(), null, null);

		return ms;
	}


	void sætLyttere(){

		søgeknap.setOnClickListener(this);
		søgeknap.setOnLongClickListener(this);
		loopcb.setOnClickListener(this);
		langsomcb.setOnClickListener(this);
		søgefelt.setOnClickListener(this);
		loop.setOnClickListener(this);

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

		//-- Til resultatlisten. Skjuler/viser pilen som angiver mere end ét resultat
		resultatliste.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScrollStateChanged(AbsListView p1, int p2)
			{
				if(p2 == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
					mere.setAlpha(0);
				}
			}

			@Override
			public void onScroll(AbsListView p1, int p2, int p3, int p4)
			{
				//Denne metod bliver kaldt hele tiden, dvs ikke kun når brugeren scroller
			}
		});

		resultatliste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position != viserposition) {
					viserposition = position;
					sp.edit().putInt("position", position).commit();
					a.visPil = false;
					opdaterUI(false, a.søgeresultat.get(position).nøgle, position);
					//derBlevSøgt = true;
					t("resultatliste.onItemclick(). visPil? = "+a.visPil);

				}
			}
		});

		//-- Alternativ til at bruge knappen "Mere"
		resultatliste.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
			{
				Intent i = new Intent (MainActivity.this, FuldArtikel_akt.class);
				startActivity(i);
				return false;
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
		return (højde<bredde);
	}

	@Override
	public void onVisibilityChange(int p1)	{
		//Toast.makeText(this, "Visibility "+p1, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		player.release();
		a.main = null; // afregistrerer lytter
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		String s = søgefelt.getHint().toString();
		//t("onsaveinstancestate: "+ s);
		outState.putString("søgeord", s);
		outState.putInt("position", viserposition);
		super.onSaveInstanceState(outState);
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//////////////////////   TJEK FOR OM TOM SØGNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
		String s = savedInstanceState.getString("søgeord");
		viserposition = savedInstanceState.getInt("position");
		if (getString(R.string.hint).equalsIgnoreCase(s))
			s=  "velkommen";
		søgefelt.setHint(s);
		opdaterUI(false, s, viserposition);
		resultatliste.setSelection(viserposition);
	}

	//-- Egnet lytter-inteface
	@Override
	public void run() {
		søgeknap.setEnabled(true);
		autoSuggest = new ArrayAdapter(this,android.R.layout.simple_list_item_1, a.tilAutoComplete);
		søgefelt.setAdapter(autoSuggest);

	}


	/////----- Test / Log / debugging -------//////



	//Åbner test-/debug-aktivitet
	@Override
	public boolean onLongClick(View p1) {
		Intent i = new Intent(this, Test.class);
		startActivity(i);

		return true;
	}
	void p (Object o){
		Utill.p("Main."+o);
	}

	void t (String s){

		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}

	void ts (String s){

		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}


}
