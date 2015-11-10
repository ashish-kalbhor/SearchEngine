package ir.search.indexer;

import java.util.HashMap;
import java.util.HashSet;

public class IndexMap extends HashMap<String, HashMap<String, Integer>>
{	
	public HashMap<String,Integer> appendPosting(String term, String docId)
	{
		HashMap<String, Integer> postingMap = super.get(term);
		if(null == postingMap)
		{
			postingMap = new HashMap<String, Integer>();
			postingMap.put(docId, 1);
		}
		else if(null == postingMap.get(docId))
		{
			postingMap.put(docId, 1);
		}
		else
		{
			int count = postingMap.get(docId);
			postingMap.put(docId, count+1);
		}
		return super.put(term, postingMap);
	}	
}
