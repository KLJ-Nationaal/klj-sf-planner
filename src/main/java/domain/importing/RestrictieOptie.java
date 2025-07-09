package domain.importing;

import domain.Discipline;
import domain.RestrictieInterface;
import domain.Sport;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;

public class RestrictieOptie {

	private final StringProperty afdeling = new SimpleStringProperty();
	private RestrictieOptieObject A = new RestrictieOptieObject();
	private RestrictieOptieObject B = new RestrictieOptieObject();

	public RestrictieOptie() {} // default constructor nodig voor XML marshalling

	public RestrictieOptie(String afdeling, RestrictieInterface a, boolean alleRingenA, RestrictieInterface b, boolean alleRingenB) {
		this.afdeling.setValue(afdeling);
		this.A.setObject(a);
		this.A.setAlleRingen(alleRingenA);
		this.B.setObject(b);
		this.B.setAlleRingen(alleRingenB);
	}

	@XmlAttribute(name = "Afdeling")
	public String getAfdeling() { return afdeling.get(); }
	public StringProperty afdelingProperty() { return afdeling; }
	public void setAfdeling(String afdeling) { this.afdeling.setValue(afdeling); }

	@XmlElement(name = "A")
	public RestrictieOptieObject getA() { return A; }
	public void setA(RestrictieOptieObject a) { A = a; }

	public Optional<Sport> getAAsSport() { return A.getObject() instanceof Sport s ? Optional.of(s) : Optional.empty(); }
	public Optional<Discipline> getAAsDiscipline() { return A.getObject() instanceof Discipline s ? Optional.of(s) : Optional.empty(); }
	public Property<RestrictieInterface> aProperty() { return A.objectProperty(); }

	@XmlElement(name = "B")
	public RestrictieOptieObject getB() { return B; }
	public void setB(RestrictieOptieObject b) { B = b; }

	public Optional<Sport> getBAsSport() { return B.getObject() instanceof Sport s ? Optional.of(s) : Optional.empty(); }
	public Optional<Discipline> getBAsDiscipline() { return B.getObject() instanceof Discipline s ? Optional.of(s) : Optional.empty(); }
	public Property<RestrictieInterface> bProperty() { return B.objectProperty(); }

	@Override
	public String toString() {
		return afdeling + ": " + A + " en " + B;
	}
}
