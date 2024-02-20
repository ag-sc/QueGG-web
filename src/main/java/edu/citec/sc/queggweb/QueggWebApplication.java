package edu.citec.sc.queggweb;

import edu.citec.sc.script.indexing.MainLuceneIndex2;
import edu.citec.sc.script.processing.MainTripletoQuestionsEntity;
import edu.citec.sc.uio.Matcher;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QueggWebApplication {

    public static void main(String[] args) {
        try {
            MainLuceneIndex2.index();
            //MainTripletoQuestionsEntity.PropertyGeneration();
            //SpringApplication.run(QueggWebApplication.class, args);
        } catch (Exception ex) {
            Logger.getLogger(QueggWebApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*public static void main(String[] args) {
   
         try {
             //SpringApplication.run(QueggWebApplication.class, args);
             String input = "w";
             String output = "When was Abner W. Sibal died?";
             
             
             String[] words = input.split("\\s+");
             Integer length = words.length;
             
             if (length==1&&Matcher.isFirstKhar(input, output)) {
                 System.out.println(input+" "+output);
             }
         } catch (IOException ex) {
             Logger.getLogger(QueggWebApplication.class.getName()).log(Level.SEVERE, null, ex);
         }
       
     
    }*/
}
