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
	private int einduur;
	@XmlElementWrapper(name = "Tijdslots")
	@XmlElement(name = "Tijdslot")
	private final List<Tijdslot> tijdslots;
	@XmlIDREF
	@XmlElementWrapper(name = "Disciplines")
	@XmlElement(name = "Discipline")
	private final HashSet<Discipline> disciplines;

	public Ring(String ringNaam, String ringLetter, int ringIndex, int einduur) {
		naam = ringNaam;
		letter = ringLetter;
		index = ringIndex;
		this.einduur = einduur;
		tijdslots = new ArrayList<>();
		disciplines = new HashSet<>();
	}

	public Ring(String ringNaam, String ringLetter, int ringIndex) { this(ringNaam, ringLetter, ringIndex, 0); }
	public Ring() { this("Ring zonder naam " + Math.random(), "", 0); }

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

	public int getEinduur() {
		if (einduur != 0) return einduur;
		else if (Objects.equals(letter, "")) return Instellingen.Opties().TOTALETIJDRINGMETFINALE;
		else return Instellingen.Opties().TOTALETIJD;
	}
	public void setEinduur(int einduur) { this.einduur = einduur; }

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
			// als de discipline met gereserveerde blokken werkt, de tijdsslots anders genereren
			if (discipline.getSport().equals(Sport.TOUWTREKKEN)) {
				// touwtrekken op IRSF: eerst vroegste, dan laatste, rest opvullen om dynamisch te zijn
				int start = 0;
				int eind = Math.round(getEinduur() / 15.0f) * 15; //TODO: afronden tot op het kwartiertje, kan beter
				Tijdslot eerste = new Tijdslot(start, discipline.getDuur(), this);
				tijdslots.add(eerste);
				start += discipline.getDuur();
				Tijdslot laatste = new Tijdslot(eind - discipline.getDuur(), discipline.getDuur(), this);
				tijdslots.add(laatste);
				eind -= discipline.getDuur();
				for (; start <= (eind - discipline.getDuur()); eind -= discipline.getDuur()) {
					Tijdslot tijdslot = new Tijdslot(eind - discipline.getDuur(), discipline.getDuur(), this);
					tijdslots.add(tijdslot);
				}
			} else {
				for (int i = 0; i < getEinduur(); i = i + discipline.getDuur()) {
					Tijdslot tijdslot = new Tijdslot(i, discipline.getDuur(), this);
					tijdslots.add(tijdslot);
				}
				// laatste tijdslot als ongunstig instellen (liefst niet)
				tijdslots.getLast().setOngunstig(true);
			}
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
		} else if (o instanceof Ring other) {
			return getRingIndex() == other.getRingIndex();
		} else {
			return false;
		}
	}

}
