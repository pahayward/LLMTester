package com.automwrite.assessment;

import com.automwrite.assessment.controller.Controller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
class ApplicationTest {

    @Autowired
    private Controller myController;

    @Test
    void testControllerService() throws IOException {

        // https://stackoverflow.com/questions/21800726/using-spring-mvc-test-to-unit-test-multipart-post-request
        Path partA = Paths.get("different tones/automwrite - A - Casual tone.docx");
        Path partB = Paths.get("different tones/automwrite - B - Formal tone.docx");
        //Path partC = Paths.get("different tones/automwrite - C - Grandiloquent tone.docx");

        MockMultipartFile mpA = new MockMultipartFile("toneA",
                partA.toString(),
                "application/x-zip-compressed",
                Files.readAllBytes(partA));

        MockMultipartFile mpB = new MockMultipartFile("toneB",
                partB.toString(),
                "application/x-zip-compressed",
                Files.readAllBytes(partB));


        var resp = myController.test( mpA, mpB );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testTextOnly() throws IOException {

        // Kinda funny ;-)
        String docA = "Dear sir, I'd like to apply for yet another job in what is he roughest market I've known since the turn of the century. I realise that you may not be totally on board with my skills but I can assure I will do my best to make this a profitable venture. CHeers bud, Phil";
        String docB = "Hey dude, hows it going, do you have any jobs I can  get down with now the market is at the bottom. I think stuff is tough at the moment. Cheers, Phil";

        var resp = myController.testText( docA, docB );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

}
