package preprocessing;

import com.tonning.translation.service.TranslationService;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
//        InputToCSV csvReader = new InputToCSV();
//        ArrayList<String[]> couples = Train.ReadCSV("pairs.csv");
//        System.out.println("Loaded " + couples.size() + " pairs from CSV");
//
//       ArrayList<String[]> couples2 = Train.ReadTSV("src/main/java/preprocessing/npk_2011_2022.tsv");
//        System.out.println("Loaded " + couples2.size() + " pairs from TSV");

//        Train.generateTranslation(couples2);

//        System.out.println(TranslationService.Translate("Kan du gjøre det mens jeg ser på?"));
//     new CreateDict();

        new TestAccuracy();
    }
}