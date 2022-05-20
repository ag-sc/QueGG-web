package edu.citec.sc.uio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;




public class Statistics {

   
    public static Integer countLineOfFile(String fileName) {

        Integer lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;

    }

    public static Integer FolderFileCount(String CSV_DIR,String type) {

        File folder = new File(CSV_DIR);
        File[] listOfFiles = folder.listFiles();
        Integer sum = 0;
        System.out.println( "type::" + type);

        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (file.getName().contains(type)) {
                    Integer lines = countLineOfFile(CSV_DIR + file.getName());
                    System.out.println(file.getName() + "::" + lines);
                    sum += lines;
                }
                else if(type.contains("ALL")){
                    Integer lines = countLineOfFile(CSV_DIR + file.getName());
                    System.out.println(file.getName() + "::" + lines);
                    sum += lines;
                }

            }

        }
        System.out.println("sum::"+sum);

        return sum;
    }
    
    public static void numberQuestions(Set<String> frames, String inputDir, String outputFile,String language) {
        CsvFile csvFile = new CsvFile(new File(outputFile));
        List<String[]> rows = new ArrayList<String[]>();
        Integer sum=0;
        rows.add(new String[]{"Frame","NumOfQuestions"+"_"+language});
        
        for (String frame : frames) {
            String[] columns = new String[2];
            Integer total = FolderFileCount(inputDir, frame);
            columns[0] = frame;
            columns[1] = total.toString();
            rows.add(columns);
            sum+=total;
        }
        
        rows.add(new String[]{"total",sum.toString()});
        csvFile.writeToCSV(rows);
    }

}
