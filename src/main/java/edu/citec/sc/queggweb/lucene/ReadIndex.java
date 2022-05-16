package edu.citec.sc.queggweb.lucene;

import edu.citec.sc.queggweb.constants.Constants;
import edu.citec.sc.queggweb.data.Question;
import edu.citec.sc.queggweb.uio.CsvFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.System.exit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class ReadIndex implements Constants{

    public static Map<String,Question> readIndex(String fieldQuestion, String fieldSparql,String fieldAnswerUri,String searchText,Integer topN) throws Exception {
        IndexSearcher searcher = createSearcher();
        //final List<Question> suggestions = new ArrayList<Question>();

        
        Map<String,Question> results=new TreeMap<String,Question>();
        
        //LinkedHashSet<String> results=new LinkedHashSet<String>();

        //Search by ID
        //TopDocs foundDocs = searchById(1, searcher);

        //System.out.println("Toral Results :: " + foundDocs.totalHits);

        /*for (ScoreDoc sd : foundDocs.scoreDocs) {
            Document d = searcher.doc(sd.doc);
            System.out.println(String.format(d.get("firstName")));
        }
        System.out.println("!!!!!!!!!!!find results!!!!!!!!!!!!!!!!! :: ");
        */
        //Search by firstName
        TopDocs foundDocs2 = searchByFirstName(searchText, searcher);

        Integer index=0;
           System.out.println("search Text :: "+searchText);
        for (ScoreDoc sd : foundDocs2.scoreDocs) {
            Document d = searcher.doc(sd.doc);
            String questionT=String.format(d.get(fieldQuestion));
            //questionT=questionT.toLowerCase();
            String sparqlT =String.format(d.get(fieldSparql));
            String answerT =String.format(d.get(fieldAnswerUri));
            Question question = new Question(questionT, sparqlT, answerT);
            results.put(question.getQuestion(),question);
            index=index+1;
            if(index>topN){
                break;
            }
                
        }
        
        /*for (String questionT : results) {
            System.out.println("questionT::" + questionT);
            String sparqlT = "select  ?o    {    <http://dbpedia.org/resource/Lower_Canada> <http://dbpedia.org/ontology/capital>  ?o    }";
            String answerT = "Quebec City";
            Question question = new Question(questionT, sparqlT, answerT);
            suggestions.add(question);
            index = index + 1;
            if (index >= topN) {
                break;
            }

        }*/
          System.out.println("Toral Results :: " + foundDocs2.totalHits);
          return results;
    }

   
    private static TopDocs searchByFirstName(String firstName, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("firstName", new StandardAnalyzer());
        Query firstNameQuery = qp.parse(firstName);
        TopDocs hits = searcher.search(firstNameQuery, Limit);
        return hits;
    }

    private static TopDocs searchById(Integer id, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("id", new StandardAnalyzer());
        Query idQuery = qp.parse(id.toString());
        TopDocs hits = searcher.search(idQuery, Limit);
        return hits;
    }

    private static IndexSearcher createSearcher() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }


}
