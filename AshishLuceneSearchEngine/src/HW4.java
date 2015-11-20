
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 * To create Apache Lucene index in a folder and add files into this index based
 * on the input of the user.
 */
public class HW4 
{
    private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
    private static Analyzer sAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);

    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();

    public static void main(String[] args) throws IOException 
    { 	
    	System.out.println("Enter the FULL path where the index will be created: " + 
    			"(e.g. /Usr/index or c:\\temp\\index)");

		String indexLocation = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = br.readLine();

		HW4 indexer = null;
		try 
		{
		    indexLocation = s;
		    indexer = new HW4(s);
		} catch (Exception ex) 
		{
		    System.out.println("Cannot create index..." + ex.getMessage());
		    System.exit(-1);
		}

		// ===================================================
		// read input from user until he enters q for quit
		// ===================================================
		while (!s.equalsIgnoreCase("q")) 
		{
		    try 
		    {
				System.out.println("Enter the FULL path to add into the index (q=quit):" + 
						"(e.g. /home/mydir/docs or c:\\Users\\mydir\\docs)");
				System.out.println("[Acceptable file types: .xml, .html, .html, .txt]");
				s = br.readLine();
				if (s.equalsIgnoreCase("q")) {   break;	}
	
				// try to add file into the index
				indexer.indexFileOrDirectory(s);
		    } catch (Exception e) {
			System.out.println("Error indexing " + s + " : "
				+ e.getMessage());
		    }
	}

	// ===================================================
	// after adding, we always have to call the
	// closeIndex, otherwise the index is not created
	// ===================================================
	indexer.closeIndex();

	// =========================================================
	// Now search
	// =========================================================
	
	IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
	
	//////////////////////////////////////////////////////////////////////////////////////
	// Logic to read the contents from HTML and build HashMap of terms and term frequency.
	Fields fields = MultiFields.getFields(reader);
	Terms terms = fields.terms("contents");
	BytesRef byteRef = null;
	TermsEnum tIterator = terms.iterator(null);
	HashMap<String, Long> termMap = new HashMap<String, Long>();
	long totalTF = 0;
	// Iterate each term loaded from the Reader object.
	while(null != (byteRef = tIterator.next()))
	{
		String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
		Term t = new Term("contents", term);
		Long tf = reader.totalTermFreq(t);
		totalTF += tf;					// Calculating the total term freq simultaneously
		termMap.put(term, tf);			// Adding the term and its frequency.
	}
	
	// Removes the <pre> and <html> tags from this HashMap of terms.
	removeTags(termMap);
	
	// Print the total number of terms loaded.
	System.out.println("Total number of Terms loaded =>" + termMap.size());
	
	// Sorting the TermMap based on Term Frequency.
	TreeMap<String,Long> sortedTermMap = SortByValue(termMap);
	
	/////////////////////////////////////////////////////////////////////
	// Plotting the Zipf's curve using term frequencies
	ZipfPlot plot = new ZipfPlot(sortedTermMap, termMap, totalTF);
	plot.pack();
    RefineryUtilities.centerFrameOnScreen(plot);
    plot.setVisible(true);
	////////////////////////////////////////////////////////////////////
	IndexSearcher searcher = new IndexSearcher(reader);
	
	s = "";
	int queryLineCounter = 1;
	while (!s.equalsIgnoreCase("q")) 
	{
	    try 
	    {
			System.out.println("Enter the search query (q=quit):");
			s = br.readLine();
			
			if (s.equalsIgnoreCase("q")) {
			    break;
			}
			// Parsing the input Query.
			Query q = new QueryParser(Version.LUCENE_47, "contents", sAnalyzer).parse(s);
			// Keeping threshold to be greater than the Corpus size. Helps to get # of doc hits.
			TopScoreDocCollector collector = TopScoreDocCollector.create(5000, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			// Start writing the doc hits in a new file for each query.
			BufferedWriter queryOutput = 
					new BufferedWriter(new FileWriter("query output" + queryLineCounter + ".csv"));
	
			// Writing the Top 100 results per query in an excel file.
			for (int i = 0; i < 100 && i < hits.length; ++i) 
			{
			    int docId = hits[i].doc;
			    queryOutput.write((i + 1) + "," + String.valueOf(docId+1) + ","   + hits[i].score + "\n");
			}
			queryOutput.close();
			System.out.println("Doc Hits for " + s + ": " + hits.length);
				
			String sterms[] = s.split(" ");
			long termFreq = 0;
			long docCount = 0;
			for(int i = 0; i < sterms.length ; i++)
			{
				Term termInstance = new Term("contents", sterms[i]);
				termFreq += reader.totalTermFreq(termInstance);
				docCount += reader.docFreq(termInstance);
			}
			System.out.println(s + ": TermFrequency for each term: " + termFreq +
					", DocFrequency for each term: " + docCount);
	    } catch (Exception e) 
	    {
			System.out.println("Error searching " + s + " : " + e.getMessage());
			break;
	    }
	    queryLineCounter++;  
	}
    }

	/**
	 * Removes the extra term frequencies associated with the <pre> and <html>.
	 * @param termMap
	 */
	public static void removeTags(HashMap<String,Long> termMap)
	{
		if((termMap.get("pre") != null) && (termMap.get("html") != null))
		{
			long preCount = termMap.get("pre");
			long htmlCount = termMap.get("html");
			
			if(preCount == htmlCount)
			{
				termMap.remove("pre");
				termMap.remove("html");
			}
			else if(preCount>htmlCount)
			{
				termMap.put("pre", preCount-htmlCount);
				termMap.remove("html");
			}
			else
			{
				termMap.put("html", htmlCount-preCount);
				termMap.remove("pre");
			}
		}
	}
	
    /**
     * Constructor
     * 
     * @param indexDir
     *            the name of the folder in which the index should be created
     * @throws java.io.IOException
     *             when exception creating index.
     */
    HW4(String indexDir) throws IOException 
    {

		FSDirectory dir = FSDirectory.open(new File(indexDir));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,	sAnalyzer);
		writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     * 
     * @param fileName
     *            the name of a text file or a folder we wish to add to the
     *            index
     * @throws java.io.IOException
     *             when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
	// ===================================================
	// gets the list of files in a folder (if user has submitted
	// the name of a folder) or gets a single file name (is user
	// has submitted only the file name)
	// ===================================================
	addFiles(new File(fileName));

	int originalNumDocs = writer.numDocs();
	for (File f : queue) {
	    FileReader fr = null;
	    try {
		Document doc = new Document();

		// ===================================================
		// add contents of file
		// ===================================================
		fr = new FileReader(f);
			
		doc.add(new TextField("contents", fr));
		doc.add(new StringField("path", f.getPath(), Field.Store.YES));
		doc.add(new StringField("filename", f.getName(),Field.Store.YES));

		writer.addDocument(doc);
		System.out.println("Added: " + f);
	    } catch (Exception e) {
		System.out.println("Could not add: " + f);
	    } finally {
		fr.close();
	    }
	}

	int newNumDocs = writer.numDocs();
	System.out.println("");
	System.out.println("************************");
	System.out
		.println((newNumDocs - originalNumDocs) + " documents added.");
	System.out.println("************************");

	queue.clear();
    }

    private void addFiles(File file) {

	if (!file.exists()) {
	    System.out.println(file + " does not exist.");
	}
	if (file.isDirectory()) {
	    for (File f : file.listFiles()) {
		addFiles(f);
	    }
	} else {
	    String filename = file.getName().toLowerCase();
	    // ===================================================
	    // Only index text files
	    // ===================================================
	    if (filename.endsWith(".htm") || filename.endsWith(".html")
		    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
		queue.add(file);
	    } else {
		System.out.println("Skipped " + filename);
	    }
	}
    }

    /**
     * Close the index.
     * 
     * @throws java.io.IOException
     *             when exception closing
     */
    public void closeIndex() throws IOException {
	writer.close();
    }
    
    /**
	 * 
	 * Returns a Sorted TreeMap of the given HashMap.
	 * Sorting based on term frequencies.
	 */
	public static TreeMap<String, Long> SortByValue (HashMap<String, Long> map) 
	{
		ValueComparator vc =  new ValueComparator(map);
		TreeMap<String,Long> sortedMap = new TreeMap<String,Long>(vc);
		sortedMap.putAll(map);
		return sortedMap;
	}
	
}

/**
 * Comparator class for sorting collection based on Value. 
 * @author Ashish
 *
 */
class ValueComparator implements Comparator<String> 
{
    Map<String, Long> map;

    public ValueComparator(Map<String, Long> base)
    {
        this.map = base;
    }
 
    public int compare(String a, String b) 
    {
        if (map.get(a) >= map.get(b)) {
            return -1;
        } else {
            return 1;
        } 
    }
}

/**
 * ZipfPlot class to render a JFreeChart that renders a curve
 * of Rank v/s Probability.
 * The plot graph shows how Rank is inversely proportional to the Probability
 * of the term.
 * 
 * According to Zipf's law,
 * 
 * rank = k / probability ; where k is a constant.
 * 
 * rank: Rank of the term (higher the rank, more common the word in corpus)
 * probability: Chances of appearance of this term in the corpus.
 *  
 * @author Ashish
 *
 */
 class ZipfPlot extends JFrame 
 {
	  private static final long serialVersionUID = 1L;

	  public ZipfPlot(TreeMap<String, Long> sortedTermMap, 
				HashMap<String, Long> termMap,
				long totalTF) {
	        super("Zipf's Curve");
	        // This will create the dataset 
	        XYDataset dataset = createDataset(sortedTermMap,termMap,totalTF);
	        // based on the dataset we create the chart
	        JFreeChart chart = createChart(dataset, "Rank vs Probability");
	        // we put the chart into a panel
	        ChartPanel chartPanel = new ChartPanel(chart);
	        // default size
	        chartPanel.setPreferredSize(new java.awt.Dimension(750, 600));
	        // add it to our application
	        setAlwaysOnTop(true);
	        setContentPane(chartPanel);
	    }
	    
	    
	  	/**
	     * Creates a sample XY dataset 
	     */
	    private  XYDataset  createDataset(
	    		TreeMap<String, Long> sortedTermMap, 
				HashMap<String, Long> termMap,
				long totalTF) 
	    {
	    	final XYSeries zipfseries = new XYSeries("Zipf's Curve");
	    	final XYSeriesCollection dataset = new XYSeriesCollection( );          
	                  
	    	double tprobability = 0.0;
			long tf = 0;
			String term = null;
			int rank = 1;
	    	Iterator<String> terms = sortedTermMap.keySet().iterator();
			while(terms.hasNext())
			{
				term = terms.next();
				tf = termMap.get(term);
				tprobability = ((double)tf/(double)totalTF);
				zipfseries.add(rank, tprobability);
				rank++;
			}
			dataset.addSeries(zipfseries);
						
	        return dataset;	        
	    }
	    
	    
	    /**
	     * Creates a chart
	     */
	    private JFreeChart createChart(XYDataset dataset, String title) 
	    { 
	        JFreeChart chart = ChartFactory.createXYLineChart(
	        		title,
	                "Rank",
	                "Probability",
	                dataset,
	                PlotOrientation.VERTICAL,
	                true,true,false);
	        
	        int width = 750; /* Width of the image */
	        int height = 600; /* Height of the image */ 
	        // saving the image as a JPEG file.
	        File XYChart = new File( "Zipf.jpeg" ); 
	        try 
	        {
				ChartUtilities.saveChartAsJPEG( XYChart, chart, width, height);
			} catch (IOException e) 
	        {
				// Skip creating the JPEG
			}
	        
	        return chart;      
	    }
	} 
