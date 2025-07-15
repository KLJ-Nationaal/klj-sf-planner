package logging;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiParser {

	private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[([\\d;]*)m");

	public static List<Pair<String, String>> parse(String input) {
		List<Pair<String, String>> result = new ArrayList<>();
		Matcher matcher = ANSI_PATTERN.matcher(input);

		int lastEnd = 0;
		String currentStyle = "plain";

		while (matcher.find()) {
			if (matcher.start() > lastEnd) {
				String text = input.substring(lastEnd, matcher.start());
				result.add(new Pair<>(text, currentStyle));
			}
			currentStyle = mapAnsiToStyle(matcher.group(1));
			lastEnd = matcher.end();
		}

		if (lastEnd < input.length()) {
			result.add(new Pair<>(input.substring(lastEnd), currentStyle));
		}

		return result;
	}

	private static String mapAnsiToStyle(String code) {
		if (code == null || code.isEmpty()) return "plain";
		for (String part : code.split(";")) {
			switch (part) {
				case "30":
					return "black";
				case "31":
					return "red";
				case "32":
					return "green";
				case "33":
					return "yellow";
				case "34":
					return "blue";
				case "35":
					return "magenta";
				case "36":
					return "cyan";
				case "37":
					return "white";
				case "0":
					return "plain";
			}
		}
		return "plain";
	}
}
