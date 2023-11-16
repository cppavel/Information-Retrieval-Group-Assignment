package org.model;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
//import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
//import java.text.ParseException;

public class FedRegister {

    private static final String INDEX_DIRECTORY = "../index";

    public static void main(String[] args) throws IOException {
        String pathToFedRegister = "../data/fr94"; // 路径需要根据实际情况调整
        //  File[] directories = new File(pathToFedRegister).listFiles(File::isDirectory);
        File rootDirectory = new File(pathToFedRegister);

        // indexing
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);

        // 递归处理文档
        processDirectory(rootDirectory, iwriter);

//        // 处理文档
//        for (File directory : directories) {
//            File[] files = directory.listFiles();
//            for (File file : files) {
//                org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");
//                Elements documents = d.select("DOC");
//
//                for (Element document : documents) {
//                    String title = document.select("DOCTITLE").text();
//                    String docno = document.select("DOCNO").text();
//                    String text = document.select("TEXT").text();
//
//                    Document luceneDoc = new Document();
//                    luceneDoc.add(new TextField("docno", docno, Field.Store.YES));
//                    luceneDoc.add(new TextField("text", text, Field.Store.YES));
//                    luceneDoc.add(new TextField("headline", title, Field.Store.YES));
//
//                    iwriter.addDocument(luceneDoc);
//                }
//            }
//        }

        // 关闭索引
        iwriter.close();
        directory.close();
    }

    private static void processDirectory(File directory, IndexWriter iwriter) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file, iwriter); // 递归调用处理子目录
                } else {
                    processFile(file, iwriter); // 处理单个文件
                }
            }
        }
    }

    private static void processFile(File file, IndexWriter iwriter) throws IOException {
        org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");
        Elements documents = d.select("DOC");

        for (Element document : documents) {
            String title = document.select("DOCTITLE").text();
            String docno = document.select("DOCNO").text();
            String text = document.select("TEXT").text();

            Document luceneDoc = new Document();
            luceneDoc.add(new TextField("docno", docno, Field.Store.YES));
            luceneDoc.add(new TextField("text", text, Field.Store.YES));
            luceneDoc.add(new TextField("headline", title, Field.Store.YES));

            iwriter.addDocument(luceneDoc);
        }
    }
}
