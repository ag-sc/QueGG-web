/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.citec.sc.uio;

import java.net.MalformedURLException;

/**
 *
 * @author elahi
 */
public class Matcher {
     public static String cleanPrefix(String string) throws MalformedURLException {
        string=string.replace("http://dbpedia.org/ontology/", "dbo_");
        string=string.replace("http://dbpedia.org/property/", "dbp_");
        String result = string.trim().strip().stripLeading().stripTrailing();
        return result;
    }
    
}
