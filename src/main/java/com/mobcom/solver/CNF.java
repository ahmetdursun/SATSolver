package com.mobcom.solver;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

import java.util.*;


public class CNF implements Cloneable {

    private List<LinkedHashSet<Integer>> clauses;
    private Multiset<Integer> variablesAndOccurrences = LinkedHashMultiset.create();
    private int numberOfVariables;
    private List<Integer> selectedVariables = new ArrayList<>();

    public CNF() {
    }

    public CNF(CNF cnfContent){
        this.clauses = new ArrayList<>();
        cnfContent.getClauses().stream().forEach(clause->{
            LinkedHashSet<Integer> newClause = new LinkedHashSet<>(clause);
            this.clauses.add(newClause);
        });
        this.variablesAndOccurrences = LinkedHashMultiset.create(cnfContent.getVariablesAndOccurrences());
        this.numberOfVariables = cnfContent.getNumberOfVariables();
        this.selectedVariables = new ArrayList<>();
    }

    public List<LinkedHashSet<Integer>> getClauses() {
        return clauses;
    }

    public void setClauses(List<LinkedHashSet<Integer>> clauses) {
        this.clauses = clauses;
    }



    public int getNumberOfVariables() {
        return numberOfVariables;

    }

    public void setNumberOfVariables(int numberOfVariables) {
        this.numberOfVariables = numberOfVariables;
    }



    public Multiset<Integer> getVariablesAndOccurrences() {
        return variablesAndOccurrences;
    }

    public void setVariablesAndOccurrences(Multiset<Integer> variablesAndOccurrences) {
        this.variablesAndOccurrences = variablesAndOccurrences;
    }

    public List<Integer> getSelectedVariables() {
        return selectedVariables;
    }

    public void setSelectedVariables(List<Integer> selectedVariables) {
        this.selectedVariables = new ArrayList<>(selectedVariables);
    }




}
