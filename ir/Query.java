/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.HashSet;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public HashMap<String, Double> weights = new HashMap<String, Double>(); // Change to hashmap to make mapping between terms and their weights easier
    
    // Constants 
    public static final double alpha = 0.1;
    public static final double beta = 0.9;

    /**
     *  Creates a new empty Query 
     */
    public Query() {
	}
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
		StringTokenizer tok = new StringTokenizer( queryString );
		while ( tok.hasMoreTokens() ) {
			String token = tok.nextToken();
			terms.add(token);
			weights.put(token, new Double(1));
		}
		normalize();
	}
	
	/**
     *  Normalization
     */
	
	private void normalize()
    {
        for(String term : terms)
        {
            weights.put(term, weights.get(term)/terms.size());
        }
    }
	
    /**
     *  Returns the number of terms
     */
    public int size() {
		return terms.size();
	}
	
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
		Query queryCopy = new Query();
		queryCopy.terms = (LinkedList<String>) terms.clone();
		queryCopy.weights = (HashMap<String, Double>) weights.clone();
		return queryCopy;
	}
	
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
		// results contain the ranked list from the current search
		// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
		
		// Alpha multiplication
		
		for(String term : terms) {
            weights.put(term, weights.get(term)*alpha);
        }
        
        int numberRelevantDocs = 0;
        for(int i=0;i<docIsRelevant.length;i++) {
        	if(docIsRelevant[i]) {
        		numberRelevantDocs++;
        	}
        }
        
        for(int i=0;i<docIsRelevant.length;i++) {
        	if(docIsRelevant[i]) {
        		int docID = results.get(i).docID;
                HashSet<String> currDocRelevant = indexer.index.terms.get(docID);
                int length = currDocRelevant.size();
                int numberOfDocs = indexer.index.docLengths.keySet().size();
                int numberOfDocsMaxWithTerm = (int)(((double)numberOfDocs)/5.0); // Param number of docs with term max
                for(String term : currDocRelevant)
                {
                    PostingsList pl = indexer.index.getPostings(term);
                    
                    
                    // SPEED UP !!!!!!!! 
                    if(numberOfDocsMaxWithTerm<pl.size() && SearchGUI.speed_up)
                    {
                        continue;
                    }
                    
                    // Tf
                    double tf = 1;
                    LinkedList<PostingsEntry> list = pl.get_list();
                    for(PostingsEntry pe : list)
                    {
                        if(pe.docID == docID)
                        {
                            tf = pe.list.size();
                            break;
                        }
                    }
                    
                    tf = (double)(tf/length); // Normalization

                    // Rocchio
                    double termScore = tf*beta*(1.0/numberRelevantDocs);
                    if(!terms.contains(term))
                    {                    	
                        terms.addLast(term);
                        weights.put(term,termScore);
                    }
                    else
                    {
                        weights.put(term,weights.get(term)+termScore);
                    }
                }
        	}
        }
        
        System.err.println(terms.size());
    }
}

    
