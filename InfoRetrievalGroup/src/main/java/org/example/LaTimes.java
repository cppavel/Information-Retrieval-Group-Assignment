package org.example;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

public class LaTimes {
    private static String INDEX_DIRECTORY = "../index";

    public static void main(String[] args) throws IOException, ParseException {
        File dir = new File("InfoRetrievalGroup/latimes");
        File[] files = dir.listFiles();

        // Open the directory that contains the search index
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        Analyzer analyzer = new EnglishAnalyzer();
        // Set up an index writer to add process and save documents to the index
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);





        for(File file : files){

            BufferedReader br = new BufferedReader(new FileReader(file));
            // Keep track of line we are reading in
            String nextline = br.readLine();

            while(nextline != null){
                if(nextline.startsWith("<DOC>") || nextline.startsWith(" <DOC>")){
                    Document doc = new Document();
                    nextline = br.readLine();
                    if(nextline.startsWith("<DOCNO>")){
                        StringBuilder docNum = new StringBuilder();
                        docNum.append(nextline);
                        String docNumStr = docNum.toString();
//                        remove the unnecessary text arounf the document number
                        String filteredDoc = docNumStr.replace("<DOCNO>", "").replace("</DOCNO>", "").replace(" ", "");
                        TextField fieldNum = new TextField("DocNo", filteredDoc, Field.Store.YES);
                        doc.add(fieldNum);
                    }
//                    skip lines until we reach the headline
                    while(nextline != null && !nextline.startsWith("<HEADLINE>")){
                        nextline = br.readLine();
                    }
//                    when we reach the start of the headline we enter this if statement
                    if(nextline != null && nextline.startsWith("<HEADLINE>")){
                        nextline = br.readLine();
                        StringBuilder headLineBuilder = new StringBuilder();
//                        iterate till we finish the headline segment
                        while(!nextline.startsWith("</HEADLINE>")){
//                            only append lines that have actual content
                            if(!nextline.startsWith("</P>") && !nextline.startsWith("<P>")){
                                headLineBuilder.append(nextline);
                            }
                            nextline = br.readLine();
                        }
                        String headLineString = headLineBuilder.toString();
                        TextField fieldHeadLine = new TextField("HeadLine", headLineString, Field.Store.YES);
                        doc.add(fieldHeadLine);
                    }

//                    skip lines till we reach the text part
                    while(nextline != null && !nextline.startsWith("<TEXT>")){
                        nextline = br.readLine();
                    }

                    if(nextline != null && nextline.startsWith("<TEXT>")){
                        nextline = br.readLine();
                        StringBuilder textBuilder = new StringBuilder();

                        while(!nextline.startsWith("</TEXT>")){
                            if(!nextline.startsWith("</P>") && !nextline.startsWith("<P>")){
                                textBuilder.append(nextline);
                            }
                            nextline = br.readLine();
                        }

                        String textString = textBuilder.toString();
                        TextField fieldText = new TextField("Text", textString, Field.Store.YES);
                        doc.add(fieldText);
                    }

                    while(nextline != null && !nextline.startsWith("</DOC>")){
                        nextline = br.readLine();
                    }
//                    System.out.println(doc);
                    iwriter.addDocument(doc);
                }
                nextline = br.readLine();
            }
        }
        // Commit everything and close
        iwriter.close();
        directory.close();

    }
}
