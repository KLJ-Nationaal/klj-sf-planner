package domain;

import domain.importing.RestrictieOptie;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Afdeling {
	private static final Logger logger = LoggerFactory.getLogger(Afdeling.class);
	private String naam;
	private List<Inschrijving> inschrijvingen;
	private List<Restrictie<?, ?>> restricties;

	public Afdeling(String afdelingsNaam) {
		naam = afdelingsNaam;
		inschrijvingen = new ArrayList<>();
		restricties = new ArrayList<>();
	}

	public Afdeling() { this("Afdeling zonder naam " + Math.random()); }

	public String getNaam() { return naam; }
	public void setNaam(String naam) { this.naam = naam; }

	@XmlElementWrapper(name = "Inschrijvingen")
	@XmlElement(name = "Inschrijving")
	@ValueRangeProvider(id = "Inschrijving")
	public List<Inschrijving> getInschrijvingen() { return inschrijvingen; }
	public void setInschrijvingen(List<Inschrijving> inschrijvingen) { this.inschrijvingen = inschrijvingen; }

	@XmlElementWrapper(name = "Restricties")
	@XmlElement(name = "Restrictie")
	public List<Restrictie<?, ?>> getRestricties() { return restricties; }
	public void setRestricties(List<Restrictie<?, ?>> restricties) { this.restricties = restricties; }

	public void addRestricties(List<RestrictieOptie> restricties) {
		restricties.forEach(ro -> {
			Restrictie<?, ?> restrictie = new Restrictie<>(ro.getA().getObject(), ro.getA().isAlleRingen(),ro.getB().getObject(), ro.getB().isAlleRingen());
			this.restricties.add(restrictie);
			logger.info("Uitzondering toegevoegd aan {}: {}", getNaam(), restrictie);
		});
	}

	@Override
	public String toString() { return naam; }

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Afdeling)) return false;

		Afdeling c = (Afdeling) o;
		return naam.equals(c.naam);
	}

	@Override
	public int hashCode() { return Objects.hash(naam); }
}
