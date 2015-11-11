package ir.search.indexer;

import java.util.HashMap;

/**
 * A Data Structure for Inverted Index.
 * An IndexMap is a HashMap with:
 * 	key   => term
 *  value => HashMap of docId and term-frequency for this term in this doc.
 * @author Ashish
 *
 */
public class IndexMap extends HashMap<String, HashMap<String, Integer>>
{	
	/**
	 * Auto-generated serialVersionUID
	 */
	private static final long serialVersionUID = 8327440245839528642L;
	
	/**
	 * Append postings (docId,tf) for each corresponding word in the IndexMap.
	 * @param term
	 * @param docId
	 */
	public HashMap<String,Integer> appendPosting(String term, String docId)
	{
		HashMap<String, Integer> postingMap = super.get(term);
		// If no postings for this word, create new posting
		if(null == postingMap)
		{
			postingMap = new HashMap<String, Integer>();
			postingMap.put(docId, 1);
		}
		// If no posting corresponding to this docId, add this doc as new posting
		else if(null == postingMap.get(docId))
		{
			postingMap.put(docId, 1);
		}
		// Else increment the term frequency
		else
		{
			int count = postingMap.get(docId);
			postingMap.put(docId, count+1);
		}
				
		return super.put(term, postingMap);
	}	
}
