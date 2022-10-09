package mytweety.lexicographic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.reasoner.*;
/*Uses the fibonacci sequence to create a series of ranges within the knowledge base array 
for the SAT solver to query. Once the rank is found, it incorporates the lexicographic method 
of comparing each statement against the negation of the query statement before comparing the actual statement.*/
public class FibonacciApproach{
    static SatReasoner SATReasoner = new SatReasoner();
    static int counter = 0; //counts number of entailement checks being made
    static int counterR = 0; //counts when selected rank is checked for entailment
    static int removeRank = -1;
    public static Boolean fibEntail(PlBeliefSet[] rKB, PlFormula formula, int begin, int end) throws ParserException, IOException {
        PlFormula neg = new Negation(((Implication) formula).getFormulas().getFirst());
        SatSolver.setDefaultSolver(new Sat4jSolver());
        PlBeliefSet[] rankedKB = rKB.clone();
        int n = end - begin;
        int fib2 = 0;
        int fib1 = 1;
        int fibM = fib2 + fib1;
        //generates fibonacci ranges based on size of the recursive range
        while(fibM < n){
            fib2 = fib1;
            fib1 = fibM;
            fibM = fib2 + fib1;
        }
        //checks that array has not been fully searched
        if (begin < end) {
            //adds the minimum so that the positions of the whole array are not forgotten
            fib2 = fib2 + begin;
            fib1 = fib1 + begin;
            counter++;
            //evaluates the negation from fib2+1 to the end of the array
            if (SATReasoner.query(combine(Arrays.copyOfRange(rankedKB, fib2 + 1, rankedKB.length)),neg)) {   
                //checks that fib1 is less than the length of the array
                if (fib1 < rankedKB.length) {
                    counter++;
                    //evaluates the negation from fib1+1 to the end of the array which is within the fib2 range
                    if (SATReasoner.query(combine(Arrays.copyOfRange(rankedKB, fib1 + 1, rankedKB.length)),neg)) { 
                        return fibEntail(rankedKB, formula, fib1 + 1, end);
                    } else {
                        counter++;
                        //reduces range to only fib1 to see if previous rank is the required rank
                        if (SATReasoner.query(combine(Arrays.copyOfRange(rankedKB, fib1, rankedKB.length)),neg)) {
                            removeRank = fib1;
                        } else {
                            return fibEntail(rankedKB, formula, fib2 + 1, fib1 - 1);
                        }
                    }
                } else if (fib1 == rankedKB.length) {
                    return fibEntail(rankedKB, formula, fib2 + 1, fib1 - 1);
                }
            } else {
                counter++;
                //reduces range to only fib2 to see if previous rank is the required rank
                if (SATReasoner.query(combine(Arrays.copyOfRange(rankedKB, fib2, rankedKB.length)),neg)) {
                    removeRank = fib2;
                } else {
                    return fibEntail(rankedKB, formula, begin, fib2);
                }
            }
        } else {
            //when recursive minimum and maximum converge the rank is set to begin
            if (begin == end) {
                removeRank = begin;
            }
            else {
                return false;
            }
        }

        //takes the rank from the knowledge base to evaluate the statements in the rank 
        if(removeRank == 0){
            counter++;
            //if rank is 0 then check the whole knowledge base
            if (SATReasoner.query(combine(Arrays.copyOfRange(rankedKB, removeRank, rankedKB.length)), formula)) {
                return true;
            } 
            else{
                return false;
            }
        }
        else if (removeRank +1 < rankedKB.length){
            //converts the rank into a powerset that is combined with 'or' and 'and' operators which is easier than checking each statement separately
            Object [] rank = rankedKB[removeRank].toArray();
            ArrayList<String> powerSet = new ArrayList<>(PowerSet.printPowerSet(rank, rank.length));
            for(String ps : powerSet){
                PlBeliefSet fullSet = new PlBeliefSet();
                PlParser parse = new PlParser();
                fullSet.add((PlFormula) parse.parseFormula(ps));
                rankedKB[removeRank] = fullSet;
                PlBeliefSet combination = combine(Arrays.copyOfRange(rankedKB, removeRank, rankedKB.length));
                counterR++;
                //checks if statement is entailed by knowledge base
                if (!(SATReasoner.query(combination, neg))){
                    if (SATReasoner.query(combination, formula)) {  
                        return true;
                    } else {
                        return false;
                    }
                }
                else{
                    return false;
                }
            }
            return true;
        }
        else if (removeRank +1 == rankedKB.length){
            counter++;
            //if rank is the last rank then query the last rank
            if (SATReasoner.query(combine(Arrays.copyOfRange(rankedKB, removeRank, rankedKB.length)), formula)) {
                return true;
            } 
            else{
                return false;
            }
        }
        else{
            //default false return
            return false;
        }
    }

    /*NOTE: This function was created by Daniel Park*/
    //combine method converts the knowledge base array into a beliefset for the SAT solver
    static PlBeliefSet combine(PlBeliefSet[] ranks) {
        PlBeliefSet combination = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combination.addAll(rank);
        }
        return combination;
    }
}