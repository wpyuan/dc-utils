package com.github.dc.utils.pojo;

//import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>
 *     导入结果
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/10/12 8:35
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult<T> {
    /**
     * sheet位置
     */
    private Integer sheetIndex;
    /**
     * 行号
     */
    private Integer index;
    /**
     * 是否成功
     */
    private Boolean success;
    /**
     * 产生结果时间
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss:SSS")
    private LocalDateTime timestamp;
    /**
     * 内容
     */
    private String content;
    /**
     * 该行数据
     */
    private T row;
}
