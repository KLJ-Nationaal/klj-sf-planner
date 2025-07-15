package domain;

import domain.importing.Reeks;

import java.util.Optional;

public enum RestrictieClass {
	SPORT("Sport", Sport.class),
	DISCIPLINE("Discipline", Reeks.class);

	final String value;
	final Class<?> clazz;

	RestrictieClass(String value, Class<?> clazz) {
		this.value = value;
		this.clazz = clazz;
	}

	public static Optional<RestrictieClass> fromClass(Class<?> clazz) {
		for (RestrictieClass rc : RestrictieClass.values()) {
			if (rc.clazz.equals(clazz)) {
				return Optional.of(rc);
			}
		}
		return Optional.empty();
	}
}
