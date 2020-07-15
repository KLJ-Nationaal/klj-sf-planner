package domain.importing;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("Discipline")
public class Reeks {
	@XStreamAlias("Naam")
	private String naam;
	@XStreamAlias("Ringnaam")
	private String ringNaam;
	@XStreamAlias("Toevoeging")
	private String extensie;
	@XStreamAlias("Schema")
	private int duur;
	@XStreamAlias("AfdelingenPerRing")
	private int afdPerRing;
	@XStreamOmitField
	private int aantal;

	public Reeks(String discipline, Integer aantal) {
		this.naam = discipline;
		this.aantal = aantal;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getRingNaam() {
		return ringNaam;
	}

	public void setRingNaam(String ringNaam) {
		this.ringNaam = ringNaam;
	}

	public String getExtensie() {
		return extensie;
	}

	public void setExtensie(String extensie) {
		this.extensie = extensie;
	}

	public int getDuur() {
		return duur;
	}

	public void setDuur(int duur) {
		this.duur = duur;
	}

	public int getAfdPerRing() {
		return afdPerRing;
	}

	public void setAfdPerRing(int afdPerRing) {
		this.afdPerRing = afdPerRing;
	}

	public int getAantal() { return aantal;	}

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
