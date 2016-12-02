package com.bulmanator.ext2.Terminal;

import java.util.Scanner;

public class Interpeter {

    private Scanner input;
    private boolean exit;

    public Interpeter() {
        input = new Scanner(System.in);
        exit = false;
    }

    public void run() {
        while(!exit) {
            String next = input.nextLine();
            String command = "";
            String[] args;
            if(next.contains(" ")) {
                command = next.substring(0, next.indexOf(" "));
                args = next.substring(next.indexOf(" ") + 1).split(" ");
            }
            else {
                command = next;
                args = new String[0];
            }
            System.out.println("Command: " + command + " Arg Count: " + args.length);

            if(command.equals("exit")) { exit = true; }
        }
    }
}
