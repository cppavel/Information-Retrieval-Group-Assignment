package org.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class FedRegisterParser {
    public static void processDirectory(File directory, ArrayList<FedRegisterDocument> accumulator)
            throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file, accumulator);
                } else {
                    processFile(file, accumulator);
                }
            }
        }
    }

    public static void processFile(File file, ArrayList<FedRegisterDocument> accumulator) throws IOException {
        org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");
        Elements documents = d.select("DOC");



        for (Element document : documents) {
            String title = document.select("DOCTITLE").text();
            String docno = document.select("DOCNO").text();
            String text = document.select("TEXT").text();
            String date = document.select("DATE").text();

            accumulator.add(new FedRegisterDocument(docno, text, title, date));
        }
    }


    public static void main(String[] args) throws IOException {
        String pathToFedRegister = "../data/fr94";
        File rootDirectory = new File(pathToFedRegister);

        ArrayList<FedRegisterDocument> documents = new ArrayList<>();
        processDirectory(rootDirectory, documents);

        System.out.format("Parsed %d fed register document elements in %s\n", documents.size(), pathToFedRegister);
    }
}
