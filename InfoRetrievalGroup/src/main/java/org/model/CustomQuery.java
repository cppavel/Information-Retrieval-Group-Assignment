package org.model;

public class CustomQuery {
    public int Id;
    public String QueryText;

    public CustomQuery(String id, String queryText){
        Id=Integer.parseInt(id.replaceAll("\\s+",""));
        QueryText=queryText;
    }
}
