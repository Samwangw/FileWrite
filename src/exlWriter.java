import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;

public class exlWriter {
    public static Workbook loadFile(String filename) {
        Workbook file = null;
        try {
            InputStream is = new FileInputStream(filename);
            file = new XSSFWorkbook(is);
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

    public static void main(String[] args) {
        //init
        String filename = "exl_test.xlsx";
        Workbook file = loadFile(filename);
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
            int sheet_count = file.getNumberOfSheets();
            for (int p_sheet = 0; p_sheet < sheet_count; p_sheet++) {
                //retrieve all rows in the sheet
                Iterator<Row> iterator = file.getSheetAt(p_sheet).iterator();
                while (iterator.hasNext()) {
                    Row currentRow = iterator.next();
                    for (int i = 0; i < currentRow.getLastCellNum(); i++) {
                        if (currentRow.getCell(i) != null && currentRow.getCell(i).getCellType() == CellType.STRING) {
                            String cell_text = currentRow.getCell(i).getStringCellValue();
                            if (cell_text.contains(sword)) {
                                String new_text = cell_text.replace(sword, replacement);
                                currentRow.getCell(i).setCellValue(new_text);
                            }
                        }
                    }
                }
            }
            //write out
            FileOutputStream fos = new FileOutputStream(new File("new_" + filename));
            file.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
