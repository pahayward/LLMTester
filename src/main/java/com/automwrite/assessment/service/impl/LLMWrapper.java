package com.automwrite.assessment.service.impl;

import com.automwrite.assessment.service.LlmService;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class LLMWrapper {

    private final static String tonePrompt = "Identify the tone of the following text, generate a reply with only Casual, Formal or Grandiloquent as a response. Do not provide any explanation. This is the text:";
    private final static String updatePrompt = "Update the following text to have the tone %s. The response should have the same number of lines and retain same semantic meaning when replying. Only include your reply and not any additional explanation. This is the text : %s";


    // Get tone of text (async/long-running operation)
    // Above text "identify... etc" + what text to analyze
    public static String getTone(LlmService llmSvc, XWPFDocument inputWordDoc)
    {
        String inputDocText = getText( inputWordDoc );
        return llmSvc.generateText(tonePrompt.concat( inputDocText));
    }

    // Override for text
    public static String getTone(LlmService llmSvc, String inputText)
    {
        return llmSvc.generateText(tonePrompt.concat( inputText));
    }

    // Override for word doc
    public static String setToneOf(LlmService llmSvc,  XWPFDocument inputWordDoc, String tone)
    {
        String inputDocText = getText( inputWordDoc );
        String inputText = String.format( updatePrompt, tone, inputDocText );
        return llmSvc.generateText( inputText );
    }

    // Override for text
    public static String setToneOf(LlmService llmSvc,  String inputDocText, String tone)
    {
        String inputText = String.format( updatePrompt, tone, inputDocText );
        return llmSvc.generateText( inputText );
    }

    // Get the text from a word document:
    public static String getText(XWPFDocument document)
    {
        assert ( document != null );
        XWPFWordExtractor oExtract = new XWPFWordExtractor( document );
        return oExtract.getText();
    }

    public static XWPFDocument ReworkDocument(XWPFDocument content, String newText)
    {
        // This is the text of the original document
        String originalText = getText( content );
        String originLines[] = originalText.split("\\r?\\n");
        String newLines[] = newText.split("\\r?\\n");




        // This is the new text
        return content;
    }
}
