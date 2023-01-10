package edu.citec.sc.queggweb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderHeaderAware;
import static edu.citec.sc.queggweb.constants.Constants.ANSWER_FIELD;
import static edu.citec.sc.queggweb.constants.Constants.QUESTION_FIELD;
import static edu.citec.sc.queggweb.constants.Constants.SPARQL_FIELD;
import edu.citec.sc.queggweb.lucene.ReadIndex;
import edu.citec.sc.queggweb.views.AutocompleteSuggestion;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import static java.lang.System.exit;
import java.net.URLDecoder;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.*;
import org.apache.commons.io.IOUtils;

@Component("questions")
@Scope("singleton")
public class QuestionLoader {
    private static final String CACHE_FILENAME = loadCacheFilename();
    //private static final String InputDir =  "/tmp/resources/";

    @Autowired
    public EndpointConfiguration endpoint;

    private static String loadCacheFilename() {
        String envValue = System.getenv("QUEGG_TRIECACHE");
        if (envValue != null && !envValue.equals("")) {
            return envValue;
        }

        // default value
        return "/tmp/trie.cache";
       //return InputDir+"trie.cache";
    }
    
     private static String inputDir() {
        // default value
        //return "/tmp/trie.cache";
       return "/tmp/resources/";
    }


    private final int MAX_CHILD_SAMPLES = 200;
    private int loaded = 0;

    @Synchronized
    protected int incLoaded() {
        this.loaded++;
        return this.loaded;
    }

    @Getter
    Trie<Question> trie = new Trie<>();

    private class InsertTask implements Runnable {

        private final Trie<Question> trie;
        private final Question q;

        public InsertTask(Trie<Question> trie, Question q) {
            this.trie = trie;
            this.q = q;
        }

        @Override
        public void run() {
            try {
                int sizeBefore = trie.size();
                trie.insert(q.getQuestion(), q);
            } catch (TrieNode.DuplicateInsertException e) {
                System.err.println(e.getMessage());
            }

            if (incLoaded() % 10000 == 0) {
                System.out.println("loaded: " + loaded + " trie size: " + trie.size());
            }
        }
    }

    private void loadFromInputStream(final String streamName, InputStream is) throws IOException {
        assert is != null;

        int coreCount = Runtime.getRuntime().availableProcessors();
        int threadCount = coreCount > 2 ? coreCount - 2 : 2;
        
        String InputDir=inputDir();
         System.out.println(InputDir);

        File files = new File(InputDir);
        

        for (String fileName : files.list()) {
            if(!fileName.contains(".csv"))
                continue;
            
            
             File file=new File(fileName);
            CSVReaderHeaderAware reader = null;
            try {
                //File file = new File("src/main/resources/questions.csv");
                InputStream fileStream = new FileInputStream(file);
                val in = new BufferedReader(new InputStreamReader(fileStream));

                reader = new CSVReaderHeaderAware(in);
            } catch (Exception npe) {
                System.err.println("no header found, skipping stream " + streamName);
                return;
            }

            System.err.println("Starting ingest with " + threadCount + " threads");
            ExecutorService ingestPool = Executors.newFixedThreadPool(threadCount);

            Map<String, String> row;
            Integer index=0;
            while ((row = reader.readMap()) != null) {
                index=index+1;
                String question = row.get("question");
                question = question.replace("\"", "");

                if (question == null) {
                    throw new RuntimeException("failed to parse " + streamName + ": question is null or header malformed/missing");
                }

                if (question.trim().startsWith("SELECT ") && question.trim().endsWith("}")) {
                    System.err.println("skipping malformed question: " + question);
                    continue;
                }

                String sparql = row.get("sparql");
                sparql = sparql.replace("\"", "");
                String answer = row.get("answer");
                answer = answer.replace("\"", "");
                
                System.out.println(index+" "+question+" "+answer);
                System.err.println("inserting question (" + row.getOrDefault("id", question) + ")");
                Question q = new Question(question, sparql, answer,answer,"single");

                InsertTask task = new InsertTask(trie, q);
                ingestPool.submit(task);
            }

            ingestPool.shutdown();
            System.out.println("waiting for ingest service to complete");

            try {
                ingestPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.err.println("ingestion complete (" + threadCount + " threads terminated)");
            trie.store(CACHE_FILENAME);
        }
    }

    /*private void loadFromCSV() throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("data/questions.csv")) {
            loadFromInputStream("resource-stream", is);
            trie.dump(true);
        }
    }*/

    public QuestionLoader() throws IOException {
        /*loadFromCache();
        if (trie.size() == 0) {
            loadFromCSV();
        }

        loadExternalCSVs();*/
    }

    private int loadExternalCSVs() throws IOException {
        return loadExternalCSVs("/tmp", "glob:/tmp/question*.csv");
    }

    public int loadExternalCSVs(String baseDirectory, String globPattern) throws IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);

        int prevSize = trie.size();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(baseDirectory))) {
            for (Path child : ds) {
                if (!Files.isRegularFile(child)) {
                    continue;
                }
                if (matcher.matches(child)) {
                    try (InputStream is = Files.newInputStream(child, StandardOpenOption.READ)) {
                        System.err.println("loading " + child + ", trie size: " + trie.size());
                        loadFromInputStream(child.getFileName().toString(), is);
                        System.err.println("new trie size: " + trie.size());
                        trie.store(CACHE_FILENAME);
                    }
                }
            }
        }

        return trie.size() - prevSize;
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

    /*public List<AutocompleteSuggestion> suggestionsToResultsT(List<TrieNode<Question>> suggestions) {
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
    }*/
    
     public List<AutocompleteSuggestion> suggestionsToResults(List<Question> suggestions, String query) {
        List<AutocompleteSuggestion> results = new ArrayList<AutocompleteSuggestion>();
        List<AutocompleteSuggestion> update = new ArrayList<AutocompleteSuggestion>();
      
        AutocompleteSuggestion firstAutocompleteSuggestion = null;
        for (Question question : suggestions) {
            if (query.equals(question.getQuestion())) {
                firstAutocompleteSuggestion = new AutocompleteSuggestion(question);
                firstAutocompleteSuggestion.setLeaf(true);      
                results.add(firstAutocompleteSuggestion);
            } else {
                AutocompleteSuggestion autocompleteSuggestion = new AutocompleteSuggestion(question);
                 results.add(autocompleteSuggestion);
            }
        }
        if(firstAutocompleteSuggestion!=null)
           update.add(firstAutocompleteSuggestion);
        for (AutocompleteSuggestion autocompleteSuggestion : results) {
                update.add(autocompleteSuggestion);
        }
        
        
        return update;
    }

    private String getSparqlResourceLabel(String sparql) {
        if (sparql == null)
            return null;
        final String rprefix = endpoint.getResourcePrefix();

        if (!sparql.contains(rprefix)) {
            return null;
        }
        sparql = sparql.substring(sparql.indexOf(rprefix) + rprefix.length());

        if (!sparql.contains(endpoint.getResourceSuffix())) {
            return null;
        }
        sparql = sparql.substring(0, sparql.indexOf(endpoint.getResourceSuffix()));
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
    
    public List<Question> autocomplete(String INDEX_DIR,String query, int topN) throws Exception {
        System.out.println("read index:::"+query);
        Map<String,Question> results= ReadIndex.readIndex(INDEX_DIR, query, topN);
         List<Question> suggestions=new ArrayList<Question>();
         
        for (String questionT : results.keySet()) {
            Question question =results.get(questionT);
            //System.out.println("questionT::"+question.getQuestion()+" sparql::"+question.getSparql()+" answer:"+question.getAnswer());
            //String sparqlT = "select  ?o    {    <http://dbpedia.org/resource/Lower_Canada> <http://dbpedia.org/ontology/capital>  ?o    }";
            //String answerT = "Quebec City";
            //Question question = new Question(questionT, sparqlT, answerT);
            suggestions.add(question);   
        }
        return suggestions;
    }

    /*public List<TrieNode<Question>> autocomplete(String query, int topN, int maxDepth) {
        final List<TrieNode<Question>> suggestions = new ArrayList<>();

        if (this.trie == null || this.trie.getRoot() == null) {
            // no questions loaded into trie yet
            System.err.println("[warning] autocomplete failed: no data in trie");
            return suggestions;
        }

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
    }*/

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

        List<TrieNode<Question>> children = cur.getChildren();
        if (children.size() > MAX_CHILD_SAMPLES) {
            children = cur.sampleChildren(MAX_CHILD_SAMPLES);
        }
        for (TrieNode<Question> child: children) {
            if (child.fullPath().endsWith(" ") || (child.isLeaf() && !isRootQuery)) {
                addSuggestion(addedPrefixes, suggestions, child, topN, query, leafs);
            }
        }

        for (TrieNode<Question> child: children) {
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
