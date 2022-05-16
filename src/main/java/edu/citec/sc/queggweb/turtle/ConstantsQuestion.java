package edu.citec.sc.queggweb.turtle;


import java.util.Map;
import java.util.TreeMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author elahi
 */
public interface ConstantsQuestion {

    public static Map<String, String> labels = new TreeMap<String, String>();
    public static Integer numberOfTriples = 10000;
    public static String turtleDir = "../resources/en/turtle/";

    public static String outputDir = "../resources/en/entity/";
    public static String propertyDir = "../resources/en/property/";
    public static String SUBJECT = "subject";
    public static String PROPERTY = "property";
    public static String OBJECT = "object";
    public static String allTriple = "mappingbased_properties_cleaned_en.ttl";

    public static String entryTriple = "A-entity.ttl";
    public static String language_en = "en";
    public static String language_it = "it";
    public static String labelFile = "labels.ttl";
    public static String turtleDir_it = "../resources/"+"it"+"/turtle/";
    public static String outputDir_it = "../resources/it/entity/";
    public static String propertyDir_it = "../resources/it/property/";
    public static String allTriple_it = "mappingbased_properties_cleaned_it.ttl";
    
    public static String FIND_PROPERTY = "FIND_PROPERTY";
    public static String FIND_PROPERTY_FILES = "FIND_PROPERTY_FILES";
    public static String FIND_LABELS_OF_ENTITIES="FIND_LABELS_OF_ENTITIES";
    public static String GREP_COMMAND_FILE = "grep.sh";


    



}
