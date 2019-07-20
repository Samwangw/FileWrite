import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class pptWriter {
    public static XMLSlideShow loadFile(String filename) {
        XMLSlideShow file = null;
        try {
            InputStream is = new FileInputStream(filename);
            file = new XMLSlideShow(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return file;
        }
    }

    public static String loadSensiWord() {
        String word = "";
        word = "测试";
        return word;
    }

    public static void main(String[] args){
        //init
        String filename = "ppt_test.pptx";
        XMLSlideShow file = loadFile(filename);
        if (file == null) {
            System.out.println(filename+" open fail.");
            System.exit(1);
        }
        else{
            System.out.println(filename+" open success.");
        }
        String sword = loadSensiWord();
        String replacement = "";
        if (sword == "") {
            System.out.println("Sensitive word is empty.");
            System.exit(2);
        }
        else
            replacement = "XXX";

        //main process
        try {
            for (XSLFSlide slide : file.getSlides()) {
                for (XSLFShape shape : slide) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape txShape = (XSLFTextShape) shape;
                        System.out.println(txShape.getText());
                        String newText = txShape.getText().replace(sword,replacement);
                        txShape.setText(newText);
                    } else if (shape instanceof XSLFPictureShape) {
                        //do nothing
                    } else if (shape instanceof XSLFGraphicFrame || shape instanceof XSLFTable) {
                        //print all text in it or in its children
                    }
                }
            }

            //write out
            FileOutputStream fos = new FileOutputStream(new File("new_"+filename));
            file.write(fos);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
