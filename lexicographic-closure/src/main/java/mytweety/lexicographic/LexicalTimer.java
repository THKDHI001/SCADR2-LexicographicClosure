package mytweety.lexicographic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class LexicalTimer{
    @State(Scope.Thread)
    public static class BenchState{
        PlBeliefSet[] rankedKBArray;
        PlFormula formatQuery;
        ArrayList<PlFormula> queries;

        @Setup(Level.Trial)
        public void setup() throws IOException{
            //reads JSON file for testing
            PlParser parser = new PlParser();
            JSONParser jsonParser = new JSONParser();
            ArrayList<PlBeliefSet> rankedKnowledgeBase = new ArrayList<>();

            try (FileReader reader = new FileReader("rankedKB.json"))
            {
                Object obj = jsonParser.parse(reader); //create JSON parser to parse file
    
                JSONArray rankedKB = (JSONArray) obj; //converts parser into JSON array
                //converts contents into belief set to be added into knowledge base arraylist
                for(int i = 0; i < rankedKB.size(); i++){
                    PlBeliefSet plBeliefSet = new PlBeliefSet();
                    Object[] statements = ((JSONArray) rankedKB.get(i)).toArray();
                    for (Object statement : statements){
                        plBeliefSet.add((PlFormula) parser.parseFormula(statement.toString()));
                    }
                    rankedKnowledgeBase.add(plBeliefSet);
                }
                
    
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            PlBeliefSet[] rankedKnowledgeBaseArray = new PlBeliefSet[rankedKnowledgeBase.size()]; //convert arraylist to array
            this.rankedKBArray = rankedKnowledgeBase.toArray(rankedKnowledgeBaseArray);
            this.formatQuery = (PlFormula) parser.parseFormula("a");
            this.queries = new ArrayList<>();
            //reads individual query from file
            String query = "";
            try {
                File file = new File("input.txt");
                Scanner reader = new Scanner(file);
                if(!reader.hasNextLine()){
                    System.out.println("Please insert queries into lexfile.txt");
                    System.exit(0);
                }
                
                while(reader.hasNextLine()){
                    query = reader.nextLine();
                }
                
                reader.close();
            } catch (FileNotFoundException e) {
            }

            if (query.contains("¬")) {
                query = query.replaceAll("¬", "!");
            }
            this.formatQuery = (PlFormula) parser.parseFormula(reformatDefeasibleImplication(query));
            //This set of code has been commented out but was used for comparison of the concurrent approach
            /*String query = "";
            try {
                File file = new File("lexfile.txt");
                Scanner reader = new Scanner(file);
                if(!reader.hasNextLine()){
                    System.out.println("Please insert queries into lexfile.txt");
                    System.exit(0);
                }
                
                while(reader.hasNextLine()){
                    query = reader.nextLine();
                        if (query.contains("¬")) {
                            query = query.replaceAll("¬", "!");
                        }
                        this.formatQuery = (PlFormula) parser.parseFormula(reformatDefeasibleImplication(query));
                        System.out.println(this.formatQuery);
                        this.queries.add(this.formatQuery);
                }
                
                reader.close();
            } catch (FileNotFoundException e) {
            }*/
        }
    }
    
    @Benchmark
    @Fork(value = 2)
    @Measurement(iterations = 10, time = 1)
    @Warmup(iterations = 5, time = 1)
    public void fibonacci(BenchState state) throws IOException{
        //performs the Fibonacci search algorithm using the parameters set above
        //the for loop has been commented out but was used for compariosn of the concurrent approach
        //for(int i = 0; i < state.queries.size(); i++){
        FibonacciApproach fib = new FibonacciApproach();
        System.out.println(fib.fibEntail(state.rankedKBArray, state.formatQuery, 0, state.rankedKBArray.length));
        System.out.println("counter: "+fib.counter); //counts number of entailment checks made
        System.out.println("counterR: "+fib.counterR); //counts when the selected rank is being checked for entailment
        fib.counter = 0;
        fib.counterR = 0;
        //}
    }
    
    public static void main(String [] args) throws ParserException, RunnerException{
        new File("target/jmh-report/Fib_200_inverse/").mkdirs(); //file directory to save results (file name can be changed)
        Options opt = new OptionsBuilder().include(LexicalTimer.class.getSimpleName()).resultFormat(ResultFormatType.CSV)
        .result("target/jmh-report/Fib_200_inverse/"+System.currentTimeMillis()+".csv").forks(2).build(); //initialises options for Java Microbench Harness

        new Runner(opt).run();
    }

    /*NOTE: This function was created by Daniel Park (previous year's original work)*/
    static String reformatDefeasibleImplication(String formula) {
        int index = formula.indexOf("~>");
        formula = "(" + formula.substring(0, index) + ") => (" + formula.substring(index + 2, formula.length()) + ")";
        return formula;
    }
}