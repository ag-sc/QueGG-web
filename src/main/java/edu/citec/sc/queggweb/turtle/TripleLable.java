package edu.citec.sc.queggweb.turtle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author elahi
 */
public class TripleLable {

    private String subjectUri = null;
    private String subjectLabel = null;
    private String objectUri = null;
    private String objectLabel = null;

    public TripleLable(String subjectUri, String subjectLabel, String objectUri, String objectLabel) {
        this.subjectUri = subjectUri;
        this.subjectLabel = subjectLabel;
        this.objectUri = objectUri;
        this.objectLabel = objectLabel;
    }

    public String getSubjectUri() {
        return subjectUri;
    }

    public String getSubjectLabel() {
        return subjectLabel;
    }

    public String getObjectUri() {
        return objectUri;
    }

    public String getObjectLabel() {
        return objectLabel;
    }

}
