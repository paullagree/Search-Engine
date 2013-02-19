/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  

package ir;

import com.larvalabs.megamap.MegaMapManager;
import com.larvalabs.megamap.MegaMap;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;
import java.util.ListIterator;


public class MegaIndex implements Index {

    /** 
     *  The index as a hash map that can also extend to secondary 
     *	memory if necessary. 
     */
    private MegaMap index;


    /** 
     *  The MegaMapManager is the user's entry point for creating and
     *  saving MegaMaps on disk.
     */
    private MegaMapManager manager;


    /** The directory where to place index files on disk. */
    private static final String path = "./index";


    /**
     *  Create a new index and invent a name for it.
     */
    public MegaIndex() {
	try {
	    manager = MegaMapManager.getMegaMapManager();
	    index = manager.createMegaMap( generateFilename(), path, true, false );
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	}
    }


    /**
     *  Create a MegaIndex, possibly from a list of smaller
     *  indexes.
     */
    public MegaIndex( LinkedList<String> indexfiles ) {
	try {
	    manager = MegaMapManager.getMegaMapManager();
	    if ( indexfiles.size() == 0 ) {
		// No index file names specified. Construct a new index and
		// invent a name for it.
		index = manager.createMegaMap( generateFilename(), path, true, false );
		
	    }
	    else if ( indexfiles.size() == 1 ) {
		// Read the specified index from file
		index = manager.createMegaMap( indexfiles.get(0), path, true, false );
		HashMap<String,String> m = (HashMap<String,String>)index.get( "..docIDs" );
		if ( m == null ) {
		    System.err.println( "Couldn't retrieve the associations between docIDs and document names" );
		}
		else {
		    docIDs.putAll( m );
		}
	    }
	    else {
		// Merge the specified index files into a large index.
		MegaMap[] indexesToBeMerged = new MegaMap[indexfiles.size()];
		for ( int k=0; k<indexfiles.size(); k++ ) {
		    System.err.println( indexfiles.get(k) );
		    indexesToBeMerged[k] = manager.createMegaMap( indexfiles.get(k), path, true, false );
		}
		index = merge( indexesToBeMerged );
		for ( int k=0; k<indexfiles.size(); k++ ) {
		    manager.removeMegaMap( indexfiles.get(k) );
		}
	    }
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	}
    }


    /**
     *  Generates unique names for index files
     */
    String generateFilename() {
	String s = "index_" + Math.abs((new java.util.Date()).hashCode());
	System.err.println( s );
	return s;
    }


    /**
     *   It is ABSOLUTELY ESSENTIAL to run this method before terminating 
     *   the JVM, otherwise the index files might become corrupted.
     */
    public void cleanup() {
	// Save the docID-filename association list in the MegaMap as well
	index.put( "..docIDs", docIDs );
	// Shutdown the MegaMap thread gracefully
	manager.shutdown();
    }



    /**
     *  Returns the dictionary (the set of terms in the index)
     *  as a HashSet.
     */
    public Set getDictionary() {
	return index.getKeys();
    }


    /**
     *  Merges several indexes into one.
     */
    MegaMap merge( MegaMap[] indexes ) {
		try {
	    	MegaMap res = manager.createMegaMap( generateFilename(), path, true, false );
	    //
	    //  YOUR CODE HERE
	    //
	    	for (MegaMap index : indexes){
	    	HashMap<String,String> hm = (HashMap<String,String>)index.get( "..docIDs" );
	    	docIDs.putAll(hm);
	    	for(String k: (Set<String>)index.getKeys()){
	    		if (k.equals("..docIDs"))
	    			continue;
	    		if (res.hasKey(k)){
	    			PostingsList list1 = (PostingsList)(res.get(k));
	    			PostingsList list2 = (PostingsList)(index.get(k));
	    			PostingsList mergedList = new PostingsList();
	    			if (list1 == null || list2 == null)
	    				return null;
	    			// Merge list1 and list2 in results;
	    			ListIterator<PostingsEntry> it1 = list1.get_list().listIterator(0);
	    			ListIterator<PostingsEntry> it2 = list2.get_list().listIterator(0);
	    			PostingsEntry current1 = null;
	    			PostingsEntry current2 = null;
	    			int compteur = 0; // 0 => compteur both, 1 => only it1, 2 => only it2
	    			while (it1.hasNext() || it2.hasNext()){
	    				if (compteur == 0 || compteur == 1){
	    					if (it1.hasNext())
	    						current1 = it1.next();
	    					else
	    						current1 = null;
	    				}
	    				if (compteur == 0 || compteur == 2){
	    					if (it2.hasNext())
	    						current2 = it2.next();
	    					else
	    						current2 = null;
	    				}
	    				if (current1 == null || (current2 != null && current2.docID < current1.docID)) {
	    					mergedList.put(current2.docID, current2.list);
	    					compteur = 2;
	    				}
	    				else if (current2 == null || current1.docID < current2.docID) {
	    					mergedList.put(current1.docID, current1.list);
	    					compteur = 1;
	    				}
	    				else {
	    					mergedList.put(current1.docID, current1.list);
	    					compteur = 0;
	    				}
	    			}
	    			res.put(k, mergedList);
	    		}
	    		else{
	    			res.put(k, index.get(k));
	    		}
	    	}
	    	
	    }
	    	return res;
		}
		catch ( Exception e ) {
	    	e.printStackTrace();
	    	return null;
		}
		
    }

    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
	//
	//  COPY THE CODE FROM YOUR HashedIndex CLASS HERE
	//
		try {
			PostingsList list = (PostingsList)index.get(token);
			if(list==null) {
				list = new PostingsList();
				index.put(token,list);
			}
			list.put(docID,offset,false);
		}
		catch(Exception e) {}
		
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
		try {
	    	return (PostingsList)index.get( token );
		}
		catch( Exception e ) {
	    	return new PostingsList();
		}
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType ) {
	//
	//  REPLACE THE STATEMENT BELOW WITH THE CODE FROM
	//  YOUR HashedIndex CLASS
	//
		PostingsList result = null;
    	if(queryType == Index.INTERSECTION_QUERY || queryType == Index.PHRASE_QUERY) { //Intersection query
			for(int i=0; i<query.terms.size(); i++) {
				//System.out.print("<"+query.terms.get(i)+"> ");
				if(result == null) {
					if(getPostings(query.terms.get(i)) == null)
						result = new PostingsList();
					else
						result = getPostings(query.terms.get(i));
				} 
				else {
					PostingsList toMerge = getPostings(query.terms.get(i));
					result = result.intersection(toMerge, queryType == Index.PHRASE_QUERY);
				}
			}
			//System.out.println();
			return result;
		} 
		else {
			System.out.print("ToDo");
			return null; //next labs
		}
    }

}










 



