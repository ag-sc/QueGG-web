package edu.citec.sc.queggweb.turtle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import static edu.citec.sc.queggweb.turtle.ConstantsQuestion.PROPERTY;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import static edu.citec.sc.queggweb.turtle.ConstantsQuestion.GREP_COMMAND_FILE;

/**
 *
 * @author elahi
 */
public class PropertyManagement {

    private static Map<String, String> labels = new TreeMap<String, String>();

    public static void generateProperty(String propertyInputDir, String labelFileName,Integer numberOfTriples,String language) throws Exception {
        Set<String> properties = getFiles(propertyInputDir);
         if(properties.isEmpty()){
             throw new Exception("No property file to process!!");
         }

        for (String property : properties) {
            String propertyFile = propertyInputDir + property;
             String content = matchLabelsWithEntities(propertyFile, labelFileName, numberOfTriples);
            try {
                //propertyFile=propertyFile.replace(".ttl", "_" + language + ".txt");
                propertyFile=propertyFile.replace(".ttl", ".txt");
                stringToFile(content, propertyFile);
            } catch (IOException ex) {
                Logger.getLogger(PropertyManagement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(labelFileName);

        //String propertyFile = getPropertyFile(inputDir, property);
        //String labelFileName = tripleDir + language + "_labels.ttl";
        //labels = tripleFileToHashLabels(labelFileName, numberOfTriples);
        //System.out.println("labels:" + labels.size());
    }
    
    public static Set<String> getFiles(String propertyInputDir) {
        System.out.println("propertyInputDir:" +propertyInputDir);
        Set<String> properties = new TreeSet<String>();
        File file = new File(propertyInputDir);
        String[] propertyFiles = file.list();

        for (String propertyFile : propertyFiles) {
            if (propertyFile.contains(".ttl")) {
                properties.add(propertyFile);
            }
        }
        return properties;

    }

    public static void stringToFile(String content, String fileName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(content);
        writer.close();

    }

    public static String fileToString(String fileName) {
        InputStream is;
        String fileAsString = null;
        try {
            is = new FileInputStream(fileName);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            fileAsString = sb.toString();
            //System.out.println("Contents : " + fileAsString);
        } catch (Exception ex) {
            Logger.getLogger(PropertyManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return fileAsString;
    }

    public static Map<String, String> getUriLabelsJson(File classFile) {
        Map<String, String> map = new TreeMap<String, String>();
        Set<String> set = new TreeSet<String>();
        BufferedReader reader;
        String line = "";
        try {
            reader = new BufferedReader(new FileReader(classFile));
            //line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                if (line != null) {
                    line = line.strip().trim();
                    if (line.contains("=")) {
                        String uri = line.split("=")[0];
                        String label = line.split("=")[1];
                        map.put(uri, label);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, String> tripleFileToHash(String fileName, Integer numberOfTriples) {
        Map<String, String> results = new TreeMap<String, String>();
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
                            subject = clean(value);
                        } else if (index == 6) {
                            object = clean(value);
                        }
                    }
                    System.out.println("subject:" + subject + " " + "object:" + object+ "  property:" + fileName);
                    
                    if (lineNumber == -1)
                         ; 
                    else if (lineNumber == numberOfTriples) {
                        break;
                    }

                    results.put(subject, object);

                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;

    }
    
     public static Map<String, String> tripleFileToHashLabels(String fileName, Integer numberOfTriples) {
        Map<String, String> results = new TreeMap<String, String>();
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
                            subject = clean(value);
                        } else if (index == 6) {
                            object = clean(value);
                        }
                    }
                    
                    if (lineNumber == -1)
                         ; 
                    else if (lineNumber == numberOfTriples) {
                        break;
                    }
                    
                    subject=cleanNotation(subject);
                    object=cleanNotation(object);
                    System.out.println("subject:" + subject + " " + "object:" + object);


                    results.put(subject, object);

                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;

    }

    public static String matchLabelsWithEntities(String propertyFile, String labelFileName, Integer numberOfTriples) {
        Map<String, String> objectLabelHash = new TreeMap<String, String>();
        BufferedReader reader;
        String line = "";
        File file = new File(labelFileName);
        Map<String, String> propertySubObjEntities = tripleFileToHash(propertyFile, numberOfTriples);
        Map<String, String> propertyObjSubjEntities = reverseHash(propertySubObjEntities);
        
 
        List<OffLineResult> offLineResults = new ArrayList<OffLineResult>();
        String content = "";
        Integer lineNumber = 0;
        try {
            reader = new BufferedReader(new FileReader(labelFileName));
            line = reader.readLine();
           
            while (line != null) {
                line = reader.readLine();
                
                if (line != null&&line.contains("http"))
                    ;
                else {
                    System.out.println("line::"+line+" propertyFile:"+propertyFile);
                    continue;
                    
                }
                
                String uri = null, label = null, subjectUri = null;
                String subjectLabel = null, objectUri = null, objectLabel = null;
                if (line != null) {
                    line = line.replace("<", "\n" + "<");
                    line = line.replace(">", ">" + "\n");
                    line = line.replace("\"", "\n" + "\"");
                    String[] lines = line.split(System.getProperty("line.separator"));

                    Integer index = 0;
                    Boolean flag = false;
                    for (String value : lines) {
                        index = index + 1;
                        if (index == 2) {
                            uri = clean(value);
                        } else if (index == 6) {
                            label = clean(value);
                        }
                    }

                    if (!propertySubObjEntities.containsKey(uri)) {
                        subjectUri = null;
                    } else {
                        subjectUri = uri;
                        subjectLabel = label;
                        objectUri = propertySubObjEntities.get(subjectUri);
                    }
                    if (propertyObjSubjEntities.containsKey(uri)) {
                        objectLabelHash.put(uri, label);
                    }

                    if (subjectUri != null) {
                        lineNumber = lineNumber + 1;
                        OffLineResult offLineResult = new OffLineResult(subjectUri, subjectLabel, objectUri, objectLabel);
                        offLineResults.add(offLineResult);

                        if (numberOfTriples == -1)
                            ; 
                        else if (lineNumber >= numberOfTriples) {
                            break;
                        }

                        System.out.println(lineNumber + " subject:" + offLineResult.getSubjectUri() + " subjectLabel:");

                    }

                }
            }
            Integer index = 0;
            for (OffLineResult offLineResult : offLineResults) {
                String key = offLineResult.getObjectUri().trim().strip().stripLeading().stripTrailing();
                String objectLabel = null;
                //if (objectLabelHash.containsKey(key)) {
                index = index + 1;
                objectLabel = objectLabelHash.get(key);
                String fileLine = offLineResult.getSubjectUri() + "=" + offLineResult.getSubjectLabel()
                        + "=" + offLineResult.getObjectUri() + "=" + objectLabel;
                fileLine = fileLine + "\n";
                content += fileLine;

                System.out.println(index + " subject:" + offLineResult.getSubjectUri() + " subjectLabel:" + offLineResult.getSubjectLabel()
                        + " " + " objectUri:" + offLineResult.getObjectUri() + " " + " objectLabel:" + objectLabel);

                //}
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;

    }

    public static Map<String, OffLineResult> getEntityLabels(String propertyFile) {
        Map<String, OffLineResult> entityLabels = new TreeMap<String, OffLineResult>();
        BufferedReader reader;
        String line = "";
        File file = new File(propertyFile);
        String content = "";
        Integer lineNumber = 0;
        try {
            reader = new BufferedReader(new FileReader(propertyFile));
            line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                String subjectUri = null;
                String subjectLabel = null, objectUri = null, objectLabel = null;
                if (line != null) {
                    String[] lines = line.split("=");

                    Integer index = 0;
                    Boolean flag = false;
                    for (String value : lines) {
                        index = index + 1;
                        value = value.replace("<", "");
                        value = value.replace(">", "");
                        value = value.replace("\"", "");
                        if (index == 1) {
                            subjectUri = clean(value);
                        } else if (index == 2) {
                            subjectLabel = clean(value);
                        } else if (index == 2) {
                            subjectLabel = clean(value);
                        } else if (index == 3) {
                            objectUri = clean(value);
                        } else if (index == 4) {
                            objectLabel = clean(value);
                        }
                    }

                    lineNumber = lineNumber + 1;
                    OffLineResult offLineResult = new OffLineResult(subjectUri, subjectLabel, objectUri, objectLabel);
                    //System.out.println(lineNumber+" subject:" + subjectUri + " subjectLabel:" + subjectLabel
                    //        + " " + " objectUri:" + objectUri + " " + " objectLabel:" + objectLabel);
                    entityLabels.put(line, offLineResult);

                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entityLabels;
    }

    private static String clean(String value) {
        //value = value.replace("<", "");
        //value = value.replace(">", "");
        //value = value.replace("http://dbpedia.org/resource/", "");
        //value = value.replace("http://dbpedia.org/ontology/", "");
        //value = value.replace("^^<http://www.w3.org/2001/XMLSchema#date>", "");
        //value = value.replace("\"", "");
        value = value.trim().strip().stripLeading().stripTrailing();
        return value;
    }

    public static Map<String, List<String>> FileToHashList(String fileName) {
        Map<String, List<String>> results = new TreeMap<String, List<String>>();
        BufferedReader reader;
        String line = "";
        File file = new File(fileName);

        try {
            reader = new BufferedReader(new FileReader(fileName));
            line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                System.out.println("line:" + line);
                String subject = null;
                String object = null;
                if (line != null) {
                    line = line.replace("?o", "\n" + "?o");
                    line = line.replace("?s", "\n" + "?s");
                    System.out.println("line:" + line);
                    String[] lines = line.split(System.getProperty("line.separator"));

                    for (String value : lines) {
                        if (value.contains("?o")) {
                            object = StringUtils.substringBetween(value, "<", ">");
                        } else if (value.contains("?s")) {
                            subject = StringUtils.substringBetween(value, "<", ">");
                        }
                    }
                    String property = file.getName().replace(".txt", "");
                    if (property.contains("dbo")) {
                        property = "http://dbpedia.org/ontology/" + property;
                    } else if (property.contains("dbp")) {
                        property = "http://dbpedia.org/property/" + property;
                    }
                    results = parseLine(property, subject, object, results);

                    /*String property=file.getName().replace(".txt", "");
                    String id1=property+"_"+subject+"_"+"?o";
                    List<String> listo=new ArrayList<String>();
                    listo.add(object);
                    String id2=file.getName()+"_"+object+"_"+"?s";
                    List<String> lists=new ArrayList<String>();
                    lists.add(subject);
                    System.out.println("id1:" + id1);
                    System.out.println("id2:" + id2);
                    results.put(id1, listo);
                    results.put(id2, lists);*/
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;

    }

    public static Map<String, List<String>> parseLine(String property, String subject, String object, Map<String, List<String>> results) {
        String id1 = property + "_" + subject + "_" + "?o";
        List<String> listo = new ArrayList<String>();
        listo.add(object);
        String id2 = property + "_" + object + "_" + "?s";
        List<String> lists = new ArrayList<String>();
        lists.add(subject);
        System.out.println("id1:" + id1);
        System.out.println("id2:" + id2);
        results.put(id1, listo);
        results.put(id2, lists);
        return results;

    }

    private static Map<String, String> reverseHash(Map<String, String> propertySubObjEntities) {
        Map<String, String> reverseHash = new TreeMap<String, String>();
        for (String key : propertySubObjEntities.keySet()) {
            String value = propertySubObjEntities.get(key);
            reverseHash.put(value, key);
        }
        return reverseHash;
    }

    /*private static String getPropertyFile(String entityDir, String property) {
        return entityDir + property + ".ttl";
    }*/

    private static String cleanNotation(String line) {
        line = line.replace("http://dbpedia.org/resource/", "");
        line = line.replace("<", "");
        line = line.replace(">", "");
        line=line.strip().stripLeading().stripTrailing().trim();
        return line;
    }

    public static Set<String> getSetFromFiles(String propertyFile) {
        Set<String> results = new TreeSet<String>();
        BufferedReader reader;
        String line = "";
        File file = new File(propertyFile);

        try {
            reader = new BufferedReader(new FileReader(propertyFile));
            while ((line = reader.readLine()) != null) {
                line = reader.readLine();
                results.add(line);
            }

            reader.close();

        } catch (Exception ex) {
            Logger.getLogger(PropertyManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;
    }

    public static void getGrepCommand(String propertyDir,String propertyFile,String allTurtleFile,String language) {
        Set<String> properties = PropertyManagement.getSetFromFiles(propertyDir+propertyFile);
        String content="";
        String shellFile="#!/bin/sh";

        for (String property : properties) {
            String prefix=null,propertyName=null;
            
            prefix=StringMatcher.getPrefix(property);
            propertyName=StringMatcher.removeResourceUri(property);
            
            if (prefix != null)
                ; else
                continue;
            /*if (property.contains("http://dbpedia.org/ontology/")) {
               prefix="dbo_";
            }
            else if (property.contains("http://dbpedia.org/property/")) {
                prefix="dbp_";
            }*/
          
            //propertyName=property.replace("http://dbpedia.org/ontology/", "");
            String command = "grep " + "'" + "<" + property + ">" + "'" + " " + allTurtleFile + ">>" +propertyDir+prefix+propertyName+".ttl";
            content+=command+"\n";
            System.out.println(command);
            //command = "grep " + "'" + "<" + property + ">" + "'" + " " + allTurtleFile;
            //executeGrep(command,propertyDir+prefix+propertyName+".ttl");
        }
        
        try {
            content=shellFile+"\n"+content;
            System.out.println(content);
            stringToFile(content, language+"_"+GREP_COMMAND_FILE);
        } catch (Exception e) {

            e.printStackTrace();

        }
       

    }
    
    private static void executeGrep(String command, String propertyFile) {
        Process process = null;
        String line = null;
        Set<String> lines = new TreeSet<String>();
        String content = "";

        try {
            System.out.println(command);
            process = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = br.readLine()) != null) {
                lines.add(line);
                content += line + "\n";
                 System.out.println(line);
            }
                        System.out.println("lines::"+lines);

            process.destroy();
            if (!content.isEmpty()) {
                stringToFile(content, propertyFile);
            }
        } catch (Exception e) {
                    

            e.printStackTrace();

        }

    }


}
