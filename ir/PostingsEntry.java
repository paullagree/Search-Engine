/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.HashSet;
import java.util.HashMap;
import java.io.Serializable;
import java.util.LinkedList;
import java.lang.Math;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    public LinkedList<Integer> list;
    public double sommeCarre;
    public double squareQuery;

    public PostingsEntry(int docID) {
    	this.docID = docID;
    	list = new LinkedList<Integer>();
    	this.sommeCarre = 0;
    	this.squareQuery = 0;
    }
    
    public PostingsEntry(int docID, LinkedList<Integer> offsets) {
    	this.docID = docID;
    	this.list = offsets;
    	this.sommeCarre = 0;
    	this.squareQuery = 0;
    }
    
    public PostingsEntry(int docID, double score, double sommeCarre, double squareQuery) {
    	this.docID = docID;
    	list = new LinkedList<Integer>();
    	this.score = score;
    	this.sommeCarre = sommeCarre;
    	this.squareQuery = squareQuery;
    }
    
    public PostingsEntry() {
    	this(0);
    }

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
		return Double.compare( other.score, score );
    }

    public int getDocID() {
		return docID;
    }
    
    public void add(int offset) {
    	list.add(offset);
    }
    
    public void computeScore(int nbDocsWithWord, HashMap<Integer,Integer> numberDocs, HashMap<String,Integer> docLengths) {
    	// Compute here the new score
    	double N = (double)numberDocs.size();
    	this.score = ((double)list.size())*Math.log(N/nbDocsWithWord)/docLengths.get(""+docID);
    	this.sommeCarre = this.score*this.score;
    	this.squareQuery = 1;
    }
    
    public void computeScore(int nbDocsWithWord, HashMap<Integer,Integer> numberDocs, HashMap<String,Integer> docLengths, Query query, String termQuery) {
    	// Compute here the new score with weights for query terms
    	double N = (double)numberDocs.size();
    	this.score = (query.weights.get(termQuery))*((double)list.size())*Math.log(N/nbDocsWithWord)/docLengths.get(""+docID);
    	this.sommeCarre = this.score*this.score;
    	this.squareQuery = 1;
    }
    
    public void normalizeScore() {
    	this.score = this.score/(Math.sqrt(this.squareQuery)*Math.sqrt(this.sommeCarre));
    }

}

    
