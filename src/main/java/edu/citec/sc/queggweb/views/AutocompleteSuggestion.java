package edu.citec.sc.queggweb.views;

import edu.citec.sc.queggweb.data.Question;
import edu.citec.sc.queggweb.data.TrieNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;

@Data
@AllArgsConstructor
public class AutocompleteSuggestion {
    private String text;
    private boolean answerable;
    private boolean leaf;
    private int size;

    public AutocompleteSuggestion(TrieNode<Question> fromNode) {
        this.setText(fromNode.fullPath());
        this.setLeaf(fromNode.isLeaf());
        this.setAnswerable(fromNode.getData() != null);
        this.setSize(fromNode.size());
    }
}
