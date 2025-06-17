package domain;

import jakarta.xml.bind.annotation.*;
import persistence.Instellingen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Ring {
	private String naam;
	private String letter;
	private int index;
	@XmlElementWrapper(name = "Tijdslots")
	@XmlElement(name = "Tijdslot")
	private final List<Tijdslot> tijdslots;
	@XmlIDREF
	@XmlElementWrapper(name = "Disciplines")
	@XmlElement(name = "Discipline")
	private final HashSet<Discipline> disciplines;

	public Ring(String ringNaam, String ringLetter, int ringIndex) {
		naam = ringNaam;
		letter = ringLetter;
		index = ringIndex;
		tijdslots = new ArrayList<>();
		disciplines = new HashSet<>();
	}

	public Ring() {
		this("Ring zonder naam " + Math.random(), "", 0);
	}

	public String getNaam() { return naam; }
	public void setNaam(String naam) { this.naam = naam; }

	public String getLetter() { return letter; }
	public void setLetter(String letter) { this.letter = letter; }

	@XmlID
	@XmlAttribute(name = "id")
	public String getRef() { return "R" + index; }
	public void setRef(String s) { this.index = Integer.parseInt(s.substring(1)); }

	public int getRingIndex() { return index; }
	public void setRingIndex(int ringIndex) { index = ringIndex; }

	@XmlTransient
	public List<Tijdslot> getTijdslots() {
		return tijdslots;
	}

	@XmlTransient
	public HashSet<Discipline> getDisciplines() { return disciplines; }
	public void addDiscipline(Discipline discipline) {
		disciplines.add(discipline);
		// tijdslots voor ring maken als ze nog niet bestaan
		if (tijdslots.isEmpty()) {
			int totaletijd = Instellingen.Opties().TOTALETIJDRINGMETFINALE;
			if (Objects.equals(letter, "")) totaletijd = Instellingen.Opties().TOTALETIJD;
			for (int i = 0; i < totaletijd; i = i + discipline.getDuur()) {  //TODO: property van maken
				Tijdslot tijdslot = new Tijdslot(i, discipline.getDuur(), this);
				tijdslots.add(tijdslot);
			}
			// laatste tijdslot als ongunstig instellen (liefst niet)
			tijdslots.get(tijdslots.size() - 1).setOngunstig(true);
		}
	}

	public String getVerkorteNotatie() {
		return naam
				.replace("meisjes", "")
				.replace("jongens", "")
				.replace("gemengd", "");
	}

	@Override
	public String toString() { return naam + (!Objects.equals(letter, "") ? " " + letter : ""); }

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
