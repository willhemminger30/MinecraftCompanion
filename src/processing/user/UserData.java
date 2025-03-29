package processing.user;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserData {
    private ArrayList<DataEntry> dataEntries;
    private ArrayList<CoordinateEntry> coordinateEntries;

    public UserData(String dataFile) {
        dataEntries = new ArrayList<DataEntry>();
        coordinateEntries = new ArrayList<CoordinateEntry>();
        String line;

        File file = new File(dataFile);

        if(file.exists()) {
            // read data from file
            try(Scanner reader = new Scanner(file)) {
                while(reader.hasNext()) {
                    line = reader.nextLine();

                    // when the line references text data
                    if(line.matches("data.*")) {
                        addDataEntry(line.substring(line.indexOf("data") + 5));
                    }

                    if(line.matches("coord.*")) {
                        addCoordinateEntry(line.substring(line.indexOf("coord") + 6));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setDataEntries(ArrayList<DataEntry> dataEntries) {
        this.dataEntries = dataEntries;
    }

    private void setCoordinateEntries(ArrayList<CoordinateEntry> coordinateEntries) {
        this.coordinateEntries = coordinateEntries;
    }

    public ArrayList<DataEntry> getDataEntries() {
        return dataEntries;
    }

    public ArrayList<CoordinateEntry> getCoordinateEntries() {
        return coordinateEntries;
    }

    public void saveToFile(String dataFile) {
        try(FileWriter writer = new FileWriter(dataFile, false)) {
            for(DataEntry data : dataEntries) {
                writer.write("data " + data + "\n");
            }

            for(CoordinateEntry coord : coordinateEntries) {
                writer.write("coord " + coord + "\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public boolean addDataEntry(String line) {
        Pattern pattern = Pattern.compile("(\\S+) (.*)");
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()) {
            String category = matcher.group(1);
            String payload = matcher.group(2);
            String existingPayload;
            DataEntry existingEntry;

            // check if data with same category already exists
            Optional<DataEntry> existingOptionalEntry = dataEntries.stream().filter(dataEntry -> dataEntry.getCategory().equals(category)).findFirst();

            // append payload if this is the case
            if(existingOptionalEntry.isPresent()) {
                existingEntry = existingOptionalEntry.get();
                existingPayload = existingEntry.getPayload();
                existingEntry.setPayload(existingPayload + ";" + payload);
            } else {
                dataEntries.add(new DataEntry(category, payload));
            }
        }

    return matcher.hasMatch();
    }

    public boolean addCoordinateEntry(String line) {
        Pattern pattern = Pattern.compile("(\\S+) ([\\d.]+),([\\d.]+),([\\d.]+)");
        Matcher matcher = pattern.matcher(line);

        if(matcher.find()) {
            coordinateEntries.add(new CoordinateEntry(matcher.group(1), Double.parseDouble(matcher.group(2)),
                    Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4))));
        }

        return matcher.hasMatch();
    }

    public String getDataEntry(String category) {
        DataEntry matchedEntry;
        String[] tokens;
        String payload;
        Optional<DataEntry> matchingEntry = dataEntries.stream().filter(dataEntry -> dataEntry.getCategory()
                .equals(category)).findFirst();

        if(matchingEntry.isPresent()) {
            payload = "";
            matchedEntry = matchingEntry.get();
            tokens = matchedEntry.getPayload().split(";");

            for(int i = 0; i < tokens.length; i++) {
                payload += tokens[i] + (i < (tokens.length - 1) ? " -- " : "");
            }

            return payload;
        } else {
            return "ENTRY NOT FOUND";
        }
    }

    public String getCoordinateEntry(String id) {
        Optional<CoordinateEntry> matchingEntry = coordinateEntries.stream().filter(coordinateEntry -> coordinateEntry
                .getId().equals(id)).findFirst();

        if(matchingEntry.isPresent()) {
            return matchingEntry.get().toString();
        } else {
            return "ENTRY NOT FOUND";
        }
    }

}
