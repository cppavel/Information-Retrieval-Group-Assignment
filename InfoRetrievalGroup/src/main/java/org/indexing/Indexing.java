package org.indexing;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.parsers.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;


import static org.parsers.FinancialTimesParser.ParseFilesByDirectory;

public class Indexing {
    private static String INDEX_DIRECTORY = "../index";

    public static ArrayList<IDocument> loadDocuments() throws ParserConfigurationException, IOException, SAXException {
        ArrayList<IDocument> documents = new ArrayList<>();

        HashMap<String, ArrayList<FinancialTimesDocument>> financialTimesDocs =
                ParseFilesByDirectory("../data/ft");
        System.out.println("Loaded financial times docs");

        ArrayList<FBISDocument> fbisDocuments = FBISParser.parseDocumentsInFolder("../data/fbis");
        System.out.println("Loaded fbis docs");

        ArrayList<LaTimesDocument> laTimesDocs = LaTimesParser.parseDocumentsInFolder("../data/latimes");
        System.out.println("Loaded la times docs");

        String pathToFedRegister = "../data/fr94";
        File rootDirectory = new File(pathToFedRegister);
        ArrayList<FedRegisterDocument> fedRegisterDocs = new ArrayList<>();
        FedRegisterParser.processDirectory(rootDirectory, fedRegisterDocs);
        System.out.println("Loaded fed register docs");

        for(ArrayList<FinancialTimesDocument> docs: financialTimesDocs.values()){
            documents.addAll(docs);
        }

        documents.addAll(fbisDocuments);
        documents.addAll(laTimesDocs);
        documents.addAll(fedRegisterDocs);

        System.out.format("Loaded %d documents.\n", documents.size());

        return documents;
    }

    public static void createDirectory(String directoryPath) {
        Path directory = Paths.get(directoryPath);
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (Exception e) {
            System.err.println("Error creating directory: " + e.getMessage());
        }
    }

    public static void GenerateIndex(ArrayList<IDocument> documents, float k1, float b) throws IOException, ParserConfigurationException, SAXException {

        String directoryPath = INDEX_DIRECTORY + "_k1_"+k1 + "_b_"+ b;
        createDirectory(directoryPath);
        Directory directory = FSDirectory.open(Paths.get(directoryPath));
        Analyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(new BM25Similarity(k1, b));
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter iwriter = new IndexWriter(directory, config);

        int step = 10;
        int prev = 0;
        int count = 0;
        for(IDocument doc: documents){
            Document docLucene = new Document();
            String content =  doc.GetTitle() != null? doc.GetTitle(): "";
            content+= " ";
            content+= doc.GetContent() != null? doc.GetContent(): "";

            docLucene.add(new StringField("id", doc.GetId() != null? doc.GetId(): "", Field.Store.YES));
            docLucene.add(new StringField("title",doc.GetTitle() != null? doc.GetTitle(): "", Field.Store.YES));
            docLucene.add(new StringField("date", doc.GetDate() != null? doc.GetDate(): "", Field.Store.YES));
            docLucene.add(new TextField("content", content, Field.Store.YES));
            iwriter.addDocument(docLucene);

            if(100 * count / documents.size() > prev + step){
                System.out.format("Indexing progress: %d%%\n", 100 * count / documents.size());
                prev = 100 * count / documents.size();
            }
            count++;
        }

        iwriter.close();
        directory.close();
    }
    public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException, SAXException {
        ArrayList<IDocument> documents = loadDocuments();
        GenerateIndex(documents, 1.2f, 0.75f);
    }
}
