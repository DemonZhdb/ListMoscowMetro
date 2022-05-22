import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import model.Line;
import model.Metro;
import model.Station;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Parser {
    private Metro metro = new Metro(); // Создаём объект метро
    public Map<String, String> linesMetro = new HashMap();
    public JSONObject stationObj = new JSONObject();
    public JSONArray connectionsObj = new JSONArray();

    public void parseLink(String URL) throws IOException {

        Document doc = Jsoup.connect(URL).maxBodySize(0).get();

        Elements tables = doc.select("div.mw-parser-output > table.standard");
        // выборка на странице сайта трех таблиц со станциями метро  для парсинга
        tables.forEach(table -> {

            String tableHead = table.select("th > a[href] ").attr("title");

            Elements rowsStation = table.select("tbody > tr");

            rowsStation.forEach(rs -> {
                Elements columns = rs.select("td");

                if (!(rs.getElementsByTag("th").size() > 0)) {

                    if (!(tableHead.equals("Московское центральное кольцо"))) {
                        parseMainTable(columns);
                    } else {
                        parseMCKTable(columns);
                    }
                }
            });
        });

    }

    public void fileWrite(String fileName) {
        JsonObject objMetro = new JsonObject();
        //Формируем JSON-объект метро для записи в файл
        objMetro.put("stations", getStations());
        objMetro.put("connections", metro.getConnections());
        objMetro.put("lines", getLines());

        try {
            FileWriter file = new FileWriter(fileName);
            file.write(Jsoner.prettyPrint(objMetro.toJson()));
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseFile(String fileName) {
        JSONParser jsonParser = new JSONParser();
        // Парсим сохраненный ранее JSON-файл
        try (FileReader fileReader = new FileReader((fileName))) {
            Object jsonObj = jsonParser.parse(fileReader);
            JSONObject jsonObject = (JSONObject) jsonObj;
            JSONArray arrayLines = (JSONArray) jsonObject.get("lines");
            arrayLines.forEach(l -> {
                JSONObject line = (JSONObject) l;
                linesMetro.put((String) line.get("number"), (String) line.get("name"));
            });
            stationObj = (JSONObject) jsonObject.get("stations");
            connectionsObj = (JSONArray) jsonObject.get("connections");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseLine(String lineName, String lineNumber) {
        Line line = new Line(lineNumber, lineName);
        // Парсим и добавляем линию в объект метро
        if (!metro.getLines().contains(line)) {
            metro.addLine(line);
        }

    }

    private void parseStation(String stationName, String lineName) {
        Line line = metro.getLineByName(lineName);
        Station station = new Station(stationName, line);
        // Парсим и добавляем станцию в линию
        line.addStation(station);
        // Добавляем станцию в метро
        metro.addStation(station);
    }

    private void parseConnection(List<String> connectionStations, String stationName,
                                 List<String> connectionLines, String lineNumber) {
        TreeMap<String, String> connectingMap = new TreeMap<>();
        connectingMap.put(lineNumber, stationName);
        for (int i = 0; i < connectionLines.size(); i++) {
            if (connectionLines.get(i).matches("[\\dDAА]+")) {
                connectingMap.put(connectionLines.get(i), connectionStations.get(i));
            }
        }
        // Добавляем переходы в метро
        metro.addConnections(connectingMap);

    }

    private SortedMap<String, List<String>> getStations() {
        SortedMap<String, List<String>> linesStation = new TreeMap<>();
        metro.getLines().forEach(line -> {
            List<String> stations = new ArrayList<>();
            line.getStations().forEach(station -> stations.add(station.getName()));
            linesStation.put(line.getNumber(), stations);
        });
        return linesStation;
    }

    private JsonArray getLines() {
        JsonArray linesArray = new JsonArray();
        metro.getLines().forEach(line -> {
            JsonObject objLine = new JsonObject();
            objLine.put("number", line.getNumber());
            objLine.put("name", line.getName());
            linesArray.add(objLine);

        });
        return linesArray;
    }

    private void parseMainTable(Elements col) {
        // Парсинг основной таблицы со станциями метро и таблицы монорельса
        String lineName = col.get(0).child(1).attr("title");
        String lineNumber = col.get(0).children().eachText().get(0);
        parseLine(lineName, lineNumber);
        String stationName = col.get(1).text();
        parseStation(stationName, lineName);
        List<String> connectionStations = col.get(3).children().eachAttr("title");
        if (!(connectionStations.size() == 0)) {
            for (int i = 0; i < connectionStations.size(); i++) {
                String text = connectionStations.get(i);
                connectionStations.set(i, text.substring(text.indexOf('«') + 1, text.indexOf('»')));
            }
            List<String> connectionLines = col.get(3).children().eachText();
            parseConnection(connectionStations, stationName,
                    connectionLines, lineNumber);
        }
    }

    private void parseMCKTable(Elements col) {
        //  Парсинг таблицы станций  МЦК
        String lineName = "Московское центральное кольцо";
        String lineNumber = "14";
        parseLine(lineName, lineNumber);
        String stationName = col.get(1).text();
        parseStation(stationName, lineName);
        List<String> connectionStations = new ArrayList<>();
        List<String> connectionLines = new ArrayList<>();
        Elements connectionElements = col.get(4).select("span.nowrap ");
        if (!(connectionElements.size() == 0)) {
            for (Element element : connectionElements) {
                if (!(element.select("span.sortkey").size() == 0)) {
                    connectionLines.add(element.select("span.sortkey").text());
                } else {
                    connectionLines.add(getLineNumber(element.select("a").attr("title")));
                }
                connectionStations.add(element.select("a").text());
            }
            parseConnection(connectionStations, stationName,
                    connectionLines, lineNumber);
        }

    }

    private String getLineNumber(String lineName) {
        List<Line> lines = metro.getLines();
        for (Line line : lines) {
            if (lineName.equalsIgnoreCase(line.getName())) {
                return line.getNumber();
            }
        }
        return "00";
    }

}
