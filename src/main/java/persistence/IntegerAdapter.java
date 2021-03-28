package persistence;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class IntegerAdapter extends XmlAdapter<String, Integer> {
	@Override
	public Integer unmarshal(String v) throws Exception {
		return Integer.parseInt(v);
	}

	@Override
	public String marshal(Integer v) throws Exception {
		return Integer.toString(v);
	}
}
