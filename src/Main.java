import processing.Processor;
import processing.user.UserData;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        File propertiesFile = new File("properties.txt");
        String userName = "", logFilePath = "", sessionID = "", line = "", companionPrefix = "";
        String [] properties;
        UserData user;
        File logFile;

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
                    case "sessionID" -> {
                        sessionID = properties[1];
                        System.out.println("Session ID set.");
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

        if(propertiesCheck(userName, logFilePath, sessionID, companionPrefix)) {
            System.exit(1);
        }

        // read user from file (if the file exists)
        user = new UserData(sessionID);

        //find Minecraft log file
        logFile = new File(logFilePath);

        //create session directory if one does not exist
        File sessionDirectory = new File(sessionID);

        if(sessionDirectory.exists() || sessionDirectory.mkdir()) {
            try {
                Processor.scan(user, userName, logFile, line, companionPrefix, sessionID);
            } catch (AWTException | InterruptedException e) {
                e.printStackTrace();
            }

            // save updated user data
            user.saveToFile(sessionID);
        }
    }

    private static boolean propertiesCheck(String userName, String logFilePath, String sessionID, String companionPrefix) {
        boolean propertiesError = false;

        if(userName.isEmpty()) {
            System.err.println("Username not set!");
            propertiesError = true;
        }
        if(logFilePath.isEmpty()) {
            System.err.println("Log file not set!");
            propertiesError = true;
        }
        if(sessionID.isEmpty()) {
            System.err.println("Session ID is not set!");
            propertiesError = true;
        }
        if(companionPrefix.isEmpty()) {
            System.err.println("Companion activation prefix not set!");
            propertiesError = true;
        }

        return propertiesError;
    }
}