/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.HashMap;
import java.util.LinkedList;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

    /**  Number of postings in this list  */
    public int size() {
		return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
		return list.get( i );
    }
    
    public LinkedList<PostingsEntry> get_list() {
    	return list;
    }
    
    void computeScore(HashMap<Integer,Integer> numberDocs, HashMap<String,Integer> docLengths) {
    	ListIterator<PostingsEntry> it = list.listIterator(0);
    	int nbDocsWithWord = list.size();
    	while(it.hasNext()) {
			PostingsEntry cur = it.next();
			cur.computeScore(nbDocsWithWord, numberDocs, docLengths);
		}
		
    }
    
    void sortPosList() {
    	Collections.sort(list);
    }
    
    public void put(int docID, double score, double sommeCarre, double squareQuery) {
		PostingsEntry pe = new PostingsEntry(docID, score, sommeCarre, squareQuery);
		list.add(pe);
	}
    
    public void put(int docID, int offset, boolean beginning) {
		if(beginning) {
			if(list.size() == 0 || list.getLast().docID != docID) {
				PostingsEntry pe = new PostingsEntry(docID);
				list.add(pe);
				pe.add(offset);
			} 
			else {
				list.getLast().add(offset);
			}
		} 
		else {
			ListIterator<PostingsEntry> it = list.listIterator(0);
			boolean found = false;
			
			while(it.hasNext()) {
				PostingsEntry cur = it.next();
				if(cur.docID == docID) {
					found = true;
					cur.add(offset);
					break;
				} 
				else if(cur.docID > docID) {
					found = true;
					PostingsEntry pe = new PostingsEntry();
					pe.docID = docID;
					pe.add(offset);
					if(!it.hasPrevious()) {
						list.addFirst(pe);
					} 
					else {
						it.previous();
						it.add(pe);
					}
					break;
				}
			}
			
			if(!found) {
				PostingsEntry pe = new PostingsEntry();
				pe.docID = docID;
				pe.add(offset);
				list.add(pe);
			}
		}
	}
	
	public void put(int docID, LinkedList<Integer> offsets) {
		
		
			ListIterator<PostingsEntry> it = list.listIterator(0);
			boolean found = false;
			
			while(it.hasNext()) {
				PostingsEntry cur = it.next();
				if(cur.docID == docID) {
					found = true;
					break;
				} 
				else if(cur.docID > docID) {
					found = true;
					PostingsEntry pe = new PostingsEntry(docID,offsets);
					if(!it.hasPrevious()) {
						list.addFirst(pe);
					} 
					else {
						it.previous();
						it.add(pe);
					}
					break;
				}
			}
			
			if(!found) {
				PostingsEntry pe = new PostingsEntry(docID,offsets);
				list.add(pe);
			}
		
	}

	public PostingsList intersection(PostingsList toMerge, boolean phrase) {
		PostingsList result = new PostingsList();
		
		if(toMerge == null) {
			return result;
		}
		
		ListIterator<PostingsEntry> first = this.list.listIterator(0);
		ListIterator<PostingsEntry> second = toMerge.list.listIterator(0);
		
		if(!first.hasNext() || !second.hasNext()) {
			return result;
		}
		
		PostingsEntry curFirst = first.next();
		PostingsEntry curSecond = second.next();
		while(curFirst != null && curSecond != null) {
			if(curFirst.docID < curSecond.docID) {
				if(first.hasNext()) curFirst = first.next();
				else curFirst = null;
			} 
			else if(curFirst.docID > curSecond.docID) {
				if(second.hasNext()) curSecond = second.next();
				else curSecond = null;
			} 
			else {
				if(phrase) {
					
					//Phrase intersection
					ListIterator<Integer> firstPost = curFirst.list.listIterator(0);
					ListIterator<Integer> secondPost = curSecond.list.listIterator(0);
					
					int curFirstPost = firstPost.next()+1;
					int curSecondPost = secondPost.next();
					while(true) {
						if(curFirstPost < curSecondPost) {
							if (firstPost.hasNext()) 
								curFirstPost = firstPost.next()+1;
							else 
								break;
						} 
						else if(curFirstPost > curSecondPost) {
							if (secondPost.hasNext()) 
								curSecondPost = secondPost.next();
							else 
								break;
						} 
						else {
							result.put(curFirst.docID, curFirstPost, true); 
							if (firstPost.hasNext()) 
								curFirstPost = firstPost.next()+1;
							else 
								break;
							if (secondPost.hasNext()) 
								curSecondPost = secondPost.next();
							else 
								break;
						}
					}
					
				} 
				else {
					//Simple intersection
					result.put(curFirst.docID, -1, true); //We don't care about the offsets
				}
				
				if(first.hasNext()) 
					curFirst = first.next();
				else 
					curFirst = null;
				if(second.hasNext()) 
					curSecond = second.next();
				else 
					curSecond = null;
			}
		}
		
		return result;
    }
    
    public PostingsList reunion(PostingsList toMerge) {
    	/*****************
    	REFAIRE LES ADDs
    	*****************/
    	
    	PostingsList result = new PostingsList();

		ListIterator<PostingsEntry> first = this.list.listIterator(0);
		ListIterator<PostingsEntry> second = toMerge.list.listIterator(0);
		
		PostingsEntry curFirst = null;
		PostingsEntry curSecond = null;
		
		if(first.hasNext()) {
			curFirst = first.next();
		}
			
		if(second.hasNext()) {
			curSecond = second.next();
		}	
		
		while(curFirst != null && curSecond != null) {
			if(curFirst.docID < curSecond.docID) {
				result.put(curFirst.docID,curFirst.score,curFirst.sommeCarre, curFirst.squareQuery);
				if(first.hasNext()) curFirst = first.next();
				else curFirst = null;
			} 
			else if(curFirst.docID > curSecond.docID) {
				result.put(curSecond.docID,curSecond.score,curSecond.sommeCarre, curSecond.squareQuery);
				if(second.hasNext()) curSecond = second.next();
				else curSecond = null;
			}
			else {
				//Case of equality, we create a new Entry which represents more or less the cosine
				result.put(curSecond.docID,curFirst.score + curSecond.score,curFirst.sommeCarre + curSecond.sommeCarre, curFirst.squareQuery + curSecond.squareQuery);
				
				if(first.hasNext()) 
					curFirst = first.next();
				else 
					curFirst = null;
				if(second.hasNext()) 
					curSecond = second.next();
				else 
					curSecond = null;
			}
		}
			
		if(curFirst == null) {
			while(curSecond != null) {
				result.put(curSecond.docID,curSecond.score,curSecond.sommeCarre, curSecond.squareQuery);
				if(second.hasNext()) 
					curSecond = second.next();
				else 
					curSecond = null;
			}
		}
		else {
			while(curFirst != null) {
				result.put(curFirst.docID,curFirst.score,curFirst.sommeCarre, curFirst.squareQuery);
				if(first.hasNext()) 
					curFirst = first.next();
				else 
					curFirst = null;
			}
		}
			
		return result;

    }

}
	

			   
