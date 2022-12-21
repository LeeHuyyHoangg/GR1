package localsearch.applications.idpcndu_network.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Utils {
    public static List<String> fileToStringArray(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    public static List<Integer> stringToIntList(String string) {
        List<Integer> result = new ArrayList<>();
        Scanner s = new Scanner(string);
        while (s.hasNextInt()) {
            result.add(s.nextInt());
        }
        s.close();
        return result;
    }

    public static <T> T getRandomListItem(List<T> items, int randomSeed) {
        return items.get(new Random(randomSeed).nextInt(items.size()));
    }

    public static void appendLineToFile(String line, String fileName) throws IOException {
        Writer output;
        File file = new File(System.getProperty("user.dir") + "\\" + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        output = new BufferedWriter(new FileWriter(fileName, true));
        output.append(line + "\n");
        output.close();
    }

    public static double getAverageInt(Collection<Integer> collection) {
        double sum = 0;
        for (int i : collection) {
            sum += i;
        }
        return sum / collection.size();
    }

    public static double getAverageLong(Collection<Long> collection) {
        double sum = 0;
        for (long i : collection) {
            sum += i;
        }
        return sum / collection.size();
    }

    public static double getStandardDeviationInt(Collection<Integer> collection) {
        double average = getAverageInt(collection);
        double sum = 0;
        for (int i : collection) {
            sum += (average - i) * (average - i);
        }
        sum /= collection.size();

        return Math.sqrt(sum);
    }

    public static double getStandardDeviationLong(Collection<Long> collection) {
        double average = getAverageLong(collection);
        double sum = 0;
        for (long i : collection) {
            sum += (average - i) * (average - i);
        }
        sum /= collection.size();

        return Math.sqrt(sum);
    }

}
