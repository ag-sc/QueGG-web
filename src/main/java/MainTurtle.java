


import edu.citec.sc.queggweb.turtle.ConstantsQuestion;
import edu.citec.sc.queggweb.turtle.Entity;
import edu.citec.sc.queggweb.turtle.PropertyManagement;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        Set<String> languages=new TreeSet<String>();
        
        
        String language = language_it;
        String languageDir = "../resources/" + language + "/";
        String turtleDir = "../resources/" + language + "/turtle/";
        String entittyDir = "../resources/" + language + "/entity/";
        String propertyDir = "../resources/" + language + "/property/";
        String allTriple = language + "_allTriples" + ".ttl";
        Set<String> menus=new TreeSet<String>();
        menus.add(FIND_LABELS_OF_ENTITIES);

        if (menus.contains(FIND_PROPERTY)) {
            Entity.findAllProperties(turtleDir, allTriple, propertyDir, language, numberOfTriples, PROPERTY);
        }
        if (menus.contains(FIND_PROPERTY_FILES)) {
            PropertyManagement.getGrepCommand(propertyDir, PROPERTY+".txt",turtleDir+allTriple,language);
        }
        if (menus.contains(FIND_LABELS_OF_ENTITIES)) {
            try {
                PropertyManagement.generateProperty("../resources/" + language + "/property/",
                         "../resources/" + language + "/turtle/" + labelFile,
                        numberOfTriples, language);
            } catch (Exception ex) {
                Logger.getLogger(MainTurtle.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //QueGG.generateProperty(propertyDir_it,turtleDir_it+labelFile_it,numberOfTriples, language_it);
    }

}
