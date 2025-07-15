package domain;

import domain.importing.Reeks;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import persistence.RestrictieReeksAdapter;

import java.util.Optional;

public class RestrictieOptie {
	private final ObjectProperty<RestrictieInterface> object = new SimpleObjectProperty<>();
	private final BooleanProperty alleKorpsen = new SimpleBooleanProperty(false);
	private final ObjectProperty<RestrictieClass> type = new SimpleObjectProperty<>();

	public RestrictieOptie() {
		// Bind type aan de klasse van object
		this.type.bind(Bindings.createObjectBinding(() ->
						Optional.ofNullable(this.object.get())
								.map(RestrictieInterface::getClass)
								.flatMap(RestrictieClass::fromClass)
								.orElse(null),
				this.object));
	}

	public RestrictieOptie(RestrictieInterface object, boolean alleKorpsen) {
		this();
		this.object.setValue(object);
		this.alleKorpsen.setValue(alleKorpsen);
	}

	@XmlElement(name = "Discipline")
	@XmlJavaTypeAdapter(RestrictieReeksAdapter.class)
	public Reeks getObjectAsReeks() { return (type.getValue().clazz.equals(Reeks.class) ? (Reeks) this.object.getValue() : null); }
	public void setObjectAsReeks(Reeks reeks) { this.object.setValue(reeks); }

	@XmlElement(name = "Sport")
	public Sport getObjectAsSport() { return (type.getValue().clazz.equals(Sport.class) ? (Sport) this.object.getValue() : null); }
	public void setObjectAsSport(Sport sport) { this.object.setValue(sport); }

	@XmlTransient
	public RestrictieInterface getObject() { return this.object.getValue(); }
	public Property<RestrictieInterface> objectProperty() { return this.object; }
	public void setObject(RestrictieInterface object) { this.object.setValue(object); }

	@XmlAttribute(name = "AlleKorpsen")
	public boolean getAlleKorpsen() { return alleKorpsen.get(); }
	public BooleanProperty alleKorpsenProperty() { return alleKorpsen; }
	public void setAlleKorpsen(boolean alleKorpsen) { this.alleKorpsen.setValue(alleKorpsen); }

	@XmlTransient
	public RestrictieClass getType() { return type.getValue(); }
	public ReadOnlyObjectProperty<RestrictieClass> typeProperty() { return type; }

	@Override
	public String toString() {
		return getObject() + (getAlleKorpsen() ? " (alle korpsen)" : "");
	}
}
