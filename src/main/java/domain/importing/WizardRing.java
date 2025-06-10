package domain.importing;

public class WizardRing {
	private String naam;
	private int aantalRingen;
	private int duur;
	private int aantalAfd;
	private int maxAfdPerRing;

	public WizardRing(String ring, Integer aantalAfd) {
		this.naam = ring;
		this.aantalAfd = aantalAfd;
	}

	public String getNaam() { return naam; }
	public void setNaam(String naam) { this.naam = naam; }

	public int getAantalRingen() { return aantalRingen; }
	public void setAantalRingen(int aantalRingen) { this.aantalRingen = aantalRingen; }

	public int getDuur() { return duur; }
	public void setDuur(int duur) { this.duur = duur; }

	public int getAantalAfd() { return aantalAfd; }
	public void setAantalAfd(int aantal) { this.aantalAfd = aantal; }

	public int getMaxAfdPerRing() { return maxAfdPerRing; }
	public void setMaxAfdPerRing(int maxAfdPerRing) { this.maxAfdPerRing = maxAfdPerRing; }
}
