package mytweety.lexicographic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.codehaus.jettison.json.JSONArray;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

/*Reads the input file, calls the base rank method, and saves output in the JSON file to use as the knowledge base.
This is strictly used for testing and the timer classes*/
public class fileWriter {
    public static void main (String [] args) throws IOException, ParserException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file name");
        long start = System.nanoTime();
        write(scanner.nextLine());
        long end = System.nanoTime();
        System.out.println("Time taken: "+((end-start)/1000000000.0) + "s"); //measures time taken to read and rank file into knowledge base
        scanner.close();
    }

    /*NOTE: This function was created by Daniel Park (previous year's original work)*/
    //The write function reads the file, ranks the statements, and adds the knowledge base to a JSON array
    static void write(String fileName) throws IOException, ParserException {

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
        
        ArrayList<PlBeliefSet> rankedKnowledgeBase = BaseRankThreaded.rank(beliefSet, classicalSet);
        JSONArray jsonRankedKB = new JSONArray(rankedKnowledgeBase);

        try (FileWriter file = new FileWriter("rankedKB.json")) {
            //writes the JSON array to the file
            file.write(jsonRankedKB.toString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*NOTE: This function was created by Daniel Park (previous year's original work)*/
    //method that reformats defeasible statements to classical statements
    static String reformatDefeasibleImplication(String formula) {
        int index = formula.indexOf("~>");
        formula = "(" + formula.substring(0, index) + ") => (" + formula.substring(index + 2, formula.length()) + ")";
        return formula;
    }
}