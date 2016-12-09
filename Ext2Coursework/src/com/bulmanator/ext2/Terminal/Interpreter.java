package com.bulmanator.ext2.Terminal;

import com.bulmanator.ext2.Structure.Directory;
import com.bulmanator.ext2.Structure.Ext2File;
import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Terminal.Commands.ChangeDir;
import com.bulmanator.ext2.Terminal.Commands.Concatenate;
import com.bulmanator.ext2.Terminal.Commands.List;
import com.bulmanator.ext2.Terminal.Commands.Print;

import java.util.Scanner;

public class Interpreter {

    private Scanner input;
    private boolean exit;
    private Volume volume;
    String curPath;

    public Interpreter(Volume volume) {
        this.volume = volume;
        input = new Scanner(System.in);

        exit = false;
        curPath = "/";

        run();
    }

    private void run() {
        while(!exit) {
            String path = curPath.contains("/") ? curPath.substring(curPath.lastIndexOf("/")) : "/" + curPath;
            if(path.length() == 0) path = "/";

            System.out.print("scc211@os-module " + path + "$ ");
            String next = input.nextLine();
            String[] cmd = next.split(" ");
            String arg = cmd.length > 1 ? cmd[1] : "";
            for(int i = 2; i < cmd.length; i++) {
                arg += " " + cmd[i];
            }

            if(cmd[0].equals("cat") && cmd.length > 1) {

                if(!arg.startsWith("/")) arg = "/" + arg;
                Ext2File file = new Ext2File(volume, arg);

                Concatenate c = new Concatenate(file, arg);
                c.invoke(volume);
            }
            else if(cmd[0].equals("ls")) {
                List l = new List(arg.replaceFirst("-", ""));
                l.invoke(volume);
            }
            else if(cmd[0].equals("cd")) {
                if(arg.equals("")) arg = "/";

                Directory newDir = new Directory(volume, arg);

                if(newDir.getFoundType() == 0) {
                    if(arg.equals(".") || arg.equals("..")) {
                        curPath = curPath.contains("/") ? curPath.substring(0, curPath.lastIndexOf("/")) : "/";
                    }
                    else {
                        curPath = arg.contains("/") && arg.endsWith("/") ? arg.substring(0, arg.length() - 1) : arg;
                    }
                }

                ChangeDir cd = new ChangeDir(newDir, arg);
                cd.invoke(volume);
            }
            else if(cmd[0].equals("exit") || cmd[0].equals("quit")) {
                exit = true;
            }
            else if(cmd[0].equals("print")) {

                String[] args = arg.split(" ");
                Ext2File f = null;
                if(args.length > 0) {
                    f = new Ext2File(volume, args[0]);
                    if(f.getFound() == Ext2File.FOUND || f.getFound() == Ext2File.DIR) {
                        String[] tmp = args;
                        args = new String[tmp.length - 1];
                        for (int i = 1; i < tmp.length; i++) {
                            args[i - 1] = tmp[i];
                        }
                    }
                    else { f = null; }
                }

                Print print = new Print(f, args);
                print.invoke(volume);
            }
            else if(cmd[0].equals("help")) {
                System.out.println("Available Commands: ");
                System.out.println(" - help: Print this message");
                System.out.println(" - cd: Change Directory");
                System.out.println(" - cat: Print out file data");
                System.out.println(" - ls: List directory entries");
                System.out.println(" - print: Print out/ dump various things (DEBUG / UTILITY)");
                System.out.println(" - exit/ quit: Close the interpreter");
            }
            else {
                System.out.println(cmd[0] + ": command not found");
            }
        }
    }
}
