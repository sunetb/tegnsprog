package dk.stbn.testts;

import android.content.res.Resources;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.exoplayer2.extractor.*;
import com.google.android.exoplayer2.source.*;
import android.os.AsyncTask;
import android.support.v7.app.*;
import android.widget.AbsListView.*;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnClickListener, AdapterView.OnItemClickListener, com.google.android.exoplayer2.ui.PlaybackControlView.VisibilityListener, Runnable{

	// -- Views mm
	SimpleExoPlayer afsp;
	SimpleExoPlayerView afspView;
	ImageButton søgeknap;
	TextView  fundTekst, loop, langsom;
	CheckBox loopcb, langsomcb;
	//ListView resultatliste;
	AutoCompleteTextView søgefelt;
	ImageView mere;
	ArrayAdapter autoSuggest, resultaterListeAdapter;

	private RecyclerView hovedlisten;
	private RecyclerView.Adapter adapter;
	//private RecyclerView.LayoutManager mLayoutManager;


	// -- Sys
	Appl a;
	SharedPreferences sp;

	// -- Data
	//String baseUrlVideo = "http://tegnsprog.dk/video/t/"; //+" t_"+vNr+".mp4"		kaffe = 317

	String baseUrlVideo = "http://m.tegnsprog.dk/video/mobil/t-webm/";
	String søgeurl1 = "http://tegnsprog.dk/#|tegn|386|soeg|/'tekst/'";
	String søgeurl2 = "%7Cresultat%7C10%7Ctrestjerner%7C1";
    String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
	String baseUrlArtikler ="http://m.tegnsprog.dk/artikler/"; //+artNr+".htm"		kaffe = 386
	String baseUrlBillede = "http://tegnsprog.dk/billede_t/"; //+"f_"+bNr+".jpg"	kaffe = 314
	String kaffe  = baseUrlVideo +"t_317.mp4";
	String velkommen = "t_2079.mp4";

	//Eksempel: http://www.tegnsprog.dk/video/t/t_2079.mp4

	// - Tilstand
	boolean tomsøg = true;
	boolean liggendeVisning;
	//int viserposition = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        a = Appl.a;
		a.main = this; //registrerer aktiviteten som lytter
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		//afspView = (SimpleExoPlayerView) findViewById(R.id.mainVideoView);

		søgeknap = (ImageButton) findViewById(R.id.mainButton);
		søgeknap.setEnabled(false);

		hovedlisten = (RecyclerView) findViewById(R.id.hovedlisten);

		hovedlisten.setHasFixedSize(true);

		//mLayoutManager = new LinearLayoutManager(this);
		//hovedlisten.setLayoutManager(mLayoutManager);

		adapter = new Hovedliste_adapter(a.søgeresultat, this);
		hovedlisten.setAdapter(adapter);
		hovedlisten.setLayoutManager(new LinearLayoutManager(this));

		liggendeVisning = liggendeVisning();

		//resultatliste = (ListView) findViewById(R.id.fundliste);

		//int listelayout = R.layout.listelayout;

		//if (liggendeVisning) listelayout = R.layout.listelayout_land;

		mere = (ImageView) findViewById(R.id.mere);
		mere.setAlpha(0);
		søgefelt = (AutoCompleteTextView) findViewById(R.id.søgefelt);
		loop = (TextView) findViewById(R.id.looptv);
		loopcb = (CheckBox) findViewById(R.id.loopcb);
		loopcb.setChecked(a.loop);
		langsomcb = (CheckBox) findViewById(R.id.langsomcb);
		langsom = (TextView) findViewById(R.id.langsomtv);
		sætLyttere();

		if (savedInstanceState != null) {

			this.run(); p("Startet ved skærmvending. Initialiserer autocomplete-listen (sæt adapter)");
			//viserposition = sp.getInt("position", 0);

			//p("Viser position: "+viserposition);
		}
		else velkommen();

		skjulTastatur();

		p("onCreate færdig");
    }

	@Override
	public void onClick(View klikket) {

		if (klikket == søgeknap) {
			//viserposition = 0;
			String søgeordF = forberedSøgning();
			boolean søgeResultat = søg(søgeordF);

            if (!søgeResultat) {
                //resultat.setText("Ordet \""+søgeordF+ "\" findes ikke i ordbogen");
            }
		}
/*		else if (klikket == loopcb ) {
			p("Loop-checkbox klikket");
			a.position = afsp.getCurrentPosition();
			p("position: "+a.position);
			sp.edit().putBoolean("loop", loopcb.isChecked()).commit();
			a.loop = loopcb.isChecked();
			if (a.loop){
				afsp.setRepeatMode(Player.REPEAT_MODE_ONE);
				afsp.seekTo(a.position);
				afsp.setPlayWhenReady(true);
			}
			else afsp.setRepeatMode(Player.REPEAT_MODE_OFF);
			p("Repeatmode: " + afsp.getRepeatMode());

		}
		else if (klikket == langsomcb){
			p(afsp.getPlaybackParameters());
			a.slowmotion = !a.slowmotion;
			float hast =  (a.slowmotion) ? 0.25f : 1.0f;
			afsp.setPlaybackParameters(new PlaybackParameters(hast, 1));
		}*/
		else if (klikket == søgefelt) {
			p("søgefelt klikket");
			søgefelt.setText("");
		}
		
	}
	//En slags hack hvis setPlayWhenReady(true/false) ikke kommer til at virke ordentligt
	void pauseVideo(boolean pause) {

    	float valgtHast = (a.slowmotion) ? 0.25f : 1.0f;

		float hast =  (pause) ? 0.0f : valgtHast ;
		afsp.setPlaybackParameters(new PlaybackParameters(hast, 1));
    }


	private String forberedSøgning(){

//        afspView.setControllerShowTimeoutMs(1200); /// tiden før knapperne skjules automatisk
        skjulTastatur();
        String søgeordet = søgefelt.getText().toString().toLowerCase().trim();
        p("forberedSøgning søgeord: " + søgeordet);

        søgefelt.setText("");
        if (søgeordet.equals("")) søgeordet = søgefelt.getHint().toString();

        søgefelt.setHint(søgeordet);

        if (søgefelt.getText().toString().equals(søgefelt.getHint().toString())) return ""; //Der blev trykket "Søg" uden at søgeordet var ændret
        søgeknap.setEnabled(false);
        a.søgeresultat.clear();
        return søgeordet.toLowerCase();
	}


	private void opdaterUI (boolean tomSøgning, String søgeordInd){

		p("opdaterUI kaldt! Var søgningen tom?  "+ tomSøgning);
		tomsøg = tomSøgning;
        if (tomSøgning) {
			tomsøgning(søgeordInd);
            if (afsp != null) afsp.setPlayWhenReady(false);
        }
        else {
			//resultaterListeAdapter.notifyDataSetChanged();
			adapter.notifyDataSetChanged();
			//-- Opdaterer synligheden for pilen "vis mere"
			if (a.søgeresultat.size() < 2 || !a.visPil) {
				mere.setAlpha(0);
			}

			else	{
				mere.setAlpha(100);
				a.visPil = false;
			}


                new AsyncTask(){
					@Override
					protected Object doInBackground(Object[] objects) {
						for (int i = 1 ; i <  a.søgeresultat.size(); i++) a.søgeresultat.get(i).initAfsp(getApplicationContext());
						return null;
					}

					@Override
					protected void onPostExecute(Object o) {
						super.onPostExecute(o);
						//adapter.notifyDataSetChanged(); //?
					}
				}.execute();

				//afsp.prepare(ms1);
				//afsp.setPlaybackSpeed(0.5f);
			p("Resultatliste længde: "+a.søgeresultat.size());
				a.søgeresultat.get(0).initAfsp(this);
				afsp = a.søgeresultat.get(0).afsp;

                afsp.setPlayWhenReady(true);
				if (a.loop) afsp.setRepeatMode(Player.REPEAT_MODE_ONE);
        }


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

                opdaterUI(tomsøgning2, søgeord);
                p("Tjekker søgeresultat: ");
                if (!tomsøgning2) for (Fund f : a.søgeresultat) p(f);

            }
        }.execute();
		return (a.søgeresultat.size() > 0);

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


	void sætLyttere(){

		søgeknap.setOnClickListener(this);
		//søgeknap.setOnLongClickListener(this);
//		loopcb.setOnClickListener(this);
//		langsomcb.setOnClickListener(this);
		søgefelt.setOnClickListener(this);
//		loop.setOnClickListener(this);

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
		afsp.release();
		a.releaseAlle();
		a.main = null; // afregistrerer lytter

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		String s = søgefelt.getHint().toString();
		//t("onsaveinstancestate: "+ s);
		outState.putString("søgeord", s);
		//outState.putInt("position", viserposition);
		super.onSaveInstanceState(outState);
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//////////////////////   TJEK FOR OM TOM SØGNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
		String s = savedInstanceState.getString("søgeord");
		//viserposition = savedInstanceState.getInt("position");
		if (getString(R.string.hint).equalsIgnoreCase(s))
			s=  "velkommen";
		søgefelt.setHint(s);
		opdaterUI(false, s);
		//resultatliste.setSelection(viserposition);
	}

	//-- Egnet lytter-inteface
	@Override
	public void run() {
		søgeknap.setEnabled(true);
		autoSuggest = new ArrayAdapter(this,android.R.layout.simple_list_item_1, a.tilAutoComplete);
		søgefelt.setAdapter(autoSuggest);

	}


	/////----- Test / Log / debugging -------//////


/*
	//Åbner test-/debug-aktivitet
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
	public class Hovedliste_adapter extends RecyclerView.Adapter<Hovedliste_adapter.ViewHolder> {

		ArrayList<Fund> data;
		Context c;
		Appl a = Appl.a;


		public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

			CardView c;
			com.google.android.exoplayer2.ui.SimpleExoPlayerView playerv;
			TextView  fundtekst;
			//ImageView pil;

			public ViewHolder(View v) {
				super(v);
				c = (CardView) v.findViewById(R.id.kort);
				playerv = (SimpleExoPlayerView) v.findViewById(R.id.afspillerview);
				fundtekst = (TextView) v.findViewById(R.id.fundtekst);
				//pil = (ImageView) v.findViewById(R.id.mere);

			}

			@Override
			public void onClick(View view) {
				final int position = getAdapterPosition();

			}
		}

		// Provide a suitable constructor (depends on the kind of dataset)
		public Hovedliste_adapter (ArrayList søgeresultater, Context ctx) {
			data = søgeresultater;
			c = ctx;
		}

		// Create new views (invoked by the layout manager)
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
			p("onBindViewHolder pos "+pos );
			Fund f = data.get(pos);


			holder.playerv.setPlayer(f.afsp);
			holder.fundtekst.setText(f.getTekst());



			//float visPil = a.visPil ? 100 : 0;
			//holder.pil.setAlpha(visPil);
/*

        holder.playerv
        holder.loop
        holder.hast
        holder.fundtekst

*/

		}

		// Return the size of your dataset (invoked by the layout manager)
		@Override
		public int getItemCount() {
			return data.size();
		}
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
