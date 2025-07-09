package persistence;

import ch.qos.logback.classic.Logger;
import domain.importing.RestrictieOptie;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Restricties {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(Restricties.class);
	private static final String filename = "data/uitzonderingen.xml";

	@XmlRootElement(name = "Uitzonderingen")
	private static class RestrictieWrapper {
		private List<RestrictieOptie> items;
		@XmlAttribute
		public int version = 1;
		@XmlElement(name = "Uitzondering")
		public List<RestrictieOptie> getList() { return items; }
		public void setList(List<RestrictieOptie> items) { this.items = items; }
	}

	public static List<RestrictieOptie> unMarshall() {
		File xmlFile = new File(filename);
		List<RestrictieOptie> restricties = new ArrayList<>();
		try {
			JAXBContext context = JAXBContext.newInstance(RestrictieWrapper.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();

			RestrictieWrapper wrapper = (RestrictieWrapper) unmarshaller.unmarshal(xmlFile);
			if (wrapper != null) {
				restricties.addAll(wrapper.getList());
			}
			logger.info("Bestand {} ingelezen", xmlFile.getName());
		} catch (JAXBException e) {
			logger.error("JAXB unmarshalling error: {}", e.getMessage(), e);
		}

		return restricties;
	}

	public static void marshall(List<RestrictieOptie> restricties) {
		try {
			JAXBContext context = JAXBContext.newInstance(RestrictieWrapper.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			RestrictieWrapper rw = new RestrictieWrapper();
			rw.setList(restricties);

			try (Writer w = new FileWriter(filename)) {
				m.marshal(rw, w);
			}
			logger.info("Bestand {} opgeslagen", filename);
		} catch (Exception e) {
			logger.error("Error while marshalling XML", e);
		}
	}
}
