package preprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class InputReader {

    private Trie trie;

    public InputReader(Trie trie) {
        this.trie = trie;
    }

    public void readAndInsertWords(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] words = line.trim().split("\\s+");

                for (String word : words) {
                    if (word != null && !word.isEmpty()) {

                        trie.insertWord(word.toLowerCase());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}