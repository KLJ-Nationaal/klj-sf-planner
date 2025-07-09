package domain;

public record Restrictie<A extends RestrictieInterface, B extends RestrictieInterface>(A a, boolean alleRingenA, B b, boolean alleRingenB) {
	@Override
	public String toString() {
		return a + (alleRingenA ? " (alle ringen)" : "") + " en " + b + (alleRingenB ? " (alle ringen)" : "");
	}
}
