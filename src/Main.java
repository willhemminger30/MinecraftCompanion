import processing.Processor;
import processing.user.UserData;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws AWTException, InterruptedException {
        File propertiesFile = new File("properties.txt");
        String userName = "", logFilePath = "", dataFilePath = "", acknowledgement, line = "", companionPrefix = "";
        long length = 0L, prevNumLines = 0L, currentNumLines = 0L;
        boolean continueScanning = true;
        String [] properties;
        Robot robot = new Robot();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection;
        UserData user;

        if(!propertiesFile.exists()) {
            System.err.println("Properties file not found.");
            System.exit(1);
        }

        // load user's settings
        try(Scanner reader = new Scanner(propertiesFile)) {
            while(reader.hasNext()) {
                line = reader.nextLine();
                properties = line.split("=");

                switch (properties[0]) {
                    case "userName" -> {
                        userName = properties[1];
                        System.out.println("Username set.");
                    }
                    case "logFilePath" -> {
                        logFilePath = properties[1];
                        System.out.println("Log filepath set.");
                    }
                    case "dataFilePath" -> {
                        dataFilePath = properties[1];
                        System.out.println("Data filepath set.");
                    }
                    case "companionPrefix" -> {
                        companionPrefix = properties[1];
                        System.out.println("Prefix set.");
                    }
                    default -> System.out.println("Unknown property " + properties[0] + " detected.");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(propertiesCheck(userName, logFilePath, dataFilePath, companionPrefix)) {
            System.exit(1);
        }

        // read user from file (if the file exists)
        user = new UserData(dataFilePath);

        File logFile = new File(logFilePath);

        acknowledgement = "/w " + userName + " ";

        while(continueScanning) {
            if(logFile.exists()) {
                if(logFile.length() != length) {
                    //if the log logFile length has changed
                    length = logFile.length();

                    try(Scanner reader = new Scanner(logFile)) {
                        while(reader.hasNext()) {
                            //find the last line in the log logFile
                            currentNumLines++;
                            line = reader.nextLine();
                        }

                        //check that log file lines have increased
                        if(currentNumLines > prevNumLines && line.contains("[Render thread/INFO]: [CHAT] " + userName + " whispers to you: " + companionPrefix)) {
                            if(line.contains("EXIT COMPANION")) {
                                continueScanning = false;
                            }

                            prevNumLines = currentNumLines;
                            currentNumLines = 0;

                            // process user's input
                            line = line.substring(line.indexOf(companionPrefix) + companionPrefix.length()).trim();
                            System.out.println(line);

                            // send acknowledgement back to user
                            if(continueScanning) {
                                // acknowledgement will contain result of processor
                                stringSelection = new StringSelection(acknowledgement + Processor.process(line, userName, dataFilePath, user));
                            } else {
                                stringSelection = new StringSelection(acknowledgement + "COMPANION SHUTDOWN");
                            }

                            // copy and paste acknowledgement into user's chat
                            clipboard.setContents(stringSelection, null);

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

        // save updated user data
        user.saveToFile(dataFilePath);
    }

    private static boolean propertiesCheck(String userName, String logFilePath, String dataFilePath, String companionPrefix) {
        boolean propertiesError = false;

        if(userName.isEmpty()) {
            System.err.println("Username not set!");
            propertiesError = true;
        }
        if(logFilePath.isEmpty()) {
            System.err.println("Log file not set!");
            propertiesError = true;
        }
        if(dataFilePath.isEmpty()) {
            System.err.println("Data file path is not set!");
            propertiesError = true;
        }
        if(companionPrefix.isEmpty()) {
            System.err.println("Companion activation prefix not set!");
            propertiesError = true;
        }

        return propertiesError;
    }
}