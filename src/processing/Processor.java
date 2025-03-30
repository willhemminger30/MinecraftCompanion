package processing;

import processing.user.CoordinateEntry;
import processing.user.DataEntry;
import processing.user.UserData;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor {

    private static boolean teleport;

    /**
     * Accept user input and process accordingly
     * @param line
     * @param userName
     * @param fileName
     * @param user
     * @return
     */
    public static String process(String line, String userName, String fileName, UserData user) {
        //isolate the first word of the line.  This is the command
        String response;
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
                    case "export" -> { // exports to text file
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
                    case "export" -> { // exports to csv file
                        response = exportCoordinates(user, userName, fileName);
                    }
                    case "tp" -> { // teleports player to corresponding location
                        line = matcher.group(3).trim();
                        response = teleport(user, line);
                        if(response.contains("/tp")) {
                            teleport = true;
                        }
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

        return response;
    }

    /**
     * sends text data to user object
     * @param line
     * @param user
     * @return
     */
    private static String storeData(String line, UserData user) {
        if(user.addDataEntry(line)) {
            return "DATA SENT TO COMPANION";
        } else {
            return "Invalid Data Format";
        }
    }

    /**
     * retrieves text data from user object
     * @param line
     * @param user
     * @return
     */
    private static String getData(String line, UserData user) {
        return user.getDataEntry(line);
    }

    /**
     * list available user data entries
     * @param user
     * @return
     */
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

    /**
     * export data to text file
     * @param user
     * @param userName
     * @param sessionID
     * @return
     */
    private static String exportData(UserData user, String userName, String sessionID) {
        if(user.exportData(userName, sessionID)) {
            return "DATA EXPORT COMPLETE";
        } else {
            return "DATA EXPORT FAILED";
        }
    }

    /**
     * store coordinate data in user object
     * @param line
     * @param user
     * @return
     */
    private static String storeCoordinate(String line, UserData user) {
        if(user.addCoordinateEntry(line)) {
            return "COORDINATE SENT TO COMPANION";
        } else {
            return "Invalid Data Format";
        }
    }

    /**
     * retrieve coordinate from user object
     * @param line
     * @param user
     * @return
     */
    private static String getCoordinate(String line, UserData user) {
        return user.getCoordinateEntry(line);
    }

    /**
     * retrieve list of coordinate IDs from user object
     * @param user
     * @return
     */
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

    /**
     * export coordinates to CSV file
     * @param user
     * @param userName
     * @param sessionID
     * @return
     */
    private static String exportCoordinates(UserData user, String userName, String sessionID) {
        if(user.exportCoordinates(userName, sessionID)) {
            return "DATA EXPORT COMPLETE";
        } else {
            return "DATA EXPORT FAILED";
        }
    }

    /**
     * teleport player to specified coordinate
     * @param user
     * @param line
     * @return
     */
    private static String teleport(UserData user, String line) {
        return user.teleportUser(line);
    }

    /**
     * Method to scan log file for commands and process them accordingly
     * @param user
     * @param userName
     * @param logFile
     * @param companionPrefix
     * @param sessionID
     * @throws AWTException
     * @throws InterruptedException
     */
    public static void scan(UserData user, String userName, File logFile, String companionPrefix, String sessionID) throws AWTException, InterruptedException {
        long length, prevLength = 0L;
        boolean continueScanning = true;
        Robot robot = new Robot();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection;
        String command;
        boolean playerLeftGame = false;
        String acknowledgement = "/w " + userName + " ";
        String response;

        if(logFile.exists()) {
            while(continueScanning) {
                length = logFile.length();
                if(length > prevLength) {
                    prevLength = length;
                    command = parseLogFile(logFile, companionPrefix, userName);

                    if(!command.isEmpty()) {

                        if(command.equals("EXIT")) {
                            continueScanning = false;
                            playerLeftGame = true;
                        }

                        if(command.contains("EXIT COMPANION")) {
                            continueScanning = false;
                        }

                        // process user's input
                        System.out.println(command);
                        command = command.substring(command.indexOf(companionPrefix) + companionPrefix.length()).trim();

                        // send acknowledgement back to user
                        if(continueScanning) {
                            // acknowledgement will contain result of processor
                            response = Processor.process(command, userName, sessionID, user);

                            if(teleport) {
                                teleport = false;
                                stringSelection = new StringSelection(response);
                                sendToKeyboard(clipboard, robot, stringSelection);
                                stringSelection = new StringSelection(acknowledgement + "@COMPANION: TELEPORT COMPLETE");
                            } else {
                                stringSelection = new StringSelection(acknowledgement + "@COMPANION: " + response);
                            }

                        } else {
                            stringSelection = new StringSelection(acknowledgement + "@COMPANION: COMPANION SHUTDOWN");
                        }

                        // if the player has left the game, do not paste response using keyboard
                        if(!playerLeftGame) {
                            sendToKeyboard(clipboard, robot, stringSelection);
                        }
                    }
                }
            }

        } else {
            System.err.println("Log file not found.");
        }
    }

    /**
     * method to parse log file from bottom up
     * @param logFile
     * @param companionPrefix
     * @param userName
     * @return
     */
    private static String parseLogFile(File logFile, String companionPrefix, String userName) {
        String line;
        String whisperFormat = "[Render thread/INFO]: [CHAT] " + userName + " whispers to you: ";
        String commandFormat = whisperFormat + companionPrefix;
        String responseFormat = whisperFormat + "@COMPANION";
        String leftGame = "[Server thread/INFO]: " + userName + " left the game";
        String joinedGame = "[Server thread/INFO]: " + userName + " joined the game";
        String joinedServer = "[Render thread/INFO]: Connecting to";

        byte b;

        try(RandomAccessFile file = new RandomAccessFile(logFile, "r")) {
            for(long i = file.length() - 2; i > -1; i--) {
                file.seek(i); // start seeking at end of file
                b = (byte) file.read();

                //when encountering a newline character, read the entire line
                if(b == 10) {
                    line = file.readLine();
                    if(line.contains(responseFormat) || line.contains(joinedGame) || line.contains(joinedServer)) {
                        return "";
                    }
                    if (line.contains(commandFormat)) {
                        return(line);
                    }
                    if(line.contains(leftGame)) {
                        return "EXIT"; // will shut down companion application in this scenario
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static void sendToKeyboard(Clipboard clipboard, Robot robot, StringSelection stringSelection) throws InterruptedException {
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

        try {
            Thread.sleep(500); // wait to allow file to be finalized after changes are made
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
