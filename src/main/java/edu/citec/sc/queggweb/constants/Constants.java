/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.citec.sc.queggweb.constants;

/**
 *
 * @author elahi
 */
public interface Constants {

    //public static final String INDEX_DIR = "../resources/en/index/";
    public static final String resourceDir = "resources/";
    //public static final String resourceDir = "resources/";
    public static final String indexDir = "/index/";
    public static final String questionDir = "/questions/";

    public static final String WRITE_INDEX = "WRITE";
    public static final String READ_INDEX = "READ";
    public static final String NUMBER_OF_QUESTIONS="NUMBER_OF_QUESTIONS";
    public static final String DUPLICATE_REMOVER="DUPLICATE_REMOVER";

    
    public static final String ID_FIELD = "id";
    public static final String QUESTION_FIELD = "firstName";
    public static final String SPARQL_FIELD = "lastName";
    public static final String ANSWER_FIELD = "website";
    public static final String ANSWER_LABEL = "label";
    public static final String ANSWER_TYPE = "type";
    
    public static String FIND_WIKI_LINK = "FIND_WIKI_LINK";
    public static String FIND_ABSTRACT = "FIND_ABSTRACT";
    public static String FIND_IMAGE_LINK = "FIND_IMAGE_LINK";
    



    public static final Integer Limit=500;
    public static final Integer batchSize=10000;
    public static final Integer rowLimit=100000;

}
