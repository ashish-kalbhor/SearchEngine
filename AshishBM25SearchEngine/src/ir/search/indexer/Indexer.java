package ir.search.indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class creates an Inverted Index from the given corpus.
 * @author Ashish
 *
 */
public class Indexer 
{
	// Data Structures
	public static IndexMap index = new IndexMap();
	public static HashMap<String, Integer> docLength = new HashMap<String, Integer>();
	
	/**
	 * Load the corpus text file and populate the index and doclength hashmaps.
	 * @param indexPath
	 * @throws IOException
	 */
	public static void loadCorpus(String indexPath) throws IOException
	{
		BufferedReader readCorpus = new BufferedReader(new FileReader(indexPath));
		String line = readCorpus.readLine();
		String docId = "";
		
		while(null != line)
		{
			// If line is a document
			if(isDocument(line))
			{
				docId = getDocId(line);
				docLength.put(docId, 0);
			}
			// Line has tokens
			else
			{
				ArrayList<String> tokens = getTerms(line);
				docLength.put(docId, docLength.get(docId)+tokens.size());
				addPostings(docId,tokens);
			}
			
			line = readCorpus.readLine();
		}
		readCorpus.close();
		
	}
	
	/**
	 * Checks if the line is a document.
	 * @param line
	 * @return
	 */
	public static boolean isDocument(String line)
	{
		if(line.matches("# [0-9]+"))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the DocId from line.
	 * @param line
	 * @return
	 */
	public static String getDocId(String line)
	{
		return line.split(" ")[1];
	}
	
	/**
	 * Returns the ArrayList of valid tokens found on each line.
	 * @param line
	 * @return
	 */
	public static ArrayList<String> getTerms(String line)
	{
		String terms[] = line.split(" ");
		ArrayList<String> t = new ArrayList<String>();
		for(String term : terms)
		{
			if(term.matches(".*[a-zA-Z]+.*"))
			{
				t.add(term);
			}
		}
		return t;
	}
	
	/**
	 * Add the Posting created to the index hashmap.
	 * @param docId
	 * @param terms
	 */
	public static void addPostings(String docId, ArrayList<String> terms)
	{
		for(String term : terms)
		{
			index.appendPosting(term, docId);
		}
	}
	
	/**
	 * Writes the hashmap values into the text files.
	 * @param indexFilePath
	 * @param docFilePath
	 * @throws IOException
	 */
	public static void createIndexFiles(String indexFilePath, String docFilePath) throws IOException
	{
		BufferedWriter indexFile = new BufferedWriter(new FileWriter(indexFilePath));
		BufferedWriter docFile = new BufferedWriter(new FileWriter(docFilePath));
		
		Iterator<String> words = index.keySet().iterator();
		
		while(words.hasNext())
		{
			String word = words.next();
			indexFile.write(word + ":" + toFormattedText(index.get(word)).toString() + "\n");
		}
		
		Iterator<String> docIds = docLength.keySet().iterator();
		
		while(docIds.hasNext())
		{
			String docId = docIds.next();
			docFile.write(docId + "," + docLength.get(docId) + "\n");
		}
		
		indexFile.close();
		docFile.close();
	}
	
	/**
	 * Returns a complete String of docset in appropriate format
	 * to be written in the index file.
	 * @param docset
	 */
	public static StringBuffer toFormattedText(HashMap<String, Integer> docset)
	{
		StringBuffer sb = new StringBuffer();
		Iterator<String> docs = docset.keySet().iterator();
		while(docs.hasNext())
		{
			String doc = docs.next();
			sb.append(doc + "=" + docset.get(doc) + ",");
		}
		return sb;
	}
	
	/**
	 * Main method that is executed.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException 
	{
		// Default file names
		String corpusFileName = "tccorpus.txt";
		String indexFileName = "index.out";
		String docLengthFileName = "doclength.txt";
		
		if(args.length == 2)
		{
			corpusFileName = args[0];
			indexFileName = args[1];
		}
		
		loadCorpus(corpusFileName);
		createIndexFiles(indexFileName,docLengthFileName);
	}

}
