package persistence;

import domain.importing.Afdelingsoptie;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.ArrayList;

public class Visualisatie {
	private static final String[] kleuren = {
			"#ff0000", "#ff4d00", "#ff8400", "#ffb700", "#ffd000", "#ffea00",
			"#fbff00", "#e1ff00", "#aaff00", "#77ff00", "#00ff40", "#00ffa6",
			"#00ffd9", "#00eeff", "#00d5ff", "#00a2ff", "#006fff", "#9900ff",
			"#b300ff", "#cc00ff", "#e600ff", "#ff00ff", "#ffbaba", "#ffc8ba",
			"#ffdeba", "#fff9ba", "#feffba", "#e8ffba", "#daffba", "#c6ffba",
			"#baffe0", "#baf4ff", "#badfff", "#bac9ff", "#c7baff", "#d4baff",
			"#e3baff", "#ffbaff", "#cfcfcf", "#b0b0b0"
	};

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class Afdelingsopties {
		@XmlElement(name = "Afdelingsoptie")
		public ArrayList<Afdelingsoptie> list = new ArrayList<>();

	}

	private static Afdelingsopties afdelingsopties = new Afdelingsopties();

	public static String getKleur(String afdeling) {
		if (afdelingsopties.list == null) unMarshall();

		return afdelingsopties.list.stream()
				.filter(afdelingsoptie -> afdelingsoptie.naam.equals(afdeling))
				.findFirst()
				.orElseGet(() -> generateNewKleurForAfdeling(afdeling))
				.kleur;
	}

	private static Afdelingsoptie generateNewKleurForAfdeling(String afdeling) {
		Afdelingsoptie afdelingsoptie = new Afdelingsoptie();
		afdelingsoptie.naam = afdeling;
		afdelingsoptie.kleur = kleuren[afdelingsopties.list.size() % kleuren.length];
		afdelingsopties.list.add(afdelingsoptie);
		marshall();
		return afdelingsoptie;
	}

	private static void marshall() {
		try {
			JAXBContext context = JAXBContext.newInstance(Afdelingsopties.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			try (Writer w = new FileWriter("data/visualisatie.xml")) {
				m.marshal(afdelingsopties, w);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void unMarshall() {
		try {
			JAXBContext context = JAXBContext.newInstance(Afdelingsopties.class);
			Unmarshaller m = context.createUnmarshaller();

			Reader r = new FileReader("data/visualisatie.xml");
			afdelingsopties = (Afdelingsopties) m.unmarshal(r);
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
			afdelingsopties.list = new ArrayList<>();
		}
	}

}
