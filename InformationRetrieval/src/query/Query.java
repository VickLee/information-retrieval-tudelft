package query;

import indexing.DocumentIndex;
import indexing.PermutermFacilities;
import indexing.TokenAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ranking.CosineRanker;
import soundex.Soundex;

public class Query {
   public static int K_TOP = 400;
   public static int MODE_BOOLEAN  = 0;
   public static int MODE_BAG_OF_WORDS = 1;


   private CommonTree tree;
   public static final boolean DEBUG = true;

   // public DocumentIndex index;
   private List<Integer> results;

   public List<Integer> rankResult() {
       try {
           if (results == null)
               getResult();
       } catch (RecognitionException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }

       // for (Integer i: )
       return null;

   }

   public Query(String query) throws RecognitionException {
       // this.index = index;
       query = query.trim().toLowerCase();
       query = query.replaceAll("[^a-zA-Z0-9() .\t" + PermutermFacilities.PERMUTERM_SYMBOL + PermutermFacilities.WILDCARD_SYMBOL + "]", "");

       QueryGrammarLexer lexer = new QueryGrammarLexer(new ANTLRStringStream(query));
       CommonTokenStream tokens = new CommonTokenStream(lexer);
       QueryGrammarParser parser = new QueryGrammarParser(tokens);
       QueryGrammarParser.query_return r = parser.query();
       tree = (CommonTree) r.getTree();
   }

   // public Query(InputStream in) throws IOException, RecognitionException {
   // ANTLRInputStream input = new ANTLRInputStream(System.in);
   // QueryGrammarLexer lexer = new QueryGrammarLexer(new
   // ANTLRInputStream(in));
   // CommonTokenStream tokens = new CommonTokenStream(lexer);
   // QueryGrammarParser parser = new QueryGrammarParser(tokens);
   // QueryGrammarParser.query_return r = parser.query();
   // tree = (CommonTree)r.getTree();
   // }


   public static List<String> takeOutQueryTerms(String inputQuery) {

       ArrayList<String> out = new ArrayList<String>();

       TokenAnalyzer ta = new TokenAnalyzer(inputQuery);
       String term = null;

       while ((term = ta.getNextToken()) != null) {
    	   
    	   //term = TokenAnalyzer.Stem(term);
    	   
           if (!out.contains(term))
               out.add(term);
       }
       return out;
   }

   // returns document ids
   public List<Integer> getResult() throws RecognitionException {
       CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
//        tree.getText();

       QueryExecuter walker = new QueryExecuter(nodes);
       TokenStream ts = nodes.getTokenStream();

       results = walker.query();
       return results;

   }

   public void printAST() {
       System.out.println(tree.toStringTree());
   }
   
   public static String join (List<String> list, String seperator){
	   
	   String result = "";
	   for(int i = 0; i < list.size() ; i++){
		   result += list.get(i);
		   if(i < list.size() - 1)
			   result += " " + seperator + " ";
	   }
	   return result;
   }
   
   private static void bagOfWords(String input) throws RecognitionException {
//		if(input.contains("*")){
//			   String s = join(takeOutQueryTerms(input), "or");
//		       System.out.println("Entered query is replaced by this boolean query: " + s);
//			   Query q2 = new Query(s);
//		       List<Integer> r2 = q2.getResult();
//		       System.out.println(r2);
//		   }
//		   else{
		       System.out.printf("K: %d\n",K_TOP);
			   List <String> queryParsed = takeOutQueryTerms(input);
		       System.out.println("High-idf turned off:");
		       System.out.println(CosineRanker.rankingResults(queryParsed, K_TOP, false));
		       System.out.println("High-idf turned on:");
		       System.out.println("Ignored term(s): ");
		       System.out.println(CosineRanker.rankingResults(queryParsed, K_TOP, true));
//		   }
   }
   
   private static void booleanQueries(String input) throws RecognitionException {

	   if (!input.trim().contains(" ") && !input.contains("*"))
	   bagOfWords(input);
	   
	   Query q = new Query(input);
	   List<Integer> r = q.getResult(); 
	   System.out.println(r);
	   

	}

   public static void main(String[] args) throws Exception {
       // trim the input, insert and instead of spaces, lowercase

       File db = new File("reuters.db");
       // DocumentIndex index = null;

       if (!db.exists()) {
           System.out.println("Creating index...");
           DocumentIndex.instance().createIndex("reutersTXT/");
           DocumentIndex.instance().save("reuters.db");
           System.out.println("Done.");
       } else {
           System.out.println("Loading index...");
           DocumentIndex.instance().load("reuters.db");
           System.out.println("Done.");
       }

       while (true) {
           Scanner in = new Scanner(System.in);

              // Reads a single line from the console
              // and stores into name variable
           
           System.out.print("Query > ");
           String input = in.nextLine();
           input = input.toLowerCase().trim();

           String soundex = Soundex.guessSoundex(input);
           
           if (soundex != null)
               System.out.println("Did you mean: " + soundex);
          
           try { 								// Try boolean
               booleanQueries(input);
           } catch (Exception ex) { 			// Bag of words
        	   System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEe");
        	   bagOfWords(input);
           }
       }
   }
}