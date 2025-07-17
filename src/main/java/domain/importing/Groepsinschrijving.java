package domain.importing;

public class Groepsinschrijving {
	private String sportfeest;
	private String afdeling;
	private String sport;
	private String regio;
	private int aantal;

	public Groepsinschrijving(String sportfeest, String afdeling, String sport, int aantal) {
		this.sportfeest = sportfeest;
		this.afdeling = afdeling;
		this.sport = sport;
		this.aantal = aantal;
	}

	public String getSportfeest() { return sportfeest; }
	public void setSportfeest(String sportfeest) { this.sportfeest = sportfeest; }

	public String getAfdeling() { return afdeling; }
	public void setAfdeling(String afdeling) { this.afdeling = afdeling; }

	public String getSport() { return sport; }
	public void setSport(String sport) { this.sport = sport; }

	public String getRegio() { return regio; }
	public void setRegio(String regio) { this.regio = regio; }

	public int getAantal() { return aantal; }
	public void setAantal(int aantal) { this.aantal = aantal; }

}
