package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        //5 . find the word: "F1 - Gas 1 ready"

        String filePath = "/Users/samm/Downloads/prod001.log";
        String searchTerm = "F1 - Gas 1 ready";
        System.out.println(searchInFile(filePath, searchTerm));

        //4. Extract Nom
        Output4 output = findNomAndAVGAndReturn(filePath, "Start Step No. 4");
        System.out.println(output);
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
        int sum = 0;
        boolean isIt2Or3 = false;
        List<String> lines = Files.readAllLines(Path.of(filePath));
        for (String line : lines) {
            if (line.contains("Start Step No. 2") || line.contains("Start Step No. 3")) {
                isIt2Or3 = true;
            } else if (isIt2Or3 && line.contains("Process Step Time")) {
                String[] linePart = line.split("\\s+");
                sum += Integer.parseInt(linePart[7].trim());
                isIt2Or3 = false;

            }
        }
        return sum;

    }

    private static Output4 findNomAndAVGAndReturn(String filePath, String stepNo) throws IOException {


        boolean isStep4Satisfied = false;
        Output4 output4 = new Output4();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        for (String line : lines) {
            if (line.contains(stepNo)) {
                isStep4Satisfied = true;
            } else if (isStep4Satisfied && line.contains("XTAL Thickness")) {
                String[] lineParts = line.split("\\s+");
                if (lineParts.length == 10 && lineParts[6].equals("Nom.:") && lineParts[8].equals("Act.:")) {
                    output4.setNom(Double.parseDouble(lineParts[7].trim()));
                    output4.setAvg(Double.parseDouble(lineParts[9].trim()));

                }
            }
        }


        return output4;
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

    public Output4() {
    }

    public Output4(double nom, double avg) {
        this.nom = nom;
        this.avg = avg;
    }

    public double getNom() {
        return nom;
    }

    public void setNom(double nom) {
        this.nom = nom;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    @Override
    public String toString() {
        return "nom: " + this.nom + ", act: " + this.avg;
    }
}