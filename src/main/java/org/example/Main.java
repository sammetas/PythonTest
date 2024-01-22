package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        //5 . find the word: "F1 - Gas 1 ready"

        String filePath = "/Users/samm/Downloads/prod001.log";
        String searchTerm = "F1 - Gas 1 ready";
        System.out.println(searchInFile(filePath, searchTerm));

        //4. Extract Nom
        List<Output4> output4List = findNomAndAVGAndReturn(filePath);
        output4List.forEach(output -> System.out.println(output.toString()));
        //3. Process step times
        System.out.println(findProcessStepTims(filePath));

        //2. numbers with step name:
        System.out.println(writeNumbers(filePath));

        //1. find duration
        System.out.println(calculateLogDuration(filePath));
    }

    private static long calculateLogDuration(String filePath) throws IOException, ParseException {
        Path path = Paths.get(filePath);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        String firstLine = Files.lines(path).findFirst().orElseThrow();
        String lastLine = Files.lines(path).reduce((first, second) -> second).orElseThrow();

        Date startTime = dateFormat.parse(firstLine.substring(0, 8));
        Date endTime = dateFormat.parse(lastLine.substring(0, 8));

        long durationInMillis = endTime.getTime() - startTime.getTime();
        return durationInMillis / 1000; // Convert milliseconds to seconds
    }

    private static Map<String, String> writeNumbers(String filePath) throws IOException {
        List<String> output = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));
        //16: 11 567
        for (String line : lines) {
            if (line.contains("Start Step No")) {
                String lineParts[] = line.split("\\s+");
                if (lineParts.length == 17) {
                    String line1 = "Step No. " + lineParts[5] + " " + lineParts[6] + " " + lineParts[7];
                    String line2 = lineParts[11].trim();
                    map.put(line1, line2);
                }
            }
        }

        return map;
    }

    private static int findProcessStepTims(String filePath) throws IOException {
        try (var stream = Files.lines(Path.of(filePath))) {
            return stream.filter(line -> line.contains("Process Step Time")).map(line -> {
                String[] linePart = line.split("\\s+");
                return Integer.parseInt(linePart[7].trim());
            }).reduce(0, (number, acc) -> number + acc);
        }

    }

    private static List<Output4> findNomAndAVGAndReturn(String filePath) throws IOException {
        List<Output4> output4List = new ArrayList<>();
        try (var stream = Files.lines(Path.of(filePath))) {

            List<String> filteredLines = stream.filter(s -> s.contains("Nom.:") && s.contains("Avg.:")).collect(Collectors.toList());

            for (String line : filteredLines) {
                String[] lineParts = line.split("\\s+");

                if (lineParts.length == 10 && lineParts[6].equals("Nom.:") && lineParts[8].equals("Avg.:")) {
                    Output4 output4 = new Output4(Double.parseDouble(lineParts[7].trim()), Double.parseDouble(lineParts[9].trim()));
                    output4List.add(output4);
                }
            }

        }
        return output4List;
    }

    private static int searchInFile(String filePath, String searchTerm) throws IOException {

        try (var stream = Files.lines(Path.of(filePath))) {

            return (int) stream.filter(s -> s.contains(searchTerm)).count();
        }
    }
}

class Output4 {
    double nom;
    double avg;

    public Output4(double nom, double avg) {
        this.nom = nom;
        this.avg = avg;
    }

    public double getNom() {
        return nom;
    }

    public double getAvg() {
        return avg;
    }

    @Override
    public String toString() {
        return "nom: " + this.nom + ", act: " + this.avg;
    }
}