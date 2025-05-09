package com.github.dc.utils.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 写入excel设置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelWriteSetup {
    /**
     * 内存窗口大小（保留最近100行在内存）
     */
    @Builder.Default
    private Integer rowAccessWindowSize = 100;
    /**
     * 分批次导入
     */
    @Builder.Default
    private boolean batchWrite = false;
    /**
     * 表头设置
     */
    private List<HeaderCell> headerCell;

    /**
     * 头列设置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class HeaderCell {
        /**
         * 列顺序号
         */
        private Integer orderSeq;
        /**
         * 头列内容
         */
        private String text;
        /**
         * 绑定的变量名
         */
        private String varName;
    }
}
