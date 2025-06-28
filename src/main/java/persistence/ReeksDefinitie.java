package persistence;

import ch.qos.logback.classic.Logger;
import domain.importing.Reeks;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReeksDefinitie {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(ReeksDefinitie.class);

	@XmlRootElement(name = "list")
	private static class ReeksWrapper {
		public List<Reeks> items;
		@XmlElement(name = "Discipline")
		public List<Reeks> getList() { return items; }
		public void setList(List<Reeks> items) { this.items = items; }
	}

	public static ArrayList<Reeks> unMarshall() {
		File xmlFile = new File("data/reeksdefinitie.xml");
		ArrayList<Reeks> reeksdefinitie = new ArrayList<>();
		try {
			JAXBContext context = JAXBContext.newInstance(ReeksWrapper.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();

			ReeksWrapper wrapper = (ReeksWrapper) unmarshaller.unmarshal(xmlFile);
			if (wrapper != null) {
				reeksdefinitie.addAll(wrapper.getList());
			}

		} catch (JAXBException e) {
			logger.error("JAXB unmarshalling error: {}", e.getMessage(), e);
		}

		return reeksdefinitie;
	}

}
