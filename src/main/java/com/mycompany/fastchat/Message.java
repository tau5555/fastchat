package com.mycompany.fastchat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * Message.java
 *
 * Represents one chat message in FastChat (PROG5121 POE Parts 2 and 3). Each
 * message has a randomly generated ten-digit ID, a message number, a
 * recipient, the message text, and an auto-generated message hash.
 *
 * A message can also be stored in a JSON file to send later. The JSON reading
 * and writing (and its attribution) is in MessageStore.
 *
 * @author Andile
 */
public final class Message {

    // ---------------------------------------------------------------
    // Fixed values used across the class.
    // ---------------------------------------------------------------
    public static final int MAX_MESSAGE_LENGTH = 250;
    public static final int MESSAGE_ID_LENGTH = 10;

    // Options the user can choose once a message has been written.
    public static final int OPTION_SEND = 1;
    public static final int OPTION_DISREGARD = 2;
    public static final int OPTION_STORE = 3;

    // ---------------------------------------------------------------
    // Response messages. Public constants so the application and the
    // unit tests use exactly the same text.
    // ---------------------------------------------------------------
    public static final String RECIPIENT_SUCCESS = "Cell phone number successfully captured.";
    public static final String RECIPIENT_FAIL =
            "Cell phone number is incorrectly formatted or does not contain an "
            + "international code. Please correct the number and try again.";

    public static final String LENGTH_SUCCESS = "Message ready to send.";
    public static final String LENGTH_PROMPT = "Please enter a message of less than 250 characters.";

    public static final String SENT_SUCCESS = "Message successfully sent.";
    public static final String DISREGARD_PROMPT = "Press 0 to delete the message.";
    public static final String STORED_SUCCESS = "Message successfully stored.";
    public static final String INVALID_OPTION = "Invalid option; please choose 1, 2 or 3.";

    private static final String MESSAGE_ID_PREFIX = "Message ID generated: ";
    private static final String STORAGE_FILE = "messages.json";

    private static final Random RANDOM = new Random();

    private String messageId;
    private int messageNumber;
    private String recipient;
    private String messageText;
    private String messageHash;

    /** Used by Gson when it reads stored messages back from the JSON file. */
    private Message() {
    }

    /**
     * Creates a message with a known ID. Used by the unit tests so the
     * message hash can be checked against fixed test data.
     *
     * @param messageId     the ten-digit message ID
     * @param messageNumber the number of this message (starts at 0)
     * @param recipient     the recipient's cell number
     * @param messageText   the message text
     */
    public Message(String messageId, int messageNumber, String recipient, String messageText) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    /**
     * Creates a message with a randomly generated ten-digit ID.
     *
     * @param messageNumber the number of this message (starts at 0)
     * @param recipient     the recipient's cell number
     * @param messageText   the message text
     */
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageId(), messageNumber, recipient, messageText);
    }

    /**
     * Builds a random ten-digit ID one digit at a time. Kept as a String so
     * that IDs starting with 0 keep their leading zero.
     *
     * @return a ten-digit message ID
     */
    public static String generateMessageId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < MESSAGE_ID_LENGTH; i++) {
            id.append(RANDOM.nextInt(10));
        }
        return id.toString();
    }

    /**
     * Checks that the message ID is not more than ten characters long.
     *
     * @return true if the message ID is valid
     */
    public boolean checkMessageID() {
        return messageId != null && messageId.length() <= MESSAGE_ID_LENGTH;
    }

    /**
     * @return the "Message ID generated" message containing this message's ID
     */
    public String messageIdMessage() {
        return MESSAGE_ID_PREFIX + messageId;
    }

    /**
     * Checks a recipient's cell number using the same rule as registration
     * (international code followed by no more than ten characters).
     *
     * @param cellNumber the recipient's cell number
     * @return true if the number is correctly formatted
     */
    public static boolean isValidRecipient(String cellNumber) {
        return Login.isValidCellNumber(cellNumber);
    }

    /**
     * @param cellNumber the recipient's cell number
     * @return the recipient success or failure message for that number
     */
    public static String recipientMessage(String cellNumber) {
        return isValidRecipient(cellNumber) ? RECIPIENT_SUCCESS : RECIPIENT_FAIL;
    }

    /**
     * Checks this message's recipient cell number.
     *
     * @return the recipient success or failure message
     */
    public String checkRecipientCell() {
        return recipientMessage(recipient);
    }

    /**
     * @param text the message text
     * @return true if the text is no more than 250 characters long
     */
    public static boolean isMessageLengthValid(String text) {
        return lengthOf(text) <= MAX_MESSAGE_LENGTH;
    }

    /**
     * @param text the message text
     * @return "Message ready to send." or how many characters the text is over the limit
     */
    public static String checkMessageLength(String text) {
        if (isMessageLengthValid(text)) {
            return LENGTH_SUCCESS;
        }
        int excess = lengthOf(text) - MAX_MESSAGE_LENGTH;
        return "Message exceeds " + MAX_MESSAGE_LENGTH + " characters by " + excess
                + "; please reduce the size.";
    }

    private static int lengthOf(String text) {
        return text == null ? 0 : text.length();
    }

    /**
     * Creates the message hash: the first two digits of the message ID, a
     * colon, the message number, a colon, then the first and last words of
     * the message, all in capital letters (for example 00:0:HITONIGHT).
     *
     * @return the message hash
     */
    public String createMessageHash() {
        String[] words = messageText.trim().split("\\s+");
        String firstWord = removePunctuation(words[0]);
        String lastWord = removePunctuation(words[words.length - 1]);
        String hash = messageId.substring(0, 2) + ":" + messageNumber + ":" + firstWord + lastWord;
        return hash.toUpperCase();
    }

    // Removes punctuation such as the "?" in "tonight?" so only letters and digits remain.
    private static String removePunctuation(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Returns the response message for the option the user chose.
     *
     * @param choice 1 = send, 2 = disregard, 3 = store to send later
     * @return the message to show the user for that option
     */
    public String sentMessage(int choice) {
        return switch (choice) {
            case OPTION_SEND -> SENT_SUCCESS;
            case OPTION_DISREGARD -> DISREGARD_PROMPT;
            case OPTION_STORE -> STORED_SUCCESS;
            default -> INVALID_OPTION;
        };
    }

    /**
     * @return this message's details in the order: Message ID, Message Hash,
     *         Recipient, Message
     */
    public String details() {
        String newLine = System.lineSeparator();
        return "Message ID: " + messageId + newLine
                + "Message Hash: " + messageHash + newLine
                + "Recipient: " + recipient + newLine
                + "Message: " + messageText;
    }

    /**
     * Stores this message in messages.json so it can be sent later.
     *
     * @throws IOException if the file cannot be read or written
     */
    public void storeMessage() throws IOException {
        storeMessage(Paths.get(STORAGE_FILE));
    }

    /**
     * Adds this message to the JSON file at the given path. Messages that are
     * already in the file are kept.
     *
     * @param file the JSON file to store the message in
     * @throws IOException if the file cannot be read or written
     */
    public void storeMessage(Path file) throws IOException {
        Message[] storedMessages = MessageStore.readMessages(file);
        Message[] updatedMessages = Arrays.copyOf(storedMessages, storedMessages.length + 1);
        updatedMessages[storedMessages.length] = this;
        MessageStore.writeMessages(file, updatedMessages);
    }

    /** @return the ten-digit message ID */
    public String getMessageId() {
        return messageId;
    }

    /** @return the message hash */
    public String getMessageHash() {
        return messageHash;
    }

    /** @return the recipient's cell number */
    public String getRecipient() {
        return recipient;
    }

    /** @return the message text */
    public String getMessageText() {
        return messageText;
    }
}
