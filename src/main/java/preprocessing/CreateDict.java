// Functions for creating a local version of the dictionary available from the Ordbøkene API
package preprocessing;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

public class CreateDict {
    public CreateDict() {

        // Using the ReadTSV function to read the corpus
        ArrayList<String[]> couples = Train.ReadTSV("src/main/java/preprocessing/npk_2011_2022.tsv");

        String[] sentence = {};

        // For storing each unique word of the corpus. Later used for calling the API to create a local dictionary
        HashSet<String> wordSet = new HashSet<>();

        // The arraylist read from the TSV is processed line by line
        for (String[] couple : couples) {
            sentence = couple[0].split("[^a-zA-Zæøåòèê]"); // Bokmål sentences are split on any non-standard character

            wordSet.addAll(Arrays.asList(sentence)); // All words are added to the set
        }
        // Counter and length variable for measuring progress
        int counter = 0;
        int length = wordSet.size();

        // For each word in the set, the function GetArticleNum is called, which then calls GetArticleInfo
        for (String word : wordSet) {
            counter++;
            System.out.println("\nWord " + counter + " of " + length + ". " + ((counter/length)*100) + "% done");
            GetArticleNum(word);
        }
    }

    public static void GetArticleNum(String word) {
        try {
            // The word is inserted into the URL, preparing for an API call
            String url = "https://ord.uib.no/api/articles?w=" + word.toLowerCase() + "&dict=bm&scope=ei";

            // Creating a client for the HTTP request
            HttpClient client = HttpClient.newHttpClient();

            // Building request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // Send request and get response
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Parse JSON object
            JSONObject json = new JSONObject(response.body());

                /*
                     The format of the json file has the object articles, and the array bm (bokmål)
                     that contains the number of the articles that correspond to the search term, the first being most related;
                 */
            JSONArray bmArray = json.getJSONObject("articles").getJSONArray("bm");

            int articleNumber;

            // If the search gives no hits, a flag value of -1 is set, if not, the value is saved
            if (bmArray.isEmpty()) {
                articleNumber = -1;
            } else {
                articleNumber = bmArray.getInt(0);
            }
            System.out.println("The article number of " + word + " is: " + articleNumber);

            // The article number and the word is passed on to the next function that retrieves the information of the article
            getArticleInfo(articleNumber, word);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getArticleInfo(int articleNum, String word) {
        if (articleNum != -1) {
            try {
                String lemma = "";
                String[] articleTags = new String[2];

                HttpClient client = HttpClient.newHttpClient();

                // Calling the API with the article num we retrieved in the last step
                String url = "https://ord.uib.no/bm/article/" + articleNum + ".json";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();


                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject article = new JSONObject(response.body());

                // First getting the lemma information from the JSON
                JSONArray lemmas = article.getJSONArray("lemmas");
                if (!lemmas.isEmpty()) {
                    // In case there are multiple meanings of the same word, we are only interested in the first lemma
                    JSONObject firstLemma = lemmas.getJSONObject(0);
                    JSONArray paradigmInfo = firstLemma.getJSONArray("paradigm_info");
                    lemma = firstLemma.getString("lemma");

                    // The structure of the JSON (that we are interested in) is: lemmas{ paradigm_info{ tags { *wordClass*, *gender*
                    if (!paradigmInfo.isEmpty()) {
                        JSONObject firstParadigm = paradigmInfo.getJSONObject(0);
                        JSONArray tags = firstParadigm.getJSONArray("tags");

                        if (!tags.isEmpty()) {
                            articleTags[0] = tags.getString(0);

                            // If it is a noun, it will have gender information. For other word classes, it defaults to na
                            if (Objects.equals(articleTags[0], "NOUN") && tags.length() > 1) {
                                articleTags[1] = tags.getString(1);
                            }
                            else{
                                articleTags[1] = "na";
                            }
                        }
                    }
                }
                System.out.println("The word " + word + " with lemma " + lemma + " belongs to " + articleTags[0] + " (and is " + articleTags[1] + ")\n");
                WriteDict(word, lemma, articleTags[0], articleTags[1]); // This information is then sent to the final function to be written to a CSV

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println(word + " does not exist in the dictionary"); // If the flag of the word is -1
        }
    }

    public static void WriteDict(String word, String lemma, String wordClass, String gender) {
        // The information is saved to a CSV, ensuring local and quick access
        try (FileWriter writer = new FileWriter("src/main/java/preprocessing/dictionary.csv", true)) {
            if(!wordClass.equalsIgnoreCase("noun")){
                gender =  "na";
            }
            writer.append(word.toLowerCase()).append(",").append(lemma).append(",").append(wordClass).append(",").append(gender).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}