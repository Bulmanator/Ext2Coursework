package com.bulmanator.ext2;

import com.bulmanator.ext2.Structure.Directory;
import com.bulmanator.ext2.Structure.Ext2File;
import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Utils.Helper;

import java.util.Scanner;

public class Main {

    private Volume v;

    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }

    private void run() {
        Scanner s = new Scanner(System.in);
        v = null;
        try { v = new Volume("resources/ext2fs"); }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: Failed to create File System!");
            System.exit(-1);
        }

        Directory d = new Directory(v, "/");
        String curPath = "/";

      //  Ext2File f = new Ext2File(v, "/files/trpl-ind-e");

       // f.seek(17180403712L);
       // byte[] buf = f.read(2048);

        //Helper.dumpHexBytes(buf);

        //System.out.printf("%s", new String(buf));

        boolean exit = false;
        while(!exit) {
            System.out.print("scc211@os-module /$ ");
            String next = s.nextLine();
            String[] cmd = next.split(" ");
            String arg = cmd.length > 1 ? cmd[1] : "";
            for(int i = 2; i < cmd.length; i++) {
                arg += " " + cmd[i];
            }

            if(cmd[0].equals("cat") && cmd.length > 1) {
                Ext2File file = new Ext2File(v, arg);
                for(long i = 0; i < file.size; i += v.BLOCK_SIZE * 2L) {
                    byte[] data = file.read(i, v.BLOCK_SIZE * 2L < (file.size - i) ? v.BLOCK_SIZE * 2L : (file.size - i));
                    boolean valid = false;
                    for(int j = 0; j < data.length; j++) {
                        if(data[j] > 9 && data[j] < 127) {
                            System.out.printf("%c", data[j]);
                            valid = true;
                        }
                    }
                   // if(valid) System.out.printf("Data From: %d\n", i);
                }
            }
            else if(cmd[0].equals("exit")) exit = true;
        }

      /*  boolean exit = false;
        while (!exit) {
            System.out.print("scc211@os-module: " + curPath + "$ ");
            String next = s.nextLine();
            String[] cmd = next.split(" ");

            if(cmd[0].equals("cd") && cmd.length > 1) {
                Directory oldDir = d;
                String arg = "";
                for(int i = 1; i < cmd.length; i++) {
                    if(i == 1) {
                        arg += cmd[i];
                    }
                    else {
                        arg += " " + cmd[i];
                    }
                }

                if(arg.charAt(0) != '/') {
                    if(!curPath.endsWith("/")) {
                        arg = curPath + "/" + arg;
                    }
                    else {
                        arg = curPath + arg;
                    }
                }

                d = new Directory(v, arg);

                if(d.getInode() == null) {
                    switch (d.getFoundType()) {
                        case Directory.FILE:
                            System.out.println("cd: " + cmd[1] + ": Not a directory");
                            break;
                        case Directory.NOT_FOUND:
                            System.out.println("cd: " + cmd[1] + ": No such file or directory");
                    }
                    d = oldDir;
                }
                else {
                    if(arg.equals("..")) {
                        curPath = curPath.substring(0, curPath.lastIndexOf("/"));
                        if(curPath.equals("")) curPath = "/";
                    }
                    else {
                        curPath = arg;
                    }
                }
            }
            else if(cmd[0].equals("ls") && cmd.length > 1) {
                switch (cmd[1]) {
                    case "-l":
                        d.printLSL(1);
                        break;
                    case "-la":
                        d.printLSL(2);
                        break;
                    case "-a":
                        d.printLS(2);
                }
            }
            else if(cmd[0].equals("ls") && cmd.length == 1) {
                d.printLS(1);
            }
            else if(cmd[0].equals("cat") && cmd.length > 1) {
                String arg = "";
                for(int i = 1; i < cmd.length; i++) {
                    if(i == 1) arg += cmd[i];
                    else arg += " " + cmd[i];
                }

                if(arg.charAt(0) != '/') {
                    if(!curPath.endsWith("/")) {
                        arg = curPath + "/" + arg;
                    }
                    else {
                        arg = curPath + arg;
                    }
                }

                Ext2File file = new Ext2File(v, arg);
                if(file.getFound() == 0) {
                    byte[] data = new byte[(int)file.getInode().getSize()];

                   // data = v.getData(file.getInode(), 0L, file.getInode().getSize());
                    for(int i = 0; i < data.length; i++) {
                        System.out.printf("0x%02x ", data[i]);
                        if((i + 1) % 16 == 0) System.out.println();
                    }
                }
                else {
                    switch (file.getFound()) {
                        case 1:
                            System.out.println("cat: " + arg  + ": No such file or directory");
                            break;
                        case 2:
                            System.out.println("cat: " + arg + ": Is a directory");
                    }
                }
            }
            else if(cmd[0].equals("exit") || cmd[0].equals("quit")) {
                exit = true;
            }
            else {
                System.out.println(cmd[0] + ": command not found");
            }
        }*/

        v.close();
    }
}
