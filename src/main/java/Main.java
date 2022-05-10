import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    private final static String FOLDER = "out/";
    private final static String FILE_NAME = "mapMetroMSK.json";
    private final static String URL = "https://skillbox-java.github.io/";

    public static void main(String[] args) throws IOException {

        JsonObject objMetro = new JsonObject();
        Document doc = Jsoup.connect(URL).maxBodySize(0).get();
        System.out.println(" Московский метрополитен ");
        Elements lines = doc.select("span.js-metro-line");

        JsonArray arLine = new JsonArray();
        lines.forEach(l -> {
            JsonObject objLine = new JsonObject();
            objLine.put("number", l.attr("data-line"));
            objLine.put("name", l.text());

            arLine.add(objLine);

        });

        Elements linesSt = doc.select("div.js-metro-stations");

        SortedMap<String, List<String>> objectsLine = new TreeMap<>();
        JsonArray connectionAr = new JsonArray();
        linesSt.forEach(l -> {

            List<String> stationList = new ArrayList();

            l.select("p.single-station").forEach(s -> {

                if (!s.select("span.t-icon-metroln").isEmpty()) {
                    JsonArray connectStationAr = new JsonArray();
                    JsonObject stationFromConnect = new JsonObject();
                    stationFromConnect.put("line", l.attr("data-line"));

                    stationFromConnect.put("station", s.text().substring(s.text().indexOf(" ")));

                    connectStationAr.add(stationFromConnect);


                    s.select("span.t-icon-metroln").forEach(c -> {

                        JsonObject stationToConnect = new JsonObject();
                        stationToConnect.put("line", c.className().substring(18));

                        stationToConnect.put("station", c.attr("title").substring(20,
                                c.attr("title").indexOf('»')));

                        connectStationAr.add(stationToConnect);

                    });
                    connectionAr.add(connectStationAr);

                }

                stationList.add(s.text().substring(1 + s.text().indexOf(" ")));

            });
            objectsLine.put(l.attr("data-line"), stationList);

        });

        objMetro.put("stations", objectsLine);
        objMetro.put("connections", connectionAr);
        objMetro.put("lines", arLine);

        try {
            FileWriter file = new FileWriter(FOLDER + FILE_NAME);
            file.write(Jsoner.prettyPrint(objMetro.toJson()));
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONParser jsonParser = new JSONParser();
        try (FileReader fileReader = new FileReader((FOLDER + FILE_NAME))) {

            Object jsonObj = jsonParser.parse(fileReader);
            JSONObject jsonObject = (JSONObject) jsonObj;
            Map<String, String> linesMetro = new HashMap();
            System.out.println("Количество станций метро по линиям: ");
            JSONArray linesArray = (JSONArray) jsonObject.get("lines");
            linesArray.forEach(l -> {
                JSONObject line = (JSONObject) l;
                linesMetro.put((String) line.get("number"), (String) line.get("name"));
            });

            JSONObject stationObj = (JSONObject) jsonObject.get("stations");
            stationObj.keySet().stream()
                    .sorted()
                    .forEach(lineNumObj -> {
                        String lineNumber = (String) lineNumObj;
                        JSONArray stArray = (JSONArray) stationObj.get(lineNumObj);
                        System.out.println(linesMetro.get(lineNumber) + "  - " + stArray.size());
                    });
            JSONArray connectionsObj = (JSONArray) jsonObject.get("connections");
            System.out.println();
            System.out.println("Количество переходов в метро: " + connectionsObj.size());
            /* расчет всех переходов между внутри станций
             int sum =0;
             for (int i =0; i<connectionsObj.size();i++) {
               JSONArray connection = (JSONArray) connectionsObj.get(i);
               sum +=connection.size()-1;
               }
             */

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}


