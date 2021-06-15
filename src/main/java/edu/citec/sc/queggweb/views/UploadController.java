package edu.citec.sc.queggweb.views;

import edu.citec.sc.queggweb.data.QuestionLoader;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Locale;

@Controller
public class UploadController {

    @Autowired
    private QuestionLoader questions;

    private boolean uploadsAllowed() {
        String envFlag = System.getenv().getOrDefault("QUEGG_ALLOW_UPLOADS", "false");
        if (envFlag == null) {
            return false;
        }
        envFlag = envFlag.toLowerCase();

        if ("1".equals(envFlag) || "true".equals(envFlag)) {
            return true;
        }
        return false;

    }

    @PostMapping("/import")
    public ResponseEntity<String> handleFileUpload(@RequestParam(value = "file", required = true) MultipartFile file,
                                                   @RequestParam(required=false, defaultValue = "en") String lang,
                                                   @RequestParam(required=false, defaultValue = "10") Integer maxBindingCount,
                                                   @RequestParam(required=false, defaultValue = "nouns") String targetType,
                                                   @RequestParam(required=false, defaultValue = "CSV") String inputFormat) {
        if (!uploadsAllowed()) {
            System.err.println("Upload received but QUEGG_ALLOW_UPLOADS environment variable is not set to 'true'");

            return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        }

        lang = lang.toUpperCase();
        inputFormat = inputFormat.toUpperCase();

        if (!"CSV".equals(inputFormat) && !"TTL".equals(inputFormat)) {
            System.err.println("Upload received but parameter 'inputFormat' contained an invalid value");
            return new ResponseEntity<>("parameter 'inputFormat' can only be 'CSV' or 'TTL'", HttpStatus.BAD_REQUEST);
        }
        if (!"DE".equals(lang) && !"EN".equals(lang)) {
            System.err.println("Upload received but parameter 'lang' contained an invalid value");
            return new ResponseEntity<>("parameter 'lang' can only be 'EN' or 'DE'", HttpStatus.BAD_REQUEST);
        }
        if (!"nouns".equals(targetType) && !"adjectives".equals(targetType) && !"verbs".equals(targetType)) {
            System.err.println("Upload received but parameter 'targetType' contained an invalid value");
            return new ResponseEntity<>("parameter 'targetType' can only be 'nouns', 'verbs', or 'adjectives'", HttpStatus.BAD_REQUEST);
        }

        System.err.println("Starting import conversion, file length: " + file.getSize() +
                " bytes, language: " + lang);

        new File("/tmp/import/" + targetType).mkdirs();
        new File("/tmp/generatorout").mkdirs();

        String tmpFilename = "/tmp/import/" + targetType + "/import." + ("CSV".equals(inputFormat) ? "csv" : "ttl");
        File tmpFile = new File(tmpFilename);

        String responseStatus = "";

        try (OutputStream os = new FileOutputStream(tmpFile)) {
            os.write(file.getBytes());
            responseStatus = "written " + file.getSize() + " bytes\n";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        responseStatus += "upload ok\n";

        String generatorCommand = "java -jar /app/generator.jar " + lang + " /tmp/import /tmp/generatorout " + maxBindingCount.toString() + " " + inputFormat.toLowerCase();
        System.err.println("[info] invoking command line: " + generatorCommand);

        Runtime run = Runtime.getRuntime();
        Process pr = null;
        try {
            pr = run.exec(generatorCommand);

            int exitCode = pr.waitFor();
            responseStatus += "generator exit code: " + exitCode + "\n";

            responseStatus += "# STDOUT:\n";
            responseStatus += this.readStream(pr.getInputStream()) + "\n";
            responseStatus += "# STDERR:\n";
            responseStatus += this.readStream(pr.getErrorStream()) + "\n";

            System.err.println("[info] starting import of generator results");
            int added = questions.loadExternalCSVs("/tmp/generatorout",
                    "glob:/tmp/generatorout/questions*.csv");
            responseStatus += "# TRIE:\n";
            responseStatus += Integer.toString(added) + " added trie entries\n";
            responseStatus += "new size:" + questions.getTrie().size() + "\n";

            return new ResponseEntity<>(responseStatus,
                    HttpStatus.OK);
        } catch (IOException | InterruptedException e) {
            responseStatus += e.getMessage() + "\n";
            return new ResponseEntity<>(responseStatus,
                    HttpStatus.BAD_REQUEST);
        }

    }

    private String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream))) {

            String result = "";
            String line = "";
            while ((line = buf.readLine()) != null) {
                result += line.strip() + "\n";
            }
            return result;
        }
    }
}
