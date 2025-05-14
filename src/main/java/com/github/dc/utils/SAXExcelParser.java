package com.github.dc.utils;

import com.github.dc.utils.pojo.Pair;
import com.github.dc.utils.pojo.ThreeConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * <p>
 *     SAX方式解析Excel 工具
 *     注：导入前把所有单元格格式转为文本，格式转换后再比对是否数值出现异常，特别关注日期格式，如若异常，请特别处理”将日期格式转换成文本格式“
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/10/12 8:55
 */
@Slf4j
public class SAXExcelParser {
    /**
     * 要读取的文件
     */
    private File file;
    /**
     * 文件有效区域的起始行号，可设置数据行，跳过标题行读取。默认标题行在第一行
     */
    private int beginRowNum = 1;


    public static SAXExcelParser start() {
        return new SAXExcelParser();
    }

    public SAXExcelParser file(File file) {
        this.file = file;
        return this;
    }

    public SAXExcelParser beginRowNum(int beginRowNum) {
        this.beginRowNum = beginRowNum;
        return this;
    }

    public void run(ThreeConsumer<Integer, Integer, List<String>> rowHandler) throws Exception {
        long startTime = System.currentTimeMillis();
        try (OPCPackage pkg = OPCPackage.open(this.file)) {
            XSSFReader xssfReader = new XSSFReader(pkg);
            XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            try (SharedStringsTable sst = xssfReader.getSharedStringsTable()){
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                int sheetIndex = 1;
                while (it.hasNext()) {
                    try (InputStream is = it.next()) {
                        SheetHandler handler = new SheetHandler(sst, rowHandler, sheetIndex, this.beginRowNum);
                        saxParser.parse(is, handler);
                    } finally {
                        sheetIndex ++;
                    }
                }
            }
        } finally {
            log.debug("读取【{}】Excel并处理结束，耗时{}秒", this.file.getName(), BigDecimal.valueOf((System.currentTimeMillis() - startTime)).divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP));
        }
    }

    private class SheetHandler extends DefaultHandler {
        //取SST 的索引对应的值
        private SharedStringsTable sst;
        // 每行数据读完就执行
        private ThreeConsumer<Integer, Integer, List<String>> rowHandler;
        // 第几个sheet页，从1开始
        private int sheetIndex;
        // 读取excel内容起始行
        private int beginRowNum;

        public SheetHandler(SharedStringsTable sst, ThreeConsumer<Integer, Integer, List<String>> rowHandler, int sheetIndex, int beginRowNum) {
            this.sst = sst;
            this.rowHandler = rowHandler;
            this.sheetIndex = sheetIndex;
            this.beginRowNum = beginRowNum;
        }

        /**
         * 存储cell标签下v标签包裹的字符文本内容
         * 在v标签开始后，解析器自动调用characters()保存到 lastContents
         * 【但】当cell标签的属性 s是 t时, 表示取到的lastContents是 SharedStringsTable 的index值
         * 需要在v标签结束时根据 index(lastContents)获取一次真正的值
         */
        private String lastContents;
        //有效数据矩形区域,A1:Y2
        private String dimension;
        //根据dimension得出每行的数据长度
        private int longest;
        //上个有内容的单元格id，判断空单元格
        private String lastCellid;
        //上一行id, 判断空行
        private String lastRowid;
        // 判断单元格cell的c标签下是否有v，否则可能数据错位
        private boolean hasV = false;
        //行数据保存
        private List<String> currentRow;
        //单元格内容是SST 的索引
        private boolean isSSTIndex = false;


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            lastContents = "";
            if (qName.equals("dimension")) {
                dimension = attributes.getValue("ref");
                longest = covertRowIdToInt(dimension.substring(dimension.indexOf(":") + 1));
            }
            //行开始
            if (qName.equals("row")) {
                String rowNum = attributes.getValue("r");
                //判断空行
                if (lastRowid != null) {
                    //与上一行相差2, 说明中间有空行
                    int gap = Integer.parseInt(rowNum) - Integer.parseInt(lastRowid);
                    if (gap > 1) {
                        gap -= 1;
                        while (gap > 0) {
                            if (this.beginRowNum < Integer.valueOf(rowNum)-1) {
                                rowHandler.accept(sheetIndex, Integer.parseInt(rowNum)-1, new ArrayList<>());
                            }
                            gap--;
                        }
                    }
                }

                lastRowid = attributes.getValue("r");
                currentRow = new ArrayList<>();
            }
            if (qName.equals("c")) {
                String rowId = attributes.getValue("r");

                //空单元判断，添加空字符到list
                if (lastCellid != null) {
                    int gap = covertRowIdToInt(rowId) - covertRowIdToInt(lastCellid);
                    for (int i = 0; i < gap - 1; i++) {
                        currentRow.add("");
                    }
                } else {
                    //第一个单元格可能不是在第一列
                    if (!"A1".equals(rowId)) {
                        for (int i = 0; i < covertRowIdToInt(rowId) - 1; i++) {
                            currentRow.add("");
                        }
                    }
                }
                lastCellid = rowId;


                //判断单元格的值是SST 的索引，不能直接characters方法取值
                if (attributes.getValue("t") != null && attributes.getValue("t").equals("s")) {
                    isSSTIndex = true;
                } else {
                    isSSTIndex = false;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            //行结束,存储一行数据
            if (qName.equals("row")) {

                //判断最后一个单元格是否在最后，补齐列数
                //【注意】有的单元格只修改单元格格式，而没有内容，会出现c标签下没有v标签，导致currentRow少
                if (covertRowIdToInt(lastCellid) < longest) {
                    int min = Math.min(currentRow.size(), covertRowIdToInt(lastCellid));
                    for (int i = 0; i < longest - min; i++) {
                        currentRow.add("");
                    }
                }
                if (this.beginRowNum < Integer.parseInt(lastRowid)) {
                    rowHandler.accept(sheetIndex, Integer.valueOf(lastRowid), currentRow);
                }
                lastCellid = null;
            }

            //单元格结束，没有v时需要补位
            if (qName.equals("c")){
                if (!hasV) {
                    //单元格的值是SST 的索引
                    if (isSSTIndex) {
                        String sstIndex = lastContents.toString();
                        try {
                            int idx = Integer.parseInt(sstIndex);
                            XSSFRichTextString rtss = new XSSFRichTextString(
                                    sst.getEntryAt(idx));
                            lastContents = rtss.toString();
                            currentRow.add(lastContents);
                        } catch (NumberFormatException ex) {
                            log.warn("数字格式化失败！数据：" + lastContents, ex);
                        }
                    } else {
                        currentRow.add(lastContents);
                    }
                }
                hasV = false;
            }

            //单元格内容标签结束，characters方法会被调用处理内容
            if (qName.equals("v")) {
                hasV = true;
                //单元格的值是SST 的索引
                if (isSSTIndex) {
                    String sstIndex = lastContents.toString();
                    try {
                        int idx = Integer.parseInt(sstIndex);
                        XSSFRichTextString rtss = new XSSFRichTextString(
                                sst.getEntryAt(idx));
                        lastContents = rtss.toString();
                        currentRow.add(lastContents);
                    } catch (NumberFormatException ex) {
                        log.warn("数字格式化失败！数据：" + lastContents, ex);
                    }
                } else {
                    currentRow.add(lastContents);
                }

            }

        }


        /**
         * 获取element的文本数据
         *
         * @see ContentHandler#characters
         */
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            lastContents += new String(ch, start, length);
        }

        /**
         * 列号转数字   AB7-->28 第28列
         *
         * @param cellId 单元格定位id，行列号，AB7
         * @return
         */
        public int covertRowIdToInt(String cellId) {
            StringBuilder sb = new StringBuilder();
            String column = "";
            //从cellId中提取列号
            for(char c:cellId.toCharArray()){
                if (Character.isAlphabetic(c)){
                    sb.append(c);
                }else{
                    column = sb.toString();
                }
            }
            //列号字符转数字
            int result = 0;
            for (char c : column.toCharArray()) {
                result = result * 26 + (c - 'A') + 1;
            }
            return result;
        }
    }

}
