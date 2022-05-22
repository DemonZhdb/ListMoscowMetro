import model.Metro;
import org.json.simple.JSONArray;

import java.io.IOException;

public class Main {
    private final static String FILE = "out/wikiMetroMSK.json";
    private final static String URL = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";
    public static Metro metro = new Metro();

    public static void main(String[] args) throws IOException {
        Parser parser = new Parser();
        parser.parseLink(URL);
        parser.fileWrite(FILE);
        parser.parseFile(FILE);
        printResultsParse(parser);
    }

    public static void printResultsParse(Parser parser) {
        // Печать результатов парсинга соформированного JSON-файла
        System.out.println("Сайт wikipedia со станциями Московского метрополитена \n");
        System.out.println("Количество станций метро по линиям: ");
        parser.stationObj.keySet().stream()
                .sorted()
                .forEach(lineNumObj -> {
                    String lineNumber = (String) lineNumObj;
                    JSONArray stArray = (JSONArray) parser.stationObj.get(lineNumObj);
                    System.out.println(parser.linesMetro.get(lineNumber) + "  - " + stArray.size());
                });
        System.out.println("\nКоличество переходов в метро: " + parser.connectionsObj.size());
    }
}


