package domain;

import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "Afdeling")
public class Afdeling {
	private String naam;
	private List<Inschrijving> inschrijvingen;
	private boolean meisjesDansenVendelen;

	public Afdeling(String afdelingsNaam) {
		naam = afdelingsNaam;
		inschrijvingen = new ArrayList<>();
		meisjesDansenVendelen = false;
		if(afdelingsNaam.toLowerCase().contains("kluizen")) meisjesDansenVendelen = true;
		if(afdelingsNaam.toLowerCase().contains("ertvelde")) meisjesDansenVendelen = true;
	}

	public String getNaam() {
		return naam;
	}
	public void setNaam(String naam) {
		this.naam = naam;
	}

	@ValueRangeProvider(id = "Inschrijving")
	public List<Inschrijving> getInschrijvingen() {
		return inschrijvingen;
	}
	public void setInschrijvingen(List<Inschrijving> inschrijvingen) {
		this.inschrijvingen = inschrijvingen;
	}

	public boolean isMeisjesDansenVendelen() { return meisjesDansenVendelen; }

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
