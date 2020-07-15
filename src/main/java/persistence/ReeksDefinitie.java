package persistence;

import com.thoughtworks.xstream.XStream;
import domain.importing.Reeks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ReeksDefinitie {
	public static ArrayList<Reeks> unMarshall() {
		XStream xstream = new XStream();
		xstream.processAnnotations(Reeks.class);
		File xmlFile = new File("data/reeksdefinitie.xml");

		ArrayList<Reeks> reeksdefinitie = new ArrayList<>();
		try {
			reeksdefinitie = (ArrayList<Reeks>) xstream.fromXML(new FileInputStream(xmlFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return reeksdefinitie;
	}

}
