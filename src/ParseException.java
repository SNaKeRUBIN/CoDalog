package src;

public class ParseException extends Exception {

    public ParseException(String message) {
        LoggerClass.logger(message);
    }

    public ParseException(Exception ex) {
        LoggerClass.logger(ex.getMessage());
    }
}
