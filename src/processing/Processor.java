package processing;

import processing.user.UserData;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor {
    public static String process(String line, String userName, String fileName, UserData user) {
        //isolate the first word of the line.  This is the command
        String response = "";
        String command;
        String subCommand;
        Pattern pattern = Pattern.compile("(\\S+) (\\S+) (.+)");
        Matcher matcher = pattern.matcher(line);

        if(!matcher.find()) {
            return "Invalid Data Format";
        }

        command = matcher.group(1);
        subCommand = matcher.group(2);

        switch(command) {
            // manipulating text data
            case "data" -> {

                switch (subCommand) {
                    case "store" -> {
                        line = matcher.group(3);
                        response = storeData(line, user);
                    }
                    case "get" -> {
                        line = matcher.group(3);
                        response = getData(line, user);
                    }
                    default -> {
                        response = "Invalid data command";
                    }
                }
            }
            // manipulating coordinate data
            case "coord" -> {

                switch(subCommand) {
                    case "store" -> {
                        line = matcher.group(3);
                        response = storeCoordinate(line, user);
                    }
                    case "get" -> {
                        line = matcher.group(3);
                        response = getCoordinate(line, user);
                    }
                    default -> {
                        response = "Invalid coordinate command";
                    }
                }
            }
            default ->
                response = "Unknown Command";
        }
        return response;
    }

    private static String storeData(String line, UserData user) {
        if(user.addDataEntry(line)) {
            return "DATA SENT TO COMPANION";
        } else {
            return "Invalid Data Format";
        }
    }

    private static String getData(String line, UserData user) {
        return user.getDataEntry(line);
    }

    private static String storeCoordinate(String line, UserData user) {
        if(user.addCoordinateEntry(line)) {
            return "COORDINATE SENT TO COMPANION";
        } else {
            return "Invalid Data Format";
        }
    }

    private static String getCoordinate(String line, UserData user) {
        return user.getCoordinateEntry(line);
    }
}
