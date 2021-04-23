package wiki.blaze.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @Author wangcy
 * @Date 2021/4/22 14:48
 */
public class StringUtils {

    public static boolean isBlank(String str) {
        return str == null || "".equals(str);
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String concat(Object... arr) {
        if(arr == null || arr.length == 0) {
            return "NULL";
        }
        return Arrays.stream(arr).map(o -> {
            if(o == null) {
                return "NULL";
            }else {
                return String.valueOf(o);
            }
        }).collect(Collectors.joining());
    }

    public static String currentTimestamp() {
        DateFormat df = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
        return df.format(new Date());
    }

}
