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
//import java.util.Collections.sort;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class BiwordIndex implements Index {

	/** The index as a hashtable. */
	private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
	public HashMap<Integer,Integer> numberDocs = new HashMap<Integer,Integer>();

	private String lastTerm;
	private int lastDocID;

	public BiwordIndex() {
		lastTerm = "";
		lastDocID = -1;
	}


	/**
	 *  Inserts this token in the index.
	 */

	public void insert( String token, int docID, int offset ) {

		if(docID != lastDocID) {
			lastDocID = docID;
			lastTerm = token;
		}

		else {

			String biWord = lastTerm +"+" + token;
			lastTerm = token;

			if(numberDocs.containsKey(docID)) {
				Integer n = numberDocs.get(docID);
				numberDocs.put(docID,n+1);
			}
			else
				numberDocs.put(docID,1);
			PostingsList list = index.get(biWord);
			if(list==null) {
				list = new PostingsList();
				index.put(biWord,list);
			}
			list.put(docID,offset,true);

		}
	}


	/**
	 *  Returns the postings for a specific term, or null
	 *  if the term is not in the index.
	 */
	public PostingsList getPostings( String token ) {

		return index.get(token);
	}


	/**
	 *  Searches the index for postings matching the query.
	 */
	public PostingsList search( Query query, int queryType, int rankingType ) {

		PostingsList result = null;
		String oldTerm = null;
		double time = 0;

		if(query.terms.size() < 2) {
			return null;
		}

		/*****************************
		 ****CASE OF TF_IDF RANKING****
		 *****************************/

		if(rankingType == Index.TF_IDF) {
			oldTerm = null;
			if(queryType == Index.INTERSECTION_QUERY || queryType == Index.PHRASE_QUERY) {
				for(int i=0; i<query.terms.size(); i++) {
					if(oldTerm == null) {
						oldTerm = query.terms.get(i);
					}
					else {
						String tmp = oldTerm+"+"+query.terms.get(i);
						oldTerm = query.terms.get(i);
						if(result == null) {
							if(getPostings(tmp) == null)
								result = new PostingsList();
							else
								result = getPostings(tmp);
						} 
						else {
							PostingsList toMerge = getPostings(tmp);
							result = result.intersection(toMerge, queryType == Index.PHRASE_QUERY);
						}
					}
				}
				return result;
			} 
			else if(queryType == Index.RANKED_QUERY){
				time = System.nanoTime();
				PostingsList tabScoresTerms[] = new PostingsList[query.terms.size()];
				String biTermQuery = "";
				System.err.println("Query : "+query.terms.size());
				//System.err.println("Query : "+query.terms.size());
				for(int i=0; i<query.terms.size(); i++) {
					if(oldTerm == null) {
						oldTerm = query.terms.get(i);
					}
					else {
						biTermQuery = oldTerm+"+"+query.terms.get(i);
						oldTerm = query.terms.get(i);
						if(getPostings(biTermQuery) == null)
							tabScoresTerms[i] = new PostingsList();
						else {
							tabScoresTerms[i] = getPostings(biTermQuery);
							tabScoresTerms[i].computeScore(numberDocs, docLengths/*, query, biTermQuery*/);
						}
					}
				}

				result = tabScoresTerms[1];

				for(int i=2; i<query.terms.size(); i++) {
					//System.err.println(result.size());
					result = result.reunion(tabScoresTerms[i]); //In PostingsList
				}

				PostingsList result2 = new PostingsList();
				ListIterator<PostingsEntry> resultIterator = result.get_list().listIterator(0);
				while(resultIterator.hasNext()) {
					PostingsEntry cur = resultIterator.next();
					result2.put(cur.docID,cur.score,cur.sommeCarre, cur.squareQuery);
				}

				result2.sortPosList();
				time = System.nanoTime() - time;
				System.err.println(time+" nanoseconds");
				return result2;
			}
			else {
				//ToDO
				return null;
			}
		}

		/*****************************
		 ***CASE OF PAGERANK RANKING***
		 *****************************/

		else if(rankingType == Index.PAGERANK) {
			//ToDo
			if(queryType == Index.RANKED_QUERY){
				PostingsList tabScoresTerms[] = new PostingsList[query.terms.size()];
				for(int i=0; i<query.terms.size(); i++) {
					if(getPostings(query.terms.get(i)) == null)
						tabScoresTerms[i] = new PostingsList();

					else {
						tabScoresTerms[i] = getPostings(query.terms.get(i));
						tabScoresTerms[i].computeScore(numberDocs, docLengths);
					}
				}

				result = tabScoresTerms[0];

				for(int i=1; i<query.terms.size(); i++) {
					System.err.println(result.size());
					result = result.reunion(tabScoresTerms[i]); //In PostingsList
				}

				PostingsList result2 = new PostingsList();
				ListIterator<PostingsEntry> resultIterator = result.get_list().listIterator(0);

				while(resultIterator.hasNext()) {
					PostingsEntry cur = resultIterator.next();
					//System.err.println(Index.docIDs.get(""+cur.docID)+" pour docID  "+cur.docID);  Line to know the name of files used !!!!!!!!!!!!
					result2.put(cur.docID,SearchGUI.pageRank.getScore(Index.docIDs.get(""+cur.docID)),cur.sommeCarre, cur.squareQuery);
				}

				result2.sortPosList();
				
				return result2;
			}
			else {
				//ToDO
				return null;
			}
		}

		/*****************************
		 *CASE OF COMBINATION RANKING**
		 *****************************/

		else {
			if(queryType == Index.RANKED_QUERY){
				PostingsList tabScoresTerms[] = new PostingsList[query.terms.size()];
				for(int i=0; i<query.terms.size(); i++) {
					if(getPostings(query.terms.get(i)) == null)
						tabScoresTerms[i] = new PostingsList();
					else {
						tabScoresTerms[i] = getPostings(query.terms.get(i));
						tabScoresTerms[i].computeScore(numberDocs, docLengths);
					}
				}

				result = tabScoresTerms[0];

				for(int i=1; i<query.terms.size(); i++) {
					System.err.println(result.size());
					result = result.reunion(tabScoresTerms[i]); //In PostingsList
				}

				PostingsList result2 = new PostingsList();
				ListIterator<PostingsEntry> resultIterator = result.get_list().listIterator(0);

				while(resultIterator.hasNext()) {
					PostingsEntry cur = resultIterator.next();
					//System.err.println(Index.docIDs.get(""+cur.docID)+" pour docID  "+cur.docID);  //Line to know the name of files used !!!!!!!!!!!!
					result2.put(cur.docID,cur.score+3*SearchGUI.pageRank.getScore(Index.docIDs.get(""+cur.docID)),cur.sommeCarre, cur.squareQuery);
				}

				result2.sortPosList();
				return result2;
			}
			else {
				//ToDO
				return null;
			}
		}
	}

	public void addTerm(int docID, String token)
	{
		HashSet<String> tmp = terms.get(docID);
		if(tmp == null)
		{
			tmp = new HashSet<String>();
			terms.put(docID, tmp);
		}
		tmp.add(token);
	}

	/**
	 *  No need for cleanup in a HashedIndex.
	 */
	public void cleanup() {
	}
}
