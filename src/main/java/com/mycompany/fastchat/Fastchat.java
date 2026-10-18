package com.mycompany.fastchat;

import java.io.IOException;
import java.util.Scanner;

/**
 * Fastchat.java
 *
 * Console entry point for the FastChat application (PROG5121 POE Parts 1
 * and 2). The user registers and logs in, and once logged in can choose
 * from a numeric menu to send messages, see recent messages (coming soon)
 * or quit.
 *
 * Console-only — no GUI elements are used, per the assignment brief.
 *
 * @author Andile
 */
public class Fastchat {

    private static final String WELCOME_MESSAGE = "Welcome to FastChat.";
    private static final String COMING_SOON = "Coming Soon.";

    // Main menu options.
    private static final int MENU_SEND = 1;
    private static final int MENU_RECENT = 2;
    private static final int MENU_QUIT = 3;

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        Login login = registerNewUser();

        boolean registeredSuccessfully = login.checkUserName()
                && login.checkPasswordComplexity()
                && login.checkCellPhoneNumber();

        // Only a fully registered user who then logs in successfully can send messages.
        if (registeredSuccessfully && logInUser(login)) {
            runApplication();
        }

        SCANNER.close();
    }

    // ---------------------------------------------------------------
    // Part 1: registration and login
    // ---------------------------------------------------------------

    private static Login registerNewUser() {
        System.out.println("=== FastChat Registration ===");

        String username = readLine("Enter a username: ");
        String password = readLine("Enter a password: ");
        String cell = readLine("Enter your South African cell number (e.g. +27838968976): ");
        String firstName = readLine("Enter your first name: ");
        String lastName = readLine("Enter your last name: ");

        Login login = new Login(username, password, cell, firstName, lastName);
        System.out.println(login.registerUser());
        return login;
    }

    private static boolean logInUser(Login login) {
        System.out.println();
        System.out.println("=== FastChat Login ===");

        String loginUsername = readLine("Enter your username: ");
        String loginPassword = readLine("Enter your password: ");

        boolean success = login.loginUser(loginUsername, loginPassword);
        System.out.println(login.returnLoginStatus(success));
        return success;
    }

    // ---------------------------------------------------------------
    // Part 2: menu and messages
    // ---------------------------------------------------------------

    private static void runApplication() {
        System.out.println();
        System.out.println(WELCOME_MESSAGE);

        int messagesAllowed = readPositiveInt("How many messages do you wish to enter? ");
        int messagesEntered = 0;

        // The application keeps running until the user chooses Quit.
        boolean running = true;
        while (running) {
            printMenu();
            int option = readInt("Choose an option: ");

            switch (option) {
                case MENU_SEND -> messagesEntered = sendMessages(messagesEntered, messagesAllowed);
                case MENU_RECENT -> System.out.println(COMING_SOON);
                case MENU_QUIT -> running = false;
                default -> System.out.println("Invalid option; please choose 1, 2 or 3.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1) Send Messages");
        System.out.println("2) Show recently sent messages");
        System.out.println("3) Quit");
    }

    /**
     * Lets the user enter the messages they still have left, using a for loop
     * whose counter is also the message number.
     *
     * @return how many messages have been entered so far
     */
    private static int sendMessages(int messagesEntered, int messagesAllowed) {
        if (messagesEntered >= messagesAllowed) {
            System.out.println("You have already entered all " + messagesAllowed + " messages.");
            return messagesEntered;
        }

        for (int messageNumber = messagesEntered; messageNumber < messagesAllowed; messageNumber++) {
            enterMessage(messageNumber);
        }

        System.out.println();
        System.out.println("Total messages sent: " + Message.returnTotalMessages());
        return messagesAllowed;
    }

    private static void enterMessage(int messageNumber) {
        System.out.println();
        System.out.println("--- Message " + (messageNumber + 1) + " ---");

        String recipient = readRecipient();
        String messageText = readMessageText();

        Message message = new Message(messageNumber, recipient, messageText);
        System.out.println(message.messageIdMessage());

        handleSendOption(message);
    }

    private static String readRecipient() {
        String recipient = readLine("Enter the recipient's cell number (e.g. +27718693002): ");
        while (!Message.isValidRecipient(recipient)) {
            System.out.println(Message.recipientMessage(recipient));
            recipient = readLine("Enter the recipient's cell number: ");
        }
        System.out.println(Message.recipientMessage(recipient));
        return recipient;
    }

    private static String readMessageText() {
        String messageText = readLine("Enter your message (maximum 250 characters): ");
        while (messageText.isBlank() || !Message.isMessageLengthValid(messageText)) {
            if (messageText.isBlank()) {
                System.out.println("A message cannot be empty.");
            } else {
                System.out.println(Message.LENGTH_PROMPT);
                System.out.println(Message.checkMessageLength(messageText));
            }
            messageText = readLine("Enter your message (maximum 250 characters): ");
        }
        System.out.println(Message.checkMessageLength(messageText));
        return messageText;
    }

    /**
     * Asks the user whether to send, disregard or store the message, and
     * keeps asking until the message has been dealt with.
     */
    private static void handleSendOption(Message message) {
        boolean handled = false;
        while (!handled) {
            System.out.println();
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case Message.OPTION_SEND -> {
                    System.out.println(message.sentMessage(choice));
                    Message.recordSent(message);
                    System.out.println(message.details());
                    handled = true;
                }
                case Message.OPTION_DISREGARD -> {
                    System.out.println(message.sentMessage(choice));
                    handled = confirmDelete();
                }
                case Message.OPTION_STORE -> handled = storeForLater(message);
                default -> System.out.println(message.sentMessage(choice));
            }
        }
    }

    private static boolean confirmDelete() {
        String input = readLine("> ");
        if (input.trim().equals("0")) {
            System.out.println("Message deleted.");
            return true;
        }
        System.out.println("Message not deleted.");
        return false;
    }

    private static boolean storeForLater(Message message) {
        try {
            message.storeMessage();
            System.out.println(message.sentMessage(Message.OPTION_STORE));
            return true;
        } catch (IOException e) {
            System.out.println("The message could not be stored: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Input helpers
    // ---------------------------------------------------------------

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine();
    }

    private static int readInt(String prompt) {
        Integer value = null;
        while (value == null) {
            String input = readLine(prompt);
            try {
                value = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
        return value;
    }

    private static int readPositiveInt(String prompt) {
        int value = readInt(prompt);
        while (value < 1) {
            System.out.println("Please enter a number of 1 or more.");
            value = readInt(prompt);
        }
        return value;
    }
}
