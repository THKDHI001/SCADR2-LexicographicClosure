package mytweety.lexicographic;

import org.tweetyproject.logics.pl.syntax.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/*Thread that takes in a set of queries and a knowledge base and calls the Fibonacci search algorithm.
It also reduces the latch so that when all threads are complete, the program continues.*/
public class ConcurrentApproach implements Runnable{

    private PlBeliefSet [] kb;
    private ArrayList<PlFormula> formula;
    private int start;
    private int end;
    private CountDownLatch latch;

    public ConcurrentApproach(PlBeliefSet [] kb, ArrayList<PlFormula> form, int start, int end, CountDownLatch lat) {
        this.formula = form;
        this.kb = kb;
        this.start = start;
        this.end = end;
        this.latch = lat;
    }

    public void run() {
        try {
            for(int i = 0; i < formula.size();i++){
                System.out.println(formula.get(i) + ": " + FibonacciApproach.fibEntail(kb, formula.get(i), start, end)); //performs each query using the Fibonacci search
            }
        } catch (Exception e) {
            
        }
        latch.countDown(); //reduces latch
    }

}