package org.parsers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;

public class LaTimesParser {

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
    public static ArrayList<LaTimesDocument> parseDocumentsInFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();


        ArrayList<LaTimesDocument> parsedDocuments = new ArrayList<>();

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

    public static Date convertToDateLegacy(String day, String month, String year) {
        try {
            String dateString = String.format("%s %s %s", day, month, year);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date parseDateString(String dateString){
        String [] tokens = dateString.split(",");
        String [] monthDay = tokens[0].split(" ");

        if(monthDay.length == 2 && tokens.length >= 2){
            String month = monthDay[0];
            String day = monthDay[1];
            String year = tokens[1];
            return convertToDateLegacy(day, month, year);
        }
        return null;
    }

    public static ArrayList<LaTimesDocument> parseDocuments(String filePath) throws ParserConfigurationException,
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

        ArrayList<LaTimesDocument> result = new ArrayList<>();

        for(int i = 0; i < nodeList.getLength(); i++){

            try {

                Node docNode = nodeList.item(i);

                String docNo = GetNodeByTagNameRecursive(docNode, "DOCNO");
                String dateString = GetNodeByTagNameRecursive(docNode, "DATE");
                String title = GetNodeByTagNameRecursive(docNode, "HEADLINE");
                String text = GetNodeByTagNameRecursive(docNode, "TEXT");

                Date date = new Date();

                if (!dateString.isEmpty()) {
                    date = parseDateString(dateString);
                }

                if(date == null){
                    dateString = GetNodeByTagNameRecursive(docNode, "CORRECTION-DATE");
                    date = parseDateString(dateString);
                }

                result.add(new LaTimesDocument(docNo, title, text, date));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return result;
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
                writer.write(line);
                writer.newLine();
            }

            writer.write("</root>");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        ArrayList<LaTimesDocument> documents = parseDocumentsInFolder("../data/latimes");
        System.out.format("Parsed %d LA Times document elements in %s\n", documents.size(),"../data/latimes");
    }
}
