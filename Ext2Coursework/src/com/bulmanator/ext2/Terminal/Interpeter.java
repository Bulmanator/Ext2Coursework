package com.bulmanator.ext2.Terminal;

import com.bulmanator.ext2.Structure.Directory;
import com.bulmanator.ext2.Structure.Ext2File;
import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Terminal.Commands.ChangeDir;
import com.bulmanator.ext2.Terminal.Commands.Concatenate;
import com.bulmanator.ext2.Terminal.Commands.List;

import java.util.Scanner;

public class Interpeter {

    private Scanner input;
    private boolean exit;
    private Volume volume;
    String curPath;

    public Interpeter(Volume volume) {
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
            else if(cmd[0].equals("pwd")) {
                System.out.println(curPath.startsWith("/") ? curPath : "/" + curPath);
            }
            else if(cmd[0].equals("print")) {
                if(arg.equals("")) volume.getCurrentDir().getInode().printInodeData();
            }
            else if(cmd[0].equals("exit") || cmd[0].equals("quit")) {
                exit = true;
            }
            else {
                System.out.println(cmd[0] + ": command not found");
            }
        }
    }
}
