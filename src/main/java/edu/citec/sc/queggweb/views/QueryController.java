package edu.citec.sc.queggweb.views;

import edu.citec.sc.queggweb.data.Question;
import edu.citec.sc.queggweb.data.QuestionLoader;
import lombok.val;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class QueryController {

    @Autowired
    private QuestionLoader questions;

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
        List<Map<String, String>> results = new ArrayList<>();

        val suggestions = questions.autocomplete(query, results, 20, 5);

        result.put("results", results);
        result.put("answer", null);

        if (suggestions.size() == 1 && !"".equals(answer)) {
            Question question = suggestions.get(0).getData();
            result.put("question", question.getQuestion());
            result.put("answer", question.getAnswer());
            result.put("sparql", question.getSparql());
            executeSparql(result, question.getSparql());

        }

        return result;
    }

    private Map<String, String> resourceSparql(String resource) {
        String sparql = "PREFIX      rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX     rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?etype ?elabel ?elink ?eabstract ?ethumbnail WHERE {\n" +
                "    OPTIONAL{ %RES% rdf:type ?etype .}\n" +
                "    OPTIONAL{ %RES% rdfs:label ?elabel . " +
                "           FILTER (lang(?elabel) = 'en') }\n" +
                "    OPTIONAL{ %RES% <http://xmlns.com/foaf/0.1/isPrimaryTopicOf> ?elink . }\n" +
                "    OPTIONAL{ %RES% <http://dbpedia.org/ontology/abstract> ?eabstract . \n" +
                "            FILTER (lang(?eabstract) = 'en')\n" +
                "    }\n" +
                "    OPTIONAL{ %RES% <http://dbpedia.org/ontology/thumbnail> ?ethumbnail . }\n" +
                "    \n" +
                "} LIMIT 1\n";
        while (sparql.contains("%RES%")) {
            sparql = sparql.replaceAll("%RES%", resource);
        }
        Query query = QueryFactory.create(sparql);

        // Remote execution.
        try ( QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query) ) {
            // Set the DBpedia specific timeout.
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
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void executeSparql(Map<String, Object> result, String sparql) {
        Query query = QueryFactory.create(sparql);
        List<String> resolveResources =new ArrayList<>();
        List<Map<String, String>> rsmap = new ArrayList<>();

        // Remote execution.
        try ( QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query) ) {
            // Set the DBpedia specific timeout.
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
            result.put("sparql-error", null);
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
                rsmap.add(0, resolved);
            } else {
                Map<String, String> tmp = new HashMap<>();
                tmp.put("o", oresource);
                rsmap.add(0, tmp);
            }
        }

    }

    private String extractResource(String sparql) {
        if (!sparql.contains("<http://dbpedia.org/resource/")) {
            return null;
        }
        sparql = sparql.substring(sparql.indexOf("<http://dbpedia.org/resource/"));
        if (!sparql.contains(">")) {
            return null;
        }
        return sparql.substring(0, sparql.indexOf(">") + 1);
    }

    private List<Map<String, String>> resultSetToMap(List<String> resolveResources, ResultSet rs) {
        val result = new ArrayList<Map<String, String>>();

        for (; rs.hasNext();) {
            boolean rowToResolve = false;
            Map<String, String> resultRow = new HashMap<>();
            QuerySolution row = rs.nextSolution();
            for (String rvar: rs.getResultVars()) {
                val rval = row.get(rvar).toString();
                if (rval.startsWith("http://dbpedia.org/resource/")) {
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
