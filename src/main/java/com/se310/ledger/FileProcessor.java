package com.se310.ledger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * FileProcessor class to handle commands from a file
 *
 * @author  Joshua Vaysman
 * @version 1.0
 */

public class FileProcessor extends CommandProcessor {
    /**
     * Process File from the command line
     */
    public void processCommandFile(String fileName){

        // Does this do anything??
        List<String> tokens = new ArrayList<>();

        AtomicInteger atomicInteger = new AtomicInteger(0);

        //Process all the lines in the file
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream
                    .forEach(line -> {
                        try {
                            atomicInteger.getAndIncrement();
                            if(!line.trim().startsWith("#") && !line.trim().isEmpty()) {
                                processCommand(line);
                            }
                        } catch (CommandProcessorException e) {
                            e.setLineNumber(atomicInteger.get());
                            System.out.println("Failed due to: " + e.getReason() + " for Command: " + e.getCommand()
                                    + " On Line Number: " + e.getLineNumber());
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
