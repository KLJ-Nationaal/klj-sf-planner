package persistence;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;

public class Instellingen {

	private final static Logger logger = (Logger) LoggerFactory.getLogger(Instellingen.class);
	private final static String filename = "data/opties.xml";

	@XmlRootElement(name = "Opties")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Opties {
		@XmlElement(name = "MinMinuten")
		public int MINMINUTEN = 3;
		@XmlElement(name = "TotaleTijd")
		public int TOTALETIJD = 176;
		@XmlElement(name = "TotaleTijdMetFinale")
		public int TOTALETIJDRINGMETFINALE = 150;
		@XmlElement(name = "TabelMinuten")
		public int TABELMINUTEN = 180;
		@XmlElement(name = "RijHoogte")
		public int ROW_HEIGHT = 18;
		@XmlElement(name = "StartTijd")
		public String STARTTIJD = "08:00";
		@XmlElement(name = "OplossingTijdslimiet")
		public int SOLVERTIMELIMIT = 20;
	}

	public static synchronized Opties Opties() {
		if (opties == null) {
			opties = new Opties();
		}
		return opties;
	}
	private static Opties opties;

	public static void load() {
		try {
			JAXBContext context = JAXBContext.newInstance(Opties.class);
			Unmarshaller m = context.createUnmarshaller();

			Reader r = new FileReader(filename);
			opties = (Opties) m.unmarshal(r);
		} catch (IOException | JAXBException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public static void save() {
		try {
			JAXBContext context = JAXBContext.newInstance(Opties.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			try (Writer w = new FileWriter(filename)) {
				m.marshal(opties, w);
			}

		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
}
