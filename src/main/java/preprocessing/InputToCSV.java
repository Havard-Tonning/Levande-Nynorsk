package preprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.min;


public class InputToCSV {

    public List<String> bokmaalSentences = new ArrayList<>();
    public List<String> nynorskSentences = new ArrayList<>();

    public InputToCSV() {
        runCSV("src/InputBokmaal.txt", true);
        runCSV("src/InputNynorsk.txt", false);
        writeCSV();
    }

    public void runCSV(String filePath, boolean bokmaal) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String fullText = "";
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                fullText += line + "\n";
            }
//            String[] lines = fullText.split(".");

            String[] lines = fullText.split("\\n\\d+");

            for (String singleLine : lines) {
                if (singleLine != null && !singleLine.isEmpty()) {
                    String outputLine = "";
                    for (int i = 0; i < singleLine.length(); i++) {
                        if (!Character.isDigit(singleLine.charAt(i))) {
                            outputLine += singleLine.charAt(i);
                        }
                    }
                    if (bokmaal) {
                        bokmaalSentences.add(outputLine);
                    } else {
                        nynorskSentences.add(outputLine);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public void writeCSV() {
        try (FileWriter writer = new FileWriter("src/pairs.csv", true)) {

        int min = min(bokmaalSentences.size(), nynorskSentences.size());
        for (int i = 0; i < min; i++) {
            String bokmaal = escapeCSV(bokmaalSentences.get(i));
            String nynorsk = escapeCSV(nynorskSentences.get(i));

            writer.append(bokmaal + "," + nynorsk +"\n");
            System.out.println("Wrote line " + i);

        }
        } catch (IOException e) {
        throw new RuntimeException(e);
    }
    }

    public String escapeCSV(String sentence){
        if(sentence.contains(",") || sentence.contains("\n")){
            return "\"" + sentence.replace("\"", "\"\"") + "\"";
        }
        return sentence;
    }
}

