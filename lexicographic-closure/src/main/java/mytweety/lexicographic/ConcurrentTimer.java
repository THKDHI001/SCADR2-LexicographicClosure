package mytweety.lexicographic;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ConcurrentTimer{
    @State(Scope.Thread)
    public static class BenchState{
        PlBeliefSet[] rankedKBArray;
        PlFormula formatQuery;
        ArrayList<PlFormula> queries;
        CountDownLatch countDownLatch;

        @Setup(Level.Trial)
        public void setup() throws IOException{
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
            /*String fileName = "";
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
                e.printStackTrace();
            }
            PlBeliefSet beliefSet = new PlBeliefSet();
            PlParser parser = new PlParser();
            PlBeliefSet classicalSet = new PlBeliefSet();
            //Parser
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
                System.out.println("Output not in correct format. Please ensure each formula is in a separate line, with the first line being the defeasible query, and the remainder being the knowledge base. All formulas must use the following syntax:");
                System.out.println("Implication symbol: =>");
                System.out.println("Defeasible Implication symbol: ~>");
                System.out.println("Conjunction symbol: && ");
                System.out.println("Disjunction symbol: ||");
                System.out.println("Equivalence symbol: <=>");
                System.out.println("Negation symbol: !");
            }
            
            ArrayList<PlBeliefSet> rankedKnowledgeBase = BaseRankThreaded.rank(beliefSet, classicalSet);*/
            PlBeliefSet[] rankedKnowledgeBaseArray = new PlBeliefSet[rankedKnowledgeBase.size()];
            this.rankedKBArray = rankedKnowledgeBase.toArray(rankedKnowledgeBaseArray);
            this.formatQuery = (PlFormula) parser.parseFormula("a");
            this.queries = new ArrayList<>();

            String query = "";
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
            }

            countDownLatch = new CountDownLatch(3);
        }
    }
    
    @Benchmark
    @Fork(value = 2)
    @Measurement(iterations = 10, time = 1)
    @Warmup(iterations = 5, time = 1)
    public void fibonacci(BenchState state) throws IOException{
        int mid1 = state.queries.size()/3;
                int mid2 = state.queries.size()-(state.queries.size()/3);
                ArrayList<PlFormula> firstThird = new ArrayList<>(state.queries.subList(0, mid1));
                ArrayList<PlFormula> secondThird = new ArrayList<>(state.queries.subList(mid1, mid2));
                ArrayList<PlFormula> thirdThird = new ArrayList<>(state.queries.subList(mid2, state.queries.size()));
                Thread obj = new Thread(new ConcurrentApproach(state.rankedKBArray, firstThird, 0, state.rankedKBArray.length,state.countDownLatch));
                obj.start();
                Thread obj2 = new Thread(new ConcurrentApproach(state.rankedKBArray, secondThird, 0, state.rankedKBArray.length,state.countDownLatch));
                obj2.start();
                Thread obj3 = new Thread(new ConcurrentApproach(state.rankedKBArray, thirdThird, 0, state.rankedKBArray.length,state.countDownLatch));
                obj3.start();
    }
    
    public static void main(String [] args) throws ParserException, RunnerException{
        new File("target/jmh-report/Conc_200/").mkdirs();
        Options opt = new OptionsBuilder().include(ConcurrentTimer.class.getSimpleName()).resultFormat(ResultFormatType.CSV)
        .result("target/jmh-report/Conc_200/"+System.currentTimeMillis()+".csv").forks(2).jvmArgs("-Xms2048m", "-Xmx2048m", "-XX:MaxDirectMemorySize=512M").build();

        new Runner(opt).run();
    }

    static String reformatDefeasibleImplication(String formula) {
        int index = formula.indexOf("~>");
        formula = "(" + formula.substring(0, index) + ") => (" + formula.substring(index + 2, formula.length()) + ")";
        return formula;
    }
}