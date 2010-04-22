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
   public static int K_TOP = 20;


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

	   if (!input.trim().contains(" ") && !input.contains("*")){
		   bagOfWords(input);
		   return;
	   }
	   Query q = new Query(input);
	   List<Integer> r = q.getResult(); 
	   System.out.println(r);
	   

	}

   public static void main(String[] args) throws Exception {
       // trim the input, insert and instead of spaces, lowercase

	   DocumentIndex.stemming = false;
       manageDB("reuters.db");
       
       DocumentIndex.stemming = true;
       manageDB("reuters_stemmed.db");

       DocumentIndex.stemming = true;
       //TODO get stemming
      
       
       while (true) {
           Scanner in = new Scanner(System.in);

              // Reads a single line from the console
              // and stores into name variable
//           DocumentIndex.stemming = !DocumentIndex.stemming;////////////////////////////////////////////
           System.out.println("Stemming is " + (DocumentIndex.stemming ? "on" : "off"));
           
           
           System.out.print("Query > ");
           String input = in.nextLine();
           input = input.toLowerCase().trim();

           String soundex = Soundex.guessSoundex(input);
           
           if (soundex != null)
               System.out.println("Did you mean: " + soundex);
          
           try { 								// Try boolean
               booleanQueries(input);
           } catch (Exception ex) { 			// Bag of words
        	   System.out.println("Exception");
        	   bagOfWords(input);
           }
       }
   }

	private static void manageDB(String dbName) throws Exception {
		File db = new File(dbName);
	       // DocumentIndex index = null;
	
		System.out.println( DocumentIndex.stemming ? "Stemming:" : "");
	       if (!db.exists()) {
	    	   
	           System.out.println("Creating index...");
	           DocumentIndex.instance().createIndex("reutersTXT/");
	           DocumentIndex.instance().save(dbName);
	           System.out.println("Done.");
	       } else {
	           System.out.println("Loading index...");
	           DocumentIndex.instance().load(dbName);
	           System.out.println("Done.");
	       }
	}
}