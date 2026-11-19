package com.mycompany.fastchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MessageStoreTest.java
 *
 * Unit tests for the MessageStore class, using the five test messages from
 * the PROG5121 POE Part 3 brief.
 *
 * @author Andile
 */
public class MessageStoreTest {

    // Test data for the five messages (message 4's ID is the number in the brief).
    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    private static final String RECIPIENT_2_AND_5 = "+27838884567";

    private MessageStore store;

    // Message 1 is sent, 2 is stored, 3 is disregarded, 4 is sent and 5 is stored.
    @BeforeEach
    public void fillStoreWithTestData() {
        store = new MessageStore();
        store.addSent(new Message("1234567890", 0, "+27834557896", TEXT_1));
        store.addStored(new Message("2345678901", 1, RECIPIENT_2_AND_5, TEXT_2));
        store.addDisregarded(new Message("3456789012", 2, "+27834484567", TEXT_3));
        store.addSent(new Message("0838884567", 3, "0838884567", TEXT_4));
        store.addStored(new Message("5678901234", 4, RECIPIENT_2_AND_5, TEXT_5));
    }

    // ---- Arrays correctly populated ----

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        assertArrayEquals(new String[]{"Did you get the cake?", "It is dinner time!"},
                store.sentMessageTexts());
    }

    @Test
    public void testDisregardedMessagesArrayCorrectlyPopulated() {
        assertArrayEquals(new String[]{"Yohoooo, I am at your gate."},
                store.disregardedMessageTexts());
    }

    @Test
    public void testStoredMessagesArrayCorrectlyPopulated() {
        assertArrayEquals(new String[]{TEXT_2, TEXT_5}, store.storedMessageTexts());
    }

    @Test
    public void testMessageHashArrayCorrectlyPopulated() {
        assertArrayEquals(new String[]{"12:0:DIDCAKE", "23:1:WHERETIME", "34:2:YOHOOOOGATE",
            "08:3:ITTIME", "56:4:OKYOU"}, store.getMessageHashes());
    }

    @Test
    public void testMessageIdArrayCorrectlyPopulated() {
        assertArrayEquals(new String[]{"1234567890", "2345678901", "3456789012",
            "0838884567", "5678901234"}, store.getMessageIds());
    }

    @Test
    public void testTotalMessagesSent() {
        assertEquals(2, store.returnTotalMessages());
    }

    // ---- Sender and recipient of stored messages ----

    @Test
    public void testSenderAndRecipientOfStoredMessages() {
        String newLine = System.lineSeparator();
        String expected = "Sender: kyl_1 | Recipient: +27838884567" + newLine
                + "Sender: kyl_1 | Recipient: +27838884567" + newLine;
        assertEquals(expected, store.storedSendersAndRecipients("kyl_1"));
    }

    // ---- Longest stored message ----

    @Test
    public void testLongestStoredMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                store.longestStoredMessage().getMessageText());
    }

    @Test
    public void testLongestStoredMessageWhenNoneStored() {
        assertNull(new MessageStore().longestStoredMessage());
    }

    // ---- Search for a message ID ----

    @Test
    public void testSearchForMessageId() {
        Message found = store.findById("0838884567");
        assertEquals("It is dinner time!", found.getMessageText());
        assertEquals("0838884567", found.getRecipient());
    }

    @Test
    public void testSearchForMessageIdNotFound() {
        assertNull(store.findById("0000000000"));
    }

    // ---- Search for a recipient ----

    @Test
    public void testSearchMessagesForRecipient() {
        assertArrayEquals(new String[]{TEXT_2, TEXT_5},
                store.messagesForRecipient(RECIPIENT_2_AND_5));
    }

    @Test
    public void testSearchMessagesForRecipientNotFound() {
        assertEquals(0, store.messagesForRecipient("+27800000000").length);
    }

    // ---- Delete using the message hash ----

    @Test
    public void testDeleteMessageUsingHash() {
        String result = store.deleteByHash("23:1:WHERETIME");

        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\""
                + " successfully deleted.", result);
        assertArrayEquals(new String[]{TEXT_5}, store.storedMessageTexts());
        assertArrayEquals(new String[]{"12:0:DIDCAKE", "34:2:YOHOOOOGATE", "08:3:ITTIME",
            "56:4:OKYOU"}, store.getMessageHashes());
        assertArrayEquals(new String[]{"1234567890", "3456789012", "0838884567", "5678901234"},
                store.getMessageIds());
    }

    @Test
    public void testDeleteMessageUsingUnknownHash() {
        assertEquals("No message found with that message hash.", store.deleteByHash("99:9:NOTHING"));
        assertEquals(2, store.storedMessageTexts().length);
    }

    // ---- Report ----

    @Test
    public void testReportShowsSentMessages() {
        String newLine = System.lineSeparator();
        String expectedSentSection = "=== Sent Messages ===" + newLine
                + "Message ID: 1234567890" + newLine
                + "Message Hash: 12:0:DIDCAKE" + newLine
                + "Recipient: +27834557896" + newLine
                + "Message: Did you get the cake?" + newLine + newLine
                + "Message ID: 0838884567" + newLine
                + "Message Hash: 08:3:ITTIME" + newLine
                + "Recipient: 0838884567" + newLine
                + "Message: It is dinner time!" + newLine + newLine;
        assertEquals(expectedSentSection, store.printMessages());
        assertTrue(store.report().startsWith(expectedSentSection));
    }

    @Test
    public void testReportShowsStoredMessages() {
        String newLine = System.lineSeparator();
        String expectedStoredSection = "=== Stored Messages ===" + newLine
                + "Message ID: 2345678901" + newLine
                + "Message Hash: 23:1:WHERETIME" + newLine
                + "Recipient: +27838884567" + newLine
                + "Message: " + TEXT_2 + newLine + newLine
                + "Message ID: 5678901234" + newLine
                + "Message Hash: 56:4:OKYOU" + newLine
                + "Recipient: +27838884567" + newLine
                + "Message: " + TEXT_5 + newLine + newLine;
        assertTrue(store.report().contains(expectedStoredSection));
    }

    // ---- Reading and saving the JSON file ----

    @Test
    public void testStoredMessagesReadFromJsonFileIntoArray(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("messages.json");
        new Message("2345678901", 1, RECIPIENT_2_AND_5, TEXT_2).storeMessage(file);
        new Message("5678901234", 4, RECIPIENT_2_AND_5, TEXT_5).storeMessage(file);

        MessageStore loaded = new MessageStore();
        loaded.loadStoredMessages(file);

        assertArrayEquals(new String[]{TEXT_2, TEXT_5}, loaded.storedMessageTexts());
        assertArrayEquals(new String[]{"23:1:WHERETIME", "56:4:OKYOU"}, loaded.getMessageHashes());
        assertArrayEquals(new String[]{"2345678901", "5678901234"}, loaded.getMessageIds());
    }

    @Test
    public void testNothingLoadedWhenJsonFileDoesNotExist(@TempDir Path tempDir) throws IOException {
        MessageStore loaded = new MessageStore();
        loaded.loadStoredMessages(tempDir.resolve("missing.json"));
        assertEquals(0, loaded.storedMessageTexts().length);
    }

    @Test
    public void testDeletedMessageIsGoneFromJsonFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("messages.json");
        store.saveStoredMessages(file);
        store.deleteByHash("23:1:WHERETIME");
        store.saveStoredMessages(file);

        MessageStore loaded = new MessageStore();
        loaded.loadStoredMessages(file);
        assertArrayEquals(new String[]{TEXT_5}, loaded.storedMessageTexts());
    }
}
