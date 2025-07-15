package domain;

import ch.qos.logback.classic.Logger;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlID;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class Discipline {
	private String naam;
	private String ringNaam;
	private String extensie;
	private int duur;
	private boolean meisjes;
	private boolean jongens;
	private boolean vendeluniform;
	private boolean hoofdgilde;
	private boolean dans;
	private Sport sport;

	private final static Logger logger = (Logger) LoggerFactory.getLogger(Discipline.class);

	@XmlID
	public String getNaam() { return naam; }
	public void setNaam(String naam) {
		this.naam = naam;
		meisjes = true;
		jongens = true;
		vendeluniform = false;
		hoofdgilde = false;
		dans = false;
		if (naam.toLowerCase().contains("gilden")) {
			meisjes = false;
			vendeluniform = true;
		}
		if (naam.toLowerCase().contains("vendelen")) {
			meisjes = false;
			vendeluniform = true;
		}
		if (naam.toLowerCase().contains("jongens")) meisjes = false;
		if (naam.toLowerCase().contains("wimpelen")) jongens = false;
		if (naam.toLowerCase().contains("meisjes")) jongens = false;
		if (naam.toLowerCase().contains("keur")) hoofdgilde = true;
		if (naam.toLowerCase().contains("hoofdgilde")) hoofdgilde = true;
		if (naam.toLowerCase().contains("dans")) dans = true;
		if (naam.toLowerCase().contains("bondsreeks")) dans = true;
		if (naam.toLowerCase().contains("vrije ritmiek")) dans = true;
	}

	public String getVerkorteNaam() {
		if (naam.toLowerCase().contains("wimpelen"))
			return naam.replace("reeks", "r.");
		return naam;
	}

	public String getRingNaam() { return ringNaam; }
	public void setRingNaam(String ringNaam) { this.ringNaam = ringNaam; }

	public String getExtensie() { return extensie; }
	public void setExtensie(String extensie) { this.extensie = extensie; }

	public int getDuur() { return duur; }
	public void setDuur(int duur) { this.duur = duur; }

	public boolean isMeisjes() { return meisjes; }
	public boolean isJongens() { return jongens; }
	public boolean isVendeluniform() { return vendeluniform; }
	public boolean isHoofdgilde() { return hoofdgilde; }
	public boolean isDans() { return dans; }

	public Sport getSport() { return sport; }
	public void setSport(Sport sport) { this.sport = sport; }

	// wordt automatisch opgeroepen na het unmarshallen (ook al is er geen @override)
	// de sport afleiden van de naam als die null is, want bij oudere versies bestond dit nog niet
	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (sport == null && naam != null) {
			if (naam.toLowerCase().contains("gilden") || naam.toLowerCase().contains("vendelen") || naam.toLowerCase().contains("hoofdgilde")) {
				sport = Sport.VENDELEN;
			} else if (naam.toLowerCase().contains("wimpelen") || naam.toLowerCase().contains("keur")) {
				sport = Sport.WIMPELEN;
			} else if (naam.toLowerCase().contains("piramide")) {
				sport = Sport.PIRAMIDE;
			} else if (naam.toLowerCase().contains("touwtrekken")) {
				sport = Sport.TOUWTREKKEN;
			} else if (naam.toLowerCase().contains("dans") || naam.toLowerCase().contains("bondsreeks") || naam.toLowerCase().contains("vrije ritmiek")) {
				sport = Sport.DANS;
			} else {
				logger.error("Sport werd niet herkend in de Discipline {}", naam);
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Discipline that = (Discipline) o;
		return naam.equals(that.naam);
	}

	@Override
	public String toString() {
		return getVerkorteNaam();
	}

	@Override
	public int hashCode() {
		return Objects.hash(naam);
	}
}
