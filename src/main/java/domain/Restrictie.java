package domain;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class Restrictie {

	private final StringProperty afdeling = new SimpleStringProperty();
	private RestrictieOptie A = new RestrictieOptie();
	private RestrictieOptie B = new RestrictieOptie();

	public Restrictie() {} // default constructor nodig voor XML marshalling

	public Restrictie(String afdeling, RestrictieInterface a, boolean alleKorpsenA, RestrictieInterface b, boolean alleKorpsenB) {
		this.afdeling.setValue(afdeling);
		this.A.setObject(a);
		this.A.setAlleKorpsen(alleKorpsenA);
		this.B.setObject(b);
		this.B.setAlleKorpsen(alleKorpsenB);
	}

	@XmlAttribute(name = "Afdeling")
	public String getAfdeling() { return afdeling.get(); }
	public StringProperty afdelingProperty() { return afdeling; }
	public void setAfdeling(String afdeling) { this.afdeling.setValue(afdeling); }

	@XmlElement(name = "A")
	public RestrictieOptie getA() { return A; }
	public void setA(RestrictieOptie a) { A = a; }
	public Property<RestrictieInterface> aProperty() { return A.objectProperty(); }

	@XmlElement(name = "B")
	public RestrictieOptie getB() { return B; }
	public void setB(RestrictieOptie b) { B = b; }
	public Property<RestrictieInterface> bProperty() { return B.objectProperty(); }

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof Restrictie that)) return false;

		return afdeling.equals(that.afdeling) && Objects.equals(A, that.A) && Objects.equals(B, that.B);
	}

	@Override
	public int hashCode() {
		return Objects.hash(afdeling, A, B);
	}

	@Override
	public String toString() {
		return afdeling.getValue() + ": " + A + " en " + B;
	}
}
