import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.DictionaryEncoding;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class pdfWriter {
    //read pdf


    // the text you want to highlight (or annotate, as it's called in PDF language)
    static String[] texts = { "diversity", "Strikeout", "Squiggly", "Highlight" };
    //{"diversity", "Strikeout", "Squiggly", "Highlight" };

    public static void main (String[] args) throws Exception {


        PDDocument doc = null;
        //init the parameters
        String filename = "test.pdf";
        String searchString = "two";
        String replacement = "twoooo";

//      //open pdf file
        doc = PDDocument.load(new File(filename));
        if (doc != null){
            System.out.println(filename + " open success.");
        }

        //process
        PDPageTree pages = doc.getDocumentCatalog().getPages();
        System.out.println(pages.getCount()  + " pages.");
        for (int page_index = 0;page_index<pages.getCount();page_index++){
            PDPage page = pages.get(page_index);
            PDFStreamParser parser = new PDFStreamParser(page);
            parser.parse();
            List tokens = parser.getTokens();
            for (int j = 0; j < tokens.size(); j++) {
                Object next = tokens.get(j);
                if (next instanceof Operator) {
                    Operator op = (Operator) next;
                    //Tj and TJ are the two operators that display strings in a PDF
                    if (op.getName().equals("Tj")) {//Tj is Show text
                        // Tj takes one operator and that is the string to display so lets update that operator
                        COSString previous = (COSString) tokens.get(j - 1);
                        byte[] bytes = previous.getBytes();//.getString();
                        System.out.println(bytes);
                        String string = new String(bytes);
                        System.out.println(string);
                        string = string.replaceFirst(searchString, replacement);
                        previous.setValue(string.getBytes());
                    } else if (op.getName().equals("TJ")) {//TJ is show text with position adjustments
                        COSArray previous = (COSArray) tokens.get(j - 1);
                        for (int k = 0; k < previous.size(); k++) {
                            Object arrElement = previous.getObject(k);
                            if (arrElement instanceof COSString) {
                                COSString cosString = (COSString) arrElement;
                                String string = cosString.getString();
                                System.out.println(string);
                                //string = StringUtils.replaceOnce(string, searchString, replacement);
                                cosString.setValue(string.getBytes());
                            }
                        }
                    }
                }
            }
            PDStream updatedStream = new PDStream(doc);
            OutputStream out = updatedStream.createOutputStream();
            ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
            tokenWriter.writeTokens( tokens );
            page.setContents( updatedStream );
            out.close();
        }

        doc.save( "test2.pdf" );

//
//        PDFTextStripper tStripper = new PDFTextStripper();
//        tStripper.setStartPage(1);
//        tStripper.setEndPage(3);
//        PDDocument document = PDDocument.load(new File("src/test2.pdf"));
//        document.getClass();
//        String content = "";
//        if (!document.isEncrypted()) {
//            String pdfFileInText = tStripper.getText(document);
//            String[] lines = pdfFileInText.split("\\r\\n\\r\\n");
//            for (String line : lines) {
//                String[] words = line.split(" ");
//                for (int pos =1 ;pos<words.length;pos++){
//                    if (sensi_texts.contains(words[pos])){
//                        System.out.println("found "+ words[pos]);
//                    }
//                }
//
//                //System.out.println(line);
//                //content += line;
//            }
//        }
        //System.out.println(content.trim());

        if (true)
            System.exit(0);
        String outputFileName = "test.pdf";
        if (args.length > 0)
            outputFileName = args[0];

        // Create a document and add a page to it
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        // PDRectangle.LETTER and others are also possible
        PDRectangle rect = page.getMediaBox();
        // rect can be used to get the page width and height
        document.addPage(page);

        // Start a new content stream which will hold the content of the page
        PDPageContentStream cos = new PDPageContentStream(document, page);

        // Define a text content stream using the selected font, move the cursor and draw some text
        cos.setFont(PDType1Font.HELVETICA, 14);

        int line = 0;

        // add some text so we have something to annotate
        for (String str : texts) {
            cos.beginText();
            cos.newLineAtOffset(100, rect.getHeight() - 50*(++line));
            cos.showText(str);
            cos.endText();
        }

        // Make sure that the content stream is closed:
        cos.close();

        // some ornamental extras for some of the text:
        // highlighting, strikethrough, underline and squiggly underline
        PDFTextStripper annotate = new MyAnnotator();
        annotate.setSortByPosition(true);
        annotate.setStartPage(0);
        annotate.setEndPage(document.getNumberOfPages());
        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        annotate.writeText(document, dummy);

        // Save the results and ensure that the document is properly closed:
        document.save(outputFileName);
        document.close();
    }

    private static class MyAnnotator extends PDFTextStripper {

        public MyAnnotator() throws IOException {
            super();
        }

        @Override
        protected void writeString (String string, List<TextPosition> textPositions) throws IOException {
            float posXInit = 0, posXEnd = 0, posYInit = 0, posYEnd = 0, width = 0, height = 0, fontHeight = 0;

            int foundTextNo = -1;
            for (int i = 0; i < texts.length; i++) {
                if (string.contains(texts[i])) {
                    foundTextNo = i;
                    break;
                }
            }
            if (foundTextNo != -1) {
                posXInit = textPositions.get(0).getXDirAdj();
                posXEnd  = textPositions.get(textPositions.size() - 1).getXDirAdj()
                        + textPositions.get(textPositions.size() - 1).getWidth();
                posYInit = textPositions.get(0).getPageHeight()
                        - textPositions.get(0).getYDirAdj();
                posYEnd  = textPositions.get(0).getPageHeight()
                        - textPositions.get(textPositions.size() - 1).getYDirAdj();
                width    = textPositions.get(0).getWidthDirAdj();
                height   = textPositions.get(0).getHeightDir();

                List<PDAnnotation> annotations = document.getPage(this.getCurrentPageNo() - 1).getAnnotations();
                PDAnnotationTextMarkup markup = null;
                // choose any color you want, they can be different for each annotation
                PDColor color = new PDColor(new float[]{ 1, 1 / 255F, 1 }, PDDeviceRGB.INSTANCE);
                switch (foundTextNo) {
                    case 0:
                        markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE);
                        break;
                    case 1:
                        markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_STRIKEOUT);
                        break;
                    case 2:
                        markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_SQUIGGLY);
                        break;
                    case 3:
                        markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
                        break;
                }

                PDRectangle position = new PDRectangle();
                position.setLowerLeftX(posXInit);
                position.setLowerLeftY(posYEnd);
                position.setUpperRightX(posXEnd);
                position.setUpperRightY(posYEnd + height);
                markup.setRectangle(position);

                float quadPoints[] = {posXInit, posYEnd + height + 2,
                        posXEnd, posYEnd + height + 2,
                        posXInit, posYInit - 2,
                        posXEnd, posYEnd - 2};
                markup.setQuadPoints(quadPoints);

                markup.setColor(color);
                annotations.add(markup);
            }
        }
    }
}