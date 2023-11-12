package org.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

public class FinancialTimesParser {

    public static String GetNodeByTagName(NodeList nodeList, String tagName) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                return node.getTextContent();
            }
        }

        return "";
    }

    public static ArrayList<FinancialTimesDocument> Parse(String filename) throws ParserConfigurationException, IOException, SAXException {
        AddRootToXML(filename);
        File inputFile = new File(filename);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("DOC"); // Replace with your XML tag name

        ArrayList<FinancialTimesDocument> parsedDocuments = new ArrayList<FinancialTimesDocument>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            NodeList childNodes = nodeList.item(i).getChildNodes();

            String docNo = GetNodeByTagName(childNodes, "DOCNO");
            String profile = GetNodeByTagName(childNodes, "PROFILE");

            String dateString = GetNodeByTagName(childNodes, "DATE");
            Date date = new Date(Integer.parseInt(dateString.substring(0, 2)),
                            Integer.parseInt(dateString.substring(2, 4)) - 1,
                                    Integer.parseInt(dateString.substring(4, 6)));
            String headline = GetNodeByTagName(childNodes, "HEADLINE");
            String text = GetNodeByTagName(childNodes, "TEXT");
            String pub = GetNodeByTagName(childNodes, "PUB");
            String page = GetNodeByTagName(childNodes, "PAGE");
            String byline = GetNodeByTagName(childNodes, "BYLINE");

            parsedDocuments.add(new FinancialTimesDocument(docNo, profile, date, headline, text, pub, page, byline));
        }

        return parsedDocuments;
    }

    public static void AddRootToXML(String filename) {
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
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

    public static ArrayList<String> GetAllDirectories(String rootDirectoryPath){
        File dir = new File(rootDirectoryPath);
        File[] files = dir.listFiles();

        ArrayList<String> result = new ArrayList<>();

        for(File file :files){
            if(file.isDirectory() && file.getName().contains("ft9")){
                result.add(file.getName());
            }
        }

        return result;
    }

    public static HashMap<String, ArrayList<FinancialTimesDocument>> ParseFilesByDirectory(String rootDirectoryPath) throws ParserConfigurationException, IOException, SAXException {
        ArrayList<String> directories = GetAllDirectories(rootDirectoryPath);
        HashMap<String, ArrayList<FinancialTimesDocument>> result = new HashMap<>();

        for(String directory: directories){
            String directoryPath = rootDirectoryPath + "/" + directory;
            File[] directoryFiles = new File(directoryPath).listFiles();

            ArrayList<FinancialTimesDocument> directoryFtDocuments = new ArrayList<>();
            for(File directoryFile: directoryFiles){
                directoryFtDocuments.addAll(Parse(directoryFile.getAbsolutePath()));
            }

            result.put(directory, directoryFtDocuments);
        }

        return result;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        HashMap<String, ArrayList<FinancialTimesDocument>> documentsByDirectory =
                ParseFilesByDirectory("../data/ft");

        for(String directory: documentsByDirectory.keySet()){
            System.out.format("Found directory %s and %d financial times document elements in it\n", directory,
                    documentsByDirectory.get(directory).size());
        }
    }
}
