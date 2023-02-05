/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.citec.sc.uio;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 *
 * @author elahi
 */
public class Matcher {

    public static String cleanPrefix(String string) throws MalformedURLException {
        string = string.replace("http://dbpedia.org/ontology/", "dbo_");
        string = string.replace("http://dbpedia.org/property/", "dbp_");
        String result = string.trim().strip().stripLeading().stripTrailing();
        return result;
    }
    
    public static Boolean firstFirst(String searchText, String questionT) throws IOException {
        String wordS = firstWord(searchText).toLowerCase().strip().stripLeading().stripLeading().trim();
        String wordQ = firstWord(questionT).toLowerCase().strip().stripLeading().stripLeading().trim();
        if (wordS != null && wordQ != null) {
            if (wordS.contains(wordQ)) {
                //System.out.println("wordS::" + wordS + " wordQ::" + wordQ);
                return true;

            }
        }
        return false;
    }
    
    public static Boolean firstSekond(String searchText, String questionT) throws IOException {       
            if (questionT.contains(searchText)) 
                return true;

        return false;
    }

    public static String firstWord(String input) throws IOException {
        if (input.contains(" ")) {
            int i = input.indexOf(' ');
            String word = input.substring(0, i);
            String rest = input.substring(i);
            return word;
        }
        return null;
    }
    
    public static Boolean isFirstKhar(String input, String output) throws IOException {
        if (input.length() >= 1 ) {
            input = input.toLowerCase().strip().stripLeading().stripTrailing().trim();
            output = output.toLowerCase().strip().stripLeading().stripTrailing().trim();
            //System.out.println("searh::" + input.charAt(0) + " result::" + output.charAt(0));
            if (input.charAt(0) == output.charAt(0)) {
                return true;

            }
        }

        return false;
    }

    public static boolean subStringMatch(String searchText, String questionT) {
        if(questionT.contains(searchText)){
          return true;  
        }
        return false;
    }
    
    public static void firstAndLastCharacter(String str)
    {
 
        // Finding string length
        int n = str.length();
 
        // First character of a string
        char first = str.charAt(0);
 
        // Last character of a string
        char last = str.charAt(n - 1);
 
        // Printing first and last
        // character of a string
        System.out.println("First: " + first);
        System.out.println("Last: " + last);
    }
 

}
