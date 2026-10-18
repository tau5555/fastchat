package com.mycompany.fastchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MessageTest.java
 *
 * Unit tests for the Message class, using the test data supplied in the
 * PROG5121 POE Part 2 brief.
 *
 * @author Andile
 */
public class MessageTest {

    // Test data for message 1 (sent) and message 2 (disregarded).
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    private static final String FIXED_ID_1 = "0012345678";
    private static final String FIXED_ID_2 = "0012345679";

    @BeforeEach
    public void clearSentMessages() {
        Message.clearSentMessages();
    }

    // ---- Message length ----

    @Test
    public void testMessageLengthSuccess() {
        assertTrue(Message.isMessageLengthValid(TEXT_1));
        assertEquals("Message ready to send.", Message.checkMessageLength(TEXT_1));
    }

    @Test
    public void testMessageLengthFailure() {
        String tooLong = "a".repeat(260);
        assertFalse(Message.isMessageLengthValid(tooLong));
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                Message.checkMessageLength(tooLong));
    }

    @Test
    public void testMessageLengthBoundary() {
        assertEquals("Message ready to send.", Message.checkMessageLength("a".repeat(250)));
        assertEquals("Message exceeds 250 characters by 1; please reduce the size.",
                Message.checkMessageLength("a".repeat(251)));
    }

    // ---- Recipient cell number ----

    @Test
    public void testRecipientCorrectlyFormatted() {
        Message message = new Message(FIXED_ID_1, 0, RECIPIENT_1, TEXT_1);
        assertEquals("Cell phone number successfully captured.", message.checkRecipientCell());
    }

    @Test
    public void testRecipientIncorrectlyFormatted() {
        Message message = new Message(FIXED_ID_2, 1, RECIPIENT_2, TEXT_2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.",
                message.checkRecipientCell());
    }

    // ---- Message hash ----

    @Test
    public void testMessageHashForTestMessage1() {
        Message message = new Message(FIXED_ID_1, 0, RECIPIENT_1, TEXT_1);
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
        assertEquals("00:0:HITONIGHT", message.getMessageHash());
    }

    @Test
    public void testMessageHashesInLoop() {
        String[] ids = {FIXED_ID_1, FIXED_ID_2};
        String[] recipients = {RECIPIENT_1, RECIPIENT_2};
        String[] texts = {TEXT_1, TEXT_2};
        String[] expectedHashes = {"00:0:HITONIGHT", "00:1:HIPAYMENT"};

        for (int i = 0; i < texts.length; i++) {
            Message message = new Message(ids[i], i, recipients[i], texts[i]);
            assertEquals(expectedHashes[i], message.createMessageHash());
        }
    }

    // ---- Message ID ----

    @Test
    public void testMessageIdCreated() {
        Message message = new Message(0, RECIPIENT_1, TEXT_1);
        String id = message.getMessageId();

        assertTrue(message.checkMessageID());
        assertEquals(10, id.length());
        assertTrue(id.matches("\\d{10}"));
        assertEquals("Message ID generated: " + id, message.messageIdMessage());
    }

    @Test
    public void testMessageIdTooLong() {
        Message message = new Message("12345678901", 0, RECIPIENT_1, TEXT_1);
        assertFalse(message.checkMessageID());
    }

    // ---- Send, disregard or store ----

    @Test
    public void testSendMessageOption() {
        Message message = new Message(FIXED_ID_1, 0, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully sent.", message.sentMessage(Message.OPTION_SEND));
    }

    @Test
    public void testDisregardMessageOption() {
        Message message = new Message(FIXED_ID_2, 1, RECIPIENT_2, TEXT_2);
        assertEquals("Press 0 to delete the message.", message.sentMessage(Message.OPTION_DISREGARD));
    }

    @Test
    public void testStoreMessageOption() {
        Message message = new Message(FIXED_ID_1, 0, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully stored.", message.sentMessage(Message.OPTION_STORE));
    }

    @Test
    public void testInvalidOption() {
        Message message = new Message(FIXED_ID_1, 0, RECIPIENT_1, TEXT_1);
        assertEquals("Invalid option; please choose 1, 2 or 3.", message.sentMessage(9));
    }

    // ---- Sent messages and totals ----

    @Test
    public void testTotalMessagesSent() {
        // Message 1 is sent, message 2 is disregarded, so only one is counted.
        Message.recordSent(new Message(FIXED_ID_1, 0, RECIPIENT_1, TEXT_1));
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessages() {
        Message.recordSent(new Message(FIXED_ID_1, 0, RECIPIENT_1, TEXT_1));

        String newLine = System.lineSeparator();
        String expected = "Message ID: 0012345678" + newLine
                + "Message Hash: 00:0:HITONIGHT" + newLine
                + "Recipient: +27718693002" + newLine
                + "Message: Hi Mike, can you join us for dinner tonight?" + newLine + newLine;
        assertEquals(expected, Message.printMessages());
    }

    // ---- JSON storage ----

    @Test
    public void testStoreMessageWritesJsonFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("messages.json");

        new Message(FIXED_ID_1, 0, RECIPIENT_1, TEXT_1).storeMessage(file);
        new Message(FIXED_ID_2, 1, RECIPIENT_2, TEXT_2).storeMessage(file);

        String json = Files.readString(file);
        assertTrue(json.contains("00:0:HITONIGHT"));
        assertTrue(json.contains("00:1:HIPAYMENT"));
    }
}
