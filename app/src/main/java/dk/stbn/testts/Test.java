package dk.stbn.testts;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.Date;

import dk.stbn.testts.lytter.Lytter;

public class Test extends Activity implements View.OnClickListener, Lytter{

	TextView overskrift, nulstil, mail, videoformat;
	Button nulst, mailknap;
	CheckBox format;
	ListView lv;
	ArrayAdapter ar;
	Appl a;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testmliste);
		a = Appl.a;
		a.lyttere.add(this);

		overskrift = (TextView) findViewById(R.id.overskr);
		overskrift.append(a.versionsnr());
		nulstil = (TextView) findViewById(R.id.tx_nulstil);
		format = (CheckBox) findViewById(R.id.skiftformat);
		videoformat = (TextView) findViewById(R.id.tx_format);
		lv = (ListView) findViewById(R.id.testlistview);
		mail = (TextView) findViewById(R.id.tx_mail);
		//lv.setOnItemClickListener(this); //Crasher



		format.setOnClickListener(this);
		format.setChecked(a.webm);
		if (a.webm) videoformat.setText("Videoformat valgt: WEBM");
		else videoformat.setText("Videoformat valgt: MP4");
		
		ar = new ArrayAdapter(this, android.R.layout.simple_list_item_1, Utill.debugbesked){
			@Override
			public View getView(int pos, View genbrug, ViewGroup parent){
				View v = genbrug;
				
				if (v== null) {
					v = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
				}
				TextView t = (TextView) v.findViewById(android.R.id.text1);
				t.setText(Utill.debugbesked.get(pos));
				return v;
			}
		};
		
		lv.setAdapter(ar);

		nulst = (Button) findViewById(R.id.knap_nulstil);
		nulst.setOnClickListener(this);
		mailknap = (Button) findViewById(R.id.knap_);
		mailknap.setOnClickListener(this);

	}
	
	void p (Object o){
		Utill.p("Test."+o);
	}

	void t (String s){

		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View view) {
		if (view == nulst) {
			a.nulstilTilstandHeavy();
			ar.notifyDataSetChanged();
			lv.setAdapter(ar);
			nulstil.setText("genindlæser data, vent venligst..");
			a.genstartetFraTestAkt = true;
		}
		else if (view == mailknap){
			StringBuilder log = new StringBuilder("" + new Date() + Build.MANUFACTURER + " - " + Build.MODEL + " API: " + Build.VERSION.SDK_INT + " app ver. " + a.versionsnr());
			if (Utill.debugbesked.size() < 10000) //sikrer at der ikke overføres for mange dat med intentet
				for (String s : Utill.debugbesked) log.append("\n").append(s);
			else{
				for (int i = 0 ; i < 500; i++) log.append("\n").append(Utill.debugbesked.get(i)); //tager de 500 første
				int j = Utill.debugbesked.size();
				for (int i = 0 ; i < 500; i++) log.append("\n").append(Utill.debugbesked.get((j - 500) + i)); //..og de 500 sidste
			}
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/html");
			intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"sunetb@gmail.com"});
			intent.putExtra(Intent.EXTRA_SUBJECT, "Tegnsprogsapp logbesked");
			intent.putExtra(Intent.EXTRA_TEXT, log.toString());

			startActivity(Intent.createChooser(intent, "Send Email"));
			//mail.setText("Mail er sendt");
		}
		else if (view == format){
			a.webm = !a.webm;
			a.sp.edit().putBoolean("format", a.webm).apply();
			if (a.webm) videoformat.setText("Videoformat valgt: WEBM");
			else videoformat.setText("Videoformat valgt: MP4");
		}


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		p("onactivityresult");
	}

	@Override
	public void grunddataHentet() {
		p("grunddataHentet()");
		ar.notifyDataSetChanged();
		nulstil.setText("Data genindlæst færdig!");

	}
	@Override
	public void logOpdateret() {
		ar.notifyDataSetChanged();
	}

	@Override
	public void netværksændring(boolean forbundet) {
		p("netværksstatus: har netværk? "+forbundet);
	}

	@Override
	public void fejlmeddelelse(String besked) {

	}

	@Override
	protected void onDestroy() {
		a.lyttere.remove(this);
		//Dårligt!
		startActivity(new Intent(this, MainActivity.class));
		super.onDestroy();
	}
}
