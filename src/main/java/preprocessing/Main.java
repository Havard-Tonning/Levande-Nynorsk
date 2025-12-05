package preprocessing;

import com.tonning.translation.service.TranslationService;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
//       ArrayList<String[]> couples = Train.ReadTSV("src/main/java/preprocessing/npk_2011_2022.tsv");
//       System.out.println("Loaded " + couples.size() + " pairs from TSV");
//       Train.GenerateTranslation(couples);

        System.out.println(TranslationService.Translate("Han minnes om den dagen"));

//        new TestAccuracy();

//        new CreateDict();
    }
}