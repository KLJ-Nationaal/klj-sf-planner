package domain;

import domain.importing.Reeks;

public sealed interface RestrictieInterface permits Sport, Reeks {
	String getNaam();
}