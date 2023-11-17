package org.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TopicParser {
    public static ArrayList<Topic> parseTopics(String topicFilePath) throws IOException {
        File file = new File(topicFilePath);
        org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");

        ArrayList<Topic> result = new ArrayList<>();

        Elements documents = d.select("top");

        for (Element document : documents) {
            String[] documentContent = document.select("num").text().split(" ");

            if(documentContent.length == 0)
                continue;

            int curIndex = 0;
            String line = documentContent[0];

            StringBuilder num = new StringBuilder();
            StringBuilder title = new StringBuilder();
            StringBuilder desc = new StringBuilder();
            StringBuilder narr = new StringBuilder();

            while(!line.isEmpty()){
                curIndex++;
                num.append(documentContent[curIndex]);
                num.append(" ");
                line = documentContent[curIndex];
            }

            while(!line.contains("Description")){
                title.append(documentContent[curIndex]);
                title.append(" ");
                curIndex++;
                line = documentContent[curIndex];
            }

            while(!line.contains("Narrative")){
                desc.append(documentContent[curIndex]);
                desc.append(" ");
                curIndex++;
                line = documentContent[curIndex];
            }

            while(curIndex < documentContent.length){
                narr.append(documentContent[curIndex]);
                narr.append(" ");
                curIndex++;
            }

            result.add(new Topic(num.toString(), title.toString(), desc.toString(), narr.toString()));
        }

        return result;
    }

    public static void main(String [] args) throws IOException {
        ArrayList<Topic> topics = parseTopics("../data/topics/topics");
        System.out.format("Parsed %d topics in %s\n", topics.size(), "../data/topics/topics");
    }

}
