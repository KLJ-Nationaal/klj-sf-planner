package domain.importing;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "Discipline")
public class Reeks {
	private String naam;
	private String ringNaam;
	private String extensie;
	private int duur;
	private int afdPerRing;
	private int aantal;

	public Reeks() {} // nodig voor JAXB unmarshalling

	public Reeks(String discipline, Integer aantal) {
		this.naam = discipline;
		this.aantal = aantal;
	}

	@XmlElement(name = "Naam")
	public String getNaam() { return naam; }
	public void setNaam(String naam) { this.naam = naam; }

	@XmlElement(name = "Ringnaam")
	public String getRingNaam() { return ringNaam; }
	public void setRingNaam(String ringNaam) { this.ringNaam = ringNaam; }

	@XmlElement(name = "Toevoeging")
	public String getExtensie() { return extensie; }
	public void setExtensie(String extensie) { this.extensie = extensie; }

	@XmlElement(name = "Schema")
	public int getDuur() { return duur; }
	public void setDuur(int duur) { this.duur = duur; }

	@XmlElement(name = "AfdelingenPerRing")
	public int getAfdPerRing() { return afdPerRing; }
	public void setAfdPerRing(int afdPerRing) { this.afdPerRing = afdPerRing; }

	@XmlTransient
	public int getAantal() { return aantal; }
	public void setAantal(int aantal) { this.aantal = aantal; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Reeks that = (Reeks) o;

		return naam.equals(that.naam);
	}

	@Override
	public int hashCode() {
		return naam.hashCode();
	}

	@Override
	public String toString() {
		return "ReeksDefinitie{" +
				"naam='" + naam + '\'' +
				", ringNaam='" + ringNaam + '\'' +
				", extensie='" + extensie + '\'' +
				", duur=" + duur +
				'}';
	}
}
