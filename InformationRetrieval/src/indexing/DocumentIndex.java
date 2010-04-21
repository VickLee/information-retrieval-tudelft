/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

import soundex.Soundex;

import antlr.debug.NewLineEvent;

/**
 *
 * @author msenesi
 */
public class DocumentIndex implements Serializable {

    private TreeMap<String, TermPosting> termPostings = new TreeMap<String, TermPosting>();
    private TreeMap<String, List<TermPosting>> soundexIndex = new TreeMap<String, List<TermPosting>>();
    
    public static boolean stemming = true;
    

    public TreeMap<String, List<TermPosting>> getSoundIndex()
    {
    	return soundexIndex;
    }
    
   
    //private ArrayList<Document> documents = new ArrayList<Document>();
    
    private DocumentIndex()
    { 	
    }
    
    private static DocumentIndex regularDocumentIndex = new DocumentIndex();
    private static DocumentIndex stemmingDocumentIndex = new DocumentIndex();
    public static DocumentIndex instance()
    {
    	return stemming ? stemmingDocumentIndex : regularDocumentIndex; 
    }
    
    private static void setInstance(DocumentIndex index)
    {
    	if (stemming)
    		stemmingDocumentIndex = index;
    	else
    		regularDocumentIndex = index;
    }
    


// siamak --------------------------------------------------------------------------
    //public static TreeSet<Integer> documentIds = new TreeSet<Integer>();
    public TreeMap<Integer, Double> document_IDs_And_Lenghts = new TreeMap<Integer, Double>();
// --------------------------------------------------------------------------

    

    // returns all document in which term appears

    

	public List<Integer> getTermPostingList(String term) {
		if (term == null) return Collections.emptyList();
        if (TokenAnalyzer.isStopWord(term)) {
        	return TermPosting.STOP_WORD_LIST;
        }
        
        term = PermutermFacilities.translateToPostfixWildcard(term);
    	List<Integer> result = new ArrayList<Integer>();
        
    	if (!PermutermFacilities.isPostfixWildcard(term)) {
    		TermPosting tp = termPostings.get(term);
    		if (tp == null) return Collections.emptyList();
    		
    		result.addAll(tp.postingList.keySet());
    	} else {
    		// retreive all the posting lists that match the wildcard
    		term = PermutermFacilities.removePostfixWildcard(term);
    		Map.Entry<String, TermPosting> entry = termPostings.ceilingEntry(term);
    		if (entry == null) return Collections.emptyList();

    		String regex = term.replace("$", "\\$") + ".*";
    		if (Pattern.matches(regex, entry.getKey()))
    			result.addAll(entry.getValue().postingList.keySet());
    		
    		while ((entry = termPostings.higherEntry(entry.getKey())) != null && Pattern.matches(regex, entry.getKey()) ) {
    			List<Integer> result2 = new ArrayList(entry.getValue().postingList.keySet());
    			result = TermPosting.orLists(result, result2);
    		}
    		   		
    	}
        
        return result;
	}
 
// siamak ----------------------------------------------------------------------    
    public TermPosting getTermPosting (String term){
// TODO permuterm
    	if (term == null) return null;
    	term = PermutermFacilities.translateToPostfixWildcard(term);
    	TermPosting tp = termPostings.get(term);
        return tp;
    }
// --------------------------------------------------------------------------

    public void add(File txtFile, int docid) {
// siamak --------------------------------------------------------------------------
        int numberOfTokens = 0;
//--------------------------------------------------------------------------
        try {
            // Get terms frequencies within document
            TokenAnalyzer ta = new TokenAnalyzer(txtFile);

            String term = null;
            HashMap<String, Integer> terms = new HashMap<String, Integer>();

            while ((term = ta.getNextToken()) != null) {
                Integer tf = terms.get(term);
                if (tf == null) {
                    tf = new Integer(0);
                }

                tf++;
                terms.put(term, tf);
                
                numberOfTokens ++;
            }




            // Store information in index
            Iterator it = terms.keySet().iterator();
            //Document doc = new Document(docid);
            while (it.hasNext()) {
                term = (String) it.next();
                Integer termFrequency = terms.get(term);

                String permuterm = PermutermFacilities.translateToPostfixWildcard(term);
                TermPosting tp = termPostings.get(permuterm);
                if (tp == null) {
                    tp = new TermPosting(term);
                }
                
                tp.postingList.put(docid, termFrequency);
                tp.termFrequencySum += termFrequency;
                tp.documentFrequency++;
                
                //System.out.println("- " + term + " -");
                 
                Soundex.addSoundex(term, tp);
                
                //permuterm
                for ( String p : PermutermFacilities.producePermutermList(term) ) {
                	termPostings.put(p, tp);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

	

	
    
// siamak ------------------------------------------------------------------------
    public void calculateWeightsAndLenghts(){
    	
    	if(this.termPostings.isEmpty()){
    		throw new RuntimeException("Term postiong Empty");
    	}
		System.out.println("Calculating weights ...");
    	double weight;
    	double d;
    	
    	for (TermPosting tp: termPostings.values()){
    		
    		for (Integer i: tp.postingList.keySet()){
       			//tf-idf weights
    			weight = ( ( (d = tp.postingList.get(i)) > 0 )?(1 + Math.log10(d)):(0.0) ) 
    				* Math.log10((double)this.termPostings.size()/(double)tp.postingList.size());
        		tp.postingListOfWeights.put(i, weight);
        		
//        		if (i == 1666){
//        			System.out.println(" --- " + tp.term + " : " + weight + " : " + tp.postingList.get(i) );
//        		}
        		document_IDs_And_Lenghts.put(i, (
        				document_IDs_And_Lenghts.containsKey(i) 
        				? document_IDs_And_Lenghts.get(i) + Math.pow(weight, 2)
        				: Math.pow(weight, 2) ));
    		}
    	}
    	
    	System.out.println("Calculating document lenghts ...");
    	for(Integer i: document_IDs_And_Lenghts.keySet())
    		document_IDs_And_Lenghts.put(i, Math.sqrt(document_IDs_And_Lenghts.get(i)));
    		
    } 

//--------------------------------------------------
    


    public void createIndex(String dirWithTxtArticles) {
        // Build index over reutersTXT articles collection
        File reutersDir = new File(dirWithTxtArticles);
        File[] docs = reutersDir.listFiles();

//        DocumentIndex index = new DocumentIndex();

        for (File f : docs) {
            if (f.isFile() && f.getName().endsWith(".txt")) {
                String name = f.getName().substring(0, f.getName().indexOf('.'));
                Integer docid = Integer.parseInt(name);
                DocumentIndex.instance().add(f, docid);
            }
        }
// siamak --------------------------------------------------------------------------
        DocumentIndex.instance().calculateWeightsAndLenghts();
//--------------------------------------------------------------------------            

//        documentIndex = index;
    }

    public void load(String fname) throws Exception {
        FileInputStream fin = null;
        ObjectInputStream in = null;
        fin = new FileInputStream(fname);
        in = new ObjectInputStream(fin);
        
        setInstance((DocumentIndex) in.readObject());
        in.close();
//        return index;
    }

    public void save(String fname) throws Exception {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        fos = new FileOutputStream(fname);
        out = new ObjectOutputStream(fos);
        out.writeObject(this);
        out.close();
    }

//    public static void main(String[] args) throws Exception {
//
//        //getPermutations("test");
//
//        File db = new File("reuters.db");
//        DocumentIndex index = null;
//        if (!db.exists()) {
//            System.out.print("Creating index...");
//            index = DocumentIndex.createIndex("reutersTXT/");
//            index.save("reuters.db");
//            System.out.println("Done.");
//        } else {
//            System.out.print("Loading index...");
//            index = DocumentIndex.load("reuters.db");
//            System.out.println("Done.");
//        }
//
//    }
}