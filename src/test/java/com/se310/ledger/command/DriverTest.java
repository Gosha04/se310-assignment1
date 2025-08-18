package com.se310.ledger.command;

import com.se310.ledger.CommandProcessor;
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

        CommandProcessor processor = new CommandProcessor();
        processor.processCommandFile (path.toString());
    }
}
