package com.space;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            FileOrganizer fileOrganizer = new FileOrganizer("Documents/sync");

            fileOrganizer.checkDuplicates();

            Scanner scanner = new Scanner(System.in);

            System.out.print("Delete Duplicates ? [y | yes || n| no ] : ");
            String shouldDeleteDuplicate = scanner.nextLine();
            // TODO: validate input

            // close input scanner
            scanner.close();

            System.out.println(shouldDeleteDuplicate.toUpperCase().equals("Y"));

            if (shouldDeleteDuplicate.toUpperCase().equals("Y") || shouldDeleteDuplicate.toUpperCase().equals("YES")) {
                System.out.println("Deleting duplicates");
                fileOrganizer.deleteDuplicates();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }
}