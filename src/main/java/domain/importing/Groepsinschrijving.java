package domain.importing;

public class Groepsinschrijving {
	private String afdeling;
	private String discipline;
	private int aantal;

	public Groepsinschrijving(String afdeling, String discipline, int aantal) {
		this.afdeling = afdeling;
		this.discipline = discipline;
		this.aantal = aantal;
	}

	public String getAfdeling() { return afdeling; }
	public void setAfdeling(String afdeling) { this.afdeling = afdeling; }

	public String getDiscipline() { return discipline; }
	public void setDiscipline(String discipline) { this.discipline = discipline; }

	public int getAantal() { return aantal; }
	public void setAantal(int aantal) { this.aantal = aantal; }

}
