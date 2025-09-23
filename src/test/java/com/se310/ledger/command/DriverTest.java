package com.se310.ledger.command;

import com.se310.ledger.FileProcessor;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Test Driver Class for testing Blockchain
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 */
public class DriverTest {

    @Test
    public void testDriver() throws URISyntaxException {

        Path path = Path.of(Objects.requireNonNull(getClass().getResource("/ledger.script")).toURI());

        FileProcessor processor = new FileProcessor();
        processor.processCommandFile (path.toString());
    }
}
