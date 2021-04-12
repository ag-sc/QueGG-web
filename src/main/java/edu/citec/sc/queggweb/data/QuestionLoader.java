package edu.citec.sc.queggweb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderHeaderAware;
import edu.citec.sc.queggweb.views.AutocompleteSuggestion;
import lombok.Getter;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
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

    public List<AutocompleteSuggestion> suggestionsToResults(List<TrieNode<Question>> suggestions) {
        val sparqlResourceLabels = this.gatherSparqlResourceLabels(suggestions);
        val results = new ArrayList<AutocompleteSuggestion>();
        for (TrieNode<Question> node: suggestions) {
            AutocompleteSuggestion suggestion = new AutocompleteSuggestion(node);
            if (node.isLeaf()) {
                suggestion.align(getSparqlResourceLabel(node.getData() != null ? node.getData().getSparql() : null));
            } else {
                suggestion.align(sparqlResourceLabels);
            }
            results.add(suggestion);
        }
        return results;
    }

    private String getSparqlResourceLabel(String sparql) {
        if (sparql == null)
            return null;
        if (!sparql.contains("<http://dbpedia.org/resource/")) {
            return null;
        }
        sparql = sparql.substring(sparql.indexOf("<http://dbpedia.org/resource/") + "<http://dbpedia.org/resource/".length());
        if (!sparql.contains(">")) {
            return null;
        }
        sparql = sparql.substring(0, sparql.indexOf(">"));
        try {
            sparql = URLDecoder.decode(sparql, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {}
        sparql = sparql.replace("_", " ").trim();

        if (sparql.length() == 0) return null;
        return sparql;
    }

    private List<String> gatherSparqlResourceLabels(List<TrieNode<Question>> suggestions) {
        List<String> labels = new ArrayList<>();

        for (TrieNode<Question> node: suggestions) {
            Question data = node.getData();
            if (node.getData() == null)
                continue;
            String label = getSparqlResourceLabel(data.getSparql());
            if (label != null) {
                labels.add(label);
            }
        }

        return labels;
    }

    public List<TrieNode<Question>> autocomplete(String query, int topN, int maxDepth) {
        final List<TrieNode<Question>> suggestions = new ArrayList<>();
        TrieNode<Question> cur = this.trie.getRoot().find(query, true);

        this.gatherResults(query, suggestions, cur, topN, maxDepth, 0, false, false);
        boolean extendedBySpace = false;
        if (suggestions.size() == 1 && !query.endsWith(" ")) {
            extendedBySpace = true;
            this.gatherResults(query + " ", suggestions, cur, topN, maxDepth, 0, false, false);
        }

        if (suggestions.size() <= 1) {
            this.gatherResults(query, suggestions, cur, topN, maxDepth, 0, false, true);
            if (extendedBySpace) {
                this.gatherResults(query + " ", suggestions, cur, topN, maxDepth, 0, false, true);
            }
        }

        return suggestions;
    }

    private void gatherResults(String query, List<TrieNode<Question>> suggestions, TrieNode<Question> cur, int topN, int maxDepth, int curDepth, boolean skipcur, boolean leafs) {
        if (curDepth >= maxDepth) {
            return;
        }
        if (suggestions.size() >= topN) {
            return;
        }

        val addedPrefixes = new HashSet<String>();
        for (TrieNode<Question> suggestion: suggestions) {
            if (suggestion.isLeaf()) {
                continue;
            }
            if (suggestion.fullPath().length() > query.length()) {
                addedPrefixes.add(suggestion.fullPath());
            }
        }

        val isRootQuery = "".equals(query);

        if (!skipcur && !cur.isRoot() && !"".equals(cur.fullPath())) {
            if (cur.fullPath().endsWith(" ") || (cur.isLeaf() && !isRootQuery)) {
                addSuggestion(addedPrefixes, suggestions, cur, topN, query, leafs);
            }
        }

        if (cur.getChildren() == null) {
            return;
        }

        for (TrieNode<Question> child: cur.getChildren()) {
            if (child.fullPath().endsWith(" ") || (child.isLeaf() && !isRootQuery)) {
                addSuggestion(addedPrefixes, suggestions, child, topN, query, leafs);
            }
        }

        for (TrieNode<Question> child: cur.getChildren()) {
            gatherResults(query, suggestions, child, topN, maxDepth, curDepth+2, true, leafs);
        }

    }

    private void addSuggestion(HashSet<String> addedPrefixes, List<TrieNode<Question>> suggestions, TrieNode<Question> cur, int topN, String query, boolean leafs) {
        if (suggestions.size() >= topN) { return; }
        if (!cur.fullPath().toLowerCase().startsWith(query.toLowerCase())) { return; }

        for (TrieNode<Question> other: suggestions) {
            if (other.equals(cur)) {
                return;
            }
            if (!leafs) {
                String oPath = other.fullPath();
                String cPath = cur.fullPath();
                if (cPath.length() > oPath.length() && cPath.startsWith(oPath)) {
                    return;
                }
            } else {
                if(other.fullPath().equals(cur.fullPath())) {
                    return;
                }
                // check if this leaf is covered by a non-leaf node that was already suggested
                for (String prefix: addedPrefixes) {
                    if (cur.fullPath().startsWith(prefix)) {
                        return;
                    }
                }
            }
        }

        suggestions.add(cur);
    }
}
