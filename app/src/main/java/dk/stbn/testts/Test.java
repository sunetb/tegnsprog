package dk.stbn.testts;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class Test extends Activity {

	//TextView testTv;
	ListView lv;
	ArrayAdapter ar;
	Appl a;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testmliste);
		//t("onCreate kaldt i Test");
		a = Appl.a;
		//testTv = (TextView) findViewById(R.id.testTv);
		lv = (ListView) findViewById(R.id.testlistview);
		
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
		;
		
		
		//t("Textview id: "+ testTv.getId());
		
		
		/*String udtekst = "Start på log \n";
		udtekst += Utill.debugbesked;
		udtekst += "=-=-=-=-=-=-=-=-=-=\n";
		udtekst += "start længde: "+a.søgeindeks.size()+" \n";
		
		for (Indgang i : a.søgeindeks) udtekst+=(i.toString() +"\n");
		
		testTv.setText(udtekst);
		
		*/
		
		new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
				try{
				for (Indgang i : a.søgeindeks) Utill.debugbesked.add(i.toString());
				}
				catch (Exception e){t(""+e);}
				return null;
			}

			@Override
			protected void onPostExecute(Object resultat){
				lv.setAdapter(ar);
				ar.notifyDataSetChanged();
				t("liste skrevet");
				p("liste skrevet");
			}

		}.execute();
		
	}
	
	void p (Object o){
		Utill.p("Test."+o);
	}

	void t (String s){

		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}
}
