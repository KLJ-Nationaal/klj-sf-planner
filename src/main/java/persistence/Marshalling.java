package persistence;

import ch.qos.logback.classic.Logger;
import domain.*;
import domain.importing.Groepsinschrijving;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Marshalling {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(Marshalling.class);

	public static int getActiveSheet(String filename) {
		try {
			File excelFile = new File(filename);
			FileInputStream fis = new FileInputStream(excelFile);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			return workbook.getActiveSheetIndex();
		} catch (IOException ioe) {
			logger.error("KON DATA XLSX NIET INLEZEN", ioe);
		}
		return 0;
	}

	public static ArrayList<String> getGroepsinschrijvingenFirstLine(String filename, int worksheetIndex) {
		ArrayList<String> columns = new ArrayList<>();
		try {
			File excelFile = new File(filename);
			FileInputStream fis = new FileInputStream(excelFile);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(worksheetIndex);
			Iterator<Row> rowIt = sheet.iterator();
			Row row = rowIt.next();

			if (row != null) {
				row.forEach(cell -> {
					if (cell.getStringCellValue() != null && !Objects.equals(cell.getStringCellValue(), "")) {
						columns.add(cell.getStringCellValue());
					} else {
						columns.add("");
					}
				});
			}
		} catch (IOException ioe) {
			logger.error("KON DATA XLSX NIET INLEZEN", ioe);
		}
		return columns;
	}

	public static ArrayList<Groepsinschrijving> importGroepsinschrijvingen(String filename, int worksheetIndex,
	                                                                       boolean hasTitles, int colSportfeest, int colAfdeling, int colDiscipline, int colRegio, int colAantal) {
		ArrayList<Groepsinschrijving> groepsinschrijvingen = new ArrayList<>();
		try {
			File excelFile = new File(filename);
			FileInputStream fis = new FileInputStream(excelFile);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(worksheetIndex);
			Iterator<Row> rowIt = sheet.iterator();

			if (hasTitles) rowIt.next();

			while (rowIt.hasNext()) {
				Row row = rowIt.next();
				try {
					String sportfeest = row.getCell(colSportfeest).getStringCellValue();
					String afdeling = row.getCell(colAfdeling).getStringCellValue();
					String discipline = row.getCell(colDiscipline).getStringCellValue();
					String regio = row.getCell(colRegio).getStringCellValue();
					int aantal = 0;
					if (row.getCell(colAantal).getCellType() == CellType.NUMERIC) {
						aantal = (int) row.getCell(colAantal).getNumericCellValue();
					} else if (row.getCell(colAantal).getCellType() == CellType.STRING) {
						aantal = Integer.parseInt(row.getCell(colAantal).getStringCellValue());
					}
					if (aantal == 0) {
						logger.error("Kon aantal inschrijvingen niet lezen voor afdeling {}, discipline {}", afdeling, discipline);
					}

					groepsinschrijvingen.add(new Groepsinschrijving(sportfeest, afdeling, discipline, regio, aantal));
				} catch (NullPointerException npe) {
					logger.error("Fout op rij {}: {}", row.getRowNum() + 1, npe.getLocalizedMessage());
				}
			}

		} catch (IOException ioe) {
			logger.error("KON DATA XLSX NIET INLEZEN", ioe);
		}
		return groepsinschrijvingen;
	}

	public static Sportfeest unmarshallXml(String filename){
		Sportfeest sf = new Sportfeest();
		try {
			JAXBContext context = JAXBContext.newInstance(Sportfeest.class);
			Unmarshaller m = context.createUnmarshaller();

			try (Reader r = new FileReader(filename)) {
				sf = (Sportfeest) m.unmarshal(r);
			}
			logger.info("Bestand {} ingelezen", filename);
		} catch (Exception e) {
			logger.error("Error while unmarshalling XML", e);
		}
		return sf;
	}

	public static void marshallXml(Sportfeest map, String filename) {
		try {
			JAXBContext context = JAXBContext.newInstance(Sportfeest.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			try (Writer w = new FileWriter(filename)) {
				m.marshal(map, w);
			}
			logger.info("Bestand {} opgeslagen", filename);
		} catch (Exception e) {
			logger.error("Error while marshalling XML", e);
		}
	}

	public static void marshall(Sportfeest map, String path) throws Exception {
		if (path.endsWith(".xlsx")) path = path.substring(0, path.length() - 5);
		File fileAfdelingen = new File(path + "-afdelingen.xlsx");
		File fileRingen = new File(path + "-ringen.xlsx");
		if (fileAfdelingen.exists() && !fileAfdelingen.delete())
			throw new Exception("Kon bestaand bestand " + fileAfdelingen.getName() + " niet overschrijven. Houd Excel dit bestand misschien open?");
		if (fileRingen.exists() && !fileRingen.delete())
			throw new Exception("Kon bestaand bestand " + fileRingen.getName() + " niet overschrijven. Houd Excel dit bestand misschien open?");

		int rijen = (int) Math.ceil(1.0 * Instellingen.Opties().TABELMINUTEN / Instellingen.Opties().MINMINUTEN / 2) + 3;
		fixRingNumbersOrder(map);

		//***************************************
		//Excel-bestand met werkblad per afdeling
		//***************************************
		XSSFWorkbook wb = new XSSFWorkbook();
		Map<String, CellStyle> styles = createStyles(wb);

		List<Afdeling> sortedAfdelingen = map.getAfdelingen().stream()
				.sorted(Comparator.comparing(Afdeling::getNaam))
				.toList();
		for (Afdeling afdeling : sortedAfdelingen) {
			XSSFSheet sheet = wb.createSheet(afdeling.getNaam());
			PrintSetup printSetup = sheet.getPrintSetup();
			printSetup.setLandscape(true);
			printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);
			sheet.setFitToPage(true);
			sheet.setHorizontallyCenter(true);

			//Afdelingstitel
			Row titleRow = sheet.createRow(0);
			titleRow.setHeightInPoints(20);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue(afdeling.getNaam() + " meisjes");
			titleCell.setCellStyle(styles.get("hoofding"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$D$1"));
			titleCell = titleRow.createCell(5);
			titleCell.setCellValue(afdeling.getNaam() + " jongens");
			titleCell.setCellStyle(styles.get("hoofding"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$F$1:$I$1"));

			//de rest opmaken
			for (int i = 2; i < rijen; i++) {
				Row row = sheet.createRow(i);
				SimpleDateFormat df = new SimpleDateFormat("HH:mm");
				Date d = df.parse(Instellingen.Opties().STARTTIJD);
				Calendar cal1 = Calendar.getInstance();
				cal1.setTime(d);
				cal1.add(Calendar.MINUTE, Instellingen.Opties().MINMINUTEN * (i - 2));
				Calendar cal2 = Calendar.getInstance();
				cal2.setTime(d);
				cal2.add(Calendar.MINUTE, Instellingen.Opties().MINMINUTEN * (i - 2) + (Instellingen.Opties().TABELMINUTEN / 2 + 3));
				for (int col = 0; col < 9; col++) {
					Cell cell = row.createCell(col);
					if (col == 0 || col == 5) cell.setCellValue(df.format(cal1.getTime()));
					if (col == 2 || col == 7) cell.setCellValue(df.format(cal2.getTime()));
					if (col == 0 || col == 5 || col == 2 || col == 7) cell.setCellStyle(styles.get("tijd"));
					if (col == 1 || col == 6 || col == 3 || col == 8) cell.setCellStyle(styles.get("ring"));
					if (i == (rijen - 1) && (col == 2 || col == 7 || col == 3 || col == 8)) cell.setCellStyle(styles.get("volledigzwart"));
				}
			}

			//gegevens invullen
			for (int i = 0; i <= Instellingen.Opties().TABELMINUTEN; i = i + Instellingen.Opties().MINMINUTEN) {
				int row = 2 + (i % (Instellingen.Opties().TABELMINUTEN / 2 + 3)) / 3;
				int column = (i > (Instellingen.Opties().TABELMINUTEN / 2) ? 3 : 1);
				final int time = i;
				List<Inschrijving> inschrijvingList = afdeling.getInschrijvingen().stream()
						.filter(inschr -> inschr.getTijdslot() != null)
						.filter(inschr -> inschr.getTijdslot().isIncluded(time))
						.toList();
				for (Inschrijving inschrijving : inschrijvingList) {
					boolean edited = false;

					if (inschrijving.getDiscipline().isMeisjes()) {
						edited = setCellInschrijvingForAfdeling(styles, sheet.getRow(row), column, i, inschrijving);
					}
					if (inschrijving.getDiscipline().isJongens()) {
						edited = setCellInschrijvingForAfdeling(styles, sheet.getRow(row), column + 5, i, inschrijving);
					}

					if (!edited) {
						logger.error("Inschrijving in {} van {}: jongens/meisjes kan niet bepaald worden en werd dus overgeslagen!",
								inschrijving.getRing(), inschrijving.getAfdeling());
					}
				}
			}

			//SF informatie
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = format.format(new Date());

			String sfInfo = (map.getLocatie().toLowerCase().contains("landjuweel") ? "" : "Sportfeest ") + map.getLocatie() + " op "
					+ (new SimpleDateFormat("d/MM/yyyy")).format(map.getDatum());
			Row infoRow = sheet.createRow(1);
			infoRow.setHeightInPoints(20);
			for (int i = 0; i <= 3; i++) {
				Cell infoCell = infoRow.createCell(i);
				if (i == 0) infoCell.setCellValue(sfInfo);
				infoCell.setCellStyle(styles.get("info"));
			}
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));
			for (int i = 5; i <= 8; i++) {
				Cell infoCell = infoRow.createCell(i);
				if (i == 5) infoCell.setCellValue(sfInfo);
				infoCell.setCellStyle(styles.get("info"));
			}
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 5, 8));

			//dikke lijn rondomrond
			CellRangeAddress region = CellRangeAddress.valueOf("A1:D" + rijen);
			RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
			RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
			RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
			RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
			region = CellRangeAddress.valueOf("F1:I" + rijen);
			RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
			RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
			RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
			RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);

			//kolombreedtes instellen
			int[] columnWidths = {6, 28, 6, 28, 2, 6, 28, 6, 28};
			for (int i = 0; i < 9; i++) {
				sheet.setColumnWidth(i, columnWidths[i] * 256);
			}
		}

		logger.info("Schrijven van bestand {}", fileAfdelingen);
		FileOutputStream out = new FileOutputStream(fileAfdelingen);
		wb.write(out);
		out.close();

		try {
			Desktop.getDesktop().open(fileAfdelingen);
		} catch (Exception e) { logger.warn(e.getMessage()); }

		//********************************************
		//Excel-bestand met werkblad per groep ringen
		//********************************************
		wb = new XSSFWorkbook();
		styles = createStyles(wb);

		//tabblad groepen maken
		List<String> ringGroepen = new ArrayList<>();
		for (Discipline discipline : map.getDisciplines().values()) ringGroepen.add(discipline.getRingNaam());
		List<String> sortedRingGroepen = ringGroepen.stream().distinct().sorted().toList();

		for (String ringGroep : sortedRingGroepen) {
			XSSFSheet sheet = wb.createSheet(ringGroep);
			PrintSetup printSetup = sheet.getPrintSetup();
			printSetup.setLandscape(true);
			printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);
			sheet.setMargin(PageMargin.LEFT, 1 / 2.54);
			sheet.setMargin(PageMargin.RIGHT, 1 / 2.54);
			sheet.setMargin(PageMargin.TOP, 1 / 2.54);
			sheet.setMargin(PageMargin.BOTTOM, 1 / 2.54);

			List<Ring> sortedRingen = map.getRingen().stream()
					.filter(rng -> ringGroep.equals(rng.getDisciplines().stream().findFirst().get().getRingNaam()))
					.sorted(Comparator.comparing(Ring::getVerkorteNotatie))
					.toList();
			if (sortedRingen.isEmpty()) {
				logger.error("Geen ringen gevonden voor groep {}. Dit zal worden overgeslagen", ringGroep);
				continue;
			}
			int ringGroepDuur = sortedRingen.stream()
					.flatMap(ring -> ring.getTijdslots().stream())
					.mapToInt(Tijdslot::getDuur)
					.min()
					.orElse(Instellingen.Opties().MINMINUTEN);

			//creeer eerst de rijen
			int aantalrijen = (int) Math.ceil(
					(3 + (Instellingen.Opties().TOTALETIJD / 2f / ringGroepDuur))
							* Math.ceil(sortedRingen.size() / 2f));
			for (int i = 0; i <= aantalrijen; i++) {
				Row row = sheet.createRow(i);
				row.setHeightInPoints(Instellingen.Opties().ROW_HEIGHT);
			}

			for (int i = 0; i < sortedRingen.size(); i++) {
				Ring currentRing = sortedRingen.get(i);
				int tbRowBase = (int) ((i / 2) * (4 + (Instellingen.Opties().TOTALETIJD / ringGroepDuur / 2f)));
				int tbColBase = (i % 2) * 5;

				//Ringtitel
				Cell titleCell = sheet.getRow(tbRowBase).createCell(tbColBase);
				titleCell.setCellValue(currentRing.toString());
				titleCell.setCellStyle(styles.get("hoofding"));
				sheet.addMergedRegion(new CellRangeAddress(tbRowBase, tbRowBase, tbColBase, tbColBase + 3));

				//Tijden invullen
				SimpleDateFormat df = new SimpleDateFormat("HH:mm");
				Date d = df.parse(Instellingen.Opties().STARTTIJD);
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				int slots = (Instellingen.Opties().TOTALETIJD / ringGroepDuur) + 1;
				for (int j = 0; j < slots; j++) {
					int relRow = j % (int) Math.ceil(slots / 2f); //helft, afronden naar boven
					Row row = sheet.getRow(tbRowBase + 1 + relRow);
					int secondCol = (j >= (slots / 2f) ? 2 : 0);
					Cell cell = row.createCell(tbColBase + secondCol);
					cell.setCellValue(df.format(cal.getTime()));
					cell.setCellStyle(styles.get("tijd"));
					cell = row.createCell(tbColBase + 1 + secondCol);
					cell.setCellStyle(styles.get("afdeling"));

					cal.add(Calendar.MINUTE, ringGroepDuur);
				}

				//Data invullen
				List<Inschrijving> ringInschrijvingen = map.getAfdelingen().stream()
						.map(Afdeling::getInschrijvingen)
						.flatMap(Collection::stream)
						.filter(inschr -> currentRing.equals(inschr.getRing()))
						.toList();

				//font voor extensie
				XSSFFont fontExtensie = wb.createFont();
				fontExtensie.setItalic(true);
				fontExtensie.setFontHeight(9);

				for (Inschrijving inschrijving : ringInschrijvingen) {
					if (inschrijving.getTijdslot() == null) {
						logger.error("Inschrijving in {} van {} heeft geen tijdslot toegewezen!",
								inschrijving.getRing(), inschrijving.getAfdeling());
					} else {
						String tijd = inschrijving.getTijdslot().getStartTijdFormatted();
						boolean tijdFound = false;
						for (int j = 0; j < aantalrijen; j++) {
							Row row = sheet.getRow(tbRowBase + j);
							Cell cell = row.getCell(tbColBase);
							if (cell != null && cell.getStringCellValue().equals(tijd)) {
								String value = row.getCell(tbColBase + 1).getStringCellValue()
										+ inschrijving.getAfdeling().getNaam()
										+ (inschrijving.getKorps() > 0 ? " " + inschrijving.getKorps() : "");
								XSSFRichTextString rts = new XSSFRichTextString(value);
								if (inschrijving.getDiscipline().getExtensie() != null)
									rts.append("  " + inschrijving.getDiscipline().getExtensie(), fontExtensie);
								row.getCell(tbColBase + 1).setCellValue(rts);
								tijdFound = true;
								break;
							}
							cell = row.getCell(tbColBase + 2);
							if (cell != null && cell.getStringCellValue().equals(tijd)) {
								String value = row.getCell(tbColBase + 3).getStringCellValue()
										+ inschrijving.getAfdeling().getNaam()
										+ (inschrijving.getKorps() > 0 ? " " + inschrijving.getKorps() : "");
								XSSFRichTextString rts = new XSSFRichTextString(value);
								if (inschrijving.getDiscipline().getExtensie() != null)
									rts.append("  " + inschrijving.getDiscipline().getExtensie(), fontExtensie);
								row.getCell(tbColBase + 3).setCellValue(rts);
								tijdFound = true;
								break;
							}
						}
						if (!tijdFound)
							logger.error("Inschrijving in {} van {}, kon het tijdslot in de ring niet vinden!",
									inschrijving.getRing(), inschrijving.getAfdeling());

					}
				}

				//voeg bovenaan de ring een eindemarkering in indien groter dan één pagina
				int lastBreakIndex = 0;
				if (sheet.getRowBreaks().length - 1 >= 0) {
					lastBreakIndex = sheet.getRowBreaks()[sheet.getRowBreaks().length - 1];
				}
				int lastRowPageIndex = tbRowBase + (int) Math.ceil(slots / 2f) - lastBreakIndex;
				if (lastRowPageIndex >= 550 / Instellingen.Opties().ROW_HEIGHT) {
					sheet.setRowBreak(tbRowBase - 1);
				}

			}

			//kolombreedtes instellen
			double[] columnWidths = {6, 27, 6, 27, 7, 6, 27, 6, 27};
			for (int i = 0; i < 9; i++) {
				sheet.setColumnWidth(i, (int) columnWidths[i] * 256);
			}
		}

		logger.info("Schrijven van bestand {}", fileRingen);
		out = new FileOutputStream(fileRingen);
		wb.write(out);
		out.close();

		try {
			Desktop.getDesktop().open(fileRingen);
		} catch (Exception e) { logger.warn(e.getMessage()); }
	}

	private static void fixRingNumbersOrder(Sportfeest map) {
		for (Afdeling afdeling : map.getAfdelingen()) {
			List<Inschrijving> sortedInschrijvingen = afdeling.getInschrijvingen().stream()
					.sorted(Comparator.comparing(Inschrijving::getTijdslot))
					.toList();
			HashMap<Discipline, Integer> ringCounter = new HashMap<>();
			for (Inschrijving inschrijving : sortedInschrijvingen) {
				int count = 1;
				if (ringCounter.containsKey(inschrijving.getDiscipline()))
					count += ringCounter.get(inschrijving.getDiscipline());
				if (inschrijving.getKorps() > 0) {
					inschrijving.setKorps(count);
					ringCounter.put(inschrijving.getDiscipline(), count);
				}
			}
		}
	}

	private static boolean setCellInschrijvingForAfdeling(Map<String, CellStyle> styles, XSSFRow row, int column, int i, Inschrijving inschrijving) {
		Cell cell;
		if (i == inschrijving.getTijdslot().getStartTijd()) {
			cell = row.getCell(column - 1);
			cell.setCellStyle(styles.get("tijdonderleeg"));
			cell = row.getCell(column);
			cell.setCellValue((!cell.getStringCellValue().isEmpty() ? cell.getStringCellValue() + " " : "")
					+ inschrijving.getDiscipline().getVerkorteNaam()
					+ (inschrijving.getKorps() > 0 ? " " + inschrijving.getKorps() : "")
					+ Optional
					.ofNullable(inschrijving.getRing().getLetter())
					.filter(StringUtils::isNotEmpty)
					.map(a -> " ring " + a)
					.orElse(""));
			cell.setCellStyle(styles.get("ringonderleeg"));
		} else {
			cell = row.getCell(column - 1);
			//cell.setCellValue("");
			if (i != inschrijving.getTijdslot().getEindTijd() - Instellingen.Opties().MINMINUTEN) {
				cell.setCellStyle(styles.get("tijdonderleeg"));
				cell = row.getCell(column);
				cell.setCellStyle(styles.get("ringonderleeg"));
			}
		}
		return cell != null;
	}

	private static Map<String, CellStyle> createStyles(XSSFWorkbook wb) {
		Map<String, CellStyle> styles = new HashMap<>();
		CellStyle style;
		Font hoofdingFont = wb.createFont();
		hoofdingFont.setFontHeightInPoints((short) 11);
		hoofdingFont.setColor(IndexedColors.WHITE.getIndex());
		hoofdingFont.setBold(true);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(hoofdingFont);
		style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put("hoofding", style);

		Font standaardFont10 = wb.createFont();
		standaardFont10.setFontHeightInPoints((short) 10);
		Font standaardFont11 = wb.createFont();
		standaardFont11.setFontHeightInPoints((short) 11);

		style = wb.createCellStyle();
		style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put("volledigzwart", style);

		style = wb.createCellStyle();
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styles.put("lichtgrijs", style);

		style = wb.createCellStyle();
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(standaardFont10);
		styles.put("tijd", style);

		style = wb.createCellStyle();
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.NONE);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(standaardFont10);
		styles.put("tijdonderleeg", style);

		style = wb.createCellStyle();
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(standaardFont10);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setWrapText(true);
		styles.put("ring", style);

		style = wb.createCellStyle();
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.NONE);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(standaardFont10);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setWrapText(true);
		styles.put("ringonderleeg", style);

		style = wb.createCellStyle();
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(standaardFont11);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setWrapText(true);
		styles.put("afdeling", style);

		style = wb.createCellStyle();
		Font infoFont = wb.createFont();
		infoFont.setItalic(true);
		style.setFont(infoFont);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styles.put("info", style);

		return styles;
	}
}
