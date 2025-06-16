package domain;

import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Afdeling {
	private String naam;
	private List<Inschrijving> inschrijvingen;

	public Afdeling(String afdelingsNaam) {
		naam = afdelingsNaam;
		inschrijvingen = new ArrayList<>();
	}


	public Afdeling() {
		this("Afdeling zonder naam " + Math.random());
	}

	public String getNaam() {
		return naam;
	}
	public void setNaam(String naam) {
		this.naam = naam;
	}

	@XmlElementWrapper(name = "Inschrijvingen")
	@XmlElement(name = "Inschrijving")
	@ValueRangeProvider(id = "Inschrijving")
	public List<Inschrijving> getInschrijvingen() {
		return inschrijvingen;
	}
	public void setInschrijvingen(List<Inschrijving> inschrijvingen) {
		this.inschrijvingen = inschrijvingen;
	}

	@Override
	public String toString() {
		return naam;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Afdeling)) return false;

		Afdeling c = (Afdeling) o;
		return naam.equals(c.naam);
	}

	@Override
	public int hashCode() {
		return Objects.hash(naam);
	}
}
