package org.parsers;

import java.util.Date;

public class LaTimesDocument implements IDocument {
    private String DocNo;
    private String Headline;
    private String Text;

    private Date Date;

    public LaTimesDocument(String docno, String headline, String text, Date date){
        DocNo = docno;
        Headline = headline;
        Text = text;
        Date = date;
    }

    @Override
    public String GetContent() {
        return Text;
    }

    @Override
    public String GetId() {
        return DocNo;
    }

    @Override
    public String GetTitle() {
        return Headline;
    }

    @Override
    public Date GetDate() {
        return Date;
    }
}
