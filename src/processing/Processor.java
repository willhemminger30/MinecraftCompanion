package processing;

import processing.user.CoordinateEntry;
import processing.user.DataEntry;
import processing.user.UserData;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor {
    public static String process(String line, String userName, String fileName, UserData user) {
        //isolate the first word of the line.  This is the command
        String response = "";
        String command;
        String subCommand;
        Pattern pattern = Pattern.compile("(\\S+) (\\S+)($| .+)");
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
                        line = matcher.group(3).trim();
                        response = storeData(line, user);
                    }
                    case "get" -> {
                        line = matcher.group(3).trim();
                        response = getData(line, user);
                    }
                    case "list" -> {
                        response = listData(user);
                    }
                    case "export" -> {
                        response = exportData(user, userName, fileName);
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
                        line = matcher.group(3).trim();
                        response = storeCoordinate(line, user);
                    }
                    case "get" -> {
                        line = matcher.group(3).trim();
                        response = getCoordinate(line, user);
                    }
                    case "list" -> {
                        response = listCoordinates(user);
                    }
                    case "export" -> {
                        response = exportCoordinates(user, userName, fileName);
                    }
                    default -> {
                        response = "Invalid coordinate command";
                    }
                }
            }
            default ->
                response = "Unknown Command";
        }

        if(response.trim().isEmpty()) {
            response = "NO DATA";
        }

        return "@COMPANION: " + response;
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

    private static String listData(UserData user) {
        String categories = "";
        ArrayList<DataEntry> dataEntries = user.getDataEntries();
        int size = dataEntries.size();
        int sizeMinusOne = size - 1;

        for(int i = 0; i < size; i++) {
            categories += dataEntries.get(i).getCategory() + (i < sizeMinusOne ? ", " : "");
        }

        return categories;
    }

    private static String exportData(UserData user, String userName, String sessionID) {
        if(user.exportData(userName, sessionID)) {
            return "DATA EXPORT COMPLETE";
        } else {
            return "DATA EXPORT FAILED";
        }
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

    private static String listCoordinates(UserData user) {
        String ids = "";
        ArrayList<CoordinateEntry> coordinateEntries = user.getCoordinateEntries();

        int size = coordinateEntries.size();
        int sizeMinusOne = size - 1;

        for(int i = 0; i < size; i++) {
            ids += coordinateEntries.get(i).getId() + (i < sizeMinusOne ? ", " : "");
        }

        return ids;
    }

    private static String exportCoordinates(UserData user, String userName, String sessionID) {
        if(user.exportCoordinates(userName, sessionID)) {
            return "DATA EXPORT COMPLETE";
        } else {
            return "DATA EXPORT FAILED";
        }
    }

    public static void scan(UserData user, String userName, File logFile, String line, String companionPrefix, String sessionID) throws AWTException, InterruptedException {
        long length = 0L, prevNumLines = 0L, currentNumLines = 0L;
        boolean continueScanning = true;
        Robot robot = new Robot();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection;
        String lastCommand;
        String whisperFormat = "[Render thread/INFO]: [CHAT] " + userName + " whispers to you: ";
        String commandFormat = whisperFormat + companionPrefix;
        String responseFormat = whisperFormat + "@COMPANION";
        String acknowledgement = "/w " + userName + " ";

        while(continueScanning) {
            lastCommand = "";
            if(logFile.exists()) {
                if(logFile.length() != length) {
                    //if the log logFile length has changed
                    length = logFile.length();

                    try(Scanner reader = new Scanner(logFile)) {
                        while(reader.hasNext()) {
                            //find commands in log file
                            currentNumLines++;
                            line = reader.nextLine();
                            if (line.contains(commandFormat)) {
                                lastCommand = line;
                            }

                            if(line.contains(responseFormat)) {
                                lastCommand = "";
                            }
                        }


                        //check that log file lines have increased
                        if(currentNumLines > prevNumLines && !lastCommand.isEmpty()) {
                            if(lastCommand.contains("EXIT COMPANION")) {
                                continueScanning = false;
                            }

                            prevNumLines = currentNumLines;
                            currentNumLines = 0;

                            // process user's input
                            lastCommand = lastCommand.substring(lastCommand.indexOf(companionPrefix) + companionPrefix.length()).trim();
                            System.out.println(lastCommand);

                            // send acknowledgement back to user
                            if(continueScanning) {
                                // acknowledgement will contain result of processor
                                stringSelection = new StringSelection(acknowledgement + Processor.process(lastCommand, userName, sessionID, user));
                            } else {
                                stringSelection = new StringSelection(acknowledgement + "COMPANION SHUTDOWN");
                            }

                            // copy and paste acknowledgement into user's chat
                            clipboard.setContents(stringSelection, null);

                            Thread.sleep(100);
                            robot.keyPress(KeyEvent.VK_T);
                            robot.keyRelease(KeyEvent.VK_T);
                            Thread.sleep(100);

                            robot.keyPress(KeyEvent.VK_CONTROL);
                            robot.keyPress(KeyEvent.VK_V);

                            Thread.sleep(100);

                            robot.keyRelease(KeyEvent.VK_V);
                            robot.keyRelease(KeyEvent.VK_CONTROL);

                            Thread.sleep(100);
                            robot.keyPress(KeyEvent.VK_ENTER);
                            robot.keyRelease(KeyEvent.VK_ENTER);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.err.println("Log file not found.");
                continueScanning = false;
            }
        }
    }
}
