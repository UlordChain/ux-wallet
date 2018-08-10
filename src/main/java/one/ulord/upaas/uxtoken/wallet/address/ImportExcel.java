package one.ulord.upaas.uxtoken.wallet.address;


import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImportExcel {

    public List<AddressTarget> getAllByExcel(File file) throws IOException {

        List<AddressTarget> list = new ArrayList<>();
        XSSFWorkbook workbook = null;

        try {
            // 获取Excel对象
            workbook = new XSSFWorkbook(file);
            // 获取选项卡对象 第0个选项卡
            XSSFSheet sheet = workbook.getSheetAt(0);
            // 循环选项卡中的值
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                // 获取单元格对象，然后取得单元格的值,并设置到对象中
                XSSFRow row = sheet.getRow(rowNum);
                String address = row.getCell(0).getStringCellValue();
                if (!address.startsWith("0x")) continue;
                String amount = row.getCell(1).getRawValue();
                AddressTarget person = new AddressTarget(address, amount);
                list.add(person);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null){
                workbook.close();
            }
        }
        return list;
    }
}
