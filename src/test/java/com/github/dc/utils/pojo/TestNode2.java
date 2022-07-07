package com.github.dc.utils.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/7/7 11:21
 */
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TestNode2 {
    private Integer xzqId;
    private Integer xzqParentId;
    private String name;
    private List<TestNode2> xzqChildren;
}
