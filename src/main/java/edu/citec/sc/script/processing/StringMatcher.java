package edu.citec.sc.uio;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author elahi
 */
public class StringMatcher {
    
    public static String clean(String value,String language) {
        value = value.replace("<", "");
        value = value.replace(">", "");
        if(language.contains("it")){
        value = value.replace("http://it.dbpedia.org/resource/", "");
        value = value.replace("http://it.dbpedia.org/resource//", "");      
        }
      
        value = value.replace("^^<http://www.w3.org/2001/XMLSchema#date>", "");
        value = value.trim().strip().stripLeading().stripTrailing();
        return value;
    }
    
    public static String getPrefix(String property) {
        String prefix = null;
        if (property.contains("http://dbpedia.org/ontology/")) {
            prefix = "dbo_";
        } else if (property.contains("http://dbpedia.org/property/")) {
            prefix = "dbp_";
        }
        return prefix;
    }
    
      public static String removeResourceUri(String property) {
        return property.replace("http://dbpedia.org/ontology/", "");
    }
    
  
    
}
