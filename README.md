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
