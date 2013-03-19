/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  


package ir;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.HashSet;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class NGramIndex implements Index {

	/** The index as a hashtable. */
	private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
	public HashMap<Integer,Integer> numberDocs = new HashMap<Integer,Integer>();
	
	/** Indexes used. */
    public LinkedList<Index> indexes = new LinkedList<Index>();
    
    /** Number of indexes */
    public static final int numberIndexes = 2;
    
    /** Number minimum of anwsers */
    public static final int K = 10;
    
    public NGramIndex() {
    	indexes.addFirst(new HashedIndex());
    	indexes.addFirst(new BiwordIndex());
    }

	/**
	 *  Inserts this token in the index.
	 */

	public void insert( String token, int docID, int offset ) {
		for(int i=0;i<numberIndexes;i++) {
			indexes.get(i).insert(token,docID,offset);
		}
	}


	/**
	 *  Returns the postings for a specific term, or null
	 *  if the term is not in the index.
	 */
	public PostingsList getPostings( String token ) {

		//return index.get(token);
		return null;
	}


	/**
	 *  Searches the index for postings matching the query.
	 */
	public PostingsList search( Query query, int queryType, int rankingType ) {

		PostingsList result = null;
		int n = 0;
		int resultSize = 0;
		
		while(resultSize<K && n<numberIndexes) {
			PostingsList current = indexes.get(n).search(query,queryType,rankingType);
			if(result == null)
				result = current;
			else
				result = result.reunion(current);
			for(int i=0; i < result.get_list().size(); i++) {
    			result.get_list().get(i).score *= 10;
    		}
			if(result.size()>K) {
				break;
			}
			n++;
		}
		PostingsList result2 = new PostingsList();
		
		boolean found = false;
		
		for(PostingsEntry pe : result.get_list()) {
			found = false;
			for(PostingsEntry pe2 : result2.get_list()) {
				if(pe.docID == pe2.docID) {
					pe2.score += pe.score;
					found = true;
					break;
				}
			}
			if(found == false) {
				result2.put(pe.docID, pe.score, 0, 0);
			}
		}
		result2.sortPosList();
		return result2;
	}

	public void addTerm(int docID, String token)
	{
		// Empty
	}

	/**
	 *  No need for cleanup in a HashedIndex.
	 */
	public void cleanup() {
	}
}
