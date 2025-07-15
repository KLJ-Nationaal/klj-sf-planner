package domain;

import jakarta.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum Sport implements RestrictieInterface {
	DANS("Dans"),
	PIRAMIDE("Piramide"),
	VENDELEN("Vendelen"),
	WIMPELEN("Wimpelen"),
	TOUWTREKKEN("Touwtrekken");

	private final String naam;

	Sport(String naam) { this.naam = naam; }

	public String getNaam() { return naam; }

	@Override
	public String toString() { return naam; }
}
