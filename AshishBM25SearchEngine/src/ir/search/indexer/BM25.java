package ir.search.indexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implements the BM25 algorithm for ranking the documents
 * for given queries using index data structure.
 * @author Ashish
 *
 */
public class BM25 
{
	// Data Structures
	public static IndexMap index = new IndexMap();
	public static HashMap<String, Integer> docLength = new HashMap<String, Integer>();
	public static HashMap<Integer, HashMap<String, Integer>> queries = new HashMap<Integer, HashMap<String, Integer>>();
	public static HashMap<String, Double> docScore = new HashMap<String, Double>(); 
	
	// BM25 constants
	public static double k1 = 1.2;
	public static double k2 = 100.0;
	public static double b = 0.75;
	public static double avdl = 0;
	
	/**
	 * Load the Index hashmap from index file.
	 * @param indexFilePath
	 * @throws IOException
	 */
	public static void loadIndex(String indexFilePath) throws IOException
	{
		BufferedReader indexFile = new BufferedReader(new FileReader(indexFilePath));
		
		String line = indexFile.readLine();
		
		while(null != line)
		{
			String indexes[] = line.split(":");
			index.put(indexes[0], loadDocForWord(indexes[1]));
			line = indexFile.readLine();
		}
		indexFile.close();
	}
	
	/**
	 * Load the docLength hashmap from doclength file.
	 * @param docLengthFilePath
	 * @throws IOException
	 */
	public static void loadDocLength(String docLengthFilePath) throws IOException
	{
		BufferedReader docLengthFile = new BufferedReader(new FileReader(docLengthFilePath));
		
		String line = docLengthFile.readLine();
		
		while(null != line)
		{
			String doc[] = line.split(",");
			docLength.put(doc[0], Integer.parseInt(doc[1]));
			docScore.put(doc[0], 0.0);
			line = docLengthFile.readLine();
		}
		docLengthFile.close();
	}
	
	public static HashMap<String, Integer> loadDocForWord(String docset)
	{
		HashMap<String, Integer> postings = new HashMap<String, Integer>();
		String docs[] = docset.split(",");
		
		for(String doc : docs)
		{
			String posting[] = doc.split("=");
			postings.put(posting[0], Integer.parseInt(posting[1]));
		}
		
		return postings;
	}
	
	/**
	 * Load Queries hashmap from queries text file.
	 * @param queryFilePath
	 * @throws IOException
	 */
	public static void loadQueries(String queryFilePath) throws IOException
	{
		BufferedReader queryFile = new BufferedReader(new FileReader(queryFilePath));
		
		String line = queryFile.readLine();
		int queryCount = 1;
		
		while(null != line)
		{
			HashMap<String, Integer> qterm = new HashMap<String, Integer>(); 
			String queryterms[] = line.split(" ");
			
			for(String q : queryterms)
			{
				if(null == qterm.get(q))
				{
					qterm.put(q, 1);
				}
				else
				{
					qterm.put(q, qterm.get(q)+1);
				}
			}
			queries.put(queryCount++, qterm);
			line = queryFile.readLine();
		}
		queryFile.close();
	}
	
	/**
	 * Compute K value
	 * @param dl
	 * @return
	 */
	public static double getKparam(int dl)
	{
		return (double)(k1 * ((1-b) + b*(double)(dl/avdl)));
	}
	
	/**
	 * Compute the average document length
	 * @return
	 */
	public static double getAvdl()
	{
		int totalDocLen = 0;
		
		Iterator<String> docs = docLength.keySet().iterator();
		
		while(docs.hasNext())
		{
			String doc = docs.next();
			totalDocLen += docLength.get(doc);
		}
		return (double)totalDocLen / docLength.size();
	}
	
	/**
	 * Main method to compute the doc score.
	 */
	public static void computeDocScore()
	{
		//Iterator<String> docs = docScore.keySet().iterator();
		double rankingScore = 0.0;
		int N = docScore.size();
		int dl = 0;
		
		Iterator<Integer> queryTerms = queries.keySet().iterator();
		while(queryTerms.hasNext())
		{
			Integer queryId = queryTerms.next();
			Iterator<String> docs = docLength.keySet().iterator();
			while(docs.hasNext())
			{
				// Take one document.
				String docId = docs.next();
				rankingScore = 0.0;
				// Length of the document
				dl = docLength.get(docId);
				
				Iterator<String> q = queries.get(queryId).keySet().iterator();
				while(q.hasNext())
				{
					String qi = q.next();
					rankingScore += getBM25Score(queryId,docId, qi,dl,N);
				}
								
				docScore.put(docId, rankingScore);
			}
			// DocScore for the query
			writeTopRanked(100);
			docScore.clear();
		}
		
	}
	
	/**
	 * Writes the top ranked documents.
	 * @param top
	 */
	public static void writeTopRanked(int top)
	{
		TreeMap<String, Double> sortedDocScore = SortByValue(docScore);
		
		Iterator<String> docIds = sortedDocScore.keySet().iterator();
		
		while(docIds.hasNext() && top > 0)
		{
			String docId = docIds.next();
			System.out.println(top + ". " + docId + "=>" + docScore.get(docId));
			top--;
		}	
	}
	
	/**
	 * Compute the BM25 score per query term.
	 * @param qid
	 * @param docId
	 * @param q
	 * @param dl
	 * @param N
	 * @return Score
	 */
	public static double getBM25Score(int qid, String docId, String q, int dl, int N)
	{
		double score;
		double ni = index.get(q).keySet().size();
		double qfi = queries.get(qid).get(q);
		double fi = 0;
		if(null != index.get(q).get(docId))
		{
			fi = index.get(q).get(docId);
		}
			
		score = (double)Math.log((N - ni + 0.5)/(ni + 0.5));
		score *= (double)(((k1 + 1)*fi) / (getKparam(dl)+fi));
		score *= (double)(((k2 + 1)*qfi)/(k2 + qfi));
		
		if(docId.equals("1930")){
			System.out.println(q + "=>" + qfi + "," + fi + "," + ni + "," + getKparam(dl));
		}
		return score;
	}
	
	/**
	 * 
	 * Returns a Sorted TreeMap of the given HashMap.
	 * Used document score to sort.
	 */
	public static TreeMap<String, Double> SortByValue (HashMap<String, Double> map) 
	{
		ValueComparator vc =  new ValueComparator(map);
		TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(vc);
		sortedMap.putAll(map);
		return sortedMap;
	}
	
	public static void main(String[] args) throws IOException 
	{
		loadIndex("index.out");
		loadDocLength("doclength.txt");
		loadQueries("queries.txt");
		avdl = getAvdl();
		System.out.println("avdl=" + avdl);
		computeDocScore();
		TreeMap<String, Double> sortedDocScore = SortByValue(docScore);
		//System.out.println(queries);
		//System.out.println(Math.log(0));
	}
}

/**
 * Comparator class to sort the hashmap.
 * @author Ashish
 *
 */
class ValueComparator implements Comparator<String> 
{
    Map<String, Double> map;

    public ValueComparator(Map<String, Double> base)
    {
        this.map = base;
    }
 
    public int compare(String a, String b) 
    {
        if (map.get(a) >= map.get(b)) 
        {
            return -1;
        } else 
        {
            return 1;
        } 
    }
}
