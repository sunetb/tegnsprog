package dk.stbn.testts;

import android.content.res.Resources;
import android.os.*;
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
import java.util.*;

import android.os.AsyncTask;
import android.support.v7.app.*;



public class MainActivity extends AppCompatActivity implements OnClickListener, AdapterView.OnItemClickListener, com.google.android.exoplayer2.ui.PlaybackControlView.VisibilityListener, OnLongClickListener, Runnable{
	Appl a;
	ArrayList<Fund> søgeresultat = new ArrayList();
	//ArrayList<Indgang> søgeindeks = new ArrayList<>();
	SimpleExoPlayer player;
	SimpleExoPlayerView v;
	ImageButton søgeknap;

	TextView  loop;
	CheckBox loopcb;
	ListView resultatliste;
	AutoCompleteTextView søgefelt;

	ArrayAdapter autoSuggest, resultaterListeAdapter;


	String søgeurl1 = "http://tegnsprog.dk/#|tegn|386|soeg|/'tekst/'";
	String søgeurl2 = "%7Cresultat%7C10%7Ctrestjerner%7C1";

    String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
	String baseUrlVideo = "http://tegnsprog.dk/video/t/"; //+" t_"+vNr+".mp4"		kaffe = 317
	String baseUrlArtikler ="http://m.tegnsprog.dk/artikler/"; //+artNr+".htm"		kaffe = 386
	String baseUrlBillede = "http://tegnsprog.dk/billede_t/"; //+"f_"+bNr+".jpg"	kaffe = 314
	
	String kaffe  = baseUrlVideo +"t_317.mp4";

	String velkommen = "t_2079.mp4";
	ArrayList<String> testliste = new ArrayList();



	boolean tomsøg = true;

	boolean liggendeVisning;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	/*	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActionBar().setElevation(0);
		}*/
		setContentView(R.layout.main);
		//getActionBar().setIcon(R.drawable.logo);
        a = Appl.a;
		a.main = this; //registrerer aktiviteten som lytter
		
		v = (SimpleExoPlayerView) findViewById(R.id.mainVideoView);
		player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(new AdaptiveVideoTrackSelection.Factory(new DefaultBandwidthMeter())), new DefaultLoadControl());
		//player.setVideoListener(n
		v.setPlayer(player);
		v.setControllerShowTimeoutMs(1);
		v.setControllerVisibilityListener(this);

		søgeknap = (ImageButton) findViewById(R.id.mainButton);
		søgeknap.setOnClickListener(this);
		søgeknap.setOnLongClickListener(this);
		søgeknap.setEnabled(false);

		liggendeVisning = liggendeVisning();
		resultatliste = (ListView) findViewById(R.id.fundliste);

		int listelayout = R.layout.listelayout;

		if (liggendeVisning) listelayout = R.layout.listelayout_land;

		resultaterListeAdapter = new ArrayAdapter(this, listelayout, R.id.tekst, søgeresultat){


			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

				View rod = super.getView(position, convertView, parent);

				p("getview resultatlisteadapter pos"+position);


					ImageView iv = (ImageView) rod.findViewById(R.id.billede);
					iv.setImageResource(R.drawable.kaffef314);
					TextView t = (TextView) rod.findViewById(R.id.tekst);
					t.setText(søgeresultat.get(position).toString());//søgeresultat.get(position).getTekst());

				return rod;
			}

			@Override
			public int getCount() {
				return søgeresultat.size();
			}
		};
		p("Arrayadapter: " + resultaterListeAdapter.getCount() );



		resultatliste.setAdapter(resultaterListeAdapter);
		resultatliste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				opdaterUI(false, true, søgeresultat.get(position).nøgle, position);
			}
		});		//hmm samme lytter!? Måske hellere lave en anonym

		//resultat = (TextView) findViewById(R.id.resultat);

		søgefelt = (AutoCompleteTextView) findViewById(R.id.søgefelt);
		søgefelt.setOnClickListener(this);
		loop = (TextView) findViewById(R.id.looptv);
		loop.setOnClickListener(this);
		loopcb = (CheckBox) findViewById(R.id.loopcb);
		loopcb.setOnClickListener(this);
		loopcb.setChecked(true);
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
		søgefelt.setOnItemClickListener(this);
		skjulTastatur();

		if (savedInstanceState != null) {

			this.run(); p("Startet ved skærmvending. Initialiserer autocomplete-listen (sæt adapter)");
			
		}
		else velkommen();
		p("onCreate færdig");
    }

	private boolean liggendeVisning() {
		int højde = Resources.getSystem().getDisplayMetrics().heightPixels;
		int bredde = Resources.getSystem().getDisplayMetrics().widthPixels;

		return (højde<bredde);

	}


	@Override
	public void onClick(View klikket)
	{

		if (klikket == søgeknap) {
			String søgeordF = forberedSøgning();
			boolean søgeResultat = søg(søgeordF);
            if (!søgeResultat) {
                //resultat.setText("Ordet \""+søgeordF+ "\" findes ikke i ordbogen");
            }
		}
		else if (klikket == loopcb) {
			if (loopcb.isChecked())
				player.setPlayWhenReady(true); //Virker rigtig dårligt!!!!
				else
				player.setPlayWhenReady(false);

			ts("Stadig ikke implementeret. Nu kan den pause/resume");
		}
		else if (klikket == søgefelt) søgefelt.setText("");
		
	}


	String forberedSøgning(){

        v.setControllerShowTimeoutMs(1200); /// tiden før knapperne skjules automatisk
        skjulTastatur();
        String søgeordet = søgefelt.getText().toString().toLowerCase().trim();
        p("forberedSøgning søgeord: " + søgeordet);

        søgefelt.setText("");
        if (søgeordet.equals("")) søgeordet = søgefelt.getHint().toString();

        søgefelt.setHint(søgeordet);

        if (søgefelt.getText().toString().equals(søgefelt.getHint().toString())) return "";
        søgeknap.setEnabled(false);
        søgeresultat.clear();
        return søgeordet.toLowerCase();
	}


	void opdaterUI (boolean tomSøgning, boolean loop, String søgeordInd, int pos){


		p("opdaterUI kaldt! Var søgningen tom?  "+ tomSøgning);
		tomsøg = tomSøgning;
        if (tomSøgning) {
			tomsøgning(søgeordInd);
            player.setPlayWhenReady(false);


        }
        else {

///HER SKAL HÅNDTERES FLERE RESULTATER MED EN for (Fund f : søgeresultat) ...


			resultaterListeAdapter.notifyDataSetChanged();

            try {
				MediaSource ms1;
				if (loop)
                ms1 = lavLoopKilde(søgeresultat.get(pos).videourl);
				else
				ms1 = lavKilde(søgeresultat.get(pos).videourl);
                player.prepare(ms1);
                player.setPlayWhenReady(true);
				v.setVisibility(View.VISIBLE);

			}
            catch (Exception e){
                e.printStackTrace();
                //resultat.setText("Fejl: "+e.getMessage());
            }
        }
		søgeknap.setEnabled(true);
		skjulTastatur();

	}
	
	void velkommen (){
		//søgefelt.setHint("Skriv dit søgeord her");
		forberedSøgning();
		søg("velkommen");
	}

	//Åbner test-/debug-aktivitet
	@Override
	public boolean onLongClick(View p1)
	{
		Intent i = new Intent(this, Test.class);
		startActivity(i);

		return true;
	}
	

	boolean søg (String søgeordInd){
		p("Søg("+søgeordInd+")");
		final String søgeord = søgeordInd.trim();
		if (søgeord.equalsIgnoreCase("skriv søgeord her") || søgeord.equalsIgnoreCase("Søg her")) {
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
                        søgeresultat.add(f);
                    }
                }
                return tomsøgning;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

				boolean tomsøgning2 = (boolean) o;

                opdaterUI(tomsøgning2, true, søgeord, 0);
                p("Tjekker søgeresultat: ");
                if (!tomsøgning2) for (Fund f : søgeresultat) p(f);

            }
        }.execute();



		return (søgeresultat.size() > 0);

	}

	void tomsøgning (String søgeord){

		søgeresultat.clear();
		Fund tom = new Fund(null,null);
		tom.nøgle = "Din søgning gav ikke noget resultat";
		søgeresultat.add(tom);
		resultaterListeAdapter.notifyDataSetChanged();


	}

	void skjulTastatur(){

		// Check if no view has focus:
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
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
		});//this, Util.getUserAgent(this, "TsTest"), null);


		MediaSource ms = new ExtractorMediaSource(
				s,
				kilde,
				new DefaultExtractorsFactory(), null, null);

		return ms;
	}


	LoopingMediaSource lavLoopKilde (Uri u){

		DataSource.Factory kilde = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "TsTest"), null);
		MediaSource ms = new ExtractorMediaSource(
			u,
			kilde,
			new DefaultExtractorsFactory(), null, null);

		return new LoopingMediaSource(ms);
	}

	@Override
	public void onVisibilityChange(int p1)
	{

		//Toast.makeText(this, "Visibility "+p1, Toast.LENGTH_LONG).show();
		// TODO: Implement this method
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();
		player.release();
		a.main = null; // afregistrerer lytter
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		
		String s = søgefelt.getHint().toString();
		//t("onsaveinstancestate: "+ s);
		outState.putString("søgeord", s);
		super.onSaveInstanceState(outState);
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		String s = savedInstanceState.getString("søgeord");
		if ("Skriv søgeord her".equalsIgnoreCase(s) || "Søg her".equalsIgnoreCase(s))
			s=  "velkommen";
		søgefelt.setHint(s);
		//t("obrestore... "+ s);
		forberedSøgning();
		søg(s);
		
		
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

	@Override
	public void run() {
		søgeknap.setEnabled(true);
		autoSuggest = new ArrayAdapter(this,android.R.layout.simple_list_item_1, a.tilAutoComplete);
		søgefelt.setAdapter(autoSuggest);

	}

	//-- Kun til autocomplete
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TextView t = (TextView) view;
		String s = forberedSøgning();
		p("onItemClick: fra TV: "+t.getText().toString()+ "  |  Fra forbered: "+s);
		søg(t.getText().toString());
	}
}
