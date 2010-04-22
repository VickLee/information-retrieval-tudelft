package soundex;

import indexing.DocumentIndex;
import indexing.TermPosting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.gunit.swingui.parsers.StGUnitParser.output_return;

import query.Query;

public class Soundex {
    private HashMap<String, ArrayList<TermPosting>> soundex;

    public static final int THRESHOLD = 3;
    public Soundex() {
        soundex = new HashMap<String, ArrayList<TermPosting>>();
    }
    
    
    public static String soundex(String s) { 
        
    	//System.out.println(s);
    	char[] x = s.toUpperCase().toCharArray();
        //System.out.println(x);
        char firstLetter = x[0];

        // convert letters to numeric code
        for (int i = 0; i < x.length; i++) {
            switch (x[i]) {
                case 'B':
                case 'F':
                case 'P':
                case 'V': { x[i] = '1'; break; }

                case 'C':
                case 'G':
                case 'J':
                case 'K':
                case 'Q':
                case 'S':
                case 'X':
                case 'Z': { x[i] = '2'; break; }

                case 'D':
                case 'T': { x[i] = '3'; break; }

                case 'L': { x[i] = '4'; break; }

                case 'M':
                case 'N': { x[i] = '5'; break; }

                case 'R': { x[i] = '6'; break; }

                default:  { x[i] = '0'; break; }
            }
        }

        // remove duplicates
        String output = "" + firstLetter;
        for (int i = 1; i < x.length; i++)
            if (x[i] != x[i-1] && x[i] != '0')
                output += x[i];

        // pad with 0's or truncate
        output = output + "0000";
        return output.substring(0, 4);
    }
    
    public static void addSoundex(String term, TermPosting tp) {
    	
    	if (term == null || term.trim() == "")
    		return;
		
    	//System.out.println("- " + term + " -");
    	String soundex = Soundex.soundex(term);
		
		if (DocumentIndex.instance().getSoundIndex().get(soundex) == null)
		{
			List<TermPosting> tpList = new ArrayList<TermPosting>();
			tpList.add(tp);
			DocumentIndex.instance().getSoundIndex().put(soundex, tpList);
		}
		else
			DocumentIndex.instance().getSoundIndex().get(soundex).add(tp);
	}
    
    private static String getSimilarWordWithShortestLengthFromStemmingList(TermPosting termPosting)
    {
    	if (termPosting.nonStemmedTerms == null || termPosting.nonStemmedTerms.size() == 0)
    		return termPosting.term;
    	String term = termPosting.nonStemmedTerms.get(0);
    	
    	for (String str : termPosting.nonStemmedTerms)
    		if (str.length() < term.length())
    			term = str;
    	
    	return term;
    }
    
    private static String findMostCommonSimilarTerm(String term)
    {
    	List<TermPosting> similarWords = DocumentIndex.instance().getSoundIndex().get(soundex(term));
    	
    	if (similarWords == null || similarWords.size() == 0)
    		return term;
    	
    	TermPosting max = DocumentIndex.instance().getTermPosting(term);
    	if (max == null)
    		max = similarWords.get(0);
    	
    	for (TermPosting tp : similarWords)
    		if (tp.termFrequencySum > max.termFrequencySum)
    			max = tp;
    	
    	TermPosting originalTerm = DocumentIndex.instance().getTermPosting(term);
    	if (originalTerm == null || max.termFrequencySum >= THRESHOLD * originalTerm.termFrequencySum)
    	{
    		if (DocumentIndex.stemming)
    			return getSimilarWordWithShortestLengthFromStemmingList(max);	
    		return max.term;
    	}
    	return term;
    }
    
    public static String guessSoundex(String inputQuery)
    {
    	List<String> queryTerms = Query.takeOutQueryTerms(inputQuery);
    	
    	String suggestion = "";
    	String soundexTerm = "";
    	boolean isTheSame = true;
    	
    	for (String term: queryTerms)
    		{
    			soundexTerm = findMostCommonSimilarTerm(term);
    			
    			if (!soundexTerm.equals(term))
    				isTheSame = false;
    			
    			suggestion += soundexTerm + " ";
    		}
    	
    	
    	if (isTheSame)
    		return null;
    	
    	return suggestion;
    }

//    public static void main(String[] args)
//    {
//    	System.out.println(soundex("Agriculture"));
//    	System.out.println(soundex("Agricult"));
//    }
}
