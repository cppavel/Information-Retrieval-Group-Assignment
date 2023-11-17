package org.model;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.indexing.Indexing;
import org.parsers.Topic;
import org.parsers.TopicParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Querying {
    private static final int MAX_RESULTS = 1000;
    private static final String INDEX_DIRECTORY = "../index";

    private static ArrayList<CustomQuery> GenerateQueries() throws IOException {
        ArrayList<Topic> topics = TopicParser.parseTopics("../data/topics/topics");

        ArrayList<CustomQuery> queries = new ArrayList<>();

        for(Topic topic: topics){
            queries.add(new CustomQuery(topic.TopicNum, topic.Description));
        }

        return queries;
    }

    private static ArrayList<String> QueryIndex() throws IOException, ParseException {
        ArrayList<String> results = new ArrayList<>();

        Directory dir = FSDirectory.open(Paths.get("../index"));
        DirectoryReader dirReader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(dirReader);
        searcher.setSimilarity(new BM25Similarity());
        Analyzer analyzer = new EnglishAnalyzer();

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
                new String[]{"title", "date", "content"}, analyzer);
        queryParser.setAllowLeadingWildcard(true);

        ArrayList<CustomQuery> queries = GenerateQueries();

        int count = 1;
        for(CustomQuery query: queries){
            System.out.format("Processed %d queries out of %d.\n", count, queries.size());
            count++;

            Query qry = queryParser.parse(query.QueryText);
            ScoreDoc[] hits = searcher.search(qry, MAX_RESULTS).scoreDocs;

            for(ScoreDoc hit: hits) {
                Document doc = searcher.doc(hit.doc);
                results.add(String.format("%03d 0 %s 0 %f STANDARD%n", query.Id, doc.get("id"), hit.score));
            }
        }

        dirReader.close();
        dir.close();

        return results;
    }


    public static void WriteOutResults(List<String> results) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("../results/results"))) {
            for(String line : results) {
                writer.write(line);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException, SAXException {
        Indexing.GenerateIndex();
        WriteOutResults(QueryIndex());
    }
}
