package domain;

import persistence.Marshalling;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "Ring")
public class Ring {
	@XmlElement(name = "Naam")
	private String naam;
	private String letter;
	private int index;
	private List<Tijdslot> tijdslots;
	private HashSet<Discipline> disciplines;

	public Ring(String ringNaam, String ringLetter, int ringIndex) {
		naam = ringNaam;
		letter = ringLetter;
		index = ringIndex;
		tijdslots = new ArrayList<>();
		disciplines = new HashSet<>();
	}

	public String getNaam() { return naam; }
	public void setNaam(String naam) { this.naam = naam; }

	public String getLetter() { return letter; }
	public void setLetter(String letter) { this.letter = letter; }

	@XmlElement(name = "RingIndex")
	public int getRingIndex() { return index; }
	public void setRingIndex(int ringIndex) { index = ringIndex; }

	public List<Tijdslot> getTijdslots() {
		return tijdslots;
	}

	public HashSet<Discipline> getDisciplines() { return disciplines;}
	public void addDiscipline(Discipline discipline) {
		disciplines.add(discipline);
		//tijdslots voor ring maken als ze nog niet bestaan
		if(tijdslots.size() == 0) {
			for (int i = 0; i < Marshalling.TOTALETIJD; i = i + discipline.getDuur()) {  //TODO: property van maken
				Tijdslot tijdslot = new Tijdslot(i, discipline.getDuur(), this);
				tijdslots.add(tijdslot);
			}
		}
	}

	public String getVerkorteNotatie() {
		return naam
				.replace("meisjes","")
				.replace("jongens","")
				.replace("gemengd","");
	}

	@Override
	public String toString() { return naam; }

	@Override
	public int hashCode() { return Objects.hash(getRingIndex()); }

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Ring) {
			Ring other = (Ring) o;
			return getRingIndex() == other.getRingIndex();
		} else {
			return false;
		}
	}

}
