package com.mycompany.fastchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * MessageStore.java
 *
 * Holds every message the user sends, disregards or stores in arrays
 * (PROG5121 POE Part 3), and can search them, delete a stored message by its
 * hash, and build a report. Stored messages are read from, and saved to, the
 * messages.json file.
 *
 * The messageHashes and messageIds arrays are parallel: the hash at one
 * position belongs to the ID at the same position.
 *
 * @author Andile
 */
public class MessageStore {

    public static final String NO_STORED_MESSAGES = "There are no stored messages.";
    public static final String ID_NOT_FOUND = "No message found with that message ID.";
    public static final String HASH_NOT_FOUND = "No message found with that message hash.";

    private static final String STORAGE_FILE = "messages.json";

    /*
     * Reading a JSON file into an array of objects, and writing an array of
     * objects back to a JSON file, with Gson, adapted from the attacomsian.com
     * Gson guides:
     * https://attacomsian.com/blog/gson-read-json-file
     * https://attacomsian.com/blog/gson-write-json-file
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // The arrays are filled while the program runs, so nothing is hard-coded.
    private Message[] sentMessages = new Message[0];
    private Message[] disregardedMessages = new Message[0];
    private Message[] storedMessages = new Message[0];
    private String[] messageHashes = new String[0];
    private String[] messageIds = new String[0];

    // ---------------------------------------------------------------
    // Filling the arrays
    // ---------------------------------------------------------------

    /**
     * @param message a message the user sent
     */
    public void addSent(Message message) {
        sentMessages = append(sentMessages, message);
        recordHashAndId(message);
    }

    /**
     * @param message a message the user disregarded
     */
    public void addDisregarded(Message message) {
        disregardedMessages = append(disregardedMessages, message);
        recordHashAndId(message);
    }

    /**
     * @param message a message the user stored to send later
     */
    public void addStored(Message message) {
        storedMessages = append(storedMessages, message);
        recordHashAndId(message);
    }

    private void recordHashAndId(Message message) {
        messageHashes = append(messageHashes, message.getMessageHash());
        messageIds = append(messageIds, message.getMessageId());
    }

    // ---------------------------------------------------------------
    // Reading and saving the JSON file
    // ---------------------------------------------------------------

    /**
     * Reads messages.json into the stored messages array.
     *
     * @throws IOException if the file cannot be read or is not valid JSON
     */
    public void loadStoredMessages() throws IOException {
        loadStoredMessages(Paths.get(STORAGE_FILE));
    }

    /**
     * Reads a JSON file into the stored messages array. Nothing is loaded if
     * the file does not exist yet.
     *
     * @param file the JSON file to read
     * @throws IOException if the file cannot be read or is not valid JSON
     */
    public void loadStoredMessages(Path file) throws IOException {
        for (Message message : readMessages(file)) {
            addStored(message);
        }
    }

    /**
     * Saves the stored messages array to messages.json, replacing what was there.
     *
     * @throws IOException if the file cannot be written
     */
    public void saveStoredMessages() throws IOException {
        saveStoredMessages(Paths.get(STORAGE_FILE));
    }

    /**
     * @param file the JSON file to write the stored messages to
     * @throws IOException if the file cannot be written
     */
    public void saveStoredMessages(Path file) throws IOException {
        writeMessages(file, storedMessages);
    }

    /**
     * Reads the messages in a JSON file into an array.
     *
     * @param file the JSON file to read
     * @return the messages in the file (an empty array if the file does not exist or is empty)
     * @throws IOException if the file cannot be read or is not valid JSON
     */
    static Message[] readMessages(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new Message[0];
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Message[] messages = GSON.fromJson(reader, Message[].class);
            return messages == null ? new Message[0] : messages;
        } catch (JsonParseException e) {
            throw new IOException(file.getFileName() + " is not valid JSON.", e);
        }
    }

    /**
     * Writes an array of messages to a JSON file.
     *
     * @param file     the JSON file to write
     * @param messages the messages to write
     * @throws IOException if the file cannot be written
     */
    static void writeMessages(Path file, Message[] messages) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(messages, writer);
        }
    }

    // ---------------------------------------------------------------
    // Reading the arrays
    // ---------------------------------------------------------------

    /** @return the text of every sent message */
    public String[] sentMessageTexts() {
        return textsOf(sentMessages);
    }

    /** @return the text of every disregarded message */
    public String[] disregardedMessageTexts() {
        return textsOf(disregardedMessages);
    }

    /** @return the text of every stored message */
    public String[] storedMessageTexts() {
        return textsOf(storedMessages);
    }

    /** @return the hash of every message */
    public String[] getMessageHashes() {
        return Arrays.copyOf(messageHashes, messageHashes.length);
    }

    /** @return the ID of every message, in the same order as the hashes */
    public String[] getMessageIds() {
        return Arrays.copyOf(messageIds, messageIds.length);
    }

    /** @return the total number of messages sent */
    public int returnTotalMessages() {
        return sentMessages.length;
    }

    /** @return the details of every message sent */
    public String printMessages() {
        return section("Sent Messages", sentMessages);
    }

    // ---------------------------------------------------------------
    // Features for the Stored Messages menu
    // ---------------------------------------------------------------

    /**
     * Lists the sender and recipient of every stored message.
     *
     * @param sender the user who sent the messages
     * @return one line per stored message
     */
    public String storedSendersAndRecipients(String sender) {
        if (storedMessages.length == 0) {
            return NO_STORED_MESSAGES;
        }
        String newLine = System.lineSeparator();
        StringBuilder lines = new StringBuilder();
        for (Message message : storedMessages) {
            lines.append("Sender: ").append(sender)
                    .append(" | Recipient: ").append(message.getRecipient())
                    .append(newLine);
        }
        return lines.toString();
    }

    /**
     * @return the stored message with the most characters, or null if there are none
     */
    public Message longestStoredMessage() {
        Message longest = null;
        for (Message message : storedMessages) {
            if (longest == null || message.getMessageText().length() > longest.getMessageText().length()) {
                longest = message;
            }
        }
        return longest;
    }

    /**
     * Searches the sent, stored and disregarded messages for a message ID.
     *
     * @param messageId the message ID to find
     * @return the matching message, or null if there is none
     */
    public Message findById(String messageId) {
        Message[][] allGroups = {sentMessages, storedMessages, disregardedMessages};
        for (Message[] group : allGroups) {
            for (Message message : group) {
                if (message.getMessageId().equals(messageId)) {
                    return message;
                }
            }
        }
        return null;
    }

    /**
     * Finds the sent and stored messages for one recipient.
     *
     * @param recipient the recipient's cell number
     * @return the text of each matching message (an empty array if there are none)
     */
    public String[] messagesForRecipient(String recipient) {
        Message[][] sentAndStored = {sentMessages, storedMessages};
        String[] found = new String[0];
        for (Message[] group : sentAndStored) {
            for (Message message : group) {
                if (message.getRecipient().equals(recipient)) {
                    found = append(found, message.getMessageText());
                }
            }
        }
        return found;
    }

    /**
     * Deletes a stored message using its message hash, and removes its hash and
     * ID from the hash and ID arrays. If two stored messages share a hash, the
     * first one is deleted.
     *
     * @param hash the message hash of the stored message to delete
     * @return a message saying which message was deleted, or that none was found
     */
    public String deleteByHash(String hash) {
        int index = indexOfHash(storedMessages, hash);
        if (index < 0) {
            return HASH_NOT_FOUND;
        }
        Message removed = storedMessages[index];
        storedMessages = removeAt(storedMessages, index);
        forgetHashAndId(removed);
        return "Message: \"" + removed.getMessageText() + "\" successfully deleted.";
    }

    /**
     * Builds a report with the full details of every sent, stored and
     * disregarded message: Message ID, Message Hash, Recipient and Message.
     *
     * @return the report
     */
    public String report() {
        return section("Sent Messages", sentMessages)
                + section("Stored Messages", storedMessages)
                + section("Disregarded Messages", disregardedMessages);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static String section(String title, Message[] messages) {
        String newLine = System.lineSeparator();
        StringBuilder text = new StringBuilder("=== " + title + " ===" + newLine);
        if (messages.length == 0) {
            text.append("None").append(newLine).append(newLine);
        }
        for (Message message : messages) {
            text.append(message.details()).append(newLine).append(newLine);
        }
        return text.toString();
    }

    private static String[] textsOf(Message[] messages) {
        String[] texts = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            texts[i] = messages[i].getMessageText();
        }
        return texts;
    }

    private static int indexOfHash(Message[] messages, String hash) {
        for (int i = 0; i < messages.length; i++) {
            if (messages[i].getMessageHash().equalsIgnoreCase(hash)) {
                return i;
            }
        }
        return -1;
    }

    // Removes one message's hash and ID from the parallel arrays.
    private void forgetHashAndId(Message message) {
        for (int i = 0; i < messageHashes.length; i++) {
            if (messageHashes[i].equals(message.getMessageHash())
                    && messageIds[i].equals(message.getMessageId())) {
                messageHashes = removeAt(messageHashes, i);
                messageIds = removeAt(messageIds, i);
                return;
            }
        }
    }

    // Returns a copy of the array that is one item longer, with the item added at the end.
    private static <T> T[] append(T[] array, T item) {
        T[] longer = Arrays.copyOf(array, array.length + 1);
        longer[array.length] = item;
        return longer;
    }

    // Returns a copy of the array without the item at the given position.
    private static <T> T[] removeAt(T[] array, int index) {
        T[] shorter = Arrays.copyOf(array, array.length - 1);
        System.arraycopy(array, index + 1, shorter, index, array.length - index - 1);
        return shorter;
    }
}
