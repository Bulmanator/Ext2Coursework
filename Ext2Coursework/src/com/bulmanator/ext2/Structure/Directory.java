package com.bulmanator.ext2.Structure;

import com.bulmanator.ext2.Utils.Helper;

import java.util.Vector;

public class Directory {

    public static final int FOUND = 0;
    public static final int NOT_FOUND = 1;
    public static final int FILE = 2;

    private String path;
    private DirectoryEntry[] entries;
    private Inode inode;

    private int foundType;

    /**
     * Structures a directory, on the volume, from the given path
     * @param volume The volume which the directory is held on
     * @param path The Path to the directory
     */
    public Directory(Volume volume, String path) {
        this.path = path;
        String[] directories = path.substring(1).split("/");
        Directory curDir = volume.getRoot();

        if(directories[0].equals("")) {
            entries = new DirectoryEntry[curDir.getEntryCount()];
            for(int i = 0; i < entries.length; i++) {
                entries[i] = curDir.getEntry(i);
            }
            foundType = 0;
            return;
        }

        int curPath = 0;
        boolean fileFound = false, treeEnd = false;
        while(!fileFound && !treeEnd) {
            for (int i = 0; i < curDir.getEntryCount(); i++) {
                if(directories[curPath].equals(curDir.getEntry(i).getName())) {
                    curPath++;
                    if(curPath == directories.length) {
                        fileFound = true;
                        inode = curDir.getEntry(i).getInode();
                       if(!inode.isDirectory()) {
                           inode = null;
                           foundType = 2;
                           return;
                       }
                    }
                    else {
                        curDir = new Directory(volume, curDir.getEntry(i).getInode());
                    }
                    break;
                }

                treeEnd = (i == curDir.getEntryCount() - 1);
            }
        }

        if(fileFound) {
            initDirEntries(volume);
            foundType = 0;
        }
        else {
            foundType = 1;
        }
    }

    /**
     * Creates a Directory with the volume given from the Inode
     * @param volume The Volume which the directory is present on
     * @param inode The Inode which corresponds to the directory
     */
    public Directory(Volume volume, Inode inode) {
        this.inode = inode;
        if(!inode.isDirectory()) {
            System.err.println("Directory: Not a directory");
            return;
        }

        if(inode.getSize() >= Integer.MAX_VALUE) throw new OutOfMemoryError("Error: Directory Size too big!");

        initDirEntries(volume);
    }

    private void initDirEntries(Volume volume) {
        Vector<DirectoryEntry> tmp = new Vector<>();

        int overallOffset = 0, currentOffset = 0;
        int curPointer = 0, ptrCount = 0;
        while (curPointer == 0) {
            curPointer = volume.getBlockPointer(inode, 0);
            //System.out.printf("Outside While loop! Current Pointer: 0x%02x\n", curPointer);
            ptrCount++;
        }
        while(overallOffset < inode.getSize()) {

            if(currentOffset == volume.BLOCK_SIZE) {
                curPointer = currentOffset = 0;
                while(curPointer == 0) {
                    curPointer = volume.getBlockPointer(inode, ptrCount * volume.BLOCK_SIZE);
                    ptrCount++;
                }
            }

            DirectoryEntry entry = new DirectoryEntry(volume, curPointer + currentOffset);
            if(entry.getInodeIndex() != 0) {
                tmp.add(entry);
            }
            currentOffset += entry.getLength();
            overallOffset += entry.getLength();
        }

        entries = new DirectoryEntry[tmp.size()];
        for(int i = 0; i < tmp.size(); i++) {
            entries[i] = tmp.get(i);
        }
    }

    /**
     * If the directory was found or not, used by the change directory (cd) command
     * @return An integer representing if the directory was found or not
     */
    public int getFoundType() { return foundType; }
    public Inode getInode() { return inode; }
    public DirectoryEntry getEntry(int index) { return entries[index]; }
    public int getEntryCount() { return entries.length; }

    public void printLS(int type) {
        int lineLen = 0;
        boolean newLine = false;
        for(int i = 0; i < entries.length; i++) {
            if(entries[i].getName().contains(".") && type != 2) continue;

            System.out.print(entries[i].getName() + " ");
            lineLen += entries[i].getNameLength() + 1;
            if(lineLen >= 70) {
                System.out.println();
                lineLen = 0;
                newLine = true;
            }
        }
        if(newLine) System.out.println();
    }
    public void printLSL(int type) {
        int lnSize = -1, lnUID = -1, lnGID = -1, lnLnk = -1;
        for(int i = 0; i < entries.length; i++) {
            lnSize = Math.max(String.valueOf(entries[i].getInode().getSize()).length(), lnSize);
            lnUID = Math.max(entries[i].getInode().getUserID() == 0 ? "root".length() : "scc211".length(), lnUID);
            lnGID = Math.max(entries[i].getInode().getGroupID() == 0 ? "root".length() : "users".length(), lnGID);
            lnLnk = Math.max(String.valueOf(entries[i].getInode().getLinks()).length(), lnLnk);
        }

        for(int i = 0; i < entries.length; i++) {
            if(entries[i].getName().contains(".") && type != 2) continue;
            String size = "", uid = "", gid = "", lnk = "";
            Inode cur = entries[i].getInode();
            for(int j = 0; j < (lnSize - String.valueOf(cur.getSize()).length()); j++) {
                size += " ";
            }
            for(int j = 0; j < (lnUID - (entries[i].getInode().getUserID() == 0 ? "root".length() : "scc211".length())); j++) {
                uid += " ";
            }
            for(int j = 0; j < (lnGID - (entries[i].getInode().getUserID() == 0 ? "root".length() : "users".length())); j++) {
                gid += " ";
            }
            for(int j = 0; j < (lnLnk - String.valueOf(cur.getLinks()).length()); j++) {
                lnk += " ";
            }
            System.out.println(cur.getPermissionString() + " " + lnk + cur.getLinks() + " " + (cur.getUserID() == 0 ? "root" : "scc211") + uid +
                    " " + (cur.getGroupID() == 0 ? "root" : "users") + gid + " " +  size + cur.getSize() + " " + Helper.toDate(cur.getModificationTime() * 1000L) + " " + entries[i].getName());
        }
    }
}
