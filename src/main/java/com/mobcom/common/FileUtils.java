package com.mobcom.common;


import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.mobcom.sat.App;
import com.mobcom.solver.CNF;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FileUtils {

    public static final int SIZE_OF_PLINE = 4;
    public static final int INDEX_NUMBER_OF_VARIABLES = 2;
    public static final int INDEX_NUMBER_OF_CLAUSES = 3;
    public static final int CLAUSE_LENGTH_SAT3 = 3;


    public static CNF handleFile(String fileName) throws Exception {

        int numberOfClauses = 0;
        int numberOfVariables = 0;
        CNF cnfContent = new CNF();
        Supplier<Stream<String>> lines = () -> {
            try {
                try {

                    return new BufferedReader(new InputStreamReader(App.class.getResourceAsStream(fileName))).lines();
                }catch (Exception ex){
                    //proceed to other method of loading file.
                }
                return Files.lines(Paths.get(fileName));
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return null;
            }
        };

        if(lines==null || lines.get()==null){
            System.err.println("lines null");
            throw new Exception("Given file cannot be read.");
        }

        List<List<String>> pLine = lines.get()
                .filter(line -> line.startsWith("p "))
                .map(line->{
                    List<String> content = Arrays.asList(line.split(" "));
                    return content;
                })
                .collect(Collectors.toList());
        if(pLine.size()!=1 || pLine.get(0).size()!= SIZE_OF_PLINE){
            throw new Exception("CNF file does not have valid p line.");
        }else{
            try {
                numberOfVariables = Integer.valueOf(pLine.get(0).get(INDEX_NUMBER_OF_VARIABLES));
                numberOfClauses = Integer.valueOf(pLine.get(0).get(INDEX_NUMBER_OF_CLAUSES));
            }catch (NumberFormatException nex){
                throw new Exception("P line has invalid values.");
            }
        }


        try {
            List<String> pureLines = lines.get().collect(Collectors.toList());

            int index =
                    IntStream.range(0, pureLines.size())
                            .filter(i -> pureLines.get(i).startsWith("p "))
                            .mapToObj(i -> i + 1)
                            .findFirst().get();

            List<LinkedHashSet<Integer>> wholeFile = lines.get()
                    .skip(index)
                    .map(line -> {
                        LinkedHashSet<Integer> numbers = Arrays.asList(line.split(" "))
                                .stream()
                                .map(Integer::valueOf)
                                .collect(Collectors.toCollection(LinkedHashSet::new));

                        int lastValue = (int)numbers.toArray()[numbers.size()-1];
                        if (numbers.size() > (CLAUSE_LENGTH_SAT3 + 1)) {
                            throw new IllegalArgumentException("Clause length does not match with given SAT problem.");
                        }else if(lastValue != 0){
                            throw new IllegalArgumentException("The line should be terminated by 0. Found "+lastValue+".");
                        }
                        numbers.remove(lastValue);
                        return numbers;
                    })
                    .collect(Collectors.toList());

            if (wholeFile.size() != numberOfClauses) {
                throw new IllegalArgumentException("Number of clauses does not match with given number.");
            }
            cnfContent.setClauses(wholeFile);
            cnfContent.setNumberOfVariables(numberOfVariables);

            return cnfContent;
        } catch(NumberFormatException nex){
            throw new Exception("Clauses have invalid values.");
        }
        //catch (Exception e) {
          //  e.printStackTrace();
            //throw e;
        //}



    }
    public static void writeToFile (String filename, int[] fileContent) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (int i = 1; i < fileContent.length; i++) {
            writer.write(i+" "+fileContent[i]);
            writer.newLine();

        }
        writer.flush();
        writer.close();


    }


}

