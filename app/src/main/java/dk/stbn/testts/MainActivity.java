package dk.stbn.testts;

import android.app.*;
import android.os.*;
import android.widget.*;

import java.io.BufferedInputStream;
import java.io.InputStream;
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

public class MainActivity extends Activity implements OnClickListener, com.google.android.exoplayer2.ui.PlaybackControlView.VisibilityListener, OnLongClickListener {
	Appl a;
	ArrayList<Fund> søgeresultat = new ArrayList();
	//ArrayList<Indgang> søgeindeks = new ArrayList<>();
	SimpleExoPlayer player;
	SimpleExoPlayerView v;
	Button søg;

	TextView resultat;

	EditText søgefelt;

	String søgeurl1 = "http://tegnsprog.dk/#|tegn|386|soeg|/'tekst/'";
	String søgeurl2 = "%7Cresultat%7C10%7Ctrestjerner%7C1";

    String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
	String baseUrlVideo = "http://tegnsprog.dk/video/t/"; //+" t_"+vNr+".mp4"		kaffe = 317
	String baseUrlArtikler ="http://tegnsprog.dk/artikler/"; //+artNr+".htm"		kaffe = 386
	String baseUrlBillede = "http://tegnsprog.dk/billede_t/"; //+"f_"+bNr+".jpg"	kaffe = 314
	
	String kaffe  = baseUrlVideo +"t_317.mp4";

	String velkommen = "t_2079.mp4";
	String til = "t_500.mp4";
	String ord = "t_2066.mp4";
	String bog = "t_236.mp4";
	String dansk = "t_131.mp4";
	String tegnsprog = "t_205.mp4";

	boolean intro = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActionBar().setElevation(0);
		}
		getActionBar().setIcon(R.drawable.tsikon);
        setContentView(R.layout.main);
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


		søg = (Button) findViewById(R.id.mainButton);

		søg.setOnClickListener(this);
		søg.setOnLongClickListener(this);
		resultat = (TextView) findViewById(R.id.resultat);
		søgefelt = (EditText) findViewById(R.id.søgefelt);
		//if (intro) lavintro();
		

    }

	@Override
	public void onClick(View klikket)
	{
		v.setControllerShowTimeoutMs(1200);
		skjulTastatur();
		String søgeordet = søgefelt.getText().toString();
		søgefelt.setText("");
		if (søgeordet.equals("")) søgeordet = søgefelt.getHint().toString();
		
		søgefelt.setHint(søgeordet);
		
		if (søgefelt.getText().toString().equals("")) return;
		søg(søgeordet); 
		
		switch (søgeresultat.size()) {
		   case 0 : resultat.setText("Din søgning gav ingen reultater"); return;
		   case 1 : {
			   		final Fund f = søgeresultat.get(0);

					   MediaSource ms = lavLoopKilde(f.videourl);

					   player.prepare(ms);
					   player.setPlayWhenReady(true);
					   resultat.setText("");
					   for (String dkOrd : f.ord) resultat.append(dkOrd + "\n\n");

		   }
		}
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		//søgefelt.clearFocus();



	}


	//Åbner test-/debug-aktivitet
	@Override
	public boolean onLongClick(View p1)
	{
		// TODO: Implement this method
		//a.søgeindeks = søgeindeks; // til test
		Intent i = new Intent(this, Test.class);
		startActivity(i);
		//resultat.setText("" + a.søgeindeks.get(8).toString());
		
		
		return false;
	}
	
	
	boolean søg (String søgeord){
		søg.setEnabled(false);

		Indgang fundet = null;
		for (int i = 0; i < a.søgeindeks.size(); i++){
			fundet = a.søgeindeks.get(i);
			if (søgeord.equalsIgnoreCase(fundet.søgeord)) break;
			
		}
		if (fundet == null) return false;
		else {
			
			for (String s : fundet.index){
				
				Uri u = lavUrl(baseUrlArtikler+s);
				/////////kodsnnvioadsj
				Fund f = new Fund(u, );
				
			}
			
		}
		søg.setEnabled(true);

		return true;
		//søgeresultat.add(lavTestfund());
	}

	void skjulTastatur(){

		// Check if no view has focus:
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}


	MediaSource lavKilde (String s){

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
				Uri.parse(s),
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

	void lavintro () {
		søg.setEnabled(false);
		ConcatenatingMediaSource intro =
				new ConcatenatingMediaSource(
						lavKilde(baseUrlVideo+velkommen)
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
					søg.setEnabled(false);
					new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								søgefelt.setText("k");
								søgefelt.setSelection(1);
								new Handler().postDelayed(new Runnable() {
										@Override
										public void run() {
											søgefelt.setText("ka");
											søgefelt.setSelection(2);
											new Handler().postDelayed(new Runnable() {
													@Override
													public void run() {
														søgefelt.setText("kaf");
														søgefelt.setSelection(3);
														new Handler().postDelayed(new Runnable() {
																@Override
																public void run() {
																	søgefelt.setText("kaff");
																	søgefelt.setSelection(4);
																	new Handler().postDelayed(new Runnable() {
																			@Override
																			public void run() {
																				søgefelt.setText("kaffe");
																				søgefelt.setSelection(5);
																				søg.setEnabled(true);
																				new Handler().postDelayed(new Runnable() {
																						@Override
																						public void run() {																						
																							søg.setEnabled(false);
																							new Handler().postDelayed(new Runnable() {
																									@Override
																									public void run() {
																										søg.setEnabled(true);
																										new Handler().postDelayed(new Runnable() {
																												@Override
																												public void run() {
																													søg.setEnabled(false);
																													new Handler().postDelayed(new Runnable() {
																															@Override
																															public void run() {
																																søg.setEnabled(true);
																																new Handler().postDelayed(new Runnable() {
																																		@Override
																																		public void run() {
																																			søg.setEnabled(false);
																																			new Handler().postDelayed(new Runnable() {
																																					@Override
																																					public void run() {
																																						søg.setEnabled(true);
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



	private Fund lavTestfund (){

		ArrayList a = new ArrayList();
		a.add("brun");
		a.add("kaffe");
		return new Fund(lavUrl(kaffe), a);
	}


	

	private Uri lavUrl (String s){

		return Uri.parse(s);
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
}
