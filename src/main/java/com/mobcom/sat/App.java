package com.mobcom.sat;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.mobcom.solver.CNF;
import com.mobcom.common.FileUtils;
import com.mobcom.solver.DPLLService;


/**
 * Ahmet Dursun
 */



public class App {


    public static final String P_REGEX = "^([p]\\s+)*[0-1]$";
    public static final String REGEX = "^([0-1]\\s+)*[0-1]$";
    public static int iteration = 0;

    public static void main(String[] args) {

        try {

            long startfile = System.nanoTime();
            CNF cnfContent = FileUtils.handleFile(args[0]);
            int size = cnfContent.getClauses().size();


            long elapsedTimeFile = System.nanoTime() - startfile;
            System.out.println(elapsedTimeFile/1000000000.0+" s file");
            long start = System.nanoTime();
            DPLLService dpllService = new DPLLService();
            dpllService.computeOccurrences(cnfContent);
            boolean result = dpllService.solveWithDPLL(cnfContent);
            if(result){
                System.out.println("Satisfiable");
            }else{
                System.out.println("Unsatisfiable");
            }



            if(args.length>1 && args[1]!=null && cnfContent.getSelectedVariables().size()!=0){
                int[] selectedVariables = new int[cnfContent.getNumberOfVariables()+1];
                cnfContent.getSelectedVariables().forEach(variable->{
                    if(variable>0){
                        selectedVariables[variable] = 1;
                    }else{
                        selectedVariables[-variable] = 0;
                    }
                });

                FileUtils.writeToFile(args[1],selectedVariables);
            }
            long elapsedTime = System.nanoTime() - start;

            double seconds = (double)elapsedTime / 1000000000.0;
            System.out.println(elapsedTime+" ns");
            System.out.println(seconds+" s");
            System.out.println((System.nanoTime()-startfile)/1000000000.0+" s total");

        }catch(Exception ex){
            ex.printStackTrace();
            System.err.println(ex.getMessage());

        }
    }


}
