package persistence;

import com.sun.istack.internal.logging.Logger;
import domain.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.event.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Marshalling {
    final static Logger logger = Logger.getLogger(Marshalling.class);

    public static Sportfeest unMarshall(String filename) {
        Sportfeest sf = new Sportfeest();
        HashMap<String, Discipline> disciplines = new HashMap<>();

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
                int minuten = 12; //standaard 12 minuten (wordt normaal altijd overschreven)
                if(row.getCell(3) != null && row.getCell(3).getCellType() == CellType.NUMERIC) {
                    minuten = (int) row.getCell(3).getNumericCellValue();
                }

                if(reeks.length() > 12) { //reeksen hebben altijd een naam met meer dan 12 karakters
                    Discipline discipline = new Discipline();
                    discipline.naam = reeks;
                    discipline.ringNaam = ringNaam;
                    discipline.duur = minuten;
                    disciplines.put(reeks, discipline);
                } else {
                    if(row.getRowNum() > 10) break; //dit is waarschijnlijk het einde van de lijst
                }
            }

            //lees nu de inschrijvingen in
            sheet = workbook.getSheet("Ringverdeling");
            rowIt = sheet.iterator();
            int aantalInschrijvingen = 0;

            rowIt.next(); //eerste rij overslaan
            while(rowIt.hasNext()) {
                Row row = rowIt.next();

                String afdelingsNaam = row.getCell(0).getStringCellValue();
                String discipline = row.getCell(1).getStringCellValue();
                int aantal = 1;
                if(row.getCell(2) != null && row.getCell(2).getCellType() == CellType.NUMERIC) {
                    aantal = (int) row.getCell(2).getNumericCellValue();
                } else {
                    logger.severe("Kon aantal inschrijvingen niet lezen voor afdeling " + afdelingsNaam +
                            ", discipline " + discipline);
                }
                String ringIndex = "A";
                if(row.getCell(3) != null) {
                    ringIndex = row.getCell(3).getStringCellValue();
                } else {
                    logger.severe("Inschrijving heeft geen ring toegewijzing gekregen voor afdeling " +
                            afdelingsNaam + ", discipline " + discipline);
                }

                String ringNaam = disciplines.get(discipline).ringNaam + " Ring " + ringIndex;
                Ring ring = sf.getRingen().stream()
                        .filter(rng -> ringNaam.equals(rng.naam))
                        .findAny()
                        .orElse(new Ring(ringNaam, sf.getRingen().size()+1));
                Afdeling afdeling = sf.getAfdelingen().stream()
                        .filter(afd -> afdelingsNaam.equals(afd.getNaam()))
                        .findAny()
                        .orElse(new Afdeling(afdelingsNaam));
                //tijdslots voor ring maken als ze nog niet bestaan
                if(ring.getTijdslots().size() == 0) {
                    for (int i = 0; i < 151; i = i + disciplines.get(discipline).duur) {  //TODO: property van maken
                        Tijdslot tijdslot = new Tijdslot();
                        tijdslot.setStartTijd(i);
                        tijdslot.setDuur(disciplines.get(discipline).duur);
                        tijdslot.setRing(ring);
                        ring.getTijdslots().add(tijdslot);
                    }
                }
                for(int i = 0; i < aantal; i++) {
                    Inschrijving inschrijving = new Inschrijving();
                    inschrijving.setAfdeling(afdeling);
                    inschrijving.setRing(ring);
                    inschrijving.setId(aantalInschrijvingen);
                    inschrijving.setDiscipline(disciplines.get(discipline));
                    afdeling.getInschrijvingen().add(inschrijving);
                    sf.getInschrijvingen().add(inschrijving);
                    aantalInschrijvingen++;
                }
                sf.getRingen().add(ring);
                sf.getAfdelingen().add(afdeling);
            }

            workbook.close();
            fis.close();

            for(Ring ring : sf.getRingen()) sf.getTijdslots().addAll(ring.getTijdslots());

            return sf;
        } catch (IOException ioe) {
            logger.severe("KON XLSX NIET NORMAAL INLEZEN");
            ioe.printStackTrace();

        }
        return new Sportfeest();
    }

    public static void marshallXml(Sportfeest map, String filename){
        try {
            JAXBContext context = JAXBContext.newInstance(Sportfeest.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //m.marshal(map, System.out);

            Writer w = null;
            try {
                w = new FileWriter(filename);
                m.marshal(map, w);
            } finally {
                try {
                    w.close();
                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void marshall(Sportfeest map, String filename){
        try {
            //***************************************
            //Excel-bestand met werkblad per afdeling
            //***************************************
            XSSFWorkbook wb = new XSSFWorkbook();
            Map<String, CellStyle> styles = createStyles(wb);

            for(Afdeling afdeling : map.getAfdelingen()) {
                XSSFSheet sheet = wb.createSheet(afdeling.getNaam());
                PrintSetup printSetup = sheet.getPrintSetup();
                printSetup.setLandscape(true);
                sheet.setFitToPage(true);
                sheet.setHorizontallyCenter(true);

                //Afdelingstitel
                Row titleRow = sheet.createRow(0);
                titleRow.setHeightInPoints(20);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Afdeling: " + afdeling.getNaam() + " meisjes");
                titleCell.setCellStyle(styles.get("hoofding"));
                sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$D$1"));
                titleCell = titleRow.createCell(5);
                titleCell.setCellValue(afdeling.getNaam() + " jongens");
                titleCell.setCellStyle(styles.get("hoofding"));
                sheet.addMergedRegion(CellRangeAddress.valueOf("$F$1:$I$1"));

                //SF informatie
                String sfInfo = "Sportfeest te " + map.getLocatie() + " op " + map.getDatum();
                Row infoRow = sheet.createRow(1);
                infoRow.setHeightInPoints(20);
                Cell infoCell = infoRow.createCell(0);
                infoCell.setCellValue(sfInfo);
                infoCell.setCellStyle(styles.get("info"));
                sheet.addMergedRegion(CellRangeAddress.valueOf("$A$2:$D$2"));
                infoCell = infoRow.createCell(5);
                infoCell.setCellValue(sfInfo);
                infoCell.setCellStyle(styles.get("info"));
                sheet.addMergedRegion(CellRangeAddress.valueOf("$F$2:$I$2"));

                //de rest opmaken
                for (int i = 2; i < 33; i++) {
                    Row row = sheet.createRow(i);
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                    Date d = df.parse("08:00");
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(d);
                    cal1.add(Calendar.MINUTE, 3 * (i-2));
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(d);
                    cal2.add(Calendar.MINUTE, 3 * (i-2) + 93);
                    for (int j = 0; j < 9; j++) {
                        Cell cell = row.createCell(j);
                        if(j == 0 || j == 5) cell.setCellValue(df.format(cal1.getTime()));
                        if(j == 2 || j == 7) cell.setCellValue(df.format(cal2.getTime()));
                        if(j == 0 || j == 5 || j == 2 || j == 7) cell.setCellStyle(styles.get("tijd"));
                        if(j == 1 || j == 6 || j == 3 || j == 8) cell.setCellStyle(styles.get("ring"));
                        if(i == 32 && (j == 2 || j == 7 || j == 3 || j == 8)) cell.setCellStyle(styles.get("volledigzwart"));
                    }
                }

                //gegevens invullen
                for(Inschrijving inschrijving : map.getInschrijvingen()){
                    if(inschrijving.getAfdeling() == afdeling) {
                        if (inschrijving.getTijdslot() == null) {
                            logger.severe("Inschrijving in " + inschrijving.getRing() + " van "
                                    + inschrijving.getAfdeling() + " heeft geen tijdslot toegewezen!");
                        } else {
                            int row = 2 + (inschrijving.getTijdslot().getStartTijd() % 93) / 3;
                            int column = (inschrijving.getTijdslot().getStartTijd() > 90 ? 3 : 1);
                            String cell = sheet.getRow(row).getCell(column).getStringCellValue()
                                    + inschrijving.getRing().getVerkorteNotatie();
                            if (inschrijving.getDiscipline().isMeisjes())
                                sheet.getRow(row).getCell(column).setCellValue(cell);
                            if (inschrijving.getDiscipline().isJongens())
                                sheet.getRow(row).getCell(column + 5).setCellValue(cell);
                            if (!inschrijving.getDiscipline().isJongens() && !inschrijving.getDiscipline().isMeisjes()) {
                                logger.severe("Inschrijving in " + inschrijving.getRing() + " van "
                                        + inschrijving.getAfdeling() + ": jongens/meisjes kan niet bepaald worden en werd dus overgeslagen!");
                            }
                        }
                    }
                }

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

            FileOutputStream out = new FileOutputStream("data/uurschema-afdelingen.xlsx");
            wb.write(out);
            out.close();

            try {
                Desktop.getDesktop().open(new File("data/uurschema-afdelingen.xlsx"));
            } catch (Exception e) { logger.finer(e.getMessage()); }

            //***********************************
            //Excel-bestand met werkblad per ring
            //***********************************
            wb = new XSSFWorkbook();

            for(Ring ring : map.getRingen()) {
                
            }

            out = new FileOutputStream("data/uurschema-ringen.xlsx");
            wb.write(out);
            out.close();

            try {
                Desktop.getDesktop().open(new File("data/uurschema-ringen.xlsx"));
            } catch (Exception e) { logger.finer(e.getMessage()); }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        Font standaardFont = wb.createFont();
        standaardFont.setFontHeightInPoints((short)10);

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
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(standaardFont);
        styles.put("tijd", style);

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
        style.setFont(standaardFont);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        styles.put("ring", style);

        style = wb.createCellStyle();
        Font infoFont = wb.createFont();
        infoFont.setItalic(true);
        style.setFont(infoFont);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("info", style);

        return styles;
    }
}
