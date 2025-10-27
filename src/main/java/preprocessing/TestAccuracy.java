package preprocessing;
import com.tonning.translation.service.TranslationService;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class TestAccuracy {
    TestAccuracy(){
        int hits = 0;
        int misses = 0;
        double average = 0;

        Scanner scanner = new Scanner(System.in);


        ArrayList<String[]> couples = Train.ReadTSV("src/main/java/preprocessing/npk_2011_2022.tsv");


        for(int i = 400; i < couples.size() - 400; i++){
            String inputBokmaal = couples.get(i)[0];
            String idealNynorsk = couples.get(i)[1].trim();
            String myTranslation = TranslationService.Translate(couples.get(i)[0]).trim();

            System.out.println("-----------------------------------------------------------------------");
            System.out.println("Bokmål: " + inputBokmaal);
            System.out.println("Translation: " + myTranslation);
//            System.out.println("Ideal: " + idealNynorsk);

            if(myTranslation.equals(idealNynorsk)){
                System.out.println("Automatic equals!");
                hits++;
            }
            else if(Objects.equals(scanner.nextLine(), "y"))
                hits++;
            else
                misses++;
            average = (double) hits / (hits+misses);
            System.out.println("Hits: " + hits);
            System.out.println("Misses: " + misses);
            System.out.println("Accuracy: " + average);
            System.out.println("-----------------------------------------------------------------------");

        }
    }
}
