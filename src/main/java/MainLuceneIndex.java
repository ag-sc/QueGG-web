

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
import static java.lang.System.exit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author elahi
 */
public class MainLuceneIndex implements Constants {

    private static Boolean testFlag = false;
    private static Set<String> menu = new HashSet<String>();

    public static void main(String[] args) throws Exception {
        List<String> menus = Stream.of(WRITE_INDEX).collect(Collectors.toCollection(ArrayList::new));
        //List<String> languages = Stream.of("es","en","de","it").collect(Collectors.toCollection(ArrayList::new));
        List<String> languages = Stream.of("de").collect(Collectors.toCollection(ArrayList::new));

        Set<String> frames = Stream.of("-NPP-", "-VP-", "-IPP-").collect(Collectors.toCollection(TreeSet::new));

        for (String language : languages) {
            String indexDir = resourceDir + language + Constants.indexDir;
            String questionDir = resourceDir + language + Constants.questionDir;
            String reportFile = resourceDir + language + "/" + "total.csv";
            File directory = new File(indexDir);

            if (menus.contains(NUMBER_OF_QUESTIONS)) {
                Statistics.numberQuestions(frames, questionDir, reportFile, language);
            }
            
            
            if (menus.contains(WRITE_INDEX)) {
                WriteIndex.writeIndex(questionDir, indexDir, testFlag, rowLimit);
            }
            if (menus.contains(READ_INDEX)) {
                String tokens = "wo wurde";
                System.out.println("search::" + tokens);
                //LinkedHashSet<String> results = new LinkedHashSet<String>();

                QuestionLoader questionLoader = new QuestionLoader();
                //Map<String,Question> results = ReadIndex.readIndex(QUESTION_FIELD, SPARQL_FIELD,ANSWER_FIELD,tokens, 10);
                List<Question> suggestions = questionLoader.autocomplete(indexDir, tokens, 50);
                System.out.println("result!!!!!!!");
                for (Question result : suggestions) {
                    System.out.println(result.getQuestion()+" "+result.getSparql()+" "+result.getAnswer());
                }
            }
        }
    }
    
     private static String getFileProperty(String name) {
        String []info=name.split("-");
        return info[3];
    }

    private static Map<String, Set<String>> getSelectedFiles(String questionDir, String frameType) {
        File folder = new File(questionDir);
        File[] listOfFiles = folder.listFiles();
        Map<String, Set<String>> selectedFiles = new TreeMap<String, Set<String>>();

        for (File file : listOfFiles) {
            if (!file.getName().contains(frameType)) {
                continue;
            }

            String property = getFileProperty(file.getName());
            Set<String> fileNames = new TreeSet<String>();
            String fileName=questionDir+file.getName();
            if (selectedFiles.containsKey(property)) {
                fileNames = selectedFiles.get(property);
                fileNames.add(fileName);
            } else {
                fileNames.add(fileName);
            }

            selectedFiles.put(property, fileNames);

        }
        return selectedFiles;
    }

}
