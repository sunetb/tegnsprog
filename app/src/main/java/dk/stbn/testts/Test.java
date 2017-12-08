package dk.stbn.testts;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.Date;

import dk.stbn.testts.lytter.Lytter;

public class Test extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, Lytter{

	TextView overskrift, nulstil, mail;
	Button nulst, mailknap;
	ListView lv;
	ArrayAdapter ar;
	Appl a;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testmliste);
		a = Appl.a;
		overskrift = (TextView) findViewById(R.id.overskr);
		overskrift.append(" | App-version: "+a.versionsnr());
		nulstil = (TextView) findViewById(R.id.tx_nulstil);
		lv = (ListView) findViewById(R.id.testlistview);
		//lv.setOnItemClickListener(this);
		a.lyttere.add(this);

		
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
		}
		if (view == mailknap){
			String log = ""+ new Date() + Build.MANUFACTURER + " - " + Build.MODEL;
			for (String s : Utill.debugbesked) log += "\n" + s;

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/html");
			intent.putExtra(Intent.EXTRA_EMAIL, "sunetb@gmail.com");
			intent.putExtra(Intent.EXTRA_SUBJECT, "Tegnsprogsapp logbesked");
			intent.putExtra(Intent.EXTRA_TEXT, log);

			startActivity(Intent.createChooser(intent, "Send Email"));
		}

	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		//ar.notifyDataSetChanged();
	}

	@Override
	public void grunddataHentet() {
		ar.notifyDataSetChanged();
		nulstil.setText("Data genindlæst færdig!");

	}

	@Override
	protected void onDestroy() {
		a.lyttere.remove(this);
		super.onDestroy();
	}
}
