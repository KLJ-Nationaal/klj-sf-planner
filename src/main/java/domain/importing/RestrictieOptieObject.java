package domain.importing;

import domain.RestrictieInterface;
import domain.Sport;
import jakarta.xml.bind.annotation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

import java.util.Optional;

public class RestrictieOptieObject {
	private final ObjectProperty<RestrictieInterface> object = new SimpleObjectProperty<>();
	private final BooleanProperty alleRingen = new SimpleBooleanProperty(false);
	private final ObjectProperty<RestrictieClass> type = new SimpleObjectProperty<>();

	public RestrictieOptieObject() {
		// Bind type aan de klasse van object
		this.type.bind(Bindings.createObjectBinding(() ->
						Optional.ofNullable(this.object.get())
								.map(RestrictieInterface::getClass)
								.flatMap(RestrictieClass::fromClass)
								.orElse(null),
				this.object));
	}

	public RestrictieOptieObject(RestrictieInterface object, boolean alleRingen) {
		this();
		this.object.setValue(object);
		this.alleRingen.setValue(alleRingen);
	}

	@XmlIDREF
	@XmlElements({
			@XmlElement(name = "Sport", type = Sport.class),
			@XmlElement(name = "Discipline", type = Reeks.class)
	})
	public RestrictieInterface getObject() { return this.object.getValue(); }
	public Property<RestrictieInterface> objectProperty() { return this.object; }
	public void setObject(RestrictieInterface object) { this.object.setValue(object); }

	@XmlAttribute(name = "AlleRingen")
	public boolean isAlleRingen() { return alleRingen.get(); }
	public BooleanProperty alleRingenProperty() { return alleRingen; }
	public void setAlleRingen(boolean alleringen) { alleRingen.setValue(alleringen); }

	@XmlTransient
	public RestrictieClass getType() { return type.getValue(); }
	public ReadOnlyObjectProperty<RestrictieClass> typeProperty() { return type; }

	@Override
	public String toString() {
		return getObject() + (isAlleRingen() ? " (alle ringen)" : "");
	}
}
