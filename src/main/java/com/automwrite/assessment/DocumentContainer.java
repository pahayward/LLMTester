package com.automwrite.assessment;

import lombok.Getter;
import lombok.Setter;


public class DocumentContainer {

    public DocumentContainer(String id, String docFileName, String docSentiment, byte[] payload) {
        this.id = id;
        this.docFileName = docFileName;
        this.docSentiment = docSentiment;
        this.filePayload = payload;
    }

    public DocumentContainer() {
        this.id = id;
        this.docFileName = "null";
        this.docSentiment = "null";
        this.filePayload = null;
    }

    @Setter
    @Getter
    public String id;

    @Setter @Getter
    public String docFileName;

    @Setter @Getter
    public String docSentiment;

    @Setter @Getter
    public byte[] filePayload;

}
