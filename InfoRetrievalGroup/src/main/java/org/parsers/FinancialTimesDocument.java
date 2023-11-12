package org.parsers;

import java.util.Date;

public class FinancialTimesDocument implements IDocument {
    private final String DocNo;
    private final String Profile;
    private final java.util.Date Date;
    private final String Headline;
    private final String Text;
    private final String Pub;
    private final String Page;
    private final String Byline;

    public FinancialTimesDocument(String docNo, String profile, Date date, String headline, String text, String pub, String page, String byline) {
        DocNo = docNo;
        Profile = profile;
        Date = date;
        Headline = headline;
        Text = text;
        Pub = pub;
        Page = page;
        Byline = byline;
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
    public java.util.Date GetDate() {
        return Date;
    }
}
