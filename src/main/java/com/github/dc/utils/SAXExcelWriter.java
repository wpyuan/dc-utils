package com.github.dc.utils;

import com.github.dc.utils.pojo.ExcelWriteSetup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.*;
import java.util.function.Function;

/**
 * <p>
 *     使用SXSSF + SAX模式 Apache POI的SXSSFWorkbook基于流式API，通过临时文件存储数据，内存占用仅保留最近N行数据。
 *     注：即使使用流式写入，仍需合理设置JVM内存：
 *      java -Xmx2g -Xms2g -XX:+UseG1GC YourApplication
 *      <ul>
 *          <li>-Xmx：根据物理内存设置最大堆（如2G）。</li>
 *          <li>-XX:+UseG1GC：使用G1垃圾回收器，适合大内存堆。</li>
 *      </ul>
 *      避免样式滥用：频繁设置单元格样式会导致内存泄漏。
 *      监控内存：通过jvisualvm或Arthas监控堆内存使用情况。
 * </p>
 *
 * @author wangpeiyuan
 * @date 2025/5/7 8:55
 */
@Slf4j
public class SAXExcelWriter {

    /**
     * sheet名称
     */
    private String sheetName;
    /**
     * 写入设置
     */
    private ExcelWriteSetup setup;
    /**
     * 写好文件后的输出流
     */
    private OutputStream os;
    /**
     * 表头样式
     */
    private CellStyle headerStyle;
    /**
     * 数据样式
     */
    private CellStyle dataStyle;
    /**
     * 当前sheet页
     */
    private SXSSFSheet currentSheet;
    /**
     * 单sheet页最大行数 0...1048575
     */
    public static final int MAX_ROWS_PER_SHEET = 1048575;


    public static SAXExcelWriter start() {
        return new SAXExcelWriter();
    }

    public SAXExcelWriter sheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public SAXExcelWriter setup(ExcelWriteSetup setup) {
        this.setup = setup;
        return this;
    }

    public SAXExcelWriter out(OutputStream os) {
        this.os = os;
        return this;
    }

    /**
     * 执行写入Excel
     * @param getDataFunction 传参当前批次，不分批处理时为-1，返回当前批次数据List
     */
    public void run(Function<Integer, List<Map<String, Object>>> getDataFunction) {
        long startTime = System.currentTimeMillis();
        int total = 0;
        // 1.创建工作簿，设置内存窗口大小（保留最近rowAccessWindowSize行在内存）
        // 参数表示内存中保留的行数，值越小内存占用越低，但频繁IO可能影响性能。
        // 建议：根据数据行宽度和内存大小调整，通常50-200之间。
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(this.setup.getRowAccessWindowSize())) {
            // 2.创建sheet页
            this.initSheet(workbook, this.sheetName);
            // 第几批次
            int batchNum = this.setup.isBatchWrite() ? 1 : -1;
            // 3.分批次写入数据
            List<Map<String, Object>> dataBatch = getDataFunction.apply(batchNum);
            if (CollectionUtils.isNotEmpty(dataBatch)) {
                do {
                    total += dataBatch.size();
                    writeData(workbook, dataBatch);
                } while (batchNum != -1 && (dataBatch = getDataFunction.apply(++batchNum)) != null);
            }
            // 4.写入文件并清理临时文件
            workbook.write(this.os);
            // 清理临时文件
            workbook.dispose();
        } catch (Exception e) {
            log.error("写入excel异常！", e);
        } finally {
            BigDecimal consumingTime = BigDecimal.valueOf((System.currentTimeMillis() - startTime)).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            if (consumingTime.equals(BigDecimal.ZERO)) {
                log.debug("导出{}行到【{}】Excel，耗时{}秒", total, this.sheetName, consumingTime);
            } else {
                log.debug("导出{}行到【{}】Excel，耗时{}秒，平均每秒写入{}行", total, this.sheetName, consumingTime, BigDecimal.valueOf(total).divide(consumingTime, 2, RoundingMode.HALF_UP));
            }
        }
    }

    /**
     * 创建sheet页
     * @param workbook
     * @param sheetName
     */
    private void initSheet(SXSSFWorkbook workbook, String sheetName) {
        this.currentSheet = workbook.createSheet(sheetName);
        // 创建可复用的单元格样式（关键：减少样式对象数量）
        if (this.headerStyle == null) {
            this.headerStyle = createHeaderStyle(workbook);
        }
        if (this.dataStyle == null) {
            this.dataStyle = createDataStyle(workbook);
        }
        // 写入表头
        this.setup.getHeaderCell().sort(Comparator.comparing(ExcelWriteSetup.HeaderCell::getOrderSeq));
        SXSSFRow headerRow = this.currentSheet.createRow(0);
        headerRow.setHeight((short) 400);
        SXSSFCell cell = null;
        for (ExcelWriteSetup.HeaderCell headerCell : this.setup.getHeaderCell()) {
            cell = headerRow.createCell(headerCell.getOrderSeq());
            cell.setCellValue(headerCell.getText());
            cell.setCellStyle(this.headerStyle);
            // Excel列宽单位转换：1汉字符=512单位，并增加110%缓冲
            // 简化计算：1汉字符≈1宽度单位（实际需乘以512）
            this.currentSheet.setColumnWidth(headerCell.getOrderSeq(), (int) (headerCell.getText().length() * 2.1 * 512));
        }
        // 冻结前1行和第0列（参数为行数、列数）
        this.currentSheet.createFreezePane(0, 1);
    }

    /**
     * 写入当前批次数据
     * @param workbook
     * @param dataBatch
     */
    private void writeData(SXSSFWorkbook workbook, List<Map<String, Object>> dataBatch) {
        for (Map<String, Object> rowData : dataBatch) {
            // 自动分页到新工作表
            if (this.currentSheet.getLastRowNum() >= MAX_ROWS_PER_SHEET) {
                if (workbook.getNumberOfSheets() == 1) {
                    workbook.setSheetName(0, workbook.getSheetName(0) + "（第一页）");
                }
                this.initSheet(workbook, this.sheetName + "（第" + (workbook.getNumberOfSheets() + 1) + "页）");
            }
            writeRowData(this.setup, rowData);
            try {
                // 强制刷新已写入数据到磁盘（可选，但建议定期调用），清理超过rowAccessWindowSize行的旧数据
                this.currentSheet.flushRows(this.setup.getRowAccessWindowSize());
            } catch (IOException e) {
                log.warn("写入Excel时，手动清理已写入磁盘的行异常，可忽略", e);
            }
        }
    }

    /**
     * 写入当前行数据
     * @param excelWriteSetup
     * @param rowData
     */
    private void writeRowData(ExcelWriteSetup excelWriteSetup, Map<String, Object> rowData) {
        int rowNum = this.currentSheet.getLastRowNum() + 1;
        SXSSFRow row = this.currentSheet.createRow(rowNum);
        for (ExcelWriteSetup.HeaderCell headerCell : excelWriteSetup.getHeaderCell()) {
            SXSSFCell cell = row.createCell(headerCell.getOrderSeq());
            cell.setCellStyle(this.dataStyle);
            Object value = rowData.get(headerCell.getVarName());
            if (value == null) {
                continue;
            }
            if (value instanceof Date) {
                value = DateFormat.getDateTimeInstance().format((Date) value);
            }
            // The maximum length of cell contents (text) is 32767 characters
            if (String.valueOf(value).length() > 32767) {
                value = String.valueOf(value).substring(0, 32758) + "...(超长截断)";
            }
            cell.setCellValue(String.valueOf(value));
        }
    }

    /**
     * 创建表头样式（黄色背景）
     * @param workbook
     * @return
     */
    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("宋体");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setLocked(true);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setWrapText(true);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        return style;
    }

    /**
     * 创建数据样式
     * @param workbook
     * @return
     */
    private CellStyle createDataStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("宋体");
        style.setFont(font);
//        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }
}
