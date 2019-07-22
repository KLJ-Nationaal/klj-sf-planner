package domain;

import java.util.Objects;

public class Discipline {
	private String naam;
	private String ringNaam;
	private String extensie;
	private int duur;
	private boolean meisjes;
	private boolean jongens;
	private boolean vendeluniform;

	public String getNaam() { return naam; }
	public void setNaam(String naam) {
		this.naam = naam;
		meisjes = true;
		jongens = true;
		vendeluniform = false;
		if(naam.toLowerCase().contains("gilden")) { meisjes = false; vendeluniform = true; }
		if(naam.toLowerCase().contains("vendelen")) { meisjes = false; vendeluniform = true; }
		if(naam.toLowerCase().contains("jongens")) meisjes = false;
		if(naam.toLowerCase().contains("wimpelen")) jongens = false;
		if(naam.toLowerCase().contains("meisjes")) jongens = false;
	}

	public String getVerkorteNaam() {
		if(naam.toLowerCase().contains("wimpelen"))
			return naam.replace("reeks", "");
		return naam;
	}

	public String getRingNaam() { return ringNaam; }
	public void setRingNaam(String ringNaam) { this.ringNaam = ringNaam; }

	public String getExtensie() { return extensie; }
	public void setExtensie(String extensie) { this.extensie = extensie; }

	public int getDuur() { return duur; }
	public void setDuur(int duur) { this.duur = duur; }

	public boolean isMeisjes(){ return meisjes; }
	public boolean isJongens(){ return jongens; }
	public boolean isVendeluniform() { return vendeluniform; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Discipline that = (Discipline) o;
		return naam.equals(that.naam);
	}

	@Override
	public int hashCode() {
		return Objects.hash(naam);
	}
}
