/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  
 
//package pagerank;

import java.util.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Collections;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
		//
		//   YOUR CODE HERE
		//
		
		double c = 0.85;
		
		int i=0;
		// Number of documents
		int N = numberOfDocs;
		int iter = 0;
		double x[] = new double[N];
		
		for(i=0;i<N;i++) {
			x[i] = 1.0/N;
		}
		double x2[] = new double[N];
		for(i=0;i<N;i++) {
			x2[i] = 0;
		}
		double diff = 0;
		for(i=0;i<N;i++) {
			diff += Math.abs(x[i] - x2[i]);
		}
		
		System.out.println(numberOfSinks);
		
		while(diff>EPSILON && iter<MAX_NUMBER_OF_ITERATIONS) {
			
			// ToDo
			for(i=0;i<N;i++) {
				Hashtable<Integer,Boolean> links = link.get(i);
				if(links != null) {
					Iterator<Map.Entry<Integer,Boolean>> it = links.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry<Integer,Boolean> j = it.next();
						x2[j.getKey()] += x[i]*c/out[i];
					}
					x2[i] += numberOfSinks/N/N;
					x2[i] += (1-c)/N;
				}
			}
			
			diff = 0;
			for(i=0;i<N;i++) {
				diff += Math.abs(x[i] - x2[i]);
			}
			
			System.arraycopy(x2, 0, x, 0, x2.length);
			Arrays.fill(x2, 0);
			
			iter++;
		}
		
		LinkedList<Document> list = new LinkedList<Document>();
		for(i=0;i<N;i++) {
			Document doc = new Document(x[i],docName[i]);
			list.add(doc);
		}
		System.out.println(list.size());
		Collections.sort(list);
	
		for(i=0;i<100;i++) {
			int num = i+1;
			Document curr = list.get(i);
			System.out.println(num + ".  "+curr.docNumber+"\t"+curr.score);
		}	
    }


    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
