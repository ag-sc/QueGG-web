/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import edu.citec.sc.queggweb.constants.Constants;
import edu.citec.sc.queggweb.data.Question;
import edu.citec.sc.queggweb.data.QuestionLoader;
import edu.citec.sc.queggweb.lucene.ReadIndex;
import edu.citec.sc.queggweb.lucene.WriteIndex;
import java.util.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

/**
 *
 * @author elahi
 */
public class MainLucene implements Constants {

    private static Boolean testFlag = false;
    private static Set<String> menu = new HashSet<String>();

    public static void main(String[] args) throws Exception {
        //String type="_NPP_";
        //Integer total=26798353+30704392+22753035;
        //Integer rest=

        //System.out.println(total);
        //FolderFileCount(CSV_DIR,type);
        menu.add(WRITE);
        //menu.add(READ);

        if (menu.contains(WRITE)) {
            WriteIndex.writeIndex(CSV_DIR, INDEX_DIR, testFlag, rowLimit);
        }
        if (menu.contains(READ)) {
            String tokens = "place was";
            System.out.println("search::" + tokens);
            //LinkedHashSet<String> results = new LinkedHashSet<String>();

            List<Question> results = new ArrayList<Question>();
            QuestionLoader questionLoader = new QuestionLoader();
            //Map<String,Question> results = ReadIndex.readIndex(QUESTION_FIELD, SPARQL_FIELD,ANSWER_FIELD,tokens, 10);
            List<Question> suggestions = questionLoader.autocomplete(tokens, 20);
            System.out.println("result!!!!!!!");
            for (Question result : results) {
                System.out.println(result.getQuestion());
                System.out.println(result.getSparql());
                System.out.println(result.getAnswer());
            }
        }
    }

}
