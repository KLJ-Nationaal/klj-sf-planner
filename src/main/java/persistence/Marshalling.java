package persistence;

import domain.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

public class Marshalling {
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
        try {
            JAXBContext context = JAXBContext.newInstance(Sportfeest.class);
            Unmarshaller um = context.createUnmarshaller();
            Sportfeest map = (Sportfeest) um.unmarshal(new FileReader(filename));
            int i = 0;
            for (Afdeling afdeling : map.getAfdelingen()){
                for(Inschrijving inschr : afdeling.getInschrijvingen()){
                    inschr.setId(i);
                    inschr.setAfdeling(afdeling);
                    map.addInschrijving(inschr);
                    i++;
                }
            }
            return map;
        } catch (JAXBException e) {
            System.out.println("ERROR: INLEZEN VAN DE XML");
            e.printStackTrace();

        } catch (FileNotFoundException e) {
            System.out.println("ERROR: FILE NOT FOUND");
        }
        return new Sportfeest();
    }
}
