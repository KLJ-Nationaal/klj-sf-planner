package persistence;

import com.sun.istack.internal.logging.Logger;
import domain.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.event.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Marshalling {
    final static Logger logger = Logger.getLogger(Marshalling.class);

    public static void marshall(Sportfeest map){
        marshall(map,"output.xml");
    }

    public static void marshall(Sportfeest map, String filename){
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

                String ringNaam = disciplines.get(discipline).ringNaam + " " + ringIndex;
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
}
