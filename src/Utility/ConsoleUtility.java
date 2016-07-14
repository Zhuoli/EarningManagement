package src.Utility;

/**
 * Created by zhuoli on 7/13/16.
 */
public class ConsoleUtility {

    public static void WriteLine(String... args) {
        StringBuilder sb = new StringBuilder();
        for (String str : args) {
            sb.append(str + "\t");
        }
        System.out.println(sb.toString());
    }
}
