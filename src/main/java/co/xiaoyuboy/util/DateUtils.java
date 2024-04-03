package co.xiaoyuboy.util;

/**
 * @Author: Smile
 * @Date: 2023-11-11 21:53
 * @Description:
 */
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String addDays(int days) {
        LocalDate currentDate = LocalDate.now();
        LocalDate newDate = currentDate.plusDays(days);
        return newDate.format(DATE_FORMATTER);
    }

    public static void main(String[] args) {
        int daysToAdd = 2; // 要添加的天数

        String result = addDays(daysToAdd);
        System.out.println("Result: " + result);
    }
}