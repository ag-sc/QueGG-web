package edu.citec.sc.queggweb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderHeaderAware;
import lombok.Getter;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("questions")
@Scope("singleton")
public class QuestionLoader {
    private static final String CACHE_FILENAME = "/tmp/trie.cache";
    private int loaded = 0;

    @Getter
    Trie<Question> trie = new Trie<>();

    private void loadFromCSV() throws IOException {

        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("data/questions.csv")) {

            assert is != null;

            val in = new BufferedReader(new InputStreamReader(is));
            val reader = new CSVReaderHeaderAware(in);
            Map<String, String> row;
            while ((row = reader.readMap()) != null) {
                String question = row.get("question");
                if (question.trim().startsWith("SELECT ") && question.trim().endsWith("}")) {
                    System.err.println("skipping malformed question: " + question);
                    continue;
                }

                String sparql = row.get("sparql");
                String answer = row.get("answer");

                Question q = new Question(question, sparql, answer);
                try {
                    trie.insert(q.getQuestion(), q);
                } catch (TrieNode.DuplicateInsertException e) {
                    System.err.println(e.getMessage());
                }
                this.loaded++;
                if (this.loaded % 10000 == 0) {
                    System.out.println("loaded: " + loaded + " trie size: " + trie.size());
                }
            }

            trie.dump(false);
            trie.store(CACHE_FILENAME);
        }
    }

    public QuestionLoader() throws IOException {
        loadFromCache();
        if (trie.size() == 0) {
            loadFromCSV();
        }
    }
    
    private void loadFromCache() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File infile = new File(CACHE_FILENAME);
        if (infile.exists()) {
            val trie_root = mapper.readValue(infile,
                    new TypeReference<TrieNode<Question>>() {
                    });
            fixParents(trie_root, null);
            trie.setRoot(trie_root);
            System.out.println("restored trie from cache. size: " + trie.size());
        }
    }

    private void fixParents(TrieNode<Question> trie_node, TrieNode<Question> parent) {
        trie_node.setParent(parent);

        if (trie_node.getChildren() != null) {
            for (TrieNode<Question> child : trie_node.getChildren()) {
                if (child.isLeaf()) this.loaded++;
                fixParents(child, trie_node);
            }
        }
    }

    public int size() {
        return this.loaded;
    }

    private Map<String, String> createSuggestion(TrieNode<Question> cur) {
        val result = new HashMap<String, String>();
        result.put("type", cur.isLeaf() ? "leaf" : "node");
        result.put("text", cur.fullPath());
        result.put("size", Integer.toString(cur.size()));
        return result;
    }

    public List<TrieNode<Question>> autocomplete(String query, List<Map<String, String>> results, int topN, int maxDepth) {
        final List<TrieNode<Question>> suggestions = new ArrayList<>();
        TrieNode<Question> cur = this.trie.getRoot().find(query, true);

        this.gatherResults(query, suggestions, cur, topN, maxDepth, 0, false);

        for (TrieNode<Question> suggestion: suggestions) {
            results.add(createSuggestion(suggestion));
        }

        return suggestions;
    }

    private void gatherResults(String query, List<TrieNode<Question>> suggestions, TrieNode<Question> cur, int topN, int maxDepth, int curDepth, boolean skipcur) {
        if (curDepth >= maxDepth) {
            return;
        }
        if (suggestions.size() >= topN) {
            return;
        }

        if (!skipcur && !cur.isRoot() && !"".equals(cur.fullPath())) {
            if (cur.fullPath().endsWith(" ") || (cur.isLeaf() && !"".equals(query))) {
                addSuggestion(suggestions, cur, topN, query);
            }
        }

        if (cur.getChildren() == null) {
            return;
        }

        for (TrieNode<Question> child: cur.getChildren()) {
            if (child.fullPath().endsWith(" ") || (child.isLeaf() && !"".equals(query))) {
                addSuggestion(suggestions, child, topN, query);
            }
        }

        for (TrieNode<Question> child: cur.getChildren()) {
            gatherResults(query, suggestions, child, topN, maxDepth, curDepth+1, true);
        }

    }

    private void addSuggestion(List<TrieNode<Question>> suggestions, TrieNode<Question> cur, int topN, String query) {
        if (suggestions.size() >= topN) { return; }
        if (!cur.fullPath().toLowerCase().startsWith(query.toLowerCase())) { return; }

        for (TrieNode<Question> other: suggestions) {
            if (other.fullPath().equals(cur.fullPath())) {
                return;
            }
        }

        suggestions.add(cur);
    }
}
