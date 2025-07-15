package persistence;

import domain.importing.Reeks;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;

public class RestrictieReeksAdapter extends XmlAdapter<String, Reeks> {

	private ArrayList<Reeks> reeksen = new ArrayList<>();

	@Override
	public Reeks unmarshal(String naam) {
		if (reeksen.isEmpty()) reeksen = ReeksDefinitie.unMarshall();
		return reeksen.stream()
				.filter(reeks -> reeks.getNaam().equals(naam))
				.findAny()
				.orElse(new Reeks(naam, 0));
	}

	@Override
	public String marshal(Reeks reeks) {
		return reeks.getNaam();
	}
}
