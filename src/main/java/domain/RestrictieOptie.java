package domain;

import domain.importing.Reeks;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import persistence.RestrictieReeksAdapter;

import java.util.Objects;
import java.util.Optional;

public class RestrictieOptie {
	private final StringProperty afdeling = new SimpleStringProperty("");
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

	public RestrictieOptie(String afdeling, RestrictieInterface object, boolean alleKorpsen) {
		this();
		this.afdeling.set(afdeling);
		this.object.setValue(object);
		this.alleKorpsen.setValue(alleKorpsen);
	}

	@XmlAttribute(name = "Afdeling")
	public String getAfdeling() { return afdeling.get(); }
	public StringProperty afdelingProperty() { return afdeling; }
	public void setAfdeling(String afdeling) { this.afdeling.set(afdeling); }

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
	public boolean equals(Object obj) {
		if (!(obj instanceof RestrictieOptie that)) return false;

		return Objects.equals(afdeling.get(), that.afdeling.get())
				&& Objects.equals(object.get(), that.object.get())
				&& Objects.equals(alleKorpsen.get(), that.alleKorpsen.get())
				&& Objects.equals(type.get(), that.type.get());
	}
	@Override
	public String toString() {
		return getAfdeling() + "/" + getObject() + (getAlleKorpsen() ? " (alle korpsen)" : "");
	}
}
