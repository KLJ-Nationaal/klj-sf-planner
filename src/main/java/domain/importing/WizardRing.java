package domain.importing;

public class WizardRing {
	private String naam;
	private int aantalRingen;
	private int duur;
	private int aantal;
	private int afdPerRing;

	public WizardRing(String ring, Integer aantal) {
		this.naam = ring;
		this.aantal = aantal;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public int getAantalRingen() {
		return aantalRingen;
	}

	public void setAantalRingen(int aantalRingen) {
		this.aantalRingen = aantalRingen;
	}

	public int getDuur() {
		return duur;
	}

	public void setDuur(int duur) {
		this.duur = duur;
	}

	public int getAantal() { return aantal;	}

	public void setAantal(int aantal) { this.aantal = aantal; }

	public int getAfdPerRing() { return afdPerRing;	}

	public void setAfdPerRing(int afdPerRing) {	this.afdPerRing = afdPerRing; }


}
