package com.bulmanator.ext2.Terminal.Commands;

import com.bulmanator.ext2.Structure.Directory;
import com.bulmanator.ext2.Structure.DirectoryEntry;
import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Utils.Helper;

public class List implements Command {

    private String args;
    private String format;

    public List(String args) {
        this.args = args;
    }

    @Override
    public void invoke(Volume volume) {
        Directory dir = volume.getCurrentDir();
        calcSpaces(dir);
        switch (args) {
            case "l":
                System.out.println("total: " + (dir.getEntryCount() - 2));
                for (int i = 0; i < dir.getEntryCount(); i++) {
                    if(!dir.getEntry(i).getName().contains(".")) {
                        printLine(dir.getEntry(i));
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
                    printLine(dir.getEntry(i));
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

    private void printLine(DirectoryEntry entry) {
        String user = entry.getInode().getUserID() == 0 ? "root" : entry.getInode().getUserID() == 1000 ? "scc211" : "unknown";
        String grp = entry.getInode().getGroupID() == 0 ? "root" : entry.getInode().getGroupID() == 1000 ? "scc211" : "unknown";
        System.out.printf(
                format,
                entry.getInode().getPermissionString(),
                entry.getInode().getLinks(),
                user,
                grp,
                entry.getInode().getSize(),
                Helper.toDate(entry.getInode().getModificationTime() * 1000L),
                entry.getName()
        );
    }

    private void calcSpaces(Directory dir) {
        int uid = 0, gid = 0, size = 0, links = 0;
        for(int i = 0; i < dir.getEntryCount(); i++) {
            DirectoryEntry curEnt = dir.getEntry(i);

            int val = curEnt.getInode().getUserID() == 0 ? 4 : curEnt.getInode().getUserID() == 1000 ? 6 : 7;
            if(val > uid) uid = val;

            val = curEnt.getInode().getGroupID() == 0 ? 4 : curEnt.getInode().getGroupID() == 1000 ? 6 : 7;
            if(val > gid) gid = val;

            val = String.valueOf(curEnt.getInode().getSize()).length();
            if(val > size) size = val;

            val = String.valueOf(curEnt.getInode().getLinks()).length();
            if(val > links) links = val;
        }

        format = "%s %" + links + "d %-" + uid + "s %-" + gid + "s %" + size + "d %s %s\n";
    }
}
