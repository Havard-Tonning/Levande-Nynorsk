package preprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.min;

public class Train {
    public static ArrayList<String[]> ReadCSV(String filePath) {
        ArrayList<String[]> pairs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = readCSVLine(reader)) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split on comma, unless that comma is escaped by quotes


                String[] pair = new String[2];
                pair[0] = cleanField(row[0]);
                pair[1] = cleanField(row[1]);
                pairs.add(pair);

            }
            return pairs;

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // To read and remove newlines within quotes
    private static String readCSVLine(BufferedReader reader) throws IOException {
        StringBuilder line = new StringBuilder();
        String part;
        boolean inQuotes = false;

        while ((part = reader.readLine()) != null) {
            if (line.length() > 0) {
                line.append("\n");
            }
            line.append(part);

            // Count quotes to determine if it's inside a quoted field
            for (char c : part.toCharArray()) {
                if (c == '"') {
                    inQuotes = !inQuotes;
                }
            }

            // If it's not within quotes, all is good, and it gets returned and written to the array
            if (!inQuotes) {
                return line.toString();
            }
        }

        // Return what we have if we hit EOF
        return !line.isEmpty() ? line.toString() : null;
    }

    /* Helper method to remove quotes from fields
        If it starts and ends with quotes, they are removed
        Escaped quotes get unescaped
     */
    private static String cleanField(String field) {
        if (field == null) return field;
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
            field = field.replace("\"\"", "\"");
        }
        return field;
    }

    // The news articles are formatted as perfect TSV files, so no need for cleaning
    public static ArrayList<String[]> ReadTSV(String filePath){
        ArrayList<String[]> pairs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = readCSVLine(reader)) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Splitting on tabs. The TSV has three values. We ignore the first one, which is metadata

                String[] row = line.split("\t");
                if(row.length >= 3) {
                    String[] pair = new String[2];
                    pair[0] = row[1];
                    pair[1] = row[2];
                    pairs.add(pair);
                }
            }
            return pairs;


            } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

        public static void generateTranslation(ArrayList<String[]> couples) {
        /* The outerMap contains a word from the bokmål, plus a hashmap containing the words that the nynorsk side that it has appeared together with,
        as well as the number of connections it has with that specific word.
        The appearance map contains all words from the bokmål side, and how many times they have appeared. This is used to calculate the frequency
         */

        HashMap<String, HashMap<String, Integer>> outerMap = new HashMap<>();
        HashMap<String, Integer> appearanceMap = new HashMap<String, Integer>();

        // Words that appear frequently and mess up the translation
        ArrayList<String> bannedWords = new ArrayList<String>();
        bannedWords.add("og");
        bannedWords.add("til");
        bannedWords.add("å");
        bannedWords.add("han");
        bannedWords.add("ho");
        bannedWords.add("det");
        bannedWords.add("som");
        bannedWords.add("i");
        bannedWords.add("med");
        bannedWords.add("har");
        bannedWords.add("mot");
        bannedWords.add("skal");
        bannedWords.add("du");
        bannedWords.add("den");
        bannedWords.add("for");
        bannedWords.add("på");
        bannedWords.add("meg");
        bannedWords.add("gud");
        bannedWords.add("ikkje");
        bannedWords.add("dei");
        bannedWords.add("deg");
        bannedWords.add("de");
        bannedWords.add("eg");
        bannedWords.add("av");
        bannedWords.add("men");
        bannedWords.add("var");
        bannedWords.add("så");
        bannedWords.add("frå");
        bannedWords.add("når");
        bannedWords.add("seg");
        bannedWords.add("er");
        bannedWords.add("må");
        bannedWords.add("då");
        bannedWords.add("ein");
        bannedWords.add("sa");

        String textHolder = "";
        String[] sentenceA = {};
        String[] sentenceB = {};

        // Process all couples in the ArrayList
        for (int i = 0; i < couples.size(); i++) {
            String[] couple = couples.get(i);

            // The input, where each sentence is an item in the array, gets converted to where each word is an item in the array for easier iteration
            if (couple[0] != null) {
                textHolder = couple[0].toLowerCase().replaceAll("[^a-zA-Zæøåòèê\\s-]", "");
                sentenceA = textHolder.split("[ \n]");
            }

            if (couple[1] != null) {
                textHolder = couple[1].toLowerCase().replaceAll("[^a-zA-Zæøåòèê\\s-]", "");
                sentenceB = textHolder.split("[ \n]");
            }

            // Iterate though the elements of the "left hand" sentence
            for (int j = 0; j < sentenceA.length; j++) {
                // Check to prevent common words to be checked unnecessarily
                if (bannedWords.contains(sentenceA[j])) {
                    continue;
                }

                // Arraylist to prevent a word from being added as a match twice, if it appears twice in the right hand side sentence
                ArrayList<String> addedWords = new ArrayList<String>();

                // If the word already exists in the map, increase the number of occurrences it has, if not, create a mapping
                if (appearanceMap.containsKey(sentenceA[j])) {
                    Integer counter = appearanceMap.get(sentenceA[j]);
                    counter++;
                    appearanceMap.put(sentenceA[j], counter);
                } else {
                    appearanceMap.put(sentenceA[j], 1);
                }

                // Iterate through the right hand sentence
                for (int k = 0; k < sentenceB.length; k++) {
                    if (bannedWords.contains(sentenceB[k])) {
                        continue;
                    }
                    // If the aWord does not already exist as a key in the outer map, create a mapping with aWord, bWord and 1
                    if (!outerMap.containsKey(sentenceA[j]) && !addedWords.contains(sentenceB[k])) {
                        HashMap<String, Integer> innerMap = new HashMap<>();
                        innerMap.put(sentenceB[k], 1);
                        outerMap.put(sentenceA[j], innerMap);
                        addedWords.add(sentenceB[k]);
                    }

                    // If the mapping already exists, increment the counter
                    else if (outerMap.get(sentenceA[j]).containsKey(sentenceB[k]) && !addedWords.contains(sentenceB[k])) {
                        Integer counter = (Integer) outerMap.get(sentenceA[j]).get(sentenceB[k]);
                        counter++;
                        outerMap.get(sentenceA[j]).put(sentenceB[k], counter);
                        addedWords.add(sentenceB[k]);
                    }

                    // If the key aWord exists, but not the mapping, create mapping aWord, bWord, 1
                    else if (!addedWords.contains(sentenceB[k])) {
                        outerMap.get(sentenceA[j]).put(sentenceB[k], 1);
                        addedWords.add(sentenceB[k]);
                    }
                }
            }
        }

        SaveTranslation(outerMap, appearanceMap);
    }

    private static void SaveTranslation(HashMap<String, HashMap<String, Integer>> outerMap, HashMap<String, Integer> appearanceMap) {
        AtomicInteger lineCounter = new AtomicInteger();
        // Creating an arraylist of arrays. It will have the columns aWord, translation and probability
        ArrayList<String[]> translatedWords = new ArrayList<String[]>();

        try (FileWriter writer = new FileWriter("src/main/java/preprocessing/translation.csv", false)) {

            outerMap.forEach((aWord, matches) -> {
                // Since lambda functions need effectively final data types, we get around this by using atomic integers and single element strings.
                final AtomicInteger[] maxCount = {new AtomicInteger(0)};
                final String[] bestTranslation = {""};

                matches.forEach((bWord, occurs) -> {
                    if (occurs > maxCount[0].get()) {
                        maxCount[0].set(occurs);
                        bestTranslation[0] = bWord;
                    }

                });

                if (bestTranslation[0] != "" && appearanceMap.get(aWord) != null) {
                    /*
                    If the left word appears more than once in the text, and the co-occurrence probability is more than 0,5
                    Also prunes out words that are the same in both languages
                     */
                    double probability = (double) outerMap.get(aWord).get(bestTranslation[0]) / appearanceMap.get(aWord);
                    if (appearanceMap.get(aWord) > 4 && probability > 0.5 && !Objects.equals(aWord, bestTranslation[0])) {
                        try {
                            writer.append(aWord + "," + bestTranslation[0] + "," + probability + "\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println(lineCounter + " Best translation for " + aWord + " is " + bestTranslation[0] + " with probability " + probability);
                        lineCounter.getAndIncrement();
                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AppendBannedWords();
    }

    public static void AppendBannedWords(){
        try (FileWriter writer = new FileWriter("src/main/java/preprocessing/translation.csv", true)) {
            String[] aWords = {"jeg", "også", "hun", "ham", "ikke", "de", "dere", "fra", "da", "en", "et", "hvor", "noen", "man", "dem"};
            String[] bWords = {"eg", "òg", "ho", "han", "ikkje", "dei", "dykk", "frå", "då", "ein", "eit", "kor", "nokon", "ein", "dei"};

            for(int i = 0; i < aWords.length; i++){
                writer.append(aWords[i] + "," + bWords[i] + ",1\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}