package com.github.dc.utils;

import com.github.dc.utils.pojo.ImportResult;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

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
        File file = new File("D:/big.xlsx");
        List<ImportResult<List<String>>> result = new ArrayList<>();
        try (InputStream is = new FileInputStream(file);) {
            SAXParserExcelUtils.read(is, (sheetIndexAndRowIndex, row) -> {
                log.info("位置：{}, 行：{}", sheetIndexAndRowIndex, row);
                ImportResult<List<String>> rowResult = ImportResult.<List<String>>builder()
                        .sheetIndex(sheetIndexAndRowIndex.getFirst())
                        .index(sheetIndexAndRowIndex.getSecond())
                        .success(false)
                        .timestamp(LocalDateTime.now())
                        .row(row)
                        .build();
                result.add(rowResult);
            });
        }  catch (Exception e) {
            log.error("excel读取异常", e);
            ImportResult<List<String>> exceptionResult = ImportResult.<List<String>>builder()
                    .success(false)
                    .timestamp(LocalDateTime.now())
                    //.content(StringUtils.join(ArrayUtils.insert(0, ArrayUtils.toStringArray(e.getStackTrace()), new String[]{e.getMessage()}), " "))
                    .content(e.getMessage())
                    .build();
            result.add(exceptionResult);
        }

        System.out.println(result);
    }
}
