package persistence;

import ch.qos.logback.classic.Logger;
import domain.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Marshalling {
	public static final int MINMINUTEN = 3;
	public static final int TOTALETIJD = 151;
	public static final int ROW_HEIGHT = 18;
	private final static Logger logger = (Logger) LoggerFactory.getLogger(Marshalling.class);

	public static Sportfeest unMarshall(String filename) {
		Sportfeest sf = new Sportfeest();

		try {
			File excelFile = new File(filename);
			FileInputStream fis = new FileInputStream(excelFile);
			XSSFWorkbook workbook = new XSSFWorkbook(fis);

			//lees eerst de ringnamen/minuten in
			XSSFSheet sheet = workbook.getSheet("Ringen");
			Iterator<Row> rowIt = sheet.iterator();

			while(rowIt.hasNext()) {
				Row row = rowIt.next();

				String reeks = row.getCell(0).getStringCellValue();
				String ringNaam = row.getCell(2).getStringCellValue();
				int minuten = 30;
				if(row.getCell(3) != null && row.getCell(3).getCellType() == CellType.NUMERIC) {
					minuten = (int) row.getCell(3).getNumericCellValue();
				}
				String extensie = null;
				if(row.getCell(4) != null && row.getCell(4).getCellType() == CellType.STRING) {
					extensie = row.getCell(4).getStringCellValue();
				}

				if(reeks.length() > 12) { //reeksen hebben altijd een naam met meer dan 12 karakters
					if(minuten == 30) logger.error("Kon aantal minuten voor discipline " + reeks + " niet bepalen!");

					Discipline discipline = new Discipline();
					discipline.setNaam(reeks);
					discipline.setRingNaam(ringNaam);
					discipline.setExtensie(extensie);
					discipline.setDuur(minuten);
					sf.getDisciplines().put(reeks, discipline);
				} else {
					if(row.getRowNum() > 10) break; //dit is waarschijnlijk het einde van de lijst
				}
			}

			//lees nu de inschrijvingen in
			sheet = workbook.getSheet("Ringverdeling");
			rowIt = sheet.iterator();

			sf.setLocatie(rowIt.next().getCell(1).getStringCellValue());
			sf.setDatum(rowIt.next().getCell(1).getDateCellValue());
			rowIt.next(); rowIt.next(); //rijen overslaan overslaan

			int aantalInschrijvingen = 0;

			while(rowIt.hasNext()) {
				Row row = rowIt.next();

				String afdelingsNaam = row.getCell(0).getStringCellValue();
				String disciplineNaam = row.getCell(1).getStringCellValue();
				int aantal = 1;
				if(row.getCell(2) != null && row.getCell(2).getCellType() == CellType.NUMERIC) {
					aantal = (int) row.getCell(2).getNumericCellValue();
				} else {
					logger.error("Kon aantal inschrijvingen niet lezen voor afdeling " + afdelingsNaam +
							", discipline " + disciplineNaam);
				}
				String ringLetter;
				if(row.getCell(3) != null) {
					ringLetter = row.getCell(3).getStringCellValue();
				} else {
					ringLetter = null;
				}

				String ringNaam = sf.getDisciplines().get(disciplineNaam).getRingNaam() +
						(ringLetter == null ? "" : " Ring " + ringLetter);
				Ring ring = sf.getRingen().stream()
						.filter(rng -> ringNaam.equals(rng.getNaam()))
						.findAny()
						.orElse(new Ring(ringNaam, ringLetter, sf.getRingen().size()+1));
				Afdeling afdeling = sf.getAfdelingen().stream()
						.filter(afd -> afdelingsNaam.equals(afd.getNaam()))
						.findAny()
						.orElse(new Afdeling(afdelingsNaam));
				ring.addDiscipline(sf.getDisciplines().get(disciplineNaam));

				for(int i = 0; i < aantal; i++) {  //aantal korpsen
					Inschrijving inschrijving = new Inschrijving();
					inschrijving.setAfdeling(afdeling);
					inschrijving.setRing(ring);
					inschrijving.setId(aantalInschrijvingen);
					inschrijving.setDiscipline(sf.getDisciplines().get(disciplineNaam));
					if(aantal > 1) inschrijving.setKorps(i+1);
					afdeling.getInschrijvingen().add(inschrijving);
					aantalInschrijvingen++;
				}
				sf.getRingen().add(ring);
				sf.getAfdelingen().add(afdeling);
			}

			workbook.close();
			fis.close();

			return sf;
		} catch (IOException ioe) {
			logger.error("KON DATA XLSX NIET INLEZEN");
			ioe.printStackTrace();

		}
		return new Sportfeest();
	}

	public static void marshallXml(Sportfeest map, String filename){
		//TODO diagnostische XML uitvoer
		try {
			JAXBContext context = JAXBContext.newInstance(Sportfeest.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//m.marshal(map, System.out);

			try (Writer w = new FileWriter(filename)) {
				m.marshal(map, w);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void marshall(Sportfeest map){
		fixRingNumbersOrder(map);
		try {
			//***************************************
			//Excel-bestand met werkblad per afdeling
			//***************************************
			XSSFWorkbook wb = new XSSFWorkbook();
			Map<String, CellStyle> styles = createStyles(wb);

			List<Afdeling> sortedAfdelingen = map.getAfdelingen().stream()
					.sorted(Comparator.comparing(Afdeling::getNaam))
					.collect(Collectors.toList());
			for(Afdeling afdeling : sortedAfdelingen) {
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
				for (int i = 2; i < 33; i++) {
					Row row = sheet.createRow(i);
					SimpleDateFormat df = new SimpleDateFormat("HH:mm");
					Date d = df.parse("08:00");
					Calendar cal1 = Calendar.getInstance();
					cal1.setTime(d);
					cal1.add(Calendar.MINUTE, MINMINUTEN * (i-2));
					Calendar cal2 = Calendar.getInstance();
					cal2.setTime(d);
					cal2.add(Calendar.MINUTE, MINMINUTEN * (i-2) + 93);
					for (int col = 0; col < 9; col++) {
						Cell cell = row.createCell(col);
						if(col == 0 || col == 5) cell.setCellValue(df.format(cal1.getTime()));
						if(col == 2 || col == 7) cell.setCellValue(df.format(cal2.getTime()));
						if(col == 0 || col == 5 || col == 2 || col == 7) cell.setCellStyle(styles.get("tijd"));
						if(col == 1 || col == 6 || col == 3 || col == 8) cell.setCellStyle(styles.get("ring"));
						if(i == 32 && (col == 2 || col == 7 || col == 3 || col == 8)) cell.setCellStyle(styles.get("volledigzwart"));
					}
				}

				//gegevens invullen
				for (int i = 0; i <= 180; i = i + MINMINUTEN){
					int row = 2 + (i % 93) / 3;
					int column = (i > 90 ? 3 : 1);
					final int time = i;
					List<Inschrijving> inschrijvingList = afdeling.getInschrijvingen().stream()
							.filter(inschr -> inschr.getTijdslot() != null)
							.filter(inschr -> inschr.getTijdslot().isIncluded(time))
							.collect(Collectors.toList());
					for(Inschrijving inschrijving : inschrijvingList) {
						boolean edited = false;

						if (inschrijving.getDiscipline().isMeisjes()) {
							edited = setCellInschrijvingForAfdeling(styles, sheet.getRow(row), column, i, inschrijving);
						}
						if (inschrijving.getDiscipline().isJongens()) {
							edited = setCellInschrijvingForAfdeling(styles, sheet.getRow(row), column + 5, i, inschrijving);
						}

						if (!edited) {
							logger.error("Inschrijving in " + inschrijving.getRing() + " van "
									+ inschrijving.getAfdeling() + ": jongens/meisjes kan niet bepaald worden en werd dus overgeslagen!");
						}
					}
				}

				//SF informatie
				SimpleDateFormat formatter = new SimpleDateFormat("d/MM/yyyy");
				String sfInfo = "Sportfeest te " + map.getLocatie() + " op " + formatter.format(map.getDatum());
				Row infoRow = sheet.createRow(1);
				infoRow.setHeightInPoints(20);
				for(int i = 0; i <= 3; i++) {
					Cell infoCell = infoRow.createCell(i);
					if(i==0) infoCell.setCellValue(sfInfo);
					infoCell.setCellStyle(styles.get("info"));
				}
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));
				for(int i = 5; i <= 8; i++) {
					Cell infoCell = infoRow.createCell(i);
					if(i==5) infoCell.setCellValue(sfInfo);
					infoCell.setCellStyle(styles.get("info"));
				}
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 5, 8));

				//dikke lijn rondomrond
				CellRangeAddress region = CellRangeAddress.valueOf("A1:D33");
				RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
				RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
				RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
				RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
				region = CellRangeAddress.valueOf("F1:I33");
				RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
				RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
				RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
				RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);

				//kolombreedtes instellen
				int[] columnWidths = {6, 28, 6, 28, 2, 6, 28, 6, 28};
				for (int i = 0; i < 9; i++) {
					sheet.setColumnWidth(i, columnWidths[i]*256);
				}
			}

			logger.info("Schrijven van bestand data/uurschema-afdelingen.xlsx");
			FileOutputStream out = new FileOutputStream("data/uurschema-afdelingen.xlsx");
			wb.write(out);
			out.close();

			try {
				Desktop.getDesktop().open(new File("data/uurschema-afdelingen.xlsx"));
			} catch (Exception e) { logger.warn(e.getMessage()); }

			//********************************************
			//Excel-bestand met werkblad per groep ringen
			//********************************************
			wb = new XSSFWorkbook();
			styles = createStyles(wb);

			//TODO: wat betekenen de gele vakken bij basiswimpelen muziekreeks?

			//tabblad groepen maken
			List<String> ringGroepen = new ArrayList<>();
			for(Discipline discipline : map.getDisciplines().values()) ringGroepen.add(discipline.getRingNaam());
			List<String> sortedRingGroepen = ringGroepen.stream().distinct().sorted().collect(Collectors.toList());

			for(String ringGroep : sortedRingGroepen) {
				XSSFSheet sheet = wb.createSheet(ringGroep);
				PrintSetup printSetup = sheet.getPrintSetup();
				printSetup.setLandscape(true);
				printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);
				sheet.setMargin(Sheet.LeftMargin, 1 / 2.54);
				sheet.setMargin(Sheet.RightMargin, 1 / 2.54);
				sheet.setMargin(Sheet.TopMargin, 1 / 2.54);
				sheet.setMargin(Sheet.BottomMargin, 1 / 2.54);

				List<Ring> sortedRingen = map.getRingen().stream()
						.filter(rng -> ringGroep.equals(rng.getDisciplines().stream().findFirst().get().getRingNaam()))
						.sorted(Comparator.comparing(Ring::getVerkorteNotatie))
						.collect(Collectors.toList());
				int ringGroepDuur = sortedRingen.get(0).getTijdslots().get(0).getDuur();
				//TODO: niet 100% veilig (~sortedRingen leeg, ~verschillende lengtes)

				//creeer eerst de rijen
				int aantalrijen = (int) Math.ceil(
						(3 + (TOTALETIJD / 2f / ringGroepDuur))
								* Math.ceil(sortedRingen.size() / 2f));
				for(int i = 0; i < aantalrijen; i++){
					Row row = sheet.createRow(i);
					row.setHeightInPoints(ROW_HEIGHT);
				}

				for(int i = 0; i < sortedRingen.size(); i++) {
					Ring currentRing = sortedRingen.get(i);
					int tbRowBase = (int) ((i / 2) * (4 + (TOTALETIJD / ringGroepDuur / 2f)));
					int tbColBase = (i % 2 ) * 5;

					//Ringtitel
					Cell titleCell = sheet.getRow(tbRowBase).createCell(tbColBase);
					titleCell.setCellValue(currentRing.toString());
					titleCell.setCellStyle(styles.get("hoofding"));
					sheet.addMergedRegion(new CellRangeAddress(tbRowBase, tbRowBase, tbColBase, tbColBase + 3));

					//Tijden invullen
					SimpleDateFormat df = new SimpleDateFormat("HH:mm");
					Date d = df.parse("08:00");
					Calendar cal = Calendar.getInstance();
					cal.setTime(d);
					int slots = (TOTALETIJD / ringGroepDuur) + 1;
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
							.collect(Collectors.toList());

					//font voor extensie
					XSSFFont fontExtensie= wb.createFont();
					fontExtensie.setItalic(true);
					fontExtensie.setFontHeight(9);

					for(Inschrijving inschrijving : ringInschrijvingen){
						if (inschrijving.getTijdslot() == null) {
							logger.error("Inschrijving in " + inschrijving.getRing() + " van "
									+ inschrijving.getAfdeling() + " heeft geen tijdslot toegewezen!");
						} else {
							String tijd = inschrijving.getTijdslot().getStartTijdFormatted();
							boolean tijdFound = false;
							for(int j = 0; j < aantalrijen; j++) {
								Row row = sheet.getRow(tbRowBase + j);
								Cell cell = row.getCell(tbColBase);
								if(cell != null && cell.getStringCellValue().equals(tijd)) {
									String value = row.getCell(tbColBase + 1).getStringCellValue()
											+ inschrijving.getAfdeling().getNaam()
											+ (inschrijving.getKorps() > 0 ? " " + inschrijving.getKorps() : "");
									XSSFRichTextString rts = new XSSFRichTextString(value);
									if(inschrijving.getDiscipline().getExtensie() != null)
										rts.append("  " + inschrijving.getDiscipline().getExtensie(), fontExtensie);
									row.getCell(tbColBase + 1).setCellValue(rts);
									tijdFound = true;
									break;
								}
								cell = row.getCell(tbColBase + 2);
								if(cell != null && cell.getStringCellValue().equals(tijd)) {
									String value = row.getCell(tbColBase + 3).getStringCellValue()
											+ inschrijving.getAfdeling().getNaam()
											+ (inschrijving.getKorps() > 0 ? " " + inschrijving.getKorps() : "");
									XSSFRichTextString rts = new XSSFRichTextString(value);
									if(inschrijving.getDiscipline().getExtensie() != null)
										rts.append("  " + inschrijving.getDiscipline().getExtensie(), fontExtensie);
									row.getCell(tbColBase + 3).setCellValue(rts);
									tijdFound = true;
									break;
								}
							}
							if(!tijdFound) logger.error("Inschrijving in " + inschrijving.getRing() + " van "
									+ inschrijving.getAfdeling() + ", kon het tijdslot in de ring niet vinden!");

						}
					}

					//voeg bovenaan de ring een eindemarkering in indien groter dan één pagina
					int lastBreakIndex = 0;
					if(sheet.getRowBreaks().length - 1 >= 0) {
						lastBreakIndex = sheet.getRowBreaks()[sheet.getRowBreaks().length - 1];
					}
					int lastRowPageIndex = tbRowBase + (int) Math.ceil(slots / 2f) - lastBreakIndex;
					if(lastRowPageIndex >= 550 / ROW_HEIGHT) {
						sheet.setRowBreak(tbRowBase - 1);
					}

				}

				//kolombreedtes instellen
				double[] columnWidths = {6, 27, 6, 27, 7, 6, 27, 6, 27};
				for (int i = 0; i < 9; i++) {
					sheet.setColumnWidth(i, (int)columnWidths[i]*256);
				}
			}

			logger.info("Schrijven van bestand data/uurschema-ringen.xlsx");
			out = new FileOutputStream("data/uurschema-ringen.xlsx");
			wb.write(out);
			out.close();

			try {
				Desktop.getDesktop().open(new File("data/uurschema-ringen.xlsx"));
			} catch (Exception e) { logger.warn(e.getMessage()); }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fixRingNumbersOrder(Sportfeest map) {
		for(Afdeling afdeling : map.getAfdelingen()) {
			List<Inschrijving> sortedInschrijvingen = afdeling.getInschrijvingen().stream()
					.sorted(Comparator.comparing(Inschrijving::getTijdslot))
					.collect(Collectors.toList());
			HashMap<Discipline, Integer> ringCounter = new HashMap<>();
			for(Inschrijving inschrijving : sortedInschrijvingen) {
				int count = 1;
				if(ringCounter.containsKey(inschrijving.getDiscipline()))
					count += ringCounter.get(inschrijving.getDiscipline());
				if(inschrijving.getKorps()>0) {
					inschrijving.setKorps(count);
					ringCounter.put(inschrijving.getDiscipline(), count);
				}
			}
		}
	}

	private static boolean setCellInschrijvingForAfdeling(Map<String, CellStyle> styles, XSSFRow row, int column, int i, Inschrijving inschrijving) {
		Cell cell;
		if (i == inschrijving.getTijdslot().getStartTijd()) {
			cell = row.getCell(column-1);
			cell.setCellStyle(styles.get("tijdonderleeg"));
			cell = row.getCell(column);
			cell.setCellValue((!cell.getStringCellValue().equals("") ? cell.getStringCellValue() + " " : "")
					+ inschrijving.getDiscipline().getVerkorteNaam()
					+ (inschrijving.getKorps() > 0 ? " " + inschrijving.getKorps() : "")
					+ Optional
					.ofNullable(inschrijving.getRing().getLetter())
					.map(a -> " ring " + a)
					.orElse(""));
			cell.setCellStyle(styles.get("ringonderleeg"));
		} else {
			cell = row.getCell(column-1);
			cell.setCellValue("");
			if (i != inschrijving.getTijdslot().getEindTijd() - MINMINUTEN){
				cell.setCellStyle(styles.get("tijdonderleeg"));
				cell = row.getCell(column);
				cell.setCellStyle(styles.get("ringonderleeg"));
			}
		}
		return cell != null;
	}

	private static Map<String, CellStyle> createStyles(XSSFWorkbook wb){
		Map<String, CellStyle> styles = new HashMap<>();
		CellStyle style;
		Font hoofdingFont = wb.createFont();
		hoofdingFont.setFontHeightInPoints((short)11);
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
		standaardFont10.setFontHeightInPoints((short)10);
		Font standaardFont11 = wb.createFont();
		standaardFont11.setFontHeightInPoints((short)11);

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
