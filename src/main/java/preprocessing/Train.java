package preprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Train {

    // The news articles are formatted as perfect TSV files, so no need for cleaning
    public static ArrayList<String[]> ReadTSV(String filePath){
        ArrayList<String[]> pairs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
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

    public static void GenerateTranslation(ArrayList<String[]> couples) {
        /* The outerMap contains a word from the bokmål, plus a hashmap containing the words that the nynorsk side that it has appeared together with,
        as well as the number of connections it has with that specific word.
        The appearance map contains all words from the bokmål side, and how many times they have appeared. This is used to calculate the frequency
         */

        HashMap<String, HashMap<String, Integer>> outerMap = new HashMap<>();
        HashMap<String, Integer> appearanceMap = new HashMap<String, Integer>();

        // Words that appear frequently and mess up the translation
        Set<String> bannedWords = Set.of(
                "og", "til", "å", "han", "ho", "det", "som", "i", "med", "har",
                "mot", "skal", "du", "den", "for", "på", "meg", "gud", "ikkje",
                "dei", "deg", "de", "eg", "av", "men", "var", "så", "frå",
                "når", "seg", "er", "må", "då", "ein", "sa", "vart", "ei",
                "om", "blir", "at", "kjem", "eit", "inn", "norsk", "noreg",
                "noregs", "norge", "fôr", "kan", "vil", "fortsetter", "forsette"
                );


        String textHolder = "";
        String[] sentenceA = {};
        String[] sentenceB = {};

        // Process all couples in the ArrayList
        for (String[] couple : couples) {
            // The input, where each sentence is an item in the array, gets converted to where each word is an item in the array for easier iteration
            if (couple[0] != null) {
                textHolder = couple[0].toLowerCase().replaceAll("[^a-zA-Zæøåòôèê\\s-]", "");
                sentenceA = textHolder.split("[ \n]");
            }

            if (couple[1] != null) {
                textHolder = couple[1].toLowerCase().replaceAll("[^a-zA-Zæøåòôèê\\s-]", "");
                sentenceB = textHolder.split("[ \n]");
            }

            // Iterate though the elements of the "left hand" sentence
            for (String aWord : sentenceA) {
                // Check to prevent common words to be checked unnecessarily
                if (bannedWords.contains(aWord)) {
                    continue;
                }

                // Arraylist to prevent a word from being added as a match twice, if it appears twice in the right hand side sentence
                ArrayList<String> addedWords = new ArrayList<String>();

                // If the word already exists in the map, increase the number of occurrences it has, if not, create a mapping
                if (appearanceMap.containsKey(aWord)) {
                    Integer counter = appearanceMap.get(aWord);
                    counter++;
                    appearanceMap.put(aWord, counter);
                } else {
                    appearanceMap.put(aWord, 1);
                }

                // Iterate through the right hand sentence
                for (String bWord : sentenceB) {
                    if (bannedWords.contains(bWord)) {
                        continue;
                    }
                    // If the aWord does not already exist as a key in the outer map, create a mapping with aWord, bWord and 1
                    if (!outerMap.containsKey(aWord) && !addedWords.contains(bWord)) {
                        HashMap<String, Integer> innerMap = new HashMap<>();
                        innerMap.put(bWord, 1);
                        outerMap.put(aWord, innerMap);
                        addedWords.add(bWord);
                    }

                    // If the mapping already exists, increment the counter
                    else if (outerMap.get(aWord).containsKey(bWord) && !addedWords.contains(bWord)) {
                        Integer counter = outerMap.get(aWord).get(bWord);
                        counter++;
                        outerMap.get(aWord).put(bWord, counter);
                        addedWords.add(bWord);
                    }

                    // If the key aWord exists, but not the mapping, create mapping aWord, bWord, 1
                    else if (!addedWords.contains(bWord)) {
                        outerMap.get(aWord).put(bWord, 1);
                        addedWords.add(bWord);
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

                final AtomicInteger[] secondMaxCount = {new AtomicInteger(0)};
                final String[] secondBestTranslation = {""};

                matches.forEach((bWord, occurs) -> {
                    if (occurs > maxCount[0].get()) {
                        secondMaxCount[0].set(maxCount[0].get());
                        secondBestTranslation[0] = bestTranslation[0];

                        maxCount[0].set(occurs);
                        bestTranslation[0] = bWord;
                    } else if (occurs > secondMaxCount[0].get()) {
                        secondMaxCount[0].set(occurs);
                        secondBestTranslation[0] = bWord;
                    }
                });

                if (!Objects.equals(bestTranslation[0], "") && appearanceMap.get(aWord) != null) {
                    /*
                    If the left word appears more than three times in the text, and the co-occurrence probability is more than 0,5
                    Also prunes out words that are the same in both languages and cases where the top two translations are equally likely
                     */
                    double probability = (double) outerMap.get(aWord).get(bestTranslation[0]) / appearanceMap.get(aWord);
                    double secondProbability = 0;
                    if (!secondBestTranslation[0].isEmpty() && outerMap.get(aWord).get(secondBestTranslation[0]) != null) {
                        secondProbability = (double) outerMap.get(aWord).get(secondBestTranslation[0]) / appearanceMap.get(aWord);
                    }

                    if (appearanceMap.get(aWord) > 2 && probability > 0.5 && !Objects.equals(aWord, bestTranslation[0]) && secondProbability != probability) {

                        // Prevent possessive s to be written to dictionary
                        if (aWord.endsWith("s") && aWord.length() > 2) {
                            String withoutS = aWord.substring(0, aWord.length() - 1);
                            if (!bestTranslation[0].equals(withoutS)) {
                                try {
                                    writer.append(aWord).append(",").append(bestTranslation[0]).append(",").append(String.valueOf(probability)).append("\n");
                                } catch (
                                        IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }else{
                            try {
                                writer.append(aWord).append(",").append(bestTranslation[0]).append(",").append(String.valueOf(probability)).append("\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
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
            String[] aWords = {"jeg", "også", "hun", "ham", "ikke", "de", "dere", "fra", "da", "en", "et", "hvor", "noen", "man", "dem", "kommer", "ble",
                    "sendes", "hvorav", "verdenskrig", "verdenskrigen","Norge", "enten", "forsetter", "åringer", "fremgår", "fortsetter", "fortsette"};
            String[] bWords = {"eg", "òg", "ho", "han", "ikkje", "dei", "dykk", "frå", "då", "ein", "eit", "kor", "nokon", "ein",
                    "dei", "kjem", "vart", "sendast", "kor", "verdskrig", "verdskrigen","Noreg", "anten", "held fram", "åringar", "går fram", "held fram", "halde fram"};

            for(int i = 0; i < aWords.length; i++){
                writer.append(aWords[i] + "," + bWords[i] + ",1\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}