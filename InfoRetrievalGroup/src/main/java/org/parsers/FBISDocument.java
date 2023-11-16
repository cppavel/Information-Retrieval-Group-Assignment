package org.parsers;

import java.util.Date;

public class FBISDocument implements IDocument {
    private final String DocNo;
    private final java.util.Date Date;
    private final String Title;
    private final String Text;



    public FBISDocument(String docNo, Date date, String title, String text) {
        DocNo = docNo;
        Date = date;
        Title = title;
        Text = text;
        
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
        return Title;
    }

    @Override
    public java.util.Date GetDate() {
        return Date;
    }
}
