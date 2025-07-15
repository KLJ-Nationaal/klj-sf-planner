package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class VersionInfo {
	private static final String VERSION;
	private static final Logger logger = LoggerFactory.getLogger(VersionInfo.class);

	static {
		Properties props = new Properties();
		String version = "";
		try (var input = VersionInfo.class.getResourceAsStream("/version.properties")) {
			props.load(input);
			version = props.getProperty("version", "0");
		} catch (IOException e) {
			logger.error("Kan versie niet laden", e);
		}
		VERSION = version;
	}

	public static String getVersion() {
		return VERSION;
	}
}
