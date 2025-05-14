package com.github.dc.utils;

/**
 * <p>
 *     将字节数自动转为易读的KB/MB/GB/TB/PB格式，并保留两位小数
 * </p>
 *
 * @author wangpeiyuan
 * @date 2025/5/14 15:16
 */
public class ByteFormatter {

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
    private static final int MAX_EXPONENT = UNITS.length - 1;

    public static String formatBytes(long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("字节数不能为负数");
        if (bytes == 0) return "0 B";

        int exponent = (int) (Math.log10(bytes) / Math.log10(1024));
        exponent = Math.min(exponent, MAX_EXPONENT);

        double value = (double) bytes / Math.pow(1024, exponent);
        String formattedValue = String.format("%.2f", value).replaceAll("\\.?0+$", "");

        return formattedValue + " " + UNITS[exponent];
    }

    // 测试用例
    public static void main(String[] args) {
        System.out.println(formatBytes(0));          // 0 B
        System.out.println(formatBytes(1023));        // 1023 B
        System.out.println(formatBytes(1024));        // 1 KB
        System.out.println(formatBytes(1500));        // 1.46 KB
        System.out.println(formatBytes(1048576));     // 1 MB
        System.out.println(formatBytes(5_368_709_120L)); // 5 GB
        System.out.println(formatBytes(Long.MAX_VALUE)); // 8.99 EB
    }
}
