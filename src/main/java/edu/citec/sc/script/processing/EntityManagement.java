package edu.citec.sc.queggweb.turtle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import edu.citec.sc.uio.StringMatcher;
import edu.citec.sc.uio.FileUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author elahi
 */
public class EntityManagement {

  
    public static Set<String>  findAllProperties(String entryFileName,
            String language,
            Integer numberOfTriples, String type) {
        //String entryFileName = turtleDir + turtleFile;
         String content =null;

            //find classes
            Set<String> classNames = tripleFileToHash(entryFileName, numberOfTriples, type, language, null);
            System.out.println(classNames.toString());
            //content = setToFile(classNames);

            /*//find entrities of each class
        for (String className : classNames) {
            className=className.replace("http://dbpedia.org/ontology/", "");
             
            //String entityFileName = entityDir + className + ".ttl";
            String resultFileName = outputDir + className + ".txt";
            Set<String> entities = tripleFileToHash(entryFileName, numberOfTriples, SUBJECT, language,className);
            String content = setToFile(entities, resultFileName);
            try {
                QueGG.stringToFile(content, resultFileName);
            } catch (IOException ex) {
                Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }*/
 /*if(!className.startsWith("E")) continue;*/
       
        
         return classNames;

    }
    
    public static void processResult(String entryFileName,String language,
            Integer numberOfTriples, String type) {

        try {
            //find classes
            Set<String> classNames = tripleFileToHash(entryFileName, numberOfTriples, type, language, null);
            System.out.println(classNames.toString());
            String content = setToFile(classNames);
            FileUtils.stringToFile(content, entryFileName.replace(".ttl", ".txt"));
            System.out.println("completed!!!");

            /*//find entrities of each class
        for (String className : classNames) {
            className=className.replace("http://dbpedia.org/ontology/", "");
             
            //String entityFileName = entityDir + className + ".ttl";
            String resultFileName = outputDir + className + ".txt";
            Set<String> entities = tripleFileToHash(entryFileName, numberOfTriples, SUBJECT, language,className);
            String content = setToFile(entities, resultFileName);
            try {
                QueGG.stringToFile(content, resultFileName);
            } catch (IOException ex) {
                Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }*/
 /*if(!className.startsWith("E")) continue;*/
        } catch (IOException ex) {
            Logger.getLogger(PropertyManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
     public static void generateEntities(String entityDir, String inputFileName,
            String outputDir, String language,
            Integer numberOfTriples, String type) {
        String entryFileName = entityDir + inputFileName;

        try {
            //find classes
            Set<String> classNames = tripleFileToHash(entryFileName, numberOfTriples, type, language, null);
            System.out.println(classNames.toString());
            String content = setToFile(classNames);
            FileUtils.stringToFile(content, outputDir + type + ".txt");
            System.out.println("completed!!!");

            /*//find entrities of each class
        for (String className : classNames) {
            className=className.replace("http://dbpedia.org/ontology/", "");
             
            //String entityFileName = entityDir + className + ".ttl";
            String resultFileName = outputDir + className + ".txt";
            Set<String> entities = tripleFileToHash(entryFileName, numberOfTriples, SUBJECT, language,className);
            String content = setToFile(entities, resultFileName);
            try {
                QueGG.stringToFile(content, resultFileName);
            } catch (IOException ex) {
                Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }*/
 /*if(!className.startsWith("E")) continue;*/
        } catch (IOException ex) {
            Logger.getLogger(PropertyManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
  
    public static Set<String> tripleFileToHash(String fileName, Integer numberOfTriples,String type,String language,String className) {
        Set<String> results = new TreeSet<String>();
        BufferedReader reader;
        String line = "";
        File file = new File(fileName);
        Integer lineNumber = 0;

        try {
            reader = new BufferedReader(new FileReader(fileName));
            line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                lineNumber = lineNumber + 1;
                String subject = null;
                String object = null, property = null;
                if (line != null) {
                    line = line.replace("<", "\n" + "<");
                    line = line.replace(">", ">" + "\n");
                    line = line.replace("\"", "\n" + "\"");
                    String[] lines = line.split(System.getProperty("line.separator"));

                    Integer index = 0;
                    for (String value : lines) {
                        index = index + 1;
                        if (index == 2) {
                            subject =  StringMatcher.clean(value,language);
                        } else if (index == 6) {
                            object =  StringMatcher.clean(value,language);
                        }
                        else if (index == 4) {
                            property =  StringMatcher.clean(value,language);
                        }
                    }
                  
                    
                    if (lineNumber == -1)
                         ; 
                    else if (lineNumber == numberOfTriples) {
                        break;
                    }
                    
                   //System.out.println("subject:" + subject + " " + "object:" + object+" "+ "property:" + property+" "+className);
                  

                    if(subject!=null&&object!=null){
                        ;
                    }
                    else continue;
                    
                   if( subject.contains("__")||object.contains("__"))
                       continue;
                    
                    if(className!=null&&object!=null){
                        if(object.contains(className))
                            ;
                        else 
                            continue;
                    }
                    
                    if (type.contains("subject")) {
                        System.out.println("subject:" + subject+" fileName:"+fileName);
                        results.add(subject);
                    } else if (type.contains("object")) {
                        System.out.println("object:" + object+" fileName:"+fileName);
                        results.add(object);
                    } else if (type.contains("property")) {
                        System.out.println("property:" + property+" fileName:"+fileName);
                        results.add(property);

                    }

                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;

    }

    private static String setToFile(Set<String> entities) {
        String content="";
        for(String entity:entities){
            String line=entity+"\n";
            content+=line;
        }
        return content;
    }
    
    


  

}
