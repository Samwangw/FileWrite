import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;

import java.io.*;
import java.util.List;


public class docWriter {
    public static XWPFDocument loadDocFile(String filename) {
        XWPFDocument file = null;
        try {
            InputStream is = new FileInputStream(filename);
            file = new XWPFDocument(is);
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

    public static void doOnePar(XWPFParagraph par, String sword, String replacement) {
        List<XWPFRun> runs = par.getRuns();
        //search text
        System.out.println("---------------------");
        String full_txt = "";
        for (int run_index = 0; run_index < runs.size(); run_index++) {
            XWPFRun run = runs.get(run_index);
            System.out.println("run " + run_index + " text :" + run.getText(0));
            String part_text = "";
            if (run.getText(0) != null)
                part_text = run.getText(0).trim();
            if (part_text.contains(sword)) {
                part_text = part_text.replace(sword, replacement);     //replace sensitive word
                run.setText(part_text, 0);                         //update text of the current run
            }
            full_txt += part_text;
            System.out.println("full:" + full_txt);
            if (full_txt.contains(sword)) {
                //todo
                int pos = full_txt.indexOf(sword);

                //update text in current run
                System.out.println(sword.length());
                System.out.println(full_txt.length());
                System.out.println(pos);
                System.out.println(part_text.length());
                int sword_left_length = full_txt.length() - pos - part_text.length();
                System.out.println(sword_left_length + " left");
                String newtxt = replacement + full_txt.substring(pos + sword.length());//usable substring in current run
                run.setText(newtxt, 0);
                //traceback to update text in previous runs
                for (int j = run_index - 1; j >= 0; j--) {
                    if (runs.get(j).getText(0) == null)
                        continue;
                    int j_length = runs.get(j).getText(0).length();
                    if (j_length < sword_left_length) {
                        runs.get(j).setText("", 0);
                        sword_left_length -= j_length;
                    } else {
                        System.out.println("old:" + runs.get(j).getText(0));
                        String new_j_txt = runs.get(j).getText(0).substring(0, j_length - sword_left_length);
                        System.out.println("new:" + new_j_txt);
                        runs.get(j).setText(new_j_txt, 0);
                        sword_left_length -= j_length;
                    }
                    if (sword_left_length <= 0) {
                        full_txt = "";
                        for (int k = 0; k <= run_index; k++) {
                            if (runs.get(j).getText(0) == null)
                                full_txt += runs.get(j).getText(0);
                        }
                    }

                }
            }
        }
    }

    public static void showPar(XWPFParagraph par) {
        List<XWPFRun> runs = par.getRuns();
        //search text
        for (int run_index = 0; run_index < runs.size(); run_index++) {
            XWPFRun run = runs.get(run_index);
            String txt = "";
            System.out.println("run " + run_index + ":" + run.getText(0));
        }
    }

    public static void main(String[] args) {
        //init
        String filename = "test.docx";
        XWPFDocument file = loadDocFile(filename);
        if (file == null) {
            System.out.println(filename + " open fail.");
            System.exit(1);
        } else {
            System.out.println(filename + " open success.");
        }

        String sword = loadSensiWord();
        String replacement = "";
        if (sword == "") {
            System.out.println("Sensitive word is empty.");
            System.exit(2);
        } else
            replacement = "XXX";
        //main process
        try {
            //retrieve all the paragraphs
            System.out.println("retrieve all the paragraphs");
            for (XWPFParagraph par : file.getParagraphs()) {
                doOnePar(par, sword, replacement);
            }
            //retrieve all the tables
            System.out.println("retrieve all the tables");
            List<XWPFTable> tables = file.getTables();
            for (int i = 0; i < tables.size(); i++) {
                XWPFTable curTable = tables.get(i);
                List<XWPFTableRow> rows = curTable.getRows();
                for (XWPFTableRow row : rows) {
                    List<XWPFTableCell> cells = row.getTableCells();
                    for (XWPFTableCell cell : cells) {
                        for (XWPFParagraph par : cell.getParagraphs()) {
                            //showPar(par);
                            doOnePar(par, sword, replacement);
                        }
                    }
                }
                System.out.println(curTable.getText());

            }
            //retrieve all the header
            System.out.println("retrieve all the headers");
            for (XWPFHeader header : file.getHeaderList()) {
                for (XWPFParagraph par : header.getParagraphs()) {
                    doOnePar(par, sword, replacement);
                }
            }
            //retrieve all the footers
            System.out.println("retrieve all the footers");
            for (XWPFFooter footer : file.getFooterList()) {
                for (XWPFParagraph par : footer.getParagraphs()) {
                    doOnePar(par, sword, replacement);
                }
            }
            //write out
            FileOutputStream fos = new FileOutputStream(new File("new " + filename));
            file.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
