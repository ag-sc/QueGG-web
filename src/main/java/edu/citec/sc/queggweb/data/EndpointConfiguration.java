package edu.citec.sc.queggweb.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Data
@Component("endpoint")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndpointConfiguration {

    @Getter @Setter
    private String endpoint = "https://dbpedia.org/sparql";

    @Getter @Setter
    private String language = "en";

    @Getter @Setter
    private String resourcePrefix = "<http://dbpedia.org/resource/";

    @Getter @Setter
    private String resourceSuffix = ">";

    @Getter @Setter @JsonProperty("resourceQuery")
    private String resourceQuery =  "SELECT ?etype ?elabel ?elink ?eabstract ?ethumbnail WHERE {\n" +
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

    @Getter @Setter @JsonProperty("prefix")
    private Map<String, String> prefixes = new HashMap<>();

    public void addPrefix(String prefix, String full) {
        if (!prefix.endsWith(":") && !prefix.endsWith("#")) {
            prefix = prefix + ":";
        }
        this.prefixes.put(prefix, full);
    }

    private void addDefaultPrefixes() {
        this.addPrefix("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.addPrefix("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
        this.addPrefix("dbr:", "http://dbpedia.org/resource/");
        this.addPrefix("dbo:", "http://dbpedia.org/ontology/");
        this.addPrefix("owl:", "http://www.w3.org/2002/07/owl/");
        this.addPrefix("xsd:", "http://www.w3.org/2001/XMLSchema/");
    }

    private EndpointConfiguration() {
    }

    @PostConstruct
    public void init() {
        addDefaultPrefixes();
        loadDefault();
        loadFromFile();
        saveToFile();
    }

    public void saveToFile() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);;
        final File target = configFileLocation();

        try {
            mapper.writeValue(target, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDefault() {
        final String resourceName = "data/default_configuration.json";

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            ObjectMapper mapper = new ObjectMapper();
            EndpointConfiguration loadedState = mapper.readValue(is, EndpointConfiguration.class);
            this.merge(loadedState);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[error] failed to load default configuration from resource <" + resourceName + ">: " +
                                e.getMessage());
        }
    }

    public File configFileLocation() {
        String envValue = System.getenv("QUEGG_CONFIG");
        if (envValue != null && !envValue.equals("")) {
            return new File(envValue);
        }

        // default value
        return new File("/tmp/quegg_config.json");
    }

    public void loadFromFile(File target) {
        if (target == null || !target.exists()) {
            System.err.println("[config] skipping configuration load for <" + target.getAbsolutePath() + ">: not found");
            return;
        }

        System.err.println("[config] load " + target);
        try (FileInputStream fis = new FileInputStream(target)) {
            ObjectMapper mapper = new ObjectMapper();
            EndpointConfiguration loadedState = mapper.readValue(fis, EndpointConfiguration.class);
            this.merge(loadedState);
        } catch (FileNotFoundException e) {
            // should not trigger since we make this explicit above
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[error] failed to load configuration from file <" + target.getAbsolutePath() + ">:" +
                    e.getMessage());
        }
    }

    public void loadFromFile() {
        final File target = configFileLocation();
        loadFromFile(target);
    }

    private void merge(EndpointConfiguration o) {
        if (o.getEndpoint() != null && !"".equals(o.getEndpoint())) {
            this.setEndpoint(o.getEndpoint());
        }
        if (o.getLanguage() != null && !"".equals(o.getLanguage())) {
            this.setLanguage(o.getLanguage());
        }
        if (o.getResourcePrefix() != null) {
            this.setResourcePrefix(o.getResourcePrefix());
        }
        if (o.getResourceSuffix() != null) {
            this.setResourceSuffix(o.getResourceSuffix());
        }
        if (o.getResourceQuery() != null) {
            this.setResourceQuery(o.getResourceQuery());
        }
        if (o.getPrefixes() != null && !o.getPrefixes().isEmpty()) {
            for (String prefix: o.getPrefixes().keySet()) {
                String full = o.getPrefixes().get(prefix);
                if (prefix == null || full == null || "".equals(full)) {
                    continue;
                }
                this.addPrefix(prefix, full);
            }
        }
    }

    @JsonIgnore
    public String getPrefixSparql() {
        if (this.getPrefixes() == null) {
            return "";
        }
        String sparql = "";
        for (String prefix: this.getPrefixes().keySet()) {
            final String full = this.getPrefixes().get(prefix);
            if (full == null) continue;
            sparql += "PREFIX     " + prefix + " <" + full + ">\n";
        }
        return sparql;
    }
    
    
}
