package com.mycompany.fastchat;

import java.util.Scanner;

/**
 * Fastchat.java
 *
 * Console entry point for the FastChat application (PROG5121 POE Part 1).
 * Demonstrates the registration and login feature end-to-end: captures
 * a new user's details, validates them via {@link Login}, and then lets
 * that user log back in.
 *
 * Console-only — no GUI elements are used, per the assignment brief.
 *
 * @author Andile
 */
public class Fastchat {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== FastChat Registration ===");

        System.out.print("Enter a username: ");
        String username = scanner.nextLine();

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        System.out.print("Enter your South African cell number (e.g. +27838968976): ");
        String cell = scanner.nextLine();

        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        Login login = new Login(username, password, cell, firstName, lastName);
        System.out.println(login.registerUser());

        boolean registeredSuccessfully = login.checkUserName()
                && login.checkPasswordComplexity()
                && login.checkCellPhoneNumber();

        // Only proceed to login if registration fully succeeded.
        if (registeredSuccessfully) {
            System.out.println();   
            System.out.println("=== FastChat Login ===");

            System.out.print("Enter your username: ");
            String loginUsername = scanner.nextLine();

            System.out.print("Enter your password: ");
            String loginPassword = scanner.nextLine();

            boolean success = login.loginUser(loginUsername, loginPassword);
            System.out.println(login.returnLoginStatus(success));
        }

        scanner.close();
    }
}