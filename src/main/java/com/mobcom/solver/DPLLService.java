package com.mobcom.solver;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import java.util.*;


/*
if C is consistent set of unit clauses, return true if C contains empty clause, return false
        as long as there is a unit clause {l} in C:
        C := unit-propagate(l, C)
        as long as there are pure literals l in C:
        C := pure-literal-assign(l, C):
        l := choose-literal(C)
        return DPLL(C [ {l}) || DPLL(C [ {¬l})
*/
public class DPLLService {


    public static final int NOT_FOUND = 0;


    public boolean solveWithDPLL(CNF cnfContent){
        //There are no clauses
        if(cnfContent.getClauses().size()==0){
            return  true;
        }
        //There are empty clauses.
        if(cnfContent.getClauses().stream().filter(clause->clause.size()==0).count()!=0){
            return false;
        }


        //Do unit propagation as much as possible.
        //If the given clauses has a unit clause, program will not proceed beyond this point until no more unit clause left.
        int result = findUnitClause(cnfContent);
        while(result!=NOT_FOUND){
            //unit propagation in this case
            doPropagation(cnfContent, result);
            cnfContent.getSelectedVariables().add(result);
            //There are no clauses
            if(cnfContent.getClauses().size()==0){
                return  true;
            }
            //There are empty clauses.
            if(cnfContent.getClauses().stream().filter(clause->clause.size()==0).count()!=0){
                return false;
            }
            result = findUnitClause(cnfContent);
        }

        result = hasPureLiteral(cnfContent);
        while(result != NOT_FOUND){
            //elimination cannot create a unit clause, so the program should not go back to top.
            eliminatePureLiteral(cnfContent,result);
            cnfContent.getSelectedVariables().add(result);
            if(cnfContent.getClauses().size()==0){
                return  true;
            }
            result = hasPureLiteral(cnfContent);
        }

        int selectedLiteral = chooseLiteral(cnfContent);

        CNF cnfContentTrueBranch = new CNF(cnfContent);
        CNF cnfContentComplementBranch = new CNF(cnfContent);

        cnfContentTrueBranch.getClauses().add(new LinkedHashSet<>(Arrays.asList(selectedLiteral)));
        cnfContentComplementBranch.getClauses().add(new LinkedHashSet<>(Arrays.asList(-selectedLiteral)));

        //could be an optimization
        doPropagation(cnfContentTrueBranch,selectedLiteral);
        doPropagation(cnfContentComplementBranch,-selectedLiteral);

        if(solveWithDPLL(cnfContentTrueBranch)){
            //set itself
            cnfContent.getSelectedVariables().add(selectedLiteral);
            cnfContent.getSelectedVariables().addAll(cnfContentTrueBranch.getSelectedVariables());
            return true;
        }else{
            //negation
            //set

            boolean isSatisfiable = solveWithDPLL(cnfContentComplementBranch);
            if(isSatisfiable){
                cnfContent.getSelectedVariables().addAll(cnfContentComplementBranch.getSelectedVariables());
                cnfContent.getSelectedVariables().add(-selectedLiteral);
            }
            return isSatisfiable;

        }

    }

    private int chooseLiteral(CNF cnfContent){
        int selectedLiteral;
        Optional<LinkedHashSet<Integer>> selectedClause = cnfContent.getClauses().stream().filter(clause->clause.size()==2).findFirst();
        if(selectedClause.isPresent()){
            Object[] literals = (Object[])selectedClause.get().toArray();
            if(cnfContent.getVariablesAndOccurrences().count(literals[0])<cnfContent.getVariablesAndOccurrences().count(literals[1])){
                selectedLiteral = (Integer)literals[0];
            }else{
                selectedLiteral = (Integer)literals[1];
            }
        }else{
            selectedLiteral = Multisets.copyHighestCountFirst(cnfContent.getVariablesAndOccurrences()).entrySet().iterator().next().getElement();
        }
        return selectedLiteral;
    }

    private int findUnitClause(CNF cnfContent){
        Optional<LinkedHashSet<Integer>> unitClause =  cnfContent.getClauses().stream().filter(clause->clause.size()==1).findFirst();
        if(unitClause.isPresent()){
            return unitClause.get().iterator().next();
        }else{
            return NOT_FOUND;
        }
    }

    private void doPropagation(CNF cnfContent, int literal){
        int length = cnfContent.getClauses().size();
        Multiset<Integer> variablesAndOccurrences =  cnfContent.getVariablesAndOccurrences();
        for (int i = length-1; i >= 0; i--) {
            LinkedHashSet<Integer> clause = cnfContent.getClauses().get(i);
            if(clause.contains(literal)){
                //remove clause itself
                Iterator<Integer> itr = clause.iterator();
                while(itr.hasNext()){
                    variablesAndOccurrences.remove(itr.next());
                }
                cnfContent.getClauses().remove(i);

            }else if(clause.contains(-literal)){

                //remove negation
                variablesAndOccurrences.remove(-literal);
                //cnfContent.getClauses().get(i).remove(-literal);
                clause.remove(-literal);
                cnfContent.getClauses().set(i,clause);
            }
        }
        cnfContent.setVariablesAndOccurrences(variablesAndOccurrences);

    }

    private int hasPureLiteral(CNF cnfContent){
        Multiset<Integer> variablesAndOccurences = cnfContent.getVariablesAndOccurrences();
        Iterator<Integer> itr = variablesAndOccurences.iterator();
        boolean found = false;
        Integer pureLiteral = 0;
        while(itr.hasNext() && pureLiteral==0){
            int value = itr.next();
            if(variablesAndOccurences.count(value)>0 && variablesAndOccurences.count(-value)==0){
                pureLiteral = value;
            }else if(variablesAndOccurences.count(-value)>0 && variablesAndOccurences.count(value)==0){
                pureLiteral = -value;
            }
        }
        return pureLiteral;
    }

    private void eliminatePureLiteral(CNF cnfContent, int literal){
        int length = cnfContent.getClauses().size();
        Multiset<Integer> variablesAndOccurrences =  cnfContent.getVariablesAndOccurrences();
        for (int i = length-1; i >= 0; i--) {
            LinkedHashSet<Integer> clause = cnfContent.getClauses().get(i);
            if(clause.contains(literal)){
                //remove clause
                Iterator<Integer> itr = clause.iterator();
                while(itr.hasNext()){
                    variablesAndOccurrences.remove(itr.next());
                }
                cnfContent.getClauses().remove(i);
            }
        }
        cnfContent.setVariablesAndOccurrences(variablesAndOccurrences);

    }

    public void computeOccurrences(CNF cnfContent){
        Multiset<Integer> variablesAndOccurences = cnfContent.getVariablesAndOccurrences();
        cnfContent.getClauses().stream().forEach(clause -> {
            Iterator<Integer> itr = clause.iterator();
            while(itr.hasNext()){
                variablesAndOccurences.add(itr.next());

            }
        });

        cnfContent.setVariablesAndOccurrences(variablesAndOccurences);

    }


}


//literal can be chosen in different ways. problem 4 and 5 cannot be solved by working on highest count first.
//randomly choosing is also not an option for these problems. Therefore these approaches are commented oute.

//highestCountFirst is not an optimization.
//if(App.iteration>10) {
//ImmutableMultiset<Integer> sortedSet = Multisets.copyHighestCountFirst(cnfContent.getVariablesAndOccurrences());
//Iterator<Multiset.Entry<Integer>> itr = sortedSet.entrySet().iterator();
//selectedLiteral = itr.next().getElement();

//selectedLiteral = Multisets.copyHighestCountFirst(cnfContent.getVariablesAndOccurrences()).entrySet().iterator().next().getElement();
//}else {
/*            ImmutableList<Integer> literals = Multisets.copyHighestCountFirst(cnfContent.getVariablesAndOccurrences()).elementSet().asList();
            selectedLiteral = literals.get(literals.size() - 1);
            if(cnfContent.getVariablesAndOccurrences().count(literals.get(literals.size()-1)) == cnfContent.getVariablesAndOccurrences().count(literals.get(literals.size()-2))){
                Optional<LinkedHashSet<Integer>> selectedClause =cnfContent.getClauses().stream().filter(clause->clause.size()==2).findFirst();
                if(selectedClause.isPresent()){
                    selectedLiteral = selectedClause.get().iterator().next();
                }

            }*/
//}
//ImmutableList<Integer> literals = Multisets.copyHighestCountFirst(cnfContent.getVariablesAndOccurrences()).elementSet().asList();
//selectedLiteral = literals.get(literals.size() / 2);


//int randomNum = new Random().nextInt(cnfContent.getVariablesAndOccurrences().elementSet().size());
//selectedLiteral = (Integer) cnfContent.getVariablesAndOccurrences().elementSet().toArray()[randomNum];



