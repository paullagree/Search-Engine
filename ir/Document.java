/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

package ir;

import java.util.*;
import java.io.*;
import java.io.Serializable;
import java.lang.Math;

public class Document implements Comparable<Document>, Serializable{

	public double score;
	public String docNumber;
	
	public Document( double score, String docNumber ) {
		this.score = score;
		this.docNumber = docNumber;
	}
	
	public int compareTo( Document other ) {
		return Double.compare( other.score, score );
    }
	
}
