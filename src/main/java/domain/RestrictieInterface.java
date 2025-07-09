package domain;

import domain.importing.Reeks;

public sealed interface RestrictieInterface permits Discipline, Sport, Reeks {
	String getNaam();
}