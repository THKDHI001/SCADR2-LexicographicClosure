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
/*Simulates the theoretical method of lexicographic closure.*/
public class RegularApproach{
    public static Boolean regEntail(ArrayList<PlBeliefSet> rKnowledgeBase, PlFormula formula) throws IOException, ParserException{
        ArrayList<PlBeliefSet> rkb = new ArrayList<>(rKnowledgeBase);
        SatReasoner SATReasoner = new SatReasoner();
        PlParser parser = new PlParser();
        SatSolver.setDefaultSolver(new Sat4jSolver());
        while(rkb.size() > 1){
            PlFormula neg = new Negation(((Implication) formula).getFormulas().getFirst());
            if(SATReasoner.query(combine(rkb), neg)){
                if(rkb.get(0).size() > 1){
                    Object [] curr = rkb.get(0).toArray();
                    ArrayList<ArrayList<Object>> currentSet = new ArrayList<>();

                    for(int i = 0; i < curr.length; i++){
                        ArrayList<Object> current = new ArrayList<>(Arrays.asList(curr));
                        currentSet.add(current);
                    }

                    while(currentSet.get(0).size() > 1){
                        for(int i = 0; i < currentSet.size(); i++){
                            ArrayList<Object> temp = currentSet.get(i);
                            if (i > temp.size() - 1) {
                                temp.remove(0);
                            } else {
                                temp.remove(i);
                            }

                            currentSet.set(i, temp);

                            PlBeliefSet tempSet = new PlBeliefSet();
                            for (Object p : temp) {
                                PlFormula tempFormula = (PlFormula) parser.parseFormula(p.toString());
                                tempSet.add(tempFormula);
                            }
                            rkb.set(0, tempSet);


                            if(!(SATReasoner.query(combine(rkb), neg))){
                                if(SATReasoner.query(combine(rkb), formula)){
                                    return true;
                                }
                                else{
                                    return false;
                                }
                            }
                        }
                    }
                    rkb.remove(0);
                }
                else{
                    rkb.remove(0);
                }
            }
            else{
                if(SATReasoner.query(combine(rkb), formula)){
                    return true;
                }
                else{
                    return false;
                }
            }    
        }

        if(SATReasoner.query(combine(rkb), formula)){
            return true;
        }
        else{
            return false;
        }        
    }

    static PlBeliefSet combine(ArrayList<PlBeliefSet> ranks) {
        PlBeliefSet combination = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combination.addAll(rank);
        }
        return combination;
    }
}