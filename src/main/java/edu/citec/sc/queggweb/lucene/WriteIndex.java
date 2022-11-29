/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.citec.sc.queggweb.lucene;

import edu.citec.sc.queggweb.constants.Constants;
import static edu.citec.sc.queggweb.constants.Constants.Limit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
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
import org.apache.lucene.store.FSDirectory;
import edu.citec.sc.uio.CsvFile;

/**
 *
 * @author elahi
 */
public class WriteIndex implements Constants{
    
   

    public static void writeIndex(String CSV_DIR, String INDEX_DIR, Boolean testFlag,Integer rowLimit) throws Exception {
        File folder = new File(CSV_DIR);
        File[] listOfFiles = folder.listFiles();
        IndexWriter writer = createWriter(INDEX_DIR);
        List<Document> documents = new ArrayList<Document>();

        long sum = 0;
        Integer index = 0;
        if(listOfFiles.length==0)
               throw new Exception("no questions available to generate index!!");

        for (File file : listOfFiles) {
            if (file.isFile()) {
                CsvFile csvFile = new CsvFile();
                String fileName = CSV_DIR + file.getName();
                //List<String[]> rows = csvFile.getRows(new File(fileName));
                List<String[]> rows = csvFile.getManualRow(new File(fileName), rowLimit);
                //long lines = countLineOfFile(fileName);
                //System.out.println(file.getName() + lines);
                for (String[] row : rows) {
                    System.out.println("length::"+row.length);
                    index = index + 1;
                    Boolean flag = false;
                    String id = null, question = null, sparql = null, answerUri = null,answerLabel=null,frameType=null,answerType=null;
                    String wikiLink = null, imageLink = null, abstractLink = null;

                    try {
                        //id = row[0].replace("\"", "");
                        //question = row[1].replace("\"", "");
                        //sparql = row[2].replace("\"", "");
                        //answer = row[3].replace("\"", "");
                        id = row[0];
                        question = row[1].replace("\"", "");
                        sparql = row[2].replace("\"", "");
                        answerUri = row[3].replace("\"", "");
                        answerLabel = row[4].replace("\"", "");
                        frameType = row[5].replace("\"", "");
                        answerType = row[6].replace("\"", "");
                         
                        if(question.contains(" null ")|question.contains(" null?"))
                            continue;
                        flag = true;
                    } catch (Exception ex) {
                       //throw new Exception(" something is wrong "+question+ " "+ sparql+" "+answerUri+" length:"+row.length+" "+ex.getMessage());
                       continue;
                    }
                    if (flag) {
                        //System.out.println(index + " " + question + " ");
                        System.out.println(index + " " + question + " " + sparql + " " + answerUri + " " + file.getName());

                        Document document = createDocument(id, question,sparql,answerUri,answerLabel,answerType);
                        //writer.addDocument(document);
                        //writer.commit();
                        documents.add(document);
                        if (documents.size() > batchSize) {
                            //writer.deleteAll();
                            writer.addDocuments(documents);
                            writer.commit();
                            documents = new ArrayList<Document>();
                        }
                    }
                    if (testFlag) {
                        if (index > Limit) {
                            break;
                        }
                    }

                }

                if (testFlag) {
                    if (index > Limit) {
                        break;
                    }
                }
            }

        }

        //writer.deleteAll();
        //writer.addDocuments(documents);
        //writer.commit();

        writer.close();

    }
    
    public static Document createDocument(String id, String question,String sparql,String answer,String answerLabel,String answerType) {
        Document document = new Document();
        document.add(new StringField(ID_FIELD, id, Field.Store.YES));
        document.add(new TextField(QUESTION_FIELD, question, Field.Store.YES));
        document.add(new TextField(SPARQL_FIELD, sparql, Field.Store.YES));
        document.add(new TextField(ANSWER_FIELD, answer, Field.Store.YES));
        document.add(new TextField(ANSWER_LABEL, answerLabel, Field.Store.YES));
        document.add(new TextField(ANSWER_TYPE, answerType, Field.Store.YES));
        return document;
    }
    
     /*public static Document createDocument(String id, String question, String sparql, String website) {
        Document document = new Document();
        document.add(new StringField("id", id, Field.Store.YES));
        document.add(new TextField("firstName", question, Field.Store.YES));
        //document.add(new TextField("lastName", sparql, Field.Store.YES));
        //document.add(new TextField("website", website, Field.Store.YES));
        return document;
    }*/

    public static IndexWriter createWriter(String INDEX_DIR) throws IOException {
        System.out.println(INDEX_DIR);
        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer = new IndexWriter(dir, config);
        return writer;
    }
    



}
