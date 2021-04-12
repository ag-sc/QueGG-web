package edu.citec.sc.queggweb.views;

import edu.citec.sc.queggweb.data.Question;
import edu.citec.sc.queggweb.data.TrieNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class AutocompleteSuggestion {
    private String text;
    private String sparql;
    private boolean answerable;
    private boolean leaf;
    private int size;
    private int entityOnset = -1;
    private int entityOffset = -1;

    public AutocompleteSuggestion(TrieNode<Question> fromNode) {
        this.setText(fromNode.fullPath());
        this.setLeaf(fromNode.isLeaf());
        this.setSparql(fromNode.getData() != null ? fromNode.getData().getSparql() : null);
        this.setAnswerable(fromNode.getData() != null);
        this.setSize(fromNode.size());
    }

    public void align(List<String> resourceLabels) {
        if (false) {
            int bestOnset = -1;

            int testOnset = text.lastIndexOf(" ");
            while (testOnset > 0) {
                String testText = " " + text.substring(testOnset).toLowerCase().replaceAll("[^a-zA-Z0-9]", " ").trim() + " ";
                if (!testText.trim().equals("")) {
                    for (String label : resourceLabels) {
                        // normalize
                        label = " " + label.toLowerCase().replaceAll("[^a-zA-Z0-9]", " ").trim();
                        if (label.trim().equals("")) continue;

                        if (label.startsWith(testText)) {
                            bestOnset = testOnset;
                        }
                        System.err.println(label);
                    }
                }
                testOnset = text.lastIndexOf(" ", testOnset - 1);
            }

            if (bestOnset < 0) {
                return;
            }
            this.entityOnset = bestOnset + 1;
            this.entityOffset = this.text.length() - 1;
        }
    }

    public void align(String resourceLabel) {
        resourceLabel = " " + resourceLabel.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", " ").trim() + " ";
        val checkText = " " + text.toLowerCase().replaceAll("[^a-zA-Z0-9]", " ") + " ";
        if (!checkText.contains(resourceLabel)) {
            return;
        }
        this.entityOnset = checkText.indexOf(resourceLabel);
        this.entityOffset = this.entityOnset + resourceLabel.length() - 2;

        //String tmp = text;
        //this.text = tmp.substring(0, entityOnset) + "|" + tmp.substring(entityOnset, entityOffset) + "|" + tmp.substring(entityOffset);
    }
}
