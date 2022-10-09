package mytweety.lexicographic;

import java.util.Scanner;
import java.io.IOException;
import org.tweetyproject.commons.ParserException;
/*Application that runs the reasoner. It initialises the knowledge base 
and then allows the use of the naive reasoner or the lexicographic reasoner. 
It will then require a query to evaluate. The application also measures 
the time taken to initialise the knowledge base.*/
public class App{
    public static void main (String [] args) throws IOException, ParserException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file name");
        LexicalReasoner.reason(scanner.nextLine()); //calls LexicalReasoner class to start the app
        scanner.close();
    }
}