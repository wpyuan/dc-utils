package com.github.dc.utils;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    /**
     * 日期列配置，key为列号（从1开始），value为日期格式
     */
    private Map<Integer, String> dateColumnFormats = new HashMap<>();
    /**
     * 是否自动检测日期格式单元格（默认开启）
     */
    private boolean autoDetectDate = true;
    /**
     * 默认日期时间格式
     */
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 默认日期格式
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * Excel 日期基准点（1900-01-01），但 Excel 错误地将 1900 年当作闰年，所以基准点是 1899-12-30
     */
    private static final LocalDateTime EXCEL_EPOCH = LocalDateTime.of(1899, 12, 30, 0, 0, 0);
    /**
     * Excel 内置日期格式 ID 集合
     * 14-22: 标准日期/时间格式
     * 45-47: 更多日期格式
     */
    private static final Set<Integer> BUILTIN_DATE_FORMAT_IDS = new HashSet<>(Arrays.asList(
            14, 15, 16, 17, 18, 19, 20, 21, 22, 45, 46, 47
    ));


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

    /**
     * 添加日期列配置（使用默认日期时间格式）
     * @param column 列号（从1开始）
     */
    public SAXExcelParser addDateColumn(int column) {
        this.dateColumnFormats.put(column, DEFAULT_DATETIME_FORMAT);
        return this;
    }

    /**
     * 添加日期列配置（自定义格式）
     * @param column 列号（从1开始）
     * @param format 日期格式，如 "yyyy-MM-dd HH:mm:ss"
     */
    public SAXExcelParser addDateColumn(int column, String format) {
        this.dateColumnFormats.put(column, format);
        return this;
    }

    /**
     * 批量设置日期列（使用默认日期时间格式）
     * @param columns 列号数组（从1开始）
     */
    public SAXExcelParser addDateColumns(int... columns) {
        for (int column : columns) {
            this.dateColumnFormats.put(column, DEFAULT_DATETIME_FORMAT);
        }
        return this;
    }

    /**
     * 开启自动检测日期格式单元格
     */
    public SAXExcelParser autoDetectDate() {
        this.autoDetectDate = true;
        return this;
    }

    /**
     * 判断格式字符串是否为日期格式
     * @param formatCode 格式代码
     * @return 是否为日期格式
     */
    private static boolean isDateFormat(String formatCode) {
        if (formatCode == null || formatCode.isEmpty()) {
            return false;
        }
        String upper = formatCode.toUpperCase();
        // 检查是否包含日期相关的格式字符
        // y: 年, m: 月, d: 日, h: 时, s: 秒
        // 排除纯数字格式如 "0.00" 中的 m (分钟)
        boolean hasDatePart = upper.contains("Y") || upper.contains("D");
        boolean hasTimePart = upper.contains("H") || upper.contains("S");
        // 对于 M，需要判断是月份还是分钟（如果有 H 或 S 附近的 M 是分钟，否则是月份）
        boolean hasMonth = false;
        if (upper.contains("M")) {
            // 简单判断：如果同时有 H 或 S，M 可能是分钟
            // 更安全的判断：如果没有 H 和 S，或者 M 和 Y/D 同时出现，则认为是月份
            if (!upper.contains("H") && !upper.contains("S")) {
                hasMonth = true;
            } else if (hasDatePart) {
                hasMonth = true;
            }
        }
        return hasDatePart || hasMonth || hasTimePart;
    }

    /**
     * 将 Excel 日期序列号转换为格式化字符串
     * @param excelDate Excel 日期序列号
     * @param format 日期格式
     * @return 格式化后的日期字符串
     */
    private static String formatExcelDate(double excelDate, String format) {
        if (excelDate < 0) {
            return String.valueOf(excelDate);
        }
        try {
            long wholeDays = (long) excelDate;
            double fractionOfDay = excelDate - wholeDays;

            // 计算时间部分（毫秒）
            long millisecondsInDay = Math.round(fractionOfDay * 24 * 60 * 60 * 1000);

            LocalDateTime dateTime = EXCEL_EPOCH
                    .plusDays(wholeDays)
                    .plusNanos(millisecondsInDay * 1_000_000);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return dateTime.format(formatter);
        } catch (Exception e) {
            log.warn("日期格式化失败！数据：{}，格式：{}", excelDate, format, e);
            return String.valueOf(excelDate);
        }
    }

    public void run(ThreeConsumer<Integer, Integer, List<String>> rowHandler) throws Exception {
        long startTime = System.currentTimeMillis();
        try (OPCPackage pkg = OPCPackage.open(this.file)) {
            XSSFReader xssfReader = new XSSFReader(pkg);
            XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            try (SharedStringsTable sst = xssfReader.getSharedStringsTable()){
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();

                // 解析样式表（如果启用了自动日期检测）
                Map<Integer, Boolean> dateFormatCache = new HashMap<>();
                if (this.autoDetectDate) {
                    try (InputStream stylesIs = xssfReader.getStylesData()) {
                        StylesHandler stylesHandler = new StylesHandler();
                        saxParser.parse(stylesIs, stylesHandler);
                        dateFormatCache = stylesHandler.getDateFormatCache();
                    } catch (Exception e) {
                        log.warn("解析样式表失败，将无法自动检测日期格式", e);
                    }
                }

                int sheetIndex = 1;
                while (it.hasNext()) {
                    try (InputStream is = it.next()) {
                        SheetHandler handler = new SheetHandler(sst, rowHandler, sheetIndex, this.beginRowNum, this.dateColumnFormats, this.autoDetectDate, dateFormatCache);
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

    /**
     * 样式表解析器，用于提取日期格式信息
     */
    private class StylesHandler extends DefaultHandler {
        // numFmtId -> isDateFormat（临时缓存）
        private Map<Integer, Boolean> numFmtToDateFlag = new HashMap<>();
        // 当前解析的 numFmtId
        private int currentNumFmtId = -1;
        // 格式代码内容
        private StringBuilder formatCodeBuilder = new StringBuilder();
        // cellXfs 列表索引
        private int xfIndex = -1;
        // cellXfs 的样式列表：样式索引 -> numFmtId
        private Map<Integer, Integer> xfToNumFmt = new HashMap<>();
        // 最终结果：样式索引 -> 是否为日期格式
        private Map<Integer, Boolean> styleToDateFlag = new HashMap<>();

        public Map<Integer, Boolean> getDateFormatCache() {
            return styleToDateFlag;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("numFmt")) {
                currentNumFmtId = Integer.parseInt(attributes.getValue("numFmtId"));
                formatCodeBuilder = new StringBuilder();
            } else if (qName.equals("cellXfs")) {
                xfIndex = -1;
            } else if (qName.equals("xf")) {
                xfIndex++;
                String numFmtIdStr = attributes.getValue("numFmtId");
                if (numFmtIdStr != null) {
                    int numFmtId = Integer.parseInt(numFmtIdStr);
                    xfToNumFmt.put(xfIndex, numFmtId);
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (currentNumFmtId >= 0) {
                formatCodeBuilder.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("numFmt")) {
                String formatCode = formatCodeBuilder.toString().trim();
                boolean isDate = isDateFormat(formatCode);
                numFmtToDateFlag.put(currentNumFmtId, isDate);
                currentNumFmtId = -1;
            } else if (qName.equals("styleSheet")) {
                // 样式表解析完成，建立样式索引 -> 是否为日期格式的映射
                // 1. 先将内置日期格式 ID 加入缓存
                for (Integer fmtId : BUILTIN_DATE_FORMAT_IDS) {
                    numFmtToDateFlag.put(fmtId, true);
                }
                // 2. 将样式索引转换为是否为日期格式
                for (Map.Entry<Integer, Integer> entry : xfToNumFmt.entrySet()) {
                    int styleIndex = entry.getKey();
                    int numFmtId = entry.getValue();
                    Boolean isDate = numFmtToDateFlag.get(numFmtId);
                    if (isDate != null && isDate) {
                        styleToDateFlag.put(styleIndex, true);
                    }
                }
            }
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
        // 手动配置的日期列
        private Map<Integer, String> dateColumnFormats;
        // 是否自动检测日期格式
        private boolean autoDetectDate;
        // 样式索引 -> 是否为日期格式的缓存
        private Map<Integer, Boolean> dateFormatCache;
        // 当前单元格列号（从1开始）
        private int currentColumnIndex = 0;
        // 当前单元格样式索引（s属性）
        private int currentStyleIndex = -1;

        public SheetHandler(SharedStringsTable sst, ThreeConsumer<Integer, Integer, List<String>> rowHandler, int sheetIndex, int beginRowNum, Map<Integer, String> dateColumnFormats, boolean autoDetectDate, Map<Integer, Boolean> dateFormatCache) {
            this.sst = sst;
            this.rowHandler = rowHandler;
            this.sheetIndex = sheetIndex;
            this.beginRowNum = beginRowNum;
            this.dateColumnFormats = dateColumnFormats;
            this.autoDetectDate = autoDetectDate;
            this.dateFormatCache = dateFormatCache;
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
                // 记录当前单元格列号
                currentColumnIndex = covertRowIdToInt(rowId);

                // 获取样式索引（s属性）
                String styleAttr = attributes.getValue("s");
                currentStyleIndex = styleAttr != null ? Integer.parseInt(styleAttr) : -1;

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
                            processCellValue(lastContents);
                        } catch (NumberFormatException ex) {
                            log.warn("数字格式化失败！数据：" + lastContents, ex);
                        }
                    } else {
                        processCellValue(lastContents);
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
                        processCellValue(lastContents);
                    } catch (NumberFormatException ex) {
                        log.warn("数字格式化失败！数据：" + lastContents, ex);
                    }
                } else {
                    processCellValue(lastContents);
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
         * 处理单元格值，如果是日期列则进行格式化转换
         * @param value 单元格原始值
         */
        private void processCellValue(String value) {
            // 1. 检查手动配置的日期列
            if (dateColumnFormats.containsKey(currentColumnIndex)) {
                String format = dateColumnFormats.get(currentColumnIndex);
                try {
                    double excelDate = Double.parseDouble(value);
                    currentRow.add(formatExcelDate(excelDate, format));
                } catch (NumberFormatException e) {
                    // 如果无法解析为数字，直接添加原始值
                    currentRow.add(value);
                }
                return;
            }

            // 2. 检查自动检测日期格式
            if (autoDetectDate && currentStyleIndex >= 0 && dateFormatCache != null) {
                Boolean isDateFormat = dateFormatCache.get(currentStyleIndex);
                if (isDateFormat != null && isDateFormat) {
                    try {
                        double excelDate = Double.parseDouble(value);
                        currentRow.add(formatExcelDate(excelDate, DEFAULT_DATETIME_FORMAT));
                    } catch (NumberFormatException e) {
                        currentRow.add(value);
                    }
                    return;
                }
            }

            // 3. 非日期列，直接添加原始值
            currentRow.add(value);
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
