/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import edu.citec.sc.queggweb.constants.Constants;
import edu.citec.sc.queggweb.data.Question;
import edu.citec.sc.queggweb.data.QuestionLoader;
import edu.citec.sc.queggweb.lucene.WriteIndex;
import static edu.citec.sc.queggweb.turtle.ConstantsQuestion.FIND_ENTITIES;
import static edu.citec.sc.queggweb.turtle.ConstantsQuestion.english;
import static edu.citec.sc.queggweb.turtle.ConstantsQuestion.german;
import static edu.citec.sc.queggweb.turtle.ConstantsQuestion.italian;
import static edu.citec.sc.queggweb.turtle.ConstantsQuestion.spanish;
import edu.citec.sc.uio.CsvFile;
import edu.citec.sc.uio.Statistics;
import static edu.citec.sc.uio.Statistics.FolderFileCount;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author elahi
 */
public class MainLucene implements Constants {

    private static Boolean testFlag = false;
    private static Set<String> menu = new HashSet<String>();

    public static void main(String[] args) throws Exception {
        List<String> menus = Stream.of(WRITE).collect(Collectors.toCollection(ArrayList::new));
        List<String> languages = Stream.of("es","en").collect(Collectors.toCollection(ArrayList::new));
        Set<String> frames = Stream.of("-NPP-", "-VP-", "-IPP-").collect(Collectors.toCollection(TreeSet::new));

        for (String language : languages) {
            String indexDir = resourceDir + language + Constants.indexDir;
            String questionDir = resourceDir + language + Constants.questionDir;
            String reportFile = resourceDir + language + "/" + "total.csv";
            File directory = new File(indexDir);
            FileUtils.cleanDirectory(directory);
            if (menus.contains(NUMBER_OF_QUESTIONS)) {
                Statistics.numberQuestions(frames, questionDir, reportFile, language);
            }
            if (menus.contains(WRITE)) {
                WriteIndex.writeIndex(questionDir, indexDir, testFlag, rowLimit);
            }
            if (menus.contains(READ)) {
                String tokens = "place was";
                System.out.println("search::" + tokens);
                //LinkedHashSet<String> results = new LinkedHashSet<String>();

                List<Question> results = new ArrayList<Question>();
                QuestionLoader questionLoader = new QuestionLoader();
                //Map<String,Question> results = ReadIndex.readIndex(QUESTION_FIELD, SPARQL_FIELD,ANSWER_FIELD,tokens, 10);
                List<Question> suggestions = questionLoader.autocomplete(Constants.indexDir, tokens, 20);
                System.out.println("result!!!!!!!");
                for (Question result : results) {
                    System.out.println(result.getQuestion());
                    System.out.println(result.getSparql());
                    System.out.println(result.getAnswer());
                }
            }
        }
    }

}
