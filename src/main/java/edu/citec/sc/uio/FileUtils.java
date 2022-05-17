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
import java.util.Set;
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

}
