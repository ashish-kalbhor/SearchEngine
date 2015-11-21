# SearchEngine
A mini search engine using Okapi BM25 retrieval model

Please go through the following instructions to run the Search Engine:

=========================================================================================
HOW TO RUN
=========================================================================================
1. Java 1.8 to compile the source.
2. Eclipse to open the project and run the application.

- Alternatively:
a. Compile the java file using "javac".
b. Run the Java file using "java".

=========================================================================================
PACKAGE INCLUDES
=========================================================================================
1. Source code (ir.search.indexer).
2. Input file "tccorpus.txt" and "queries.txt".
3. Requested output files:
	1. index.out (Output of first program - Indexer)
	2. results.eval (Output of second program - BM25)

=========================================================================================
Executing Indexer
=========================================================================================
Make sure the tccorpus.txt file is in place.

> java –cp “_Location_Of_Src_folder_” ir.search.indexer.Indexer tccorpus.txt index.out

1st argument: Corpus input
2nd argument: index output file name

After executing this command, following files are generated:
	1. index.out : Inverted Index representation containing postings of [DocId,termFrequency] for each term.
	2. doclength.txt : A data structure that stores the document length (number of valid tokens) of each document.
	
=========================================================================================
Executing BM25
=========================================================================================
Make sure the queries.txt file is in place.

> java –cp “_Location_Of_Src_folder_” ir.search.indexer.BM25 index.out queries.txt 100 > results.eval

1st argument: index file
2nd argument: queries file
3rd argument: Number of top ranked documents to be shown

After executing this command, following files are generated:
	1. results.eval : List of top ranked documents along with their BM25 score per query.
	2. 
	
====================================================================================================
Please go through the following instructions to run the assignment:

=========================================================================================
HOW TO RUN
=========================================================================================
1. Java 1.8 to compile the source.
2. Lucene 4.7.2
3. Eclipse to open the project and run the application.

- Alternatively:
a. Compile the java file using "javac".
b. Run the Java file using "java".

=========================================================================================
PACKAGE INCLUDES
=========================================================================================
1. Source code (HW4.java)
2. Input folder of webpages "cacm"
3. Required JAR files in a folder names "JARS TO BE INCLUDED IN PROJECT":
	a. Lucene 4.7.2 jars (core, analyzers-common, queryparser)
	b. JFreeChart jars (jfreechart, jcommon)
4. Requested output files:
	1. Sorted list of term frequencies. (termFreq.csv)
	2. Plot of Zipfian Curve. (An Excel sheet as well as JPEG image file).
	3. Lists of Top 100 DocIds per query. (4 csv files)
	4. Comparison table (Comparison Table.txt)
5. Console Output after a successful complete run. (Console Output.txt)	

=========================================================================================
Executing the program
=========================================================================================

> java –cp “_Location_Of_Src_folder_” HW4

The console prompts for the required input.
The console is quite interactive and the top 100 docs for each query are automatically
written in a file.
