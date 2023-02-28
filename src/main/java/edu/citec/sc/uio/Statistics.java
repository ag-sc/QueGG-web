package edu.citec.sc.uio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;




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
    
   /* public static void removeDuplicate(String fileName) {
        CsvFile csvFile = new CsvFile();
        List<String[]> lines = csvFile.getRows(new File(fileName));

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            for (String[] line : lines) {
                String question = line[1];
                if (line != null) {
                    question = line[1];
                    allQuestions.put(question, line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/
    
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

    public static void folderDuplicateRemover(Map<String, Set<String>> selectedFiles, String questionDir,String frameType) {
        Map<String, String[]> allQuestions = new TreeMap<String, String[]>();
        String note=null;
        if(frameType.contains("-IPP-"))
            note="InTraPP";
        else if(frameType.contains("-VP-"))
            note="Tran";
        else if(frameType.contains("-NPP-"))
            note="NounPP";

        for (String property : selectedFiles.keySet()) {
            String outputFileName = questionDir+"questions_all_" +property+"_" + note+".csv";
            Set<String> fileNames = selectedFiles.get(property);
            System.out.println("property::" + property);
            for (String fileName : fileNames) {
                System.out.println("fileName::" + fileName);

                CsvFile csvFile = new CsvFile();
                List<String[]> lines = csvFile.getRows(new File(fileName));
                Integer total = lines.size();
                Integer index = 0;
                try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                    for (String[] line : lines) {
                        index = index + 1;
                        String question = line[1].trim().strip().stripLeading().stripTrailing();
                        if (line != null) {
                            question = line[1];
                            allQuestions.put(question, line);
                            System.out.println(property + " " + index + " total:" + total + " duplicate check:" + line[1] + " fileName:" + fileName);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            List<String[]> newRows = new ArrayList<String[]>();
            for (String id : allQuestions.keySet()) {
                String[] row = allQuestions.get(id);
                newRows.add(row);
            }
            CsvFile outputCsv = new CsvFile(new File(outputFileName));
            outputCsv.writeToCSV(newRows);
        }

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

    private static String getFileProperty(String name) {
        String []info=name.split("-");
        return info[3];
    }

  
}
