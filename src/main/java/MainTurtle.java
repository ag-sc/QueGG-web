
import edu.citec.sc.queggweb.turtle.ConstantsQuestion;
import edu.citec.sc.queggweb.turtle.EntityManagement;
import edu.citec.sc.queggweb.turtle.PropertyManagement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author elahi
 */
public class MainTurtle implements ConstantsQuestion {

    //FIND_PROPERTY
    //FIND_PROPERTY_FILES
    //FIND_LABELS_OF_ENTITIES
    public static void main(String[] args) {
        List<String> languages = Stream.of(italian,german,spanish,english).collect(Collectors.toCollection(ArrayList::new));
        List<String> menus = Stream.of(BUILD_TRIPLE_WITH_LABELS).collect(Collectors.toCollection(ArrayList::new));

        for (String language : languages) {
            String languageDir = resourceDir+ language + File.separator;
            String turtleDir = languageDir+ File.separator+ turtle+ File.separator;
            String entittyDir = languageDir  + File.separator+ entity+ File.separator;
            String propertyDir = languageDir + File.separator+ property+ File.separator;
            String allTriple = mappingbased_objects+underscore +language+ ttl;
         
            if (menus.contains(FIND_PROPERTY)) {
                EntityManagement.findAllProperties(turtleDir, allTriple, propertyDir, language, numberOfTriples, property);
            }
            if (menus.contains(BUILD_PROPERTY_FILES)) {
                String grepFile=GREP_COMMAND_FILE+underscore+language+sh;
                PropertyManagement.getGrepCommand(propertyDir, property + txt, turtleDir + allTriple, grepFile);
            }
            if (menus.contains(BUILD_TRIPLE_WITH_LABELS)) {
                String labelFile=labels+underscore+language+ttl;
                try {
                    PropertyManagement propertyManagement=new PropertyManagement(language);
                    propertyManagement.generateProperty(propertyDir,
                           turtleDir + labelFile,numberOfTriples);
                    System.out.println("property management is completed!!!");
                } catch (Exception ex) {
                    Logger.getLogger(MainTurtle.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

}
