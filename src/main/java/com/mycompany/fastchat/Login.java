package com.mycompany.fastchat;

import java.util.regex.Pattern;

/**
 * Login.java
 *
 * Handles user registration and login validation for the FastChat
 * application (PROG5121 POE Part 1).
 *
 * One instance of this class represents a single registered user and
 * stores their registration details (username, password, cell number,
 * first name, last name) so that later login attempts can be verified
 * against them.
 *
 * @author Andile
 */
public class Login {

    // ---------------------------------------------------------------
    // Registration and login response messages.
    // Kept as public constants so both the application and the unit
    // tests reference the exact same text — avoids duplicated strings
    // and keeps assertEquals checks reliable.
    // ---------------------------------------------------------------

    public static final String USERNAME_SUCCESS = "Username successfully captured.";
    public static final String USERNAME_FAIL =
            "Username is not correctly formatted; please ensure that your username contains "
            + "an underscore and is no more than five characters in length.";

    public static final String PASSWORD_SUCCESS = "Password successfully captured.";
    public static final String PASSWORD_FAIL =
            "Password is not correctly formatted; please ensure that the password contains "
            + "at least eight characters, a capital letter, a number, and a special character.";

    public static final String CELL_SUCCESS = "Cell number successfully captured.";
    public static final String CELL_FAIL =
            "Cell number is incorrectly formatted or does not contain an international code; "
            + "please correct the number and try again.";

    public static final String LOGIN_FAIL = "Username or password incorrect, please try again.";

    // ---------------------------------------------------------------
    // Password complexity rules: capital letter, digit, special character.
    // Length is checked separately in checkPasswordComplexity().
    // ---------------------------------------------------------------
    private static final Pattern HAS_CAPITAL = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_NUMBER = Pattern.compile(".*[0-9].*");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile(".*[^a-zA-Z0-9].*");

    /*
     * Cell phone regex adapted from the international phone number
     * validation approach described by Baeldung:
     * https://www.baeldung.com/java-regex-validate-phone-numbers
     *
     * Requires a leading "+", followed by a 1-3 digit international
     * dialing code, followed by exactly 9 further digits — i.e. no
     * more than 10 characters after the "+".
     */
    private static final Pattern CELL_PATTERN = Pattern.compile("^\\+\\d{1,3}\\d{9}$");

    private final String username;
    private final String password;
    private final String cellphoneNumber;
    private final String firstName;
    private final String lastName;

    /**
     * Creates a Login instance holding one user's registration details.
     *
     * @param username        the chosen username
     * @param password        the chosen password
     * @param cellphoneNumber the user's cell number, including international code
     * @param firstName       the user's first name (used in the login welcome message)
     * @param lastName        the user's last name (used in the login welcome message)
     */
    public Login(String username, String password, String cellphoneNumber,
                 String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.cellphoneNumber = cellphoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Checks that the username contains an underscore and is no more
     * than five characters long.
     *
     * @return true if the username is correctly formatted
     */
    public boolean checkUserName() {
        return username != null && username.contains("_") && username.length() <= 5;
    }

    /**
     * Checks that the password is at least eight characters long and
     * contains a capital letter, a number, and a special character.
     *
     * @return true if the password meets all complexity requirements
     */
    public boolean checkPasswordComplexity() {
        if (password == null || password.length() < 8) {
            return false;
        }
        return HAS_CAPITAL.matcher(password).matches()
                && HAS_NUMBER.matcher(password).matches()
                && HAS_SPECIAL_CHAR.matcher(password).matches();
    }

    /**
     * Checks that the cell number starts with an international dialling
     * code and is no more than ten characters long after it.
     *
     * @return true if the cell number is correctly formatted
     */
    public boolean checkCellPhoneNumber() {
        return cellphoneNumber != null && CELL_PATTERN.matcher(cellphoneNumber).matches();
    }

    /**
     * @return the username success or failure message, based on
     *         {@link #checkUserName()} alone.
     */
    public String usernameMessage() {
        return checkUserName() ? USERNAME_SUCCESS : USERNAME_FAIL;
    }

    /**
     * @return the password success or failure message, based on
     *         {@link #checkPasswordComplexity()} alone.
     */
    public String passwordMessage() {
        return checkPasswordComplexity() ? PASSWORD_SUCCESS : PASSWORD_FAIL;
    }

    /**
     * @return the cell number success or failure message, based on
     *         {@link #checkCellPhoneNumber()} alone.
     */
    public String cellMessage() {
        return checkCellPhoneNumber() ? CELL_SUCCESS : CELL_FAIL;
    }

    /**
     * Runs the username, password, and cell number checks in sequence
     * and returns the message for the first one that fails. If all
     * three pass, returns a combined success message.
     *
     * @return the registration outcome message
     */
    public String registerUser() {
        if (!checkUserName()) {
            return usernameMessage();
        }
        if (!checkPasswordComplexity()) {
            return passwordMessage();
        }
        if (!checkCellPhoneNumber()) {
            return cellMessage();
        }
        return USERNAME_SUCCESS + " " + PASSWORD_SUCCESS + " " + CELL_SUCCESS;
    }

    /**
     * Verifies that the supplied credentials match the details this
     * user registered with.
     *
     * @param enteredUsername the username entered at login
     * @param enteredPassword the password entered at login
     * @return true if both match the stored registration details
     */
    public boolean loginUser(String enteredUsername, String enteredPassword) {
        return username != null && username.equals(enteredUsername)
                && password != null && password.equals(enteredPassword);
    }

    /**
     * Builds the message shown after a login attempt.
     *
     * @param loginSuccessful the result of {@link #loginUser(String, String)}
     * @return a personalised welcome message on success, or a failure message
     */
    public String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome " + firstName + ", " + lastName + " it is great to see you again.";
        }
        return LOGIN_FAIL;
    }
}