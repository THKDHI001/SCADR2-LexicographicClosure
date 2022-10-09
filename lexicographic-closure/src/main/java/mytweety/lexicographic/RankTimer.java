package mytweety.lexicographic;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class RankTimer{
    @State(Scope.Thread)
    public static class BenchState{
        PlBeliefSet beliefSet;
        PlBeliefSet classicalSet;
        @Setup(Level.Trial)
        public void setup() throws IOException{
            String fileName = "";
            try {
                File file = new File("rankfile.txt");
                Scanner reader = new Scanner(file);
                if(reader.hasNextLine()){
                    fileName = reader.nextLine();
                }
                else{
                    System.out.println("Please enter a single text file name into rankfile.txt");
                    System.exit(0);
                }
                
                reader.close();
            } catch (FileNotFoundException e) {
            }

            this.beliefSet = new PlBeliefSet();
            PlParser parser = new PlParser();
            this.classicalSet = new PlBeliefSet();

            try {
                File file = new File(fileName);
                Scanner reader = new Scanner(file);

                while (reader.hasNextLine()) {
                    String stringFormula = reader.nextLine();
                    if (stringFormula.contains("¬")) {
                        stringFormula = stringFormula.replaceAll("¬", "!");
                    }
                    if (stringFormula.contains("~>")) {
                        stringFormula = reformatDefeasibleImplication(stringFormula);
                        beliefSet.add((PlFormula) parser.parseFormula(stringFormula));
                    } else {
                        classicalSet.add((PlFormula) parser.parseFormula(stringFormula));

                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                System.out.println("Output not in correct format. Please ensure each formula is in a separate line, with the first line being the defeasible fileName, and the remainder being the knowledge base. All formulas must use the following syntax:");
                System.out.println("Implication symbol: =>");
                System.out.println("Defeasible Implication symbol: ~>");
                System.out.println("Conjunction symbol: && ");
                System.out.println("Disjunction symbol: ||");
                System.out.println("Equivalence symbol: <=>");
                System.out.println("Negation symbol: !");
            }
        }  
    }
    
    @Benchmark
    @Fork(value = 2)
    @Measurement(iterations = 10, time = 1)
    @Warmup(iterations = 5, time = 1)
    public void ranker(BenchState state) throws IOException{
        System.out.println(BaseRankThreaded.rank(state.beliefSet, state.classicalSet));
    }
    
    public static void main(String [] args) throws ParserException, RunnerException{
        Options opt = new OptionsBuilder().include(LexicalTimer.class.getSimpleName()).forks(1).build();

        new Runner(opt).run();
    }

    static String reformatDefeasibleImplication(String formula) {
        int index = formula.indexOf("~>");
        formula = "(" + formula.substring(0, index) + ") => (" + formula.substring(index + 2, formula.length()) + ")";
        return formula;
    }
}