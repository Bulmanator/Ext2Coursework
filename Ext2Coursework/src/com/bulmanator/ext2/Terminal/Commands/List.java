package com.bulmanator.ext2.Terminal.Commands;

import com.bulmanator.ext2.Structure.Directory;
import com.bulmanator.ext2.Structure.DirectoryEntry;
import com.bulmanator.ext2.Structure.Inode;
import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Utils.Helper;

public class List implements Command {

    private String args;

    public List(String args) {
        this.args = args;
    }

    @Override
    public void invoke(Volume volume) {
        Directory dir = volume.getCurrentDir();
        switch (args) {
            case "l":
                System.out.println("total: " + dir.getEntryCount());
                for (int i = 0; i < dir.getEntryCount(); i++) {
                    if(!dir.getEntry(i).getName().contains(".")) {
                        printl(dir.getEntry(i));
                    }
                }
                break;
            case "a":
                for (int i = 0; i < dir.getEntryCount(); i++) {
                    System.out.print(dir.getEntry(i).getName() + " ");
                }
                System.out.println();
                break;
            case "la":
                System.out.println("total: " + dir.getEntryCount());
                for (int i = 0; i < dir.getEntryCount(); i++) {
                    printl(dir.getEntry(i));
                }
                break;
            case "":
                for(int i = 0; i < dir.getEntryCount(); i++) {
                    if(!dir.getEntry(i).getName().contains("."))
                        System.out.print(dir.getEntry(i).getName() + " ");
                }
                System.out.println();
                break;
            default:
                System.out.println("ls: cannot access '" + args + "': No such file or directory");
                break;
        }
    }

    private void printl(DirectoryEntry entry) {
        String print = entry.getInode().getPermissionString();
        print += " " + entry.getInode().getLinks();
        print += " " + entry.getInode().getUserID();
        print += " " + entry.getInode().getGroupID();
        print += " " + entry.getInode().getSize();
        print += " " + Helper.toDate(entry.getInode().getModificationTime() * 1000L);
        print += " " + entry.getName();

        System.out.println(print);
    }
}
