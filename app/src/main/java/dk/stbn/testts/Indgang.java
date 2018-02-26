package dk.stbn.testts;

import java.util.ArrayList;

public class Indgang {

	 String søgeord;
	 ArrayList<String> index;
	 //boolean cachet = false;
	
	public Indgang (){}

	public Indgang (String ord, ArrayList<String> ix){
		søgeord = ord;
		index = ix;

	}

	public String toString () {

		String udskriv = søgeord;
		for (String s : index) udskriv += "|"+s;
		return udskriv;

	}


	public String getSøgeord () {
		return søgeord;
	}

	
}
