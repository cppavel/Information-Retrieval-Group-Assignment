package org.parsers;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;


public class FBISParser {

    private static String GetNodeByTagName(NodeList nodeList, String tagName) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                return node.getTextContent().trim();
            }
        }

        return "";
    }

    public static ArrayList<FBISDocument> parseDocumentsInFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        ArrayList<FBISDocument> parsedDocuments = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                try {
                    FBISDocument document = parseDocument(file.getAbsolutePath());
                    parsedDocuments.add(document);
                } catch (ParserConfigurationException | IOException | SAXException | ParseException e) {
                    // Handle exceptions or log them
                    e.printStackTrace();
                }
            }
        }

        return parsedDocuments;
    }

    public static FBISDocument parseDocument(String filePath) throws ParserConfigurationException, IOException, SAXException, ParseException {
        File inputFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("DOC");

        if (nodeList.getLength() == 0) {
            throw new ParseException("No DOC element found in the document.", 0);
        }

        Node docNode = nodeList.item(0);
        NodeList childNodes = docNode.getChildNodes();

        String docNo = GetNodeByTagName(childNodes, "DOCNO");
        String author = GetNodeByTagName(childNodes, "AU");
        String date = GetNodeByTagName(childNodes, "DATE1");
        String title = GetNodeByTagName(childNodes, "TI");
        String text = GetNodeByTagName(childNodes, "TEXT");

        return new FBISDocument(docNo, author, date, title, text);
    }

    
}
