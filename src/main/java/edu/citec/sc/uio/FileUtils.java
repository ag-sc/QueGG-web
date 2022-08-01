/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.citec.sc.uio;

import edu.citec.sc.queggweb.turtle.PropertyManagement;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elahi
 */
public class FileUtils {

    public static Set<String> getFiles(String propertyInputDir) {
        Set<String> properties = new TreeSet<String>();
        File file = new File(propertyInputDir);
        String[] propertyFiles = file.list();

        for (String propertyFile : propertyFiles) {
            if (propertyFile.contains(".ttl")) {
                properties.add(propertyFile);
            }
        }
        return properties;

    }

    public static void stringToFile(String content, String fileName)
            throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(content);
        writer.close();

    }

    public static String fileToString(String fileName) {
        InputStream is;
        String fileAsString = null;
        try {
            is = new FileInputStream(fileName);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            fileAsString = sb.toString();
            //System.out.println("Contents : " + fileAsString);
        } catch (Exception ex) {
            Logger.getLogger(PropertyManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return fileAsString;
    }

    public static Set<String> getSetFromFiles(String propertyFile) {
        Set<String> results = new TreeSet<String>();
        BufferedReader reader;
        String line = "";
        File file = new File(propertyFile);

        try {
            reader = new BufferedReader(new FileReader(propertyFile));
            while ((line = reader.readLine()) != null) {
                line = reader.readLine();
                results.add(line);
            }

            reader.close();

        } catch (Exception ex) {
            Logger.getLogger(PropertyManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;
    }
    
     public static Set<String> getProperties(String propertyFile) {
        Set<String> results = new TreeSet<String>();

        Path path = Paths.get(propertyFile);

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.contains("=")) {
                    String[] info = line.split("=");
                    String key = Matcher.cleanPrefix(info[0]);
                    results.add(key);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }
    
    public static Map<String, String> getHashFromFiles(String propertyFile) {
        Map<String, String> results = new TreeMap<String, String>();
        BufferedReader reader;
        String line = "";
        File file = new File(propertyFile);

        try {
            reader = new BufferedReader(new FileReader(propertyFile));
            while ((line = reader.readLine()) != null) {
                line = reader.readLine();
                 if (line!=null) {
                     ;
                 }
                 else 
                     continue;
                if (line.contains("=")) {
                    String[] info = line.split("=");
                    results.put(info[0], info[1]);
                }

            }

            reader.close();

        } catch (Exception ex) {
            Logger.getLogger(PropertyManagement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;
    }
    
    public static <T> Set<T> findCommonElements(List<T> first, List<T> second) {
        Set<T> common = new HashSet<>(first);
        common.retainAll(second);
        return common;
    }

    public static Set<String> getHashFromFile(String propertyFile) {
         Set<String> results = new TreeSet<String>();

        Path path = Paths.get(propertyFile);

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.contains("=")) {
                    String[] info = line.split("=");
                    String key = Matcher.cleanPrefix(info[0]);
                    results.add(key);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }
    
    public static Set<String> getSetFromFile(String propertyFile) {
        Set<String> results = new TreeSet<String>();

        Path path = Paths.get(propertyFile);

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.strip().stripLeading().stripTrailing().trim();
                line = Matcher.cleanPrefix(line);
                results.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }


}
