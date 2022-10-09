package mytweety.lexicographic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.io.File;

import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
/*Reads the knowledge base input file to create the knowledge base arraylist. 
It then checks which reasoner implementation you wish to use (Fibonacci, power set or concurrent) 
and asks for the query. This is an infinite loop until the user types 'quit'.*/
public class LexicalReasoner{
    public static void reason(String fileName) throws IOException{
        long starter = System.nanoTime(); //beginning timestamp for knowledge base timer
        //initialise belief sets (one for defeasible statements and one for classical statements) and parser
        PlBeliefSet beliefSet = new PlBeliefSet();
        PlParser parser = new PlParser();
        PlBeliefSet classicalSet = new PlBeliefSet();
        //parser
        try {
            File file = new File(fileName);
            Scanner reader = new Scanner(file);

            while (reader.hasNextLine()) {
                String stringFormula = reader.nextLine();
                if (stringFormula.contains("¬")) {
                    stringFormula = stringFormula.replaceAll("¬", "!"); //replace incorrect formats
                }
                if (stringFormula.contains("~>")) {
                    stringFormula = reformatDefeasibleImplication(stringFormula); //reformats statement
                    beliefSet.add((PlFormula) parser.parseFormula(stringFormula)); //add statement to defeasible belief set
                } else {
                    classicalSet.add((PlFormula) parser.parseFormula(stringFormula)); //add statement to classical belief set
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            //If file is not found then print error
            System.out.println("File is not found. Please ensure each formula is in a separate line, with the first line being the defeasible query, and the remainder being the knowledge base. All formulas must use the following syntax:");
            System.out.println("Implication symbol: =>");
            System.out.println("Defeasible Implication symbol: ~>");
            System.out.println("Conjunction symbol: && ");
            System.out.println("Disjunction symbol: ||");
            System.out.println("Equivalence symbol: <=>");
            System.out.println("Negation symbol: !");
        }
        
        ArrayList<PlBeliefSet> rankedKnowledgeBase = BaseRankThreaded.rank(beliefSet, classicalSet); //sends both belief sets to be ranked for knowledge base
        long ender = System.nanoTime(); //ending timestamp for knowledge base timer
        System.out.println("Knowledge Base Creation Time: "+((ender-starter)/1000000000.0) + "s"); //prints total time taken for knowledge base to be created

        PlBeliefSet[] rankedKnowledgeBaseArray = new PlBeliefSet[rankedKnowledgeBase.size()]; //convert arraylist to array
        PlBeliefSet[] rankedKBArray = rankedKnowledgeBase.toArray(rankedKnowledgeBaseArray);
        Scanner scanner = new Scanner(System.in);
        PlFormula formatQuery = (PlFormula) parser.parseFormula("a");
        long start, end = 0;
        //infinite loop that asks for type of reasoner and query
        while(true){
            System.out.println("Enter \"fib\" for fibonacci reasoner, \"power\" for powerset reasoner or \"con\" for the concurrent fibonacci reasoner (Enter \"quit\" to exit):");
            String type = scanner.nextLine();
            if(type.equals("fib") || type.equals("power")){
                System.out.println("Enter formula to query:");
                String query = scanner.nextLine(); //enter query
                if (query.contains("¬")) {
                    query = query.replaceAll("¬", "!");
                }
                formatQuery = (PlFormula) parser.parseFormula(fileWriter.reformatDefeasibleImplication(query));
                if(type.equals("fib")){
                    //Fibonacci search reasoner
                    start = System.nanoTime();
                    System.out.println(FibonacciApproach.fibEntail(rankedKBArray, formatQuery, 0, rankedKnowledgeBase.size()));
                    end = System.nanoTime();
                    System.out.println("Time taken: "+((end-start)/1000000000.0) + "s");
                }
                else if(type.equals("power")){
                    //power set reasoner
                    start = System.nanoTime();
                    System.out.println(PowerSet.powEntail(rankedKnowledgeBase,formatQuery));
                    end = System.nanoTime();
                    System.out.println("Time taken: "+((end-start)/1000000000.0) + "s");
                }
            }
            else if(type.equals("con")){
                //concurrent approach which uses Fibonacci search reasoner
                start = System.nanoTime();
                System.out.println("Enter query file name:"); //input file name
                String queryFile = scanner.nextLine();
                System.out.println("Reading "+queryFile);
                ArrayList<PlFormula> queries = new ArrayList<>();
                String query = "";
                //adds queries to arraylist
                try {
                    File file = new File(queryFile);
                    Scanner reader = new Scanner(file);
                    if(!reader.hasNextLine()){
                        System.out.println("Please insert queries into "+queryFile);
                        System.exit(0);
                    }
                    System.out.println("Queries:");
                    while(reader.hasNextLine()){
                        query = reader.nextLine();
                        if (query.contains("¬")) {
                            query = query.replaceAll("¬", "!");
                        }
                        formatQuery = (PlFormula) parser.parseFormula(reformatDefeasibleImplication(query));
                        System.out.println(formatQuery);
                        queries.add(formatQuery);
                    }
                    
                    reader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                CountDownLatch countDownLatch = new CountDownLatch(3);
                System.out.println("Answers:");
                //separates queries into 3 parts for 3 threads to handle
                int mid1 = queries.size()/3;
                int mid2 = queries.size()-(queries.size()/3);
                ArrayList<PlFormula> firstThird = new ArrayList<>(queries.subList(0, mid1));
                ArrayList<PlFormula> secondThird = new ArrayList<>(queries.subList(mid1, mid2));
                ArrayList<PlFormula> thirdThird = new ArrayList<>(queries.subList(mid2, queries.size()));
                Thread obj = new Thread(new ConcurrentApproach(rankedKBArray, firstThird, 0, rankedKnowledgeBase.size(),countDownLatch));
                obj.start();
                Thread obj2 = new Thread(new ConcurrentApproach(rankedKBArray, secondThird, 0, rankedKnowledgeBase.size(),countDownLatch));
                obj2.start();
                Thread obj3 = new Thread(new ConcurrentApproach(rankedKBArray, thirdThird, 0, rankedKnowledgeBase.size(),countDownLatch));
                obj3.start();

                try {
                    countDownLatch.await(); //waits until all threads are complete before continuing app
                } catch(InterruptedException e) {
                    e.printStackTrace();;
                }
                end = System.nanoTime();
                System.out.println("Time taken: "+((end-start)/1000000000.0) + "s");
            }
            else if(type.equals("quit")){
                break;
            }
            else{
                System.out.println("Please enter either lexicographic, power or con as the input."); //if invalid input for reasoner given
            }
        }
        scanner.close();
    }
    /*NOTE: This function was created by Daniel Park (previous year's original work)*/
    //method that reformats defeasible statements to classical statements
    static String reformatDefeasibleImplication(String formula) {
        int index = formula.indexOf("~>");
        formula = "(" + formula.substring(0, index) + ") => (" + formula.substring(index + 2, formula.length()) + ")";
        return formula;
    }
}