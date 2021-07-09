package edu.citec.sc.queggweb.views;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.citec.sc.queggweb.data.*;
import lombok.val;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class QueryController {

    private Cache<String, Map<String, String>> resourceCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .build();
    private Cache<String, Map<String, Object>> answerCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .build();

    @Autowired
    private QuestionLoader questions;

    @Autowired
    private EndpointConfiguration endpoint;

    @GetMapping("/resource")
    public Map<String, String> resolveResource(@RequestParam(name="r") String resourceName, Model model) {
        return resourceSparql(resourceName);
    }

    @GetMapping("/query")
    public Map<String, Object> query(@RequestParam(name="q", required=false, defaultValue="") String query,
                                     @RequestParam(name="answer", required=false, defaultValue="") String answer,
                                     Model model) {
        Map<String, Object> result = new HashMap<>();

        result.put("query", query);
        result.put("question_count", questions.size());

        val suggestions = questions.autocomplete(query,20, 4);
        val results = questions.suggestionsToResults(suggestions);

        result.put("results", results);
        result.put("answer", null);

        if (suggestions.size() > 0 && null != suggestions.get(0).getData()) {
            answer = "true";
        }

        if (!"".equals(answer) && suggestions.size() > 0 && suggestions.get(0).getData() != null) {
            Question question = suggestions.get(0).getData();
            result.put("question", question.getQuestion());
            result.put("answer", question.getAnswer());
            result.put("sparql", question.getSparql());
            executeSparql(result, endpoint.getPrefixSparql().trim() + "\n" + question.getSparql());
        }

        return result;
    }

    private List<String> gatherSparqlResourceLabels(List<TrieNode<Question>> suggestions) {
        List<String> labels = new ArrayList<>();

        for (TrieNode<Question> node: suggestions) {
            Question data = node.getData();
            if (node.getData() == null)
                continue;

            String query = endpoint.getPrefixSparql().trim() + "\n" + data.getSparql();

            for (String line: query.split("\n")) {
                line = line.trim();
                if (line.toUpperCase().startsWith("PREFIX ")) {
                    continue;
                }
                if (line == null)
                    continue;
                if (!line.contains(endpoint.getResourcePrefix())) {
                    continue;
                }
                line = line.substring(line.indexOf(endpoint.getResourcePrefix()) + endpoint.getResourcePrefix().length());
                if (!line.contains(endpoint.getResourceSuffix())) {
                    continue;
                }
                line = line.substring(0, line.indexOf(endpoint.getResourceSuffix()));
                try {
                    line = URLDecoder.decode(line, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {}
                line = line.replace("_", " ").trim();

                if (line.length() == 0) continue;

                labels.add(line);
            }
        }

        return labels;
    }

    private void markEntities(String query, List<AutocompleteSuggestion> results) {
        val extendedSuggestions = questions.autocomplete(query,100, 5);
        val extendedResults = questions.suggestionsToResults(extendedSuggestions);

        val prefixTrie = new Trie<String>();
        val suffixTrie = new Trie<String>();
        val prefix = query.toLowerCase().trim();

        for (AutocompleteSuggestion suggestion: extendedResults) {
            val suggestionText = suggestion.getText();
            val textRemainder = suggestionText.substring(prefix.length()).strip();
            if (textRemainder.length() == 0) continue;
            StringBuilder sb = new StringBuilder(textRemainder);
            System.err.println(suggestionText + " | " + textRemainder);
            try {
                suffixTrie.insert(sb.reverse().toString(), textRemainder);
            } catch (TrieNode.DuplicateInsertException ignored) {}
            try {
                prefixTrie.insert(suggestionText, suggestionText);
            } catch (TrieNode.DuplicateInsertException ignored) {}
        }

        String shortestSuffix = null;

        TrieNodeVisitor<String> visitor = new TrieNodeVisitor<String>(suffixTrie.getRoot()) {
            @Override
            protected void visit(TrieNode<String> node) {
                if (node == null) return;
                // leaf nodes can never present a suffix
                if (!node.isLeaf()) return;

                val pathParts = new ArrayList<TrieNode<String>>();
                TrieNode<String> current = node;
                while (current != null) {
                    pathParts.add(current);
                    if (current.getParent() != null && current.getParent() != current) {
                        current = current.getParent();
                    } else {
                        current = null;
                    }
                }

                for (int i = pathParts.size() - 1; i >= 0; i--) {

                }

                System.err.println(node + " " +  pathParts.toString());
            }
        };

        System.err.println(prefixTrie);
        System.err.println(suffixTrie);

    }

    private Map<String, String> resourceSparql(String resource) {
        val cached = resourceCache.getIfPresent(resource);
        if (cached != null) {
            return cached;
        }

        String sparql = endpoint.getPrefixSparql().trim() + "\n";
        sparql += endpoint.getResourceQuery().trim();

        while (sparql.contains("%RES%")) {
            sparql = sparql.replaceAll("%RES%", resource);
        }
        Query query = QueryFactory.create(sparql);

        // Remote execution.
        try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint.getEndpoint(), query) ) {
            // set endpoint timeout.
            ((QueryEngineHTTP)qexec).addParam("timeout", "10000") ;

            // Execute.
            ResultSet rs = qexec.execSelect();
            // ResultSetFormatter.out(System.out, rs, query);
            val result = new HashMap<String, String>();

            if (rs.hasNext()) {
                QuerySolution row = rs.nextSolution();
                for (String rvar: rs.getResultVars()) {
                    val rval = row.get(rvar) != null ? row.get(rvar).toString() : null;

                    result.put(rvar, rval);
                }
            }

            result.put("result_type", "resource_meta");
            postprocessResolved(result, resource);

            resourceCache.put(resource, result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void executeSparql(Map<String, Object> result, String sparql) {
        val cached = answerCache.getIfPresent(sparql);
        if (cached != null) {
            for (String k: cached.keySet()) {
                result.put(k, cached.get(k));
            }
            return;
        }

        Query query = null;
        try {
            query = QueryFactory.create(sparql);
        } catch (QueryParseException qpe) {
            System.err.println("QueryParseException: query: " + sparql);
            qpe.printStackTrace();
            result.put("sparql-result", null);
            result.put("sparql-error", qpe.toString());
            return;
        }
        List<String> resolveResources = new ArrayList<>();
        List<Map<String, String>> rsmap = new ArrayList<>();

        // Remote execution.
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint.getEndpoint(), query)) {
            // Set the endpoint specific timeout.
            ((QueryEngineHTTP)qexec).addParam("timeout", "10000") ;

            // Execute.
            ResultSet rs = qexec.execSelect();
            // ResultSetFormatter.out(System.out, rs, query);

            rsmap = resultSetToMap(resolveResources, rs);

            String sparqlResource = extractResource(sparql);
            if (sparqlResource != null) {
                Map<String, String> resourceMeta = resourceSparql(sparqlResource);
                if (resourceMeta != null && resourceMeta.size() > 0) {
                    rsmap.add(resourceMeta);
                }
            }

            result.put("sparql-result", rsmap);
            result.put("sparql-error", null);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("sparql-result", null);
            result.put("sparql-error", e.toString());
        }

        for (String resource: resolveResources) {
            String oresource = resource;
            if (!resource.startsWith("<")) {
                resource = "<" + resource;
            }
            if (!resource.endsWith(">")) {
                resource = resource + ">";
            }
            val resolved = resourceSparql(resource);
            if (resolved != null) {
                //postprocessResolved(resolved);
                rsmap.add(0, resolved);
            } else {
                Map<String, String> tmp = new HashMap<>();
                tmp.put("o", oresource);
                rsmap.add(0, tmp);
            }
        }

        result.put("sparql-result", rsmap);

        answerCache.put(sparql, result);
    }

    private void postprocessResolved(Map<String, String> resolved, String oresource) {
        if(resolved.getOrDefault("elabel", null) == null) {
            String elabel = oresource;
            for (String prefix: endpoint.getPrefixes().keySet()) {
                String longform = endpoint.getPrefixes().get(prefix);
                elabel = elabel.replace(longform, prefix);
            }
            elabel = elabel.replace("_", " ");
            elabel = elabel.trim();
            if (elabel.startsWith("<")) {
                elabel = elabel.substring(1);
            }
            if (elabel.endsWith(">")) {
                elabel = elabel.substring(0, elabel.length() - 1);
            }
            resolved.put("elabel", elabel);
        }

        if (resolved.containsKey("etype") && resolved.get("etype") != null) {
            String etype = resolved.get("etype");
            for (String prefix: endpoint.getPrefixes().keySet()) {
                String longform = endpoint.getPrefixes().get(prefix);
                etype = etype.replace(longform, prefix);
            }

            if (etype.contains("#")) {
                val parts = etype.split("#");
                etype = parts[parts.length - 1];
            }
            if (etype.contains("/")) {
                val parts = etype.split("/");
                etype = parts[parts.length - 1];
            }
            if ("owl:Thing".equals(etype)) {
                etype = null;
            }
            resolved.put("etype", etype);
        }
    }

    private String extractResource(String query) {
        for (String line: query.split("\n")) {
            line = line.trim();
            if ("".equals(line) || line.toUpperCase().startsWith("PREFIX ")) {
                continue;
            }
            if (!line.contains(endpoint.getResourcePrefix())) {
                return null;
            }
            line = line.substring(line.indexOf(endpoint.getResourcePrefix()));
            if (!line.contains(endpoint.getResourceSuffix())) {
                return null;
            }
            String resource = line.substring(0, line.indexOf(endpoint.getResourceSuffix()) + 1);
            if ("".equals(resource)) {
                return null;
            }
            return resource;
        }
        return null;
    }

    private List<Map<String, String>> resultSetToMap(List<String> resolveResources, ResultSet rs) {
        val result = new ArrayList<Map<String, String>>();

        for (; rs.hasNext();) {
            boolean rowToResolve = false;
            Map<String, String> resultRow = new HashMap<>();
            QuerySolution row = rs.nextSolution();
            for (String rvar: rs.getResultVars()) {
                val rval = row.get(rvar).toString();
                String rprefix = endpoint.getResourcePrefix();
                if (rprefix.startsWith("<")) {
                    rprefix = rprefix.substring(1);
                }
                if (rval.contains(rprefix)) {
                    resolveResources.add(rval);
                    rowToResolve = true;
                }
                resultRow.put(rvar, rval);
            }
            if (!rowToResolve) {
                result.add(resultRow);
            }
        }

        return result;
    }
}
