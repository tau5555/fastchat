package com.mycompany.fastchat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LoginTest.java
 *
 * Unit tests for the Login class, covering every test case supplied
 * in the PROG5121 POE Part 1 brief.
 *
 * @author Andile
 */
public class LoginTest {

    // ---- Username ----

    @Test
    public void testUsernameCorrectlyFormatted() {
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertTrue(login.checkUserName());
        assertEquals(Login.USERNAME_SUCCESS, login.usernameMessage());
    }

    @Test
    public void testUsernameIncorrectlyFormatted() {
        Login login = new Login("kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertFalse(login.checkUserName());
        assertEquals(Login.USERNAME_FAIL, login.usernameMessage());
    }

    // ---- Password ----

    @Test
    public void testPasswordMeetsComplexityRequirements() {
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertTrue(login.checkPasswordComplexity());
        assertEquals(Login.PASSWORD_SUCCESS, login.passwordMessage());
    }

    @Test
    public void testPasswordDoesNotMeetComplexityRequirements() {
        Login login = new Login("kyl_1", "password", "+27838968976", "Kyle", "Smith");
        assertFalse(login.checkPasswordComplexity());
        assertEquals(Login.PASSWORD_FAIL, login.passwordMessage());
    }

    // ---- Cell phone number ----

    @Test
    public void testCellPhoneCorrectlyFormatted() {
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertTrue(login.checkCellPhoneNumber());
        assertEquals(Login.CELL_SUCCESS, login.cellMessage());
    }

    @Test
    public void testCellPhoneIncorrectlyFormatted() {
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "08966553", "Kyle", "Smith");
        assertFalse(login.checkCellPhoneNumber());
        assertEquals(Login.CELL_FAIL, login.cellMessage());
    }

    // ---- Login ----

    @Test
    public void testLoginSuccessful() {
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    public void testLoginFailed() {
        Login login = new Login("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertFalse(login.loginUser("kyl_1", "wrongpassword"));
    }
}