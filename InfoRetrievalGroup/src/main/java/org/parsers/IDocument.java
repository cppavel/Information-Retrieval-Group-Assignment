package org.parsers;

import java.util.Date;

public interface  IDocument {

    public  String GetContent();
    public String GetId();
    public String GetTitle();
    public Date GetDate();
}
