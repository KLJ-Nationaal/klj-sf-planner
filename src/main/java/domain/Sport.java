package domain;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlID;

@XmlEnum
public enum Sport implements RestrictieInterface {
	DANS("Dans"),
	PIRAMIDE("Piramide"),
	VENDELEN("Vendelen"),
	WIMPELEN("Wimpelen"),
	TOUWTREKKEN("Touwtrekken");

	private final String naam;

	Sport(String naam) { this.naam = naam; }

	@XmlID
	public String getNaam() { return naam; }

	@Override
	public String toString() { return naam; }
}
