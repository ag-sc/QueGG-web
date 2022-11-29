package edu.citec.sc.uio;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author elahi
 */
public class BashScript {

    public static String FIND_WIKI_LINK = "FIND_WIKI_LINK";
    public static String FIND_ABSTRACT = "FIND_ABSTRACT";
    public static String FIND_IMAGE_LINK = "FIND_IMAGE_LINK";
    private static String COMMENT = "<http://www.w3.org/2000/01/rdf-schema#comment>";
    private String wikiLink = null;
    private String abstractText = null;
    private String imageLink = null;

    public BashScript(List<String> menus, String wikiLinkFile, String abstractfile, String imagefileName, String uri) {
        try {
            uri="<"+uri+">";
            if (menus.contains(FIND_WIKI_LINK)) {
                this.wikiLink= this.findLink(wikiLinkFile, uri);
                System.out.println(FIND_WIKI_LINK + " " + this.wikiLink);
            }
            if (menus.contains(FIND_ABSTRACT)) {
                this.abstractText = this.findAbstract(abstractfile, uri);
                System.out.println(FIND_ABSTRACT + " " + this.abstractText);
            }
            if (menus.contains(FIND_IMAGE_LINK)) {
                this.imageLink = this.findLink(imagefileName, uri);
                System.out.println(FIND_IMAGE_LINK + " " +  this.imageLink);
            }

        } catch (IOException ex) {
            Logger.getLogger(BashScript.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BashScript.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
     public BashScript(List<String> menus, String abstractfile, String uri) {
        try {
            uri="<"+uri+">";
            if (menus.contains(FIND_WIKI_LINK)) {
                this.wikiLink= this.findLink(uri);
                System.out.println(FIND_WIKI_LINK + " " + this.wikiLink);
            }
            if (menus.contains(FIND_ABSTRACT)) {
                this.abstractText = this.findAbstract(abstractfile, uri);
                System.out.println(FIND_ABSTRACT + " " + this.abstractText);
            }
            if (menus.contains(FIND_IMAGE_LINK)) {
                this.imageLink = this.findLink(uri);
                System.out.println(FIND_IMAGE_LINK + " " +  this.imageLink);
            }

        } catch (IOException ex) {
            Logger.getLogger(BashScript.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BashScript.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String findLink(String wikiFile, String answerUri) throws IOException, InterruptedException {

        String command = "grep -w" + " " + answerUri + " " + wikiFile;
        String bashResult = this.runCommandLine(command);
        String subject = null, object = null, property = null;
        String[] lines = bashResult.split(" ");
        Integer index = 0;

        for (String string : lines) {
            index = index + 1;
            if (index == 1) {
                subject = clean(string);
            } else if (index == 2) {
                property = clean(string);
            } else if (index == 3) {
                object = clean(string);
                return object;
            }
        }
        return null;
    }
    
    private String findLink(String answerUri) throws IOException, InterruptedException {
        String prefixRes = "http://dbpedia.org/resource/";
        String prefixWiki = "http://en.wikipedia.org/wiki/";
        return answerUri.replace(prefixRes, prefixWiki);
    }

    public String findAbstract(String fileName, String answerUri) throws IOException, InterruptedException {
        String command = "grep -w" + " " + answerUri + " " + fileName;
        String bashResult = this.runCommandLine(command);
        String string = bashResult.replace(COMMENT, "");
        string = string.replace(answerUri, "");
        return string.stripLeading();

    }

    private String clean(String value) {
        value = value.replace("<", "");
        value = value.replace(">", "");
        return value;
    }

    private String runCommandLine(String command) throws IOException, InterruptedException {
        BufferedReader stdInput = null;
        BufferedReader stdError = null;
        Runtime runTime = Runtime.getRuntime();
        //System.out.println("location + scriptName::" + location + scriptName);
        //String[] commands = {"perl", location + scriptName};
        //System.out.println("command::"+command);
        String readLine = null, readError = null, content = "";
        try {
            Process process = runTime.exec(command);
            stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            // Read the output from the command
            while ((readLine = stdInput.readLine()) != null) {
                String line = readLine;
                content += line + "\n";
            }
            // Read any errors from the attempted command
            while ((readError = stdError.readLine()) != null) {
                String line = readError;
                content += line + "\n";
            }
            if (process.waitFor() == 0) {
                System.err.println("Process terminated ");
            }
        } catch (IOException ex) {
            Logger.getLogger(BashScript.class.getName()).log(Level.SEVERE, null, ex);
        }

        return content;

    }

    public String getWikiLink() {
        return wikiLink;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public String getImageLink() {
        return imageLink;
    }

    public static void main(String args[]) {
        //grep -w "http://dbpedia.org/resource/Nishapur" wikipedia_links_en_filter.ttl
        List<String> menus = Stream.of(FIND_WIKI_LINK, FIND_ABSTRACT, FIND_IMAGE_LINK).collect(Collectors.toCollection(ArrayList::new));

        String abstractfile = "../resources/en/turtle/short_abstracts_sorted_en.ttl";
        String wikiLinkFile = "../resources/en/turtle/wikipedia_links_en_filter.ttl";
        String imagefileName = "../resources/en/turtle/wikipedia_links_en_filter.ttl";
        String uri = "http://dbpedia.org/resource/Nishapur";

        //old BashScript ..................
        //BashScript bashScript = new BashScript(menus, wikiLinkFile, abstractfile, imagefileName, uri);
        
        //BashScript ..................
        BashScript bashScript = new BashScript(menus, abstractfile, uri);
        System.out.println(bashScript.getWikiLink());

        /*try {

            if (menus.contains(FIND_WIKI_LINK)) {
                String result=bashScript.findLink(wikiLinkFile, uri);
                System.out.println(FIND_WIKI_LINK+" "+result);
            }
            if (menus.contains(FIND_ABSTRACT)) {
                String result=bashScript.findAbstract(abstractfile, uri);
                System.out.println(FIND_ABSTRACT+" "+result);
            }
            if (menus.contains(FIND_IMAGE_LINK)) {
                String result=bashScript.findLink(imagefileName, uri);
                System.out.println(FIND_IMAGE_LINK+" "+result);
            }

        } catch (IOException ex) {
            Logger.getLogger(BashScript.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BashScript.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    

}
