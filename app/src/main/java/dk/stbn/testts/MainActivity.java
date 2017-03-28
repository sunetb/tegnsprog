package dk.stbn.testts;

import android.app.*;
import android.os.*;
import android.text.Html;
import android.widget.*;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
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
import android.view.ActionProvider.*;
import android.os.AsyncTask;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.support.v7.app.*;

public class MainActivity extends AppCompatActivity implements OnClickListener, AdapterView.OnItemClickListener, com.google.android.exoplayer2.ui.PlaybackControlView.VisibilityListener, OnLongClickListener, Runnable{
	Appl a;
	ArrayList<Fund> søgeresultat = new ArrayList();
	//ArrayList<Indgang> søgeindeks = new ArrayList<>();
	SimpleExoPlayer player;
	SimpleExoPlayerView v;
	Button søgeknap;

	TextView resultat, loop;
	CheckBox loopcb;
	AutoCompleteTextView søgefelt;
	ArrayAdapter ar;

	String søgeurl1 = "http://tegnsprog.dk/#|tegn|386|soeg|/'tekst/'";
	String søgeurl2 = "%7Cresultat%7C10%7Ctrestjerner%7C1";

    String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
	String baseUrlVideo = "http://tegnsprog.dk/video/t/"; //+" t_"+vNr+".mp4"		kaffe = 317
	String baseUrlArtikler ="http://m.tegnsprog.dk/artikler/"; //+artNr+".htm"		kaffe = 386
	String baseUrlBillede = "http://tegnsprog.dk/billede_t/"; //+"f_"+bNr+".jpg"	kaffe = 314
	
	String kaffe  = baseUrlVideo +"t_317.mp4";

	String velkommen = "t_2079.mp4";
	String til = "t_500.mp4";
	String ord = "t_2066.mp4";
	String bog = "t_236.mp4";
	String dansk = "t_131.mp4";
	String tegnsprog = "t_205.mp4";

	String tabel = "<TABLE><TR style=\"vertical-align:top;\"><TD>1. </TD><TD>brun</TD></TR><TR style=\"vertical-align:top;\"><TD>2. </TD><TD>kaffe</TD></TR></TABLE>";



	boolean intro = false;

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
		v = (SimpleExoPlayerView) findViewById(R.id.mainVideoView);
		player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(new AdaptiveVideoTrackSelection.Factory(new DefaultBandwidthMeter())), new DefaultLoadControl());
		//player.setVideoListener(n
		v.setPlayer(player);
		v.setControllerShowTimeoutMs(1);
		v.setControllerVisibilityListener(this);
		//player.
		//v.setContro
		//v.setVideoURI(url);

		søgeknap = (Button) findViewById(R.id.mainButton);

		søgeknap.setOnClickListener(this);
		søgeknap.setOnLongClickListener(this);
		søgeknap.setEnabled(false);
		//sæt lytter
		a.main = this;
		resultat = (TextView) findViewById(R.id.resultat);
		søgefelt = (AutoCompleteTextView) findViewById(R.id.søgefelt);
		søgefelt.setOnClickListener(this);

		ar = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, a.tilAutoComplete);
		søgefelt.setAdapter(ar);
		loop = (TextView) findViewById(R.id.looptv);
		loop.setOnClickListener(this);
		loopcb = (CheckBox) findViewById(R.id.loopcb);
		loopcb.setOnClickListener(this);
		loopcb.setChecked(true);
		søgefelt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEARCH) {
						;søgeknap.performClick();
						return true;
					}
					return false;
				}
			});

		if (intro) lavintro();
		velkommen();

		p("onCreate færdig");

    }



	@Override
	public void onClick(View klikket)
	{

		if (klikket == søgeknap) {
			v.setControllerShowTimeoutMs(1200); /// tiden før knapperne skjules automatisk
			skjulTastatur();
			String søgeordet = søgefelt.getText().toString().toLowerCase().trim();
			p("OnCliclk søgeord: " + søgeordet);

			søgefelt.setText("");
			if (søgeordet.equals("")) søgeordet = søgefelt.getHint().toString();

			søgefelt.setHint(søgeordet);

			if (søgefelt.getText().toString().equals(søgefelt.getHint().toString())) return;
			søgeresultat.clear();

			final String søgeordF = søgeordet.toLowerCase();
			søgeknap.setEnabled(false);

			new AsyncTask() {

				@Override
				protected Object doInBackground(Object[] params) {

					boolean resultat = søg(søgeordF);

					if (!resultat) {

						p("Fejl " + søgeordF + " ikke fundet");


					}

					return resultat;
				}

				@Override
				protected void onPostExecute(Object retursultat) {
					//t("Artikel hentet");
					p("Artikel hentet");

					boolean tomSøgning = !(boolean) retursultat;

					søgeknap.setEnabled(true);

					if (tomSøgning) {
						resultat.setText("Din søgning gav ikke noget resultat");
						player.setPlayWhenReady(false);
					} else {


						///HER SKAL HÅNDTERES FLERE RESULTATER MED EN for (Fund f : søgeresultat) ...
						Fund f = søgeresultat.get(0);
						ArrayList<String> ord = f.ordliste;

						String resultatStreng = "Søgeord:      \"" + f.nøgle + "\"\n\n";

						for (String s : ord)
							resultatStreng += s + "\n";

						resultat.setText(resultatStreng);
						try {
							MediaSource ms1 = lavLoopKilde(søgeresultat.get(0).videourl);
							v.setVisibility(View.VISIBLE);
							player.prepare(ms1);
							player.setPlayWhenReady(true);
						}
						catch (Exception e){
							e.printStackTrace();
							resultat.setText("Fejl: "+e.getMessage());
						}
					}


				}

			}.execute();
		}
		else if (klikket == loopcb) {
			if (loopcb.isChecked())
				player.setPlayWhenReady(true);
				else
				player.setPlayWhenReady(false);

			ts("Stadig ikke implementeret. Nu kan den pause/resume");
		}
		else if (klikket == søgefelt) søgefelt.setText("");
		
	}


	void håndterSøg(String s){
		
		
	}
	
	void velkommen (){
		søgeknap.setEnabled(false);

		new AsyncTask() {

			@Override
			protected Object doInBackground(Object[] params) {
				if (!søg("velkommen")){

					p("Ordet "+velkommen + " ikke fundet");
					return null;
				}
				else{

					//a.hentArtikel(baseUrlArtikler+søgeordF+".html");
				}

				return null;
			}

			@Override
			protected void onPostExecute(Object returtat){
				//t("Artikel hentet");
				p("Artikel hentet");
				søgeknap.setEnabled(true);
				skjulTastatur();
				if (søgeresultat.size() > 0) {
					ArrayList<String> ord = søgeresultat.get(0).ordliste;
					resultat.setText("");
					for (String s : ord)
						resultat.append(s + "\n");
					MediaSource ms1 = lavLoopKilde(søgeresultat.get(0).videourl);
					player.prepare(ms1);
					player.setPlayWhenReady(true);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							v.setVisibility(View.VISIBLE);
						}
					}, 1000);
				}
				else{
					resultat.setText("Intet resultat");
				}
			}

		}.execute();





	}

	//Åbner test-/debug-aktivitet
	@Override
	public boolean onLongClick(View p1)
	{
		Intent i = new Intent(this, Test.class);
		startActivity(i);

		
		return true;
	}
	
	//kaldes fra baggrund
	boolean søg (String søgeordInd){
		p("Søg("+søgeordInd+")");
		String søgeord = søgeordInd;
		if (søgeord.substring(0,1).equalsIgnoreCase(" ")) søgeord = søgeord.substring(1); //Burde være rekursiv
		if (søgeord.equalsIgnoreCase("igår")) return søg("i går"); //Ikke pænt :)
		if (søgeord.equalsIgnoreCase("i torsdags")) søgeord = " i torsdags";


		//Kan helt klart optimeres!
		Indgang fundet = null;
		for (int i = 0; i < a.søgeindeks.size(); i++){
			fundet = a.søgeindeks.get(i);
			if (søgeord.equalsIgnoreCase(fundet.søgeord)) break;
			
		}
		if ( fundet == null || !fundet.søgeord.equalsIgnoreCase(søgeord)) {
			if (fundet == null ) p("fundet var null");
			else p("søgning var tom: "+fundet.søgeord);
			return false;
		}
		else {
			p("ordet: "+søgeord+ " blev fundet i søgeindeks");
			p("Indgang fundet: "+ fundet);
			for (String s : fundet.index){
				p("   index: "+s);

                //s= "1186";

				Fund f= a.hentArtikel(s);//baseUrlArtikler+s+".html");
				f.nøgle = fundet.getSøgeord();
				søgeresultat.add(f);

			}
			
		}

		p("Tjekker søgeresultat: ");
		for (Fund f : søgeresultat) p(f);

		return (søgeresultat.size() > 0);

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

	void lavintro () {
		søgeknap.setEnabled(false);
		ConcatenatingMediaSource intro =
				new ConcatenatingMediaSource(
						lavKilde(Uri.parse(baseUrlVideo+velkommen))
						//lavKilde(baseUrl+til),
						//lavKilde(baseUrl+ord),
						//lavKilde(baseUrl+bog),
						//lavKilde(baseUrl+dansk), lavKilde(baseUrl+tegnsprog)
				);

		player.prepare(intro);
		player.setPlayWhenReady(true);
		søgefelt.setText("velkommen");
		resultat.setText("velkommen");

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				søgefelt.setText("");
				søgeknap.setEnabled(false);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						søgefelt.setText("s");
						søgefelt.setSelection(1);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								søgefelt.setText("sø");
								søgefelt.setSelection(2);
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										søgefelt.setText("søg");
										søgefelt.setSelection(3);
										new Handler().postDelayed(new Runnable() {
											@Override
											public void run() {
												søgefelt.setText("søg");
												søgefelt.setSelection(4);
												new Handler().postDelayed(new Runnable() {
													@Override
													public void run() {
														søgefelt.setText("søge");
														søgefelt.setSelection(5);
														søgeknap.setEnabled(true);
														new Handler().postDelayed(new Runnable() {
															@Override
															public void run() {
																søgeknap.setEnabled(false);
																new Handler().postDelayed(new Runnable() {
																	@Override
																	public void run() {
																		søgeknap.setEnabled(true);
																		new Handler().postDelayed(new Runnable() {
																			@Override
																			public void run() {
																				søgeknap.setEnabled(false);
																				new Handler().postDelayed(new Runnable() {
																					@Override
																					public void run() {
																						søgeknap.setEnabled(true);
																						new Handler().postDelayed(new Runnable() {
																							@Override
																							public void run() {
																								søgeknap.setEnabled(false);
																								new Handler().postDelayed(new Runnable() {
																									@Override
																									public void run() {
																										søgeknap.setEnabled(true);
																										søgeknap.performClick();
																									}
																								}, 500);
																							}
																						}, 500);
																					}
																				}, 500);
																			}
																		}, 500);
																	}
																}, 500);
															}
														}, 1000);
													}
												}, 500);
											}
										}, 500);
									}
								}, 500);
							}
						}, 500);
					}
				}, 500);

			}
		}, 3500);
	}

	@Override
	public void run() {
		søgeknap.setEnabled(true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TextView t = (TextView) view;
		søg(t.getText().toString());
	}
}
