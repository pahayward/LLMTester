package com.automwrite.assessment.controller;

import com.automwrite.assessment.DocumentContainer;
import com.automwrite.assessment.service.LlmService;
import com.automwrite.assessment.service.impl.LLMWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class Controller {

    private final LlmService llmService;
    private final static String tonePrompt = "Identify the tone of the following text, generate a reply with only Casual, Formal or Grandiloquent as a response. Do not provide any explanation. This is the text:";
    private final static String updatePrompt = "Update the following text to have the tone %s. The response should have the same number of lines and the same semantic meaning when replying. Only include your reply and not any additional explanation. This is the text : %s";

    private Map<String, DocumentContainer> myDocs = new HashMap<>();


    // Create / Update DocumentContainers held by class in memory
    // http://localhost:8080/api/document
    @PostMapping("/document")
    public DocumentContainer PostDocument(@RequestPart("data") MultipartFile doc) {

        // Document ID
        try {
            String id = UUID.randomUUID().toString();
            DocumentContainer newDoc = new DocumentContainer(id, doc.getOriginalFilename(), "", doc.getBytes());
            if (myDocs.containsKey(newDoc.getId())) {
                myDocs.replace(id, newDoc);
            } else {
                myDocs.put(id, newDoc);
            }
            return newDoc; // Give you a copy back
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST );
        }
    }

    // Read http://localhost:8080/api/document/{id}
    @GetMapping("/document/{id}")
    public DocumentContainer GetDocument(@PathVariable String id)
    {
        var doc = myDocs.get( id );
        if (doc != null)
            return doc;

        throw new ResponseStatusException(HttpStatus.NOT_FOUND );
    }

    // Get the complete list http://localhost:8080/api/document/list
    @GetMapping("/document/list")
    public Map<String, DocumentContainer> GetList()
    {
        if (myDocs.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND );

        return myDocs;
    }

    @PostMapping("/dowork")
    public ResponseEntity<String> DoProcessing() throws IOException {
        if (myDocs.size() == 2)
        {
            // Some bad java..
            String[] keys = new String[2];
            int i = 0;
            for (String key : myDocs.keySet()) {
                keys[i] = key;
                i++;
            }

            // Turn binary payloads into word doc obj
            var docABinary = myDocs.get( keys[0] ).filePayload;
            XWPFDocument docA = new XWPFDocument(new ByteArrayInputStream(docABinary));

            var docBBinary = myDocs.get( keys[1] ).filePayload;
            XWPFDocument docB = new XWPFDocument(new ByteArrayInputStream(docBBinary));

            // Get the tone
            var tone = LLMWrapper.getTone(llmService, docA );

            var newDocument = LLMWrapper.setToneOf(llmService, docB, tone);
            return ResponseEntity.ok(newDocument);
        }


        // Not quite right
        return ResponseEntity.ok("Failed though... ");

    }


    // And just something else from testing
    @GetMapping("/about")
    public String About() {
        return "(c)2024 Phil Hayward";
    }

    // Get tone of text (async/long-running operation)
    // Ab/ove text "identify... etc" + what text to analyze
    private String getTone(LlmService llmSvc, XWPFDocument inputWordDoc)
    {
        String inputDocText = getText( inputWordDoc );
        return llmSvc.generateText(tonePrompt.concat( inputDocText));
    }

    private String setToneOf(LlmService llmSvc,  XWPFDocument inputWordDoc, String tone)
    {
        String inputDocText = getText( inputWordDoc );
        String inputText = String.format( updatePrompt, tone, inputDocText );
        return llmSvc.generateText( inputText );
    }


    // Get the text from a word document:
   private String getText(XWPFDocument document)
    {
        assert ( document != null );
        XWPFWordExtractor oExtract = new XWPFWordExtractor( document );
        return oExtract.getText();
    }

    /**
     * You should extract the tone from the `toneFile` and update the `contentFile` to convey the same content
     * but using the extracted tone.
     * @param toneFile File to extract the tone from
     * @param contentFile File to apply the tone to
     * @return A response indicating that the processing has completed
     */
    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestParam MultipartFile toneFile, @RequestParam MultipartFile contentFile) throws
        IOException {

        final long startTime = System.currentTimeMillis();

        // Load the documents; which is apparently slow.. sad
        XWPFDocument toneDocument = new XWPFDocument(toneFile.getInputStream());
        XWPFDocument contentDocument = new XWPFDocument(contentFile.getInputStream());

        // Also slow..
        var tone = LLMWrapper.getTone(llmService, toneDocument );
        log.info("tone of input document: " + tone);

        assert(!tone.isEmpty());
        var newDocument = LLMWrapper.setToneOf(llmService, contentDocument, tone);
        log.info("input output: " + getText(contentDocument));
        log.info("altered output: " + newDocument);


        // Just like, how really?
        final long endTime = System.currentTimeMillis();
        var howSlow = String.format("This took %d mS", endTime-startTime);
        log.info(howSlow);

        // Put the new document in here till work out how to return/process
        return ResponseEntity.ok(newDocument);
    }

    @PostMapping("/test_text")
    public ResponseEntity<String> testText(@RequestParam String toneText, @RequestParam String contentText) throws
            IOException {

        final long startTime = System.currentTimeMillis();


        // Also slow..
        var tone = LLMWrapper.getTone(llmService, toneText );
        log.info("tone of input document: " + tone);

        assert(!tone.isEmpty());
        var newDocument = LLMWrapper.setToneOf(llmService, contentText, tone);
        log.info("input output: " + contentText);
        log.info("altered output: " + newDocument);


        // Just like, how really?
        final long endTime = System.currentTimeMillis();
        var howSlow = String.format("This took %d mS", endTime-startTime);
        log.info(howSlow);

        // Simple response to indicate that everything completed
        // Put the new document in here till work out how to return/process
        return ResponseEntity.ok(newDocument);
    }

}
