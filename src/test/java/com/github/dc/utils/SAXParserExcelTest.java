package com.github.dc.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2023/2/6 16:55
 */
@Slf4j
public class SAXParserExcelTest {

    public static void main(String[] args) {
//        test1();
//        testDateColumn();
        testAutoDetectDate();
    }
    /**
     * 测试自动检测日期格式（默认开启）
     * 无需手动指定日期列，自动识别日期格式的单元格
     */
    public static void testAutoDetectDate() {
        File file = new File("D:\\repo\\dc-utils\\test.xlsx");
        try {
            SAXExcelParser.start()
                    .file(file)
                    // autoDetectDate 默认已开启，无需手动调用
                    .run((sheetIndex, rowIndex, row) -> {
                        log.info("第{}个sheet页第{}行, 行数据：{}", sheetIndex, rowIndex, row);
                    });
        } catch (Exception e) {
            log.error("excel读取异常", e);
        }
    }

    /**
     * 测试日期列格式化
     * 假设 Excel 第1列是日期时间，第3列是日期
     */
    public static void testDateColumn() {
        File file = new File("F:/test-date-excel.xlsx");
        try {
            SAXExcelParser.start()
                    .file(file)
                    .addDateColumn(1)                        // 第1列使用默认日期时间格式 yyyy-MM-dd HH:mm:ss
                    .addDateColumn(3, "yyyy-MM-dd")          // 第3列使用自定义日期格式
                    .run((sheetIndex, rowIndex, row) -> {
                        log.info("第{}个sheet页第{}行, 行数据：{}", sheetIndex, rowIndex, row);
                    });
        } catch (Exception e) {
            log.error("excel读取异常", e);
        }
    }

    public static void test1() {
        File file = new File("F:/test-sax-write-excel.xlsx");
        // 记录处理结果会占用大量内存，慎重使用
//        List<ReadResult<List<String>>> result = new ArrayList<>();
        try {
            SAXExcelParser.start().file(file).run((sheetIndex, rowIndex, row) -> {
//                ReadResult<List<String>> rowResult = ReadResult.<List<String>>builder()
//                        .sheetIndex(sheetIndexAndRowIndex.getFirst())
//                        .index(sheetIndexAndRowIndex.getSecond())
//                        .success(true)
//                        .build();
                try {
                    if (rowIndex == 1) {
                        log.info("第{}个sheet页第{}行, 行数据：{}", sheetIndex, rowIndex, row);
                    }
                } catch (Exception e) {
//                    rowResult.setSuccess(false);
//                    rowResult.setContent(e.getMessage());
                } finally {
//                    rowResult = rowResult.toBuilder()
//                            .timestamp(LocalDateTime.now())
//                            .row(row)
//                            .build();
//                    result.add(rowResult);
                }
            });
        }  catch (Exception e) {
            log.error("excel读取异常", e);
//            ReadResult<List<String>> exceptionResult = ReadResult.<List<String>>builder()
//                    .success(false)
//                    .timestamp(LocalDateTime.now())
//                    //.content(StringUtils.join(ArrayUtils.insert(0, ArrayUtils.toStringArray(e.getStackTrace()), new String[]{e.getMessage()}), " "))
//                    .content(e.getMessage())
//                    .build();
//            result.add(exceptionResult);
        } finally {
//            log.debug("总计处理{}行，成功{}行，失败{}行", result.size(), result.stream().filter(ReadResult::getSuccess).count(), result.size() - result.stream().filter(ReadResult::getSuccess).count());
            log.debug("当前内存已使用{}", ByteFormatter.formatBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        }
    }

}
