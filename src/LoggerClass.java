package src;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerClass {

    private static final String FILE_PATH = "D:\\kbase\\LogFile.log";

    public static void logger(String text) {

        FileHandler fh = null;
        Logger logger = Logger.getLogger("MyLog");

        try {
            fh = new FileHandler(FILE_PATH, true);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        logger.info(text);
        logger.removeHandler(fh);
        fh.close();
    }
}
