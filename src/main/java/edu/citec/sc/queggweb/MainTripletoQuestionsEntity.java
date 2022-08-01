package edu.citec.sc.queggweb;


import edu.citec.sc.queggweb.turtle.ConstantsQuestion;
import edu.citec.sc.queggweb.turtle.EntityManagement;
import edu.citec.sc.queggweb.turtle.PropertyManagement;
import edu.citec.sc.uio.FileUtils;
import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author elahi
 */
public class MainTripletoQuestionsEntity implements ConstantsQuestion {

    public static void main(String[] args) {
        //List<String> languages = Stream.of(italian,german,spanish,english).collect(Collectors.toCollection(ArrayList::new));
        //List<String> menus = Stream.of(FIND_ENTITIES).collect(Collectors.toCollection(ArrayList::new));
        List<String> languages = Stream.of(spanish).collect(Collectors.toCollection(ArrayList::new));
        List<String> menus = Stream.of(BUILD_TRIPLE_WITH_LABELS_ENTITY).collect(Collectors.toCollection(ArrayList::new));
        List<String> propertyFiles = Stream.of(mappingbased_objects, specific_mappingbased_properties, mappingbased_literals, persondata, mappingbased_objects_disjoint_domain, mappingbased_objects_disjoint_range, infobox_properties).collect(Collectors.toCollection(ArrayList::new));
        Integer numberOfTriples = -1;

        for (String language : languages) {
            String languageDir = resourceDir + language + File.separator;
            String turtleDir = languageDir + File.separator + turtle + File.separator;
            String entittyDir = languageDir + File.separator + entity + File.separator;
            String propertyDir = languageDir + File.separator + PROPERTY + File.separator;

            if (menus.contains(FIND_PROPERTY)) {
                String content = "";
                Map<String, String> allProperties = new TreeMap<String, String>();
                for (String fileName : propertyFiles) {
                    String allTriple = turtleDir+fileName + underscore + language + ttl;
                    Set<String> temp = EntityManagement.findAllProperties(allTriple, language, numberOfTriples, PROPERTY);
                    Map<String, String> tempHash = getHash(temp, allTriple);
                    allProperties.putAll(tempHash);
                }
                content = hashToFile(allProperties);
                writeInFile(content, propertyDir + PROPERTY + ".txt");
                System.out.println("completed!!!");
            }

            if (menus.contains(BUILD_PROPERTY_FILES)) {
                String allTriple = mappingbased_objects + underscore + language + ttl;
                String grepFile = GREP_COMMAND_FILE + underscore + language + sh;
                PropertyManagement.getGrepCommand(propertyDir, PROPERTY + txt, turtleDir + allTriple, grepFile);
            }
            if (menus.contains(FIND_ENTITIES)) {
                /*String entityTriple = instance_types+underscore +language+ ttl;
                EntityManagement.findAllProperties(turtleDir, entityTriple, entittyDir, language, numberOfTriples, object);
                String grepFile=GREP_COMMAND_FILE+underscore+FIND_ENTITIES+underscore+language+sh;
                PropertyManagement.getGrepCommand(entittyDir, object + txt, turtleDir + entityTriple, grepFile);*/
                String[] files = new File(entittyDir).list();
                for (String fileName : files) {
                    EntityManagement.processResult(entittyDir + fileName, language, numberOfTriples, SUBJECT);

                }
            }
            if (menus.contains(BUILD_TRIPLE_WITH_LABELS_ENTITY)) {
                
               Set<String> exitProp=getExistingProperties(entittyDir);
                   
                String labelFile = labels + underscore + language + ttl;
                try {            
                  
                    PropertyManagement propertyManagement = new PropertyManagement(language);
                    propertyManagement.generateProperty(entittyDir,
                            turtleDir + labelFile, numberOfTriples,exitProp);
                    /*propertyManagement.generatePropertyFromList(propertyDir,propertyFile,
                            turtleDir + labelFile, numberOfTriples,exitProp);*/
                    System.out.println("property management is completed!!!");
                } catch (Exception ex) {
                    Logger.getLogger(MainTripletoQuestionsEntity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (menus.contains(BUILD_TRIPLE_WITH_LABELS_PROPERTY)) {
                
               Set<String> exitProp=getExistingProperties(propertyDir);
                   
                String labelFile = labels + underscore + language + ttl;
                try {            
                  
                    PropertyManagement propertyManagement = new PropertyManagement(language);
                    propertyManagement.generateProperty(propertyDir,
                            turtleDir + labelFile, numberOfTriples,exitProp);
                    /*propertyManagement.generatePropertyFromList(propertyDir,propertyFile,
                            turtleDir + labelFile, numberOfTriples,exitProp);*/
                    System.out.println("property management is completed!!!");
                } catch (Exception ex) {
                    Logger.getLogger(MainTripletoQuestionsEntity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private static void writeInFile(String content, String propertyDir) {
        try {
            FileUtils.stringToFile(content, propertyDir);
        } catch (IOException ex) {
            Logger.getLogger(MainTripletoQuestionsEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Map<String, String> getHash(Set<String> temp, String fileName) {
        Map<String, String> map = new TreeMap<String, String>();
        for (String string : temp) {
            map.put(string, fileName);
        }
        return map;
    }

    private static String hashToFile(Map<String, String> entities) {
        String content = "";
        for (String key : entities.keySet()) {
            String value = entities.get(key);
            String line = key + "=" + value + "\n";
            content += line;
        }
        return content;
    }

    private static Set<String> getExistingProperties(String propertyDir) {
        Set<String> properties = new TreeSet<String>();
        String[] file = new File(propertyDir).list();
        for (String fileString : file) {
            if (fileString.contains(".txt")) {
                if (fileString.contains("dbo_") || fileString.contains("dbp_")) {
                    properties.add(FilenameUtils.removeExtension(new File(fileString).getName()));
                }
            }

        }
        return properties;

    }

}
