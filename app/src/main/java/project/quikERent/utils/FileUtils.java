package project.quikERent.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List readLines(File file, Object o) throws IOException {
        return readLines(file, null);
    }

    public List<PowerTool> readPowerToolFromFile() throws IOException {
        final List<PowerTool> powerTools = new ArrayList<>(100);
        final File[] list = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
        File file = null;
        for (int i = 0; i < list.length; ++i) {
            if (list[i].getName().contains("database.csv")) {
                file = list[i];
                break;
            }
        }
        List<String> lines = FileUtils.readLines(file, "UTF-8");
        for (String line : lines) {
            String[] data = line.split(";");
            powerTools.add(new PowerTool(data[0], data[1], data[2]));
        }
        return powerTools;
    }

    public class PowerTool {
        private final String powerToolName;
        private final String brand;
        private final String year;

        PowerTool(String powerToolName, String brand, String year) {
            this.powerToolName = powerToolName;
            this.brand = brand;
            this.year = year;
        }

        public String getPowerToolName() {
            return powerToolName;
        }

        public String getBrand() {
            return brand;
        }

        public String getYear() {
            return year;
        }
    }
}
