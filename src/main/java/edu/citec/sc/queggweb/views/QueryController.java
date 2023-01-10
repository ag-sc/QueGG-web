package edu.citec.sc.queggweb.views;


import edu.citec.sc.uio.BashScript;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.citec.sc.queggweb.constants.Constants;
import edu.citec.sc.uio.CsvFile;
import edu.citec.sc.queggweb.data.*;
import static edu.citec.sc.uio.BashScript.FIND_ABSTRACT;
import static edu.citec.sc.uio.BashScript.FIND_IMAGE_LINK;
import static edu.citec.sc.uio.BashScript.FIND_WIKI_LINK;
import lombok.val;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import static java.lang.System.exit;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class QueryController implements Constants{

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
        String language ="en";
        Boolean online=true;
        String INDEX_DIR = resourceDir+language+Constants.indexDir;
        
        System.out.println("INDEX_DIR::"+INDEX_DIR);
        //exit(1);
        
        List<String> menus = Stream.of(FIND_WIKI_LINK, FIND_ABSTRACT, FIND_IMAGE_LINK).collect(Collectors.toCollection(ArrayList::new));

        result.put("query", query);
        result.put("question_count", questions.size());
        
        List<AutocompleteSuggestion> autocompleteSuggestions =new ArrayList<AutocompleteSuggestion>();
        List<Question> suggestions = new ArrayList<Question>();
     
        try {
            System.out.println("search query is::" + query);
            suggestions = questions.autocomplete(INDEX_DIR,query, 20);
            autocompleteSuggestions = questions.suggestionsToResults(suggestions,query);
            result.put("results", autocompleteSuggestions);
            result.put("answer", null);
     
            //if (suggestions.size() > 0 && suggestions.get(0) != null) {
                for (AutocompleteSuggestion autocompleteSuggestion : autocompleteSuggestions) {
                    String question = autocompleteSuggestion.getText().toLowerCase().stripLeading().stripLeading().trim();
                    query = query.toLowerCase().stripLeading().stripLeading().trim();
                    String sparql = autocompleteSuggestion.getSparql();
                    String answerUri = autocompleteSuggestion.getAnswerUri();
                    String answerLabel = autocompleteSuggestion.getAnswerLabel();
                    String answerType = autocompleteSuggestion.getAnswerLabel();

                    if (autocompleteSuggestion.isLeaf()) {
                        System.out.println("query is::" + query);
                        System.out.println("question is::" + question);
                        System.out.println("answerUri is::" + answerUri);
                        //executeSparqlOffline(result, endpoint.getPrefixSparql().trim() + "\n" + sparql);
                        if (online) {
                            executeSparqlOnline(result, endpoint.getPrefixSparql().trim() + "\n" + sparql, answerUri);
                        } else {
                            executeSparqlOffline(result, endpoint.getPrefixSparql().trim() + "\n" + sparql, answerUri, answerLabel, answerType, menus);
                        }
                        query = "";

                    }

                }

           // }

            
            System.out.println("!!!!!!!!!!!!!!!!!!End!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            
           
            /*if (!"".equals(answer) && suggestions.size() > 0 && suggestions.get(0) != null) {
                Question question = suggestions.get(0);
                result.put("question", question.getQuestion());
                result.put("answer", question.getAnswer());
                result.put("sparql", question.getSparql());
                System.out.println("question::"+question.getQuestion()+" "+question.getQuestion().length());
                System.out.println("query::"+query+" "+query.length());
                System.out.println("sparql::"+question.getSparql());
                if(query.contains(question.getQuestion()))
                   executeSparql(result, endpoint.getPrefixSparql().trim() + "\n" + question.getSparql());
                //System.out.println("question::"+question.getQuestion());
                //System.out.println("answer::"+question.getAnswer());
                //System.out.println("sparql::"+question.getSparql());
                //executeSparql(result, endpoint.getPrefixSparql().trim() + "\n" + question.getSparql());
            }*/
        } catch (Exception ex) {
            Logger.getLogger(QueryController.class.getName()).log(Level.SEVERE, null, ex);
        }


        
      
        return result;
    }

    /*@GetMapping("/query")
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
        
          System.out.println("!!!!!!!!!!!!!!!!!!Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        for (AutocompleteSuggestion autocompleteSuggestion:results){
          
             System.out.println("autocompleteSuggestion.getText()::"+autocompleteSuggestion.getText());
             System.out.println("autocompleteSuggestion.getSparql()::"+autocompleteSuggestion.getSparql());
             System.out.println("autocompleteSuggestion.getSize()::"+autocompleteSuggestion.getSize());
             System.out.println();
        }
        System.out.println("!!!!!!!!!!!!!!!!!!End!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");


        if (!"".equals(answer) && suggestions.size() > 0 && suggestions.get(0).getData() != null) {
            Question question = suggestions.get(0).getData();
            result.put("question", question.getQuestion());
            result.put("answer", question.getAnswer());
            result.put("sparql", question.getSparql());
            //System.out.println("question::"+question.getQuestion());
            //System.out.println("answer::"+question.getAnswer());
            //System.out.println("sparql::"+question.getSparql());
            executeSparql(result, endpoint.getPrefixSparql().trim() + "\n" + question.getSparql());
        }

        return result;
    }*/

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

 

    private Map<String, String> resourceSparql(String resource) {
        System.out.println("resource:::::::::"+resource);
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
    
    private void executeSparqlOffline(Map<String, Object> result, String sparql, String answerUri, String answerLabel, String answerType, List<String> menus) {
        String abstractfile = "../resources/en/turtle/short_abstracts_sorted_en.ttl";
        //String wikiLinkFile = "../resources/en/turtle/wikipedia_links_en_filter.ttl";
        //String imagefileName = "../resources/en/turtle/wikipedia_links_en_filter.ttl";

        List<Map<String, String>> rsmap = new ArrayList<Map<String, String>>();
        Map<String, String> sparqlEndpointOutput = new TreeMap<String, String>();
        //String lastPart = getLastPartOfURI(answerUri);
        //String wikiLink = this.getWikiLink(answerUri);
        //String abstractText = this.getAbstract(answerUri);

        //old Wikipedia link finding
        //BashScript bashScript = new BashScript(menus, wikiLinkFile, abstractfile, imagefileName, answerUri);
        //Wikipedia link finding
        BashScript bashScript = new BashScript(menus, abstractfile, answerUri);

        
        
        //if (answerType.contains("single")) {
        //sparqlEndpointOutput.put("ethumbnail", "http://commons.wikimedia.org/wiki/Special:FilePath/Watermolen_van_de_polder_De_Dellen,_overzicht_-_Waar,_'t_-_20248138_-_RCE.jpg?width=300");
        sparqlEndpointOutput.put("result_type", "resource_meta");
        sparqlEndpointOutput.put("elabel", answerLabel + "@en");
        sparqlEndpointOutput.put("etype", "Thing");
        sparqlEndpointOutput.put("elink", bashScript.getWikiLink());
        sparqlEndpointOutput.put("eabstract", bashScript.getAbstractText() + "@en");
        rsmap.add(sparqlEndpointOutput);
        //}

        /*val cached = answerCache.getIfPresent(sparql);
        if (cached != null) {
            for (String k : cached.keySet()) {
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
            ((QueryEngineHTTP) qexec).addParam("timeout", "10000");

            // Execute.
            ResultSet rs = qexec.execSelect();
            // ResultSetFormatter.out(System.out, rs, query);

            rsmap = resultSetToMap(resolveResources, rs);

            String sparqlResource = extractResource(sparql);
            //String sparqlResource =answerUri;
            System.out.println("sparqlResource::" + sparqlResource);

            if (sparqlResource != null) {
                Map<String, String> resourceMeta = resourceSparql(sparqlResource);
                System.out.println("resourceMeta::" + resourceMeta);
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

        for (String resource : resolveResources) {
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
         */
        for (Map<String, String> testMap : rsmap) {
            for (String key : testMap.keySet()) {
                String value = testMap.get(key);
                System.out.println("key::" + key);
                System.out.println("value::" + value);
            }
        }

        result.put("sparql-result", rsmap);

        answerCache.put(sparql, result);
    }

    private String getLastPartOfURI(String answerUri) {
        URI uri;String lastPart=null;
        try {
            uri = new URI(answerUri);
            String path = uri.getPath();
            lastPart = path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException ex) {
            System.out.println("invalid URI::" + ex.getMessage());
            Logger.getLogger(QueryController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastPart;
    }

     private void executeSparqlOnline(Map<String, Object> result, String sparql,String answerUri) {
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
            //String sparqlResource =answerUri;
            //System.out.println("sparqlResource::"+sparqlResource);

            if (sparqlResource != null) {
                Map<String, String> resourceMeta = resourceSparql(sparqlResource);
                //System.out.println("resourceMeta::"+resourceMeta);
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
        /*if(rsmap.isEmpty()){
             System.out.println("rsmap::"+rsmap);
             System.out.println("sparql::"+sparql);
             System.out.println("answerUri::"+answerUri);
            exit(1);
        }*/
        //System.out.println("rsmap::!!!!!!!!!!!!!!!!!!!!!!!"+rsmap);  
         /*if(result.isEmpty()){
              System.out.println("result::!!!!!!!!!!!!!!!!!!!!!!!"+result);  
              exit(1);
         }*/
        for(String key:result.keySet()){
            Object obje=result.get(key);
           System.out.println("key::"+key);
           System.out.println("Object::"+obje);

        }
       
        answerCache.put(sparql, result);
    }

    /*private void executeSparql(Map<String, Object> result, String sparql) {
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
    }*/

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

    private String getWikiLink(String answerUri) {
        String lastPart=this.getLastPartOfURI(answerUri);
        return "http://en.wikipedia.org/wiki/"+lastPart;
    }

    private String getAbstract(String answerUri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
