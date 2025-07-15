module kljsfplanner {
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;
	requires org.fxmisc.richtext;
	requires org.fxmisc.flowless;
	requires org.controlsfx.controls;
	requires org.optaplanner.core;
	requires org.optaplanner.core.impl;
	requires org.apache.commons.lang3;
	requires java.sql;
	requires java.desktop;
	requires ch.qos.logback.core;
	requires ch.qos.logback.classic;
	requires com.google.guice;
	requires org.slf4j;
	requires jakarta.xml.bind;
	requires jakarta.inject;
	requires org.apache.poi.poi;
	requires org.apache.poi.ooxml;
	requires java.management;

	opens ui;
	opens ui.importer;
	opens ui.visualization.jfxtras.scene.control.agenda;
	opens logging;
	opens persistence;
	opens domain;
	opens domain.importing;
	opens difficulty;
	opens solver;
	opens app;
	opens util;

	exports solver;
}
