package com.github.dc.utils;

import com.github.dc.utils.pojo.ExcelWriteSetup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class SAXWriteExcelTest {

    public static void main(String[] args) throws IOException {
        List<Map<String, Object>> page1 = new ArrayList<>();
        Map<String, Object> student = new HashMap<>();
        student.put("name", "张三");
        student.put("age", 18);
        student.put("bornDate", new Date());
        student.put("score", BigDecimal.valueOf(72.5));
        student.put("remark", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        student.put("age1", 18);
        student.put("bornDate1", new Date());
        student.put("score1", BigDecimal.valueOf(72.5));
        student.put("remark1", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        student.put("age2", 18);
        student.put("bornDate2", new Date());
        student.put("score2", BigDecimal.valueOf(72.5));
        student.put("remark2", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        page1.add(student);

        student = new HashMap<>();
        student.put("name", "李四");
        student.put("age", 19);
        student.put("bornDate", new Date());
        student.put("score", BigDecimal.valueOf(82.5));
        student.put("remark", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        student.put("age1", 19);
        student.put("bornDate1", new Date());
        student.put("score1", BigDecimal.valueOf(82.5));
        student.put("remark1", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        student.put("age2", 19);
        student.put("bornDate2", new Date());
        student.put("score2", BigDecimal.valueOf(82.5));
        student.put("remark2", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        page1.add(student);

        List<Map<String, Object>> page2 = new ArrayList<Map<String, Object>>();
        student = new HashMap<>();
        student.put("name", "王五");
        student.put("age", 18);
        student.put("bornDate", new Date());
        student.put("score", BigDecimal.valueOf(72.5));
        student.put("remark", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        student.put("age1", 18);
        student.put("bornDate1", new Date());
        student.put("score1", BigDecimal.valueOf(72.5));
        student.put("remark1", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        student.put("age2", 18);
        student.put("bornDate2", new Date());
        student.put("score2", BigDecimal.valueOf(72.5));
        student.put("remark2", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        page2.add(student);

        student = new HashMap<>();
        student.put("name", "赵六");
        student.put("age", 19);
        student.put("bornDate", new Date());
        student.put("score", BigDecimal.valueOf(82.5));
        student.put("remark", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        student.put("age1", 19);
        student.put("bornDate1", new Date());
        student.put("score1", BigDecimal.valueOf(82.5));
        student.put("remark1", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        student.put("age2", 19);
        student.put("bornDate2", new Date());
        student.put("score2", BigDecimal.valueOf(82.5));
        student.put("remark2", "6. 注意事项\n" +
                "避免样式滥用：频繁设置单元格样式会导致内存泄漏。\n" +
                "关闭资源：使用try-with-resources确保流正确关闭。\n" +
                "监控内存：通过jvisualvm或Arthas监控堆内存使用情况。");
        page2.add(student);

        for (int i = 0; i < 11; i++) {
            page1.addAll(page1);
        }
        for (int i = 0; i < 11; i++) {
            page2.addAll(page2);
        }

        page1.addAll(page2);

        int pageSize = 1000;
        try (FileOutputStream fos = new FileOutputStream(new File("F:/test-sax-write-excel.xlsx"))) {
            SAXExcelWriter.start()
                    .sheetName("测试sheetName")
                    .setup(ExcelWriteSetup.builder()
//                            .batchWrite(true)
                            .headerCell(Arrays.asList(ExcelWriteSetup.HeaderCell.builder().orderSeq(0).text("姓名").varName("name").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(1).text("年龄").varName("age").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(2).text("出生日期").varName("bornDate").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(3).text("分数").varName("score").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(4).text("违纪记录明细列表").varName("remark").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(5).text("年龄1").varName("age1").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(6).text("出生日期1").varName("bornDate1").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(7).text("分数1").varName("score1").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(8).text("违纪记录明细列表1").varName("remark1").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(9).text("年龄2").varName("age2").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(10).text("出生日期2").varName("bornDate2").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(11).text("分数2").varName("score2").build(),
                                    ExcelWriteSetup.HeaderCell.builder().orderSeq(12).text("违纪记录明细列表2").varName("remark2").build()))
                            .build())
                    .out(fos)
                    .run(pageNum -> {
                        if (pageNum == -1) {
                            return page1;
                        }
                        if ((pageNum - 1) * pageSize > page1.size()) {
                            return null;
                        }
                        try {
                            return page1.subList((pageNum - 1) * pageSize, pageNum * pageSize);
                        } catch (Exception e) {
                            return page1.subList((pageNum - 1) * pageSize, page1.size() - 1);
                        }
                    });
        }
    }
}
