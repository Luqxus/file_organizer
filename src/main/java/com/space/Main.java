package com.space;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            FileOrganizer fileOrganizer = new FileOrganizer("Documents/sync");

            fileOrganizer.checkDuplicates();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }
}