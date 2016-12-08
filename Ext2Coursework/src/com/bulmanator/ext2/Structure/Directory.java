package com.bulmanator.ext2.Structure;

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

        // If the path is root then load the 2nd Inode
        if(path.equals("/")) {
            inode = volume.getInode(2);
            foundType = 0;
            initDirEntries(volume);
            return;
        }

        String[] directories = path.startsWith("/") ? path.substring(1).split("/") : path.split("/");

        Directory curDir = volume.getCurrentDir();

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
                           foundType = FILE;
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
            foundType = FOUND;
        }
        else {
            inode = null;
            foundType = NOT_FOUND;
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
            foundType = FILE;
            return;
        }

        initDirEntries(volume);
    }

    /**
     * Initialises all of the directory entries for the specified Inode
     * @param volume
     */
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

    /**
     * Gets the inode corresponding to the directory
     * @return The inode
     */
    public Inode getInode() { return inode; }

    /**
     * Gets a single directory entry from the directory
     * @param index The index corresponding to the entry needed
     * @return The entry referenced
     */
    public DirectoryEntry getEntry(int index) { return entries[index]; }

    /**
     * Gets the number of directory entries for the directory
     * @return The number of entries
     */
    public int getEntryCount() { return entries.length; }
}
