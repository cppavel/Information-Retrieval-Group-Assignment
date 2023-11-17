package org.parsers;

import javax.print.Doc;
import java.util.Date;

public class FedRegisterDocument implements  IDocument{

    private String DocNo;
    private String Text;

    private String Title;

    private String Date;


    public FedRegisterDocument(String docno, String text, String title, String date){
        DocNo = docno;
        Text = text;
        Title = title;
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
        return Title;
    }

    @Override
    public String GetDate() {
        if(Date == null)
            return "";

        return Date;
    }
}
