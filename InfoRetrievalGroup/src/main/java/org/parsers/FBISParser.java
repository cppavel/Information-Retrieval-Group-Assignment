package org.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;


public class FBISParser {

    public static Node GetNodeByTagName(NodeList nodeList, String tagName) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                return node;
            }
        }

        return null;
    }

    private static String GetNodeByTagNameRecursive(Node currentNode, String tagName){
        NodeList children = currentNode.getChildNodes();

        Node targetNode = GetNodeByTagName(children, tagName);

        if(targetNode != null){
            return targetNode.getTextContent().trim();
        }

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            String nodeContent = GetNodeByTagNameRecursive(node, tagName);

            if(nodeContent != null){
                return nodeContent;
            }
        }

        return null;
    }
    public static ArrayList<FBISDocument> parseDocumentsInFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();


        ArrayList<FBISDocument> parsedDocuments = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                try {
                    parsedDocuments.addAll(parseDocuments(file.getAbsolutePath()));
                } catch (ParserConfigurationException | IOException | SAXException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return parsedDocuments;
    }

    public static Date parseDateString(String dateString) {
       String[] formats = new String[]{"d MMMM yyyy", "MMMM d yyyy"};

       for(String format: formats){
           SimpleDateFormat dateFormat = new SimpleDateFormat(format);

           Date result;
           try {
               result =  dateFormat.parse(dateString);
           } catch (ParseException e) {
               result = null;
           }

           if(result != null){
               return result;
           }
       }

       return null;
    }

    public static ArrayList<FBISDocument> parseDocuments(String filePath) throws ParserConfigurationException,
            IOException, SAXException, ParseException {
        AddRootToXML(filePath);
        File inputFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("DOC");

        if (nodeList.getLength() == 0) {
            throw new ParseException("No DOC element found in the document.", 0);
        }

        ArrayList<FBISDocument> result = new ArrayList<>();

        for(int i = 0; i < nodeList.getLength(); i++){

            try {

                Node docNode = nodeList.item(i);

                String docNo = GetNodeByTagNameRecursive(docNode, "DOCNO");
                String dateString = GetNodeByTagNameRecursive(docNode, "DATE1");
                String title = GetNodeByTagNameRecursive(docNode, "TI");
                String text = GetNodeByTagNameRecursive(docNode, "TEXT");
                Date date = new Date();

                if (!dateString.isEmpty()) {
                    date = parseDateString(dateString);
                }

                result.add(new FBISDocument(docNo, date, title, text));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return result;
    }

    public static String quoteAttributes(String xmlString) {
        // Use a regular expression to find and replace attribute values
        return xmlString.replaceAll("=([^\\s>]+)", "=\"$1\"");
    }

    public static void AddRootToXML(String filename) {
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace('&', ' ').replaceAll("<3>", " ").
                        replaceAll("</3>", " ");
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if(lines.get(0).equals("<root>") && lines.get(lines.size() - 1).equals("</root>")){
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("<root>");
            writer.newLine();

            for (String line : lines) {
                writer.write(quoteAttributes(line));
                writer.newLine();
            }

            writer.write("</root>");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        ArrayList<FBISDocument> fbisDocuments = parseDocumentsInFolder("../data/fbis");
        System.out.format("Found %d FBIS document elements in %s", fbisDocuments.size(), "../data/fbis");
    }
}
