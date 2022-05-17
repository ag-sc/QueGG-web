package edu.citec.sc.uio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;




public class Statistics {

   
    public static long countLineOfFile(String fileName) {

        long lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;

    }

    public static void FolderFileCount(String CSV_DIR,String type) {

        File folder = new File(CSV_DIR);
        File[] listOfFiles = folder.listFiles();
        long sum = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (file.getName().contains(type)) {
                    long lines = countLineOfFile(CSV_DIR + file.getName());
                    System.out.println(file.getName() + "::" + lines);
                    sum += lines;
                }
                else if(type.contains("ALL")){
                    long lines = countLineOfFile(CSV_DIR + file.getName());
                    System.out.println(file.getName() + "::" + lines);
                    sum += lines;
                }

            }

        }

        System.out.println("total::"+sum);
    }

}
