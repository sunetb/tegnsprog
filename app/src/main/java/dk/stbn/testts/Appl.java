package dk.stbn.testts;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import dk.stbn.testts.lytter.Lytter;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.Logger;

public class Appl extends Application implements Lytter
{
	/// Aide
	/// As x

	//-- Tilstand
	String aktueltSøgeord = "velkommen";
	boolean dataKlar = false;
	boolean visPil = true;
	boolean loop = true;
	boolean slowmotion = false;
	long position = 0;
	boolean test = true;
	public int spillerNu = -1;
	public boolean genstartetFraTestAkt = false;

	//--Hentes webm eller mp4 i hentArtikel()
	boolean webm = false;
	boolean harNetværk = false;
	boolean netværkTabt = false;
	BroadcastReceiver netværksstatus;
	IntentFilter netfilter;
	boolean dataHentet = false;
	boolean nystartet = true; //sættes til false i søg() i Main

	//-- System
	public static Appl a;
	Context ctx;




	//-- Data
	SharedPreferences sp;
	ArrayList<Indgang> søgeindeks = new ArrayList<>();
	String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
	String nyUrl = "http://tegnsprog.dk/m/app-indeks/app-indeks.csv";
	ArrayList<String> tilAutoComplete = new ArrayList();
	ArrayList<Fund> søgeresultat = new ArrayList();


	//-- Lyttersystem

	ArrayList<Lytter> lyttere;
	void givBesked () { for (Lytter l : lyttere) l.grunddataHentet();}
	void givBesked (boolean forbundet) { for (Lytter l : lyttere) l.netværksændring(forbundet);}
	void givBesked (String fejl) { for (Lytter l : lyttere) l.fejlmeddelelse(fejl);}


	@Override
	public void onCreate() {
		super.onCreate();
		ctx = this.getApplicationContext();
		a=this;
		lyttere = new ArrayList();
		lyttere.add(this);
		sætNetværkslytter();
		sp= PreferenceManager.getDefaultSharedPreferences(this);

		//init("ONCREATE");




	}

	void init(String kaldtFra){

		Utill.tid = System.currentTimeMillis();
		p("Appl.init() kaldt fra "+kaldtFra);



		boolean EMULATOR = Build.PRODUCT.contains("sdk") || Build.MODEL.contains("Emulator");
		if (!EMULATOR) {
			Fabric.with(this, new Crashlytics());
			test = false;
			Logger fLog = Fabric.getLogger();
			fLog.d("test", "test");
		}


		loop = sp.getBoolean("loop", true);
		if (android.os.Build.VERSION.SDK_INT == 22) webm = true; //MP4-udgaverne af videoerne understøttes ikke på android 5.1 = API 22
		else webm = sp.getBoolean("format", true); //andre kan frit vælge i indstillinger
		p("Har vi netværk? "+harNetværk);
		if (harNetværk)	hentDataAsync();
		else givBesked (false);

	}

	void sætNetværkslytter(){

		p("Netværkslytter sættes ");
		netfilter = new IntentFilter();
		netfilter.addCategory(Intent.CATEGORY_DEFAULT);
		netfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		netværksstatus = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle b = intent.getExtras();

				for (String key : b.keySet()) {
					p( key + " => " + b.get(key));
				}
				//p("Bundle: "+b.keySet());
				//Toast.makeText(context, this + " " + intent, Toast.LENGTH_LONG).show();
				// Se http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
				// for flere muligheder

				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = cm.getActiveNetworkInfo();
				harNetværk = (netInfo != null && netInfo.isConnected());


				givBesked(harNetværk);

				p("Netværk: onRecieve() "+ harNetværk);
			}


		};
		registerReceiver(netværksstatus, netfilter);
	}

	@SuppressLint("StaticFieldLeak")
	void hentDataAsync(){
		new AsyncTask() {

			@Override
			protected Object doInBackground(Object[] params) {
				p("Kalder henSøgeindeks2()");

				try {
					hentSøgeindeks2(nyUrl);
				}
				catch (Exception e){
					e.printStackTrace();
					langsomNetBeskedFraBraggrund();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Object resultat){
				p("Søgeindeks hentet");
				if (!(lyttere.size() == 0)) {
					givBesked();
					dataKlar = true;
					dataHentet = true;
				}

			}

		}.execute();

	}

	//Kaldes kun i baggrunden
	public void hentSøgeindeks2(String u) {

		try { // Henter fil

			String heleIndholdet = "";

			if (sp.getBoolean("cachedSøgeindeks", false)){
				//todo: tjek om der er ny version..
				heleIndholdet = sp.getString("søgeindeks", "");


				Type type = new TypeToken<ArrayList<Indgang>>() {}.getType();
				søgeindeks = new Gson().fromJson(sp.getString("søgeindeks", null), type);
				Type type2 = new TypeToken<ArrayList<String>>() {}.getType();
				tilAutoComplete = new Gson().fromJson(sp.getString("tilAutoComplete", null), type2);

			}
			else {
				InputStream is = new URL(u).openStream();
				is = new BufferedInputStream(is);
				is.mark(1);
				if (is.read() == 0xef) {
					is.read();
					is.read();

				} else {
					is.reset();
				}
				p("######## hentSøgeindeks() ");
				byte[] contents = new byte[1024];

				int bytesRead = 0;

				//--Først hentes al tekst ind i én stor streng

				while ((bytesRead = is.read(contents)) != -1) {


					String linie = new String(contents, 0, bytesRead, "iso-8859-1");

					heleIndholdet += linie;

				}

				sp.edit().
						putString("søgeindeks", heleIndholdet).
						putBoolean("cachedSøgeindeks", true).
						commit();




				String [] linjesplit = heleIndholdet.split("\n");
				p("Array længde: "+linjesplit.length);
				for (int i = 0; i < linjesplit.length; i++) {
					String indgangS = linjesplit[i];
					p("indgangS: " + indgangS.length());
					p("Ind__" + indgangS);
					String[] indgangSA = indgangS.split("\t");
					if (indgangSA.length < 2) break;
					p("IndgangSA: " + indgangSA.length);
					String søgeordet = indgangSA[0];
					String[] ix = indgangSA[1].split(";");
					p("Array længde: " + ix.length);
					ArrayList<String> ix2 = new ArrayList();

					for (int j = 0; j < ix.length; j++) {
						String gammel = "";                    //tjek for dubletter
						if (j > 0) gammel = ix[j - 1].trim();

						String s = ix[j].trim();

						if (!gammel.equalsIgnoreCase(s))
							ix2.add(s); // add kun hvis den ikke findes i forvejen
					}
					//p("Ud1___"+søgeordet+ " "+ix2);
					Indgang indgang = new Indgang(søgeordet.trim(), ix2);
					søgeindeks.add(indgang);
					tilAutoComplete.add(søgeordet);
					//p("Ud2___"+indgang);

				}

				//gem
				sp.edit().putString("søgeindeks", new Gson().toJson(søgeindeks))
						.putString("tilAutoComplete", new Gson().toJson(tilAutoComplete))
						.apply();
			}


		} catch (Exception ex) {
			ex.printStackTrace();
			p(ex);
			p(ex.getMessage());
			nulNetBeskedFraBraggrund();
		}


	}

	
	void tjekIndgang (String søgeord){
		for (int i = 0; i<søgeindeks.size(); i++) {
			Indgang indg = søgeindeks.get(i);
			p("TjekIndgang søgte efter: "+ søgeord);
			if (indg.søgeord.equalsIgnoreCase(søgeord)){
				 p(" og fandt: "+i);
				p("Pos "+i);
				break;
			}
		}

	}

	//Kaldes kun fra baggrund
	public Fund hentArtikel(String u) {
		p("hentArtikel("+u+")");
		String vUrl = "";
		ArrayList <String>  beskrivelser = new ArrayList<>();


		try {
			InputStream is = new URL("http://m.tegnsprog.dk/artikler/"+u+".html").openStream();
			is = new BufferedInputStream(is);
			is.mark(1);
			if (is.read() == 0xef) {
				is.read();
				is.read();

			} else {
				is.reset();
			}
			p("#=#=#=#=#=#=#=#= hentArtikel() =#=#=#=#=#=#=#=#=#=#=#");
			byte[] contents = new byte[1024];
			String heleIndholdet = "";
			int bytesRead = 0;
			//bytesRead = is.read(contents); //skipper første linie
			//bytesRead = is.read(contents); //skipper anden linie

			while((bytesRead = is.read(contents)) != -1) {
				String linie = new String(contents, 0, bytesRead);
				heleIndholdet += linie;
				


			}
			
			//p("\nArtikel_______________________________: "+heleIndholdet);



			//-- Vi finder video-urlen

			int startindeks = heleIndholdet.indexOf("src=\"");
			String tempUrl = heleIndholdet.substring(startindeks+5);
			int slutIndeks= tempUrl.indexOf("\" type='video/mp4'>Your browser does not support the video tag.");
			vUrl = tempUrl.substring(0,slutIndeks);
			p("videourl::::::::::::::::::::::::::::::::"+vUrl);

			if (webm) {
				int underscore = vUrl.lastIndexOf("_") + 1;
				int sidstepunktum = vUrl.lastIndexOf(".");

				String indeksnr = vUrl.substring(underscore, sidstepunktum);
				p("indeksnr: " + indeksnr);
				vUrl = "http://m.tegnsprog.dk/video/mobil/t-webm/t_" + indeksnr + ".webm";
				p(vUrl);
			}



			//-- Vi finder de danske ord

			startindeks = tempUrl.indexOf("<TABLE>");
			slutIndeks = tempUrl.indexOf("<TABLE width=\"100%\">");
			String tabelDKOrd = tempUrl.substring(startindeks, slutIndeks);
			//p("__________min html:");
			//p(tabelDKOrd);
			//p("----------");
			//dKOrd = tabelDKOrd.replaceAll("\\/TR><TR", "</TR><BR><TR>").replaceAll("\\&middot\\;", " , ");
			String dKOrd = tabelDKOrd
					.replaceAll("<TABLE>", "")
					.replaceAll("</TD><TD>", " ")
					.replaceAll("\\&middot\\;", " , ")
					.replaceAll("<TABLE/>", "")
					.replaceAll("</TABLE>", "")
					.replaceAll("&nbsp;", "")
					.replaceAll("<TR>", "")
					.replaceAll("<TR style=\"vertical-align:top;\">", "")
					.replaceAll("</TR>", "")
					.replaceAll("</TD>", "ønskernylinie")
					.replaceAll("<TD>", "")
					.replaceAll("<BR>","\n")
				.replaceAll("<BR/>","\n");

			//Erstat html-koder for danske vokaler
			//æ: &aelig;
			//ø: &oslash;
			//å: &aring;

			dKOrd = dKOrd
					.replaceAll("&aelig;", "æ")
					.replaceAll("&oslash;", "ø")
					.replaceAll("&aring;", "å");





			beskrivelser = new ArrayList<String>(Arrays.asList(dKOrd.split("ønskernylinie")));


			p("Efter rens");
			p(beskrivelser.toString());



/*
			<HTML>
				<video width="100%" autoplay>
			 		<source  src="http://www.tegnsprog.dk/video/t/t_317.mp4" type='video/mp4'>
			 		Your browser does not support the video tag.
			 	</video>
				<TABLE>
					<TR style="vertical-align:top;">
						<TD>1. </TD>
						<TD>brun</TD>
					</TR>
					<TR style="vertical-align:top;">
						<TD>2. </TD>
						<TD>kaffe</TD>
					</TR>
				</TABLE>
				<TABLE width="100%">
					<TR>
						<TD>
							<A TITLE="&Aring;bn den fulde ordbogsartikel i browseren" href="#" onclick="var ref = window.open('http://www.tegnsprog.dk/#|soeg|tegn|386', '_system');">
							<IMG SRC="http://www.tegnsprog.dk/billeder/web/logo-mini.png"/>
								Vis p&aring; tegnsprog.dk</A></TD><TD style="text-align: right;">
							<A TITLE="G&aring; til toppen" href="#0">
								&#8679;
							</A>
						</TD></TR></TABLE></HTML>


						ANDEN VERSION______________________________

				<TABLE>
					<TR>
						<TD>
							&nbsp;
						</TD>
						<TD>
							foran &middot; forrest &middot; f&oslash;re &middot; komme f&oslash;rst
						</TD>
					</TR>
				</TABLE>
				<TABLE width="100%"><TR><TD><A TITLE="&Aring;bn den fulde ordbogsartikel i browseren" href="#" onclick="var ref = window.open('http://www.tegnsprog.dk/#|soeg|tegn|1000', '_system');"><IMG SRC="http://www.tegnsprog.dk/billeder/web/logo-mini.png"/>Vis p&aring; tegnsprog.dk</A></TD><TD style="text-align: right;"><A TITLE="G&aring; til toppen" href="#0">&#8679;</A></TD></TR></TABLE></HTML>

*/
		} catch (Exception ex) {
			ex.printStackTrace();
			p(ex);
			p(ex.getMessage());
			nulNetBeskedFraBraggrund();

		}
		p("hentArtikel færdig");
		return new Fund(Uri.parse(vUrl), beskrivelser);
	}

	private void nulNetBeskedFraBraggrund() {

		//Async med tom doInBackground, for at få kaldt givBesked() i forgrunden

		new AsyncTask() {
			@Override
			protected Object doInBackground(Object[] objects) {
				return null;
			}

			@Override
			protected void onPostExecute(Object o) {
				super.onPostExecute(o);
				givBesked(false);
			}
		}.execute();
	}

	private void langsomNetBeskedFraBraggrund() {

		//Async med tom doInBackground, for at få kaldt givBesked() i forgrunden

		new AsyncTask() {
			@Override
			protected Object doInBackground(Object[] objects) {
				return null;
			}

			@Override
			protected void onPostExecute(Object o) {
				super.onPostExecute(o);
				givBesked("Nettet er meget langsomt. Prøv evt. igen senere");
			}
		}.execute();
	}

	public void releaseAlle() {
		for (Fund f : søgeresultat)
			if (f.afsp != null) f.afsp.release();
			else p("Fejl: releaseAlle() gav null object "+søgeresultat.size());
    }

	public void opdaterHastighed() {
		float hast =  (a.slowmotion) ? 0.25f : 1.0f;
		for (Fund f : søgeresultat)
			if (f.afsp != null) f.afsp.setPlaybackParameters(new PlaybackParameters(hast, 1));
			else {
				String s = "";
				for (Fund f1 : søgeresultat) s+=f1.nøgle +"\n";
				p("Fejl: opdaterHastighed gav null object "+s);
		}
	}

	public void opdaterLoop() {

		for (Fund f : søgeresultat) {
			if (f.afsp == null) {
				f.initAfsp(ctx);
				p("opdaterLoop() Fejl: afsp var null");
			}
			if (loop) f.afsp.setRepeatMode(Player.REPEAT_MODE_ONE);
			else f.afsp.setRepeatMode(Player.REPEAT_MODE_OFF);
			p("opdaterloop() player "+ f.nøgle + " "+f.index + " sat til loop? "+loop);
		}

	}

	String versionsnr(){
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			p(e);
		}

		return pInfo.versionName;

	}


	void p (Object o){
		Utill.p("Appl."+o);
	}

	void t (Object o){

		Toast.makeText(ctx, ""+o, Toast.LENGTH_LONG).show();
	}


	public void nulstilTilstandLight() {

	}
	//** Kaldes fra Test: 'kører oncreate' igen
	public void nulstilTilstandHeavy() {
		p("HEAVY NULSTIL");
		//aktueltSøgeord = "";
		dataHentet = false;
		dataKlar = false;
		visPil = true;
		loop = true;
		slowmotion = false;
		position = 0;
		test = true;
		spillerNu = -1;
		genstartetFraTestAkt = false;
		webm = false;
		søgeindeks.clear();
		tilAutoComplete.clear();
		//søgeresultat.clear();

		init("HEAVY NULSTIL");

	}

	@Override
	public void grunddataHentet() {

	}

	@Override
	public void logOpdateret() {

	}

	@Override
	public void netværksændring(boolean forbundet) {
		p("netværksændring callback kaldt");
		if (harNetværk && nystartet) init("Appl.netværksændring");
	}

	@Override
	public void fejlmeddelelse(String besked) {
		//Bruges ikke her
	}
}
