package org.model;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.indexing.Indexing;
import org.parsers.IDocument;
import org.parsers.Topic;
import org.parsers.TopicParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Querying {
    private static final int MAX_RESULTS = 1000;
    private static final String INDEX_DIRECTORY = "../index";
    private static final String SYNONYM_DIRECTORY = "../synonym";

    private static SynonymMap LoadSynonymMap() throws IOException, java.text.ParseException {
        WordnetSynonymParser parser = new WordnetSynonymParser(true, true, new StandardAnalyzer());
        parser.parse(new FileReader(SYNONYM_DIRECTORY + "/" + "wn_s.pl"));
        return parser.build();
    }

    private static String ExpandQuery(String query, SynonymMap synonymMap) throws IOException {
        StringBuilder result = new StringBuilder();
        StandardAnalyzer analyzer = new StandardAnalyzer();


        TokenStream tokenStream = analyzer.tokenStream("query", new StringReader(query));
        tokenStream = new SynonymFilter(tokenStream, synonymMap, true);
        tokenStream = new RemoveDuplicatesTokenFilter(tokenStream);
        tokenStream.reset();
        CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        while (tokenStream.incrementToken()) {
            String expandedTerm = termAttribute.toString();
            result.append(" ").append(expandedTerm);
        }
        tokenStream.close();
        analyzer.close();

        return result.toString().trim();
    }

    private static ArrayList<CustomQuery> GenerateQueries(boolean enableExpansion)
            throws IOException, java.text.ParseException {
        ArrayList<Topic> topics = TopicParser.parseTopics("../data/topics/topics");

        SynonymMap synonymMap = LoadSynonymMap();
        ArrayList<CustomQuery> queries = new ArrayList<>();

        for(Topic topic: topics){
            String simpleQuery = topic.Title + " " + topic.Description + " " + topic.Narrative;
            simpleQuery = simpleQuery.replace('\n', ' ');
            String expandedQuery = ExpandQuery(simpleQuery, synonymMap);
            queries.add(new CustomQuery(topic.TopicNum, enableExpansion? expandedQuery: simpleQuery));
        }

        return queries;
    }

    private static ArrayList<String> QueryIndex(float k1, float b, boolean enableExpansion) throws IOException, ParseException, java.text.ParseException {
        ArrayList<String> results = new ArrayList<>();

        Directory dir = FSDirectory.open(Paths.get(INDEX_DIRECTORY + "_k1_"+k1 + "_b_"+ b));
        DirectoryReader dirReader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(dirReader);
        searcher.setSimilarity(new BM25Similarity(k1, b));
        Analyzer analyzer = new EnglishAnalyzer();

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[]{"content", "title"}, analyzer);
        queryParser.setAllowLeadingWildcard(true);

        ArrayList<CustomQuery> queries = GenerateQueries(enableExpansion);

        int count = 1;
        for(CustomQuery query: queries){
            System.out.format("Processed %d queries out of %d.\n", count, queries.size());
            count++;

            Query qry = queryParser.parse(QueryParser.escape(query.QueryText));
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


    public static void WriteOutResults(List<String> results, float k1, float b, String tag) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("../results/results" + "_k_" + k1 + "_b_" + b + "_" + tag + ".txt"))) {
            for(String line : results) {
                writer.write(line);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException, SAXException, java.text.ParseException {

        float k1 = 0.8f;
        float b = 0.8f;

        ArrayList<IDocument> documents = Indexing.loadDocuments();
        Indexing.GenerateIndex(documents, k1, b);
        WriteOutResults(QueryIndex(k1, b, false), k1, b, "tdn");
        System.out.format("Processing finished for k1=%f, b=%f\n", k1, b);
    }
}
