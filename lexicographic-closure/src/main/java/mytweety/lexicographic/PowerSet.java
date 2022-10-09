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
/*Creates the power set within a rank so that there are less statements to evaluate*/
public class PowerSet{
    public static ArrayList<String> printPowerSet(Object [] set, int n){
        ArrayList<String> subs = new ArrayList<>();  
        String answer = "(";
        int [] contain = new int[n];
        Arrays.fill(contain, 0); //fills array with 0's to be used for bit mapping
        for(int i = 0; i < n; i++){
            contain[i] = 1; //sets the next number to 1
            int [] Contain = new int[n];
            for(int indx = 0; indx < n; indx++){
                Contain[indx] = contain[indx]; //adds next bit from map to another array so that the original is intact
            }
            do{
                for(int j = 0; j < n; j++){               
                    if(Contain[j] == 1){
                        answer += set[j].toString() + " && "; //adds 'and' symbols between each statement
                    } 
                }
                answer = answer.substring(0,answer.length()-4);
                answer += ") || ("; //adds 'or' between each set of statements
            }while(prevPerm(Contain)); //checks all of the previous permutations of the bit map
            answer = answer.substring(0,answer.length()-5); //remove extra operators from the end
            subs.add(answer);
            answer = "(";
        }
        subs.remove(subs.size()-1); //remove final statement that includes all statements since its redundant
        return subs;
    }

    public static Boolean prevPerm(int [] subset){
        int n = subset.length - 1;
        int i = n;
        //checks how much of the map has 1's
        while(i > 0 && subset[i-1] <= subset[i]){
            i-=1;
        }
        //return false if there's no permutations (no more 1's)
        if(i <= 0){
            return false;
        }

        int j = i-1;
        while(j+1 <= n && subset[j+1]<subset[i-1]){
            j+=1;
        }

        int temp = subset[i-1];
        subset[i-1] = subset[j];
        subset[j] = temp;
        //swaps around all the different combinations for the number of 1's that are present
        int size = n - i + 1;
        for(int k = 0; k < Math.floor(size/2);k++){
            int tem = subset[k + i];
            subset[k + i] = subset[n - k];
            subset[n - k] = tem;
        }

        return true;
    }
    
    static Boolean powEntail(ArrayList<PlBeliefSet> rKB, PlFormula formula) throws ParserException, IOException{
        SatReasoner SATReasoner = new SatReasoner();
        PlFormula neg = new Negation(((Implication) formula).getFormulas().getFirst());
        SatSolver.setDefaultSolver(new Sat4jSolver());
        ArrayList <PlBeliefSet> rankedKB = new ArrayList<>(rKB);
        PlBeliefSet combination;
        while(rankedKB.size() > 1){
            if(SATReasoner.query(combine(rankedKB), neg)){
                if(rankedKB.get(0).size() > 1){
                    //creates power set and adds each subset to the knowledge base until the final decision
                    Object [] rank = rankedKB.get(0).toArray();
                    ArrayList<String> powerSet = new ArrayList<>(printPowerSet(rank, rank.length));
                    for(String ps : powerSet){
                        PlBeliefSet fullSet = new PlBeliefSet();
                        PlParser parse = new PlParser();
                        fullSet.add((PlFormula) parse.parseFormula(ps));
                        rankedKB.set(0, fullSet);
                        combination = combine(rankedKB);
                        if (!SATReasoner.query(combination, neg)){
                            if (SATReasoner.query(combination, formula)) {
                                return true;
                            } 
                            else{
                                return false;
                            }
                        }    
                    }
                    rankedKB.remove(0);
                }
                else{
                    rankedKB.remove(0);
                }
            }
            else{
                //this is done for beginning ranks
                combination = combine(rankedKB);
                if (!SATReasoner.query(combination, neg)){
                    if (SATReasoner.query(combination, formula)) {
                        return true;
                    } 
                    else{
                        return false;
                    }
                }   
            }
        }

        combination = combine(rankedKB);
        if(combination.size() == 1){
            if (SATReasoner.query(combination, formula)) {
                return true;
            } 
            else{
                return false;
            }
        }

        return false;
    }

    /*NOTE: This function was created by Daniel Park (previous year's original work)*/
    static PlBeliefSet combine(ArrayList<PlBeliefSet> ranks) {
        PlBeliefSet combination = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combination.addAll(rank);
        }
        return combination;
    }
}