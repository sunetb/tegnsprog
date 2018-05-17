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

		StringBuilder udskriv = new StringBuilder(søgeord);
		for (String s : index) udskriv.append("|").append(s);
		return udskriv.toString();

	}


	public String getSøgeord () {
		return søgeord;
	}

	
}
