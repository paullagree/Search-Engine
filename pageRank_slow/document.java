/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

package pagerank

import java.util.*;
import java.io.*;

public class Document{

	public double score;
	public int docNumber;
	
	public PageRank( double score, double docNumber ) {
		this.score = score;
		this.docNumber = docNumber;
	}
	
}
