package persistence;

import ch.qos.logback.classic.Logger;
import com.thoughtworks.xstream.XStream;
import domain.importing.Reeks;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ReeksDefinitie {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(ReeksDefinitie.class);

	public static ArrayList<Reeks> unMarshall() {
		XStream xstream = new XStream();
		xstream.processAnnotations(Reeks.class);
		File xmlFile = new File("data/reeksdefinitie.xml");

		ArrayList<Reeks> reeksdefinitie = new ArrayList<>();
		try {
			reeksdefinitie = (ArrayList<Reeks>) xstream.fromXML(new FileInputStream(xmlFile));
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		return reeksdefinitie;
	}

}
