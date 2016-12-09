package com.bulmanator.ext2.Structure;

import java.util.ArrayList;

public class Directory {

    /** Represents that the directory was found */
    public static final int FOUND = 0;
    /** Represents that the directory was not found */
    public static final int NOT_FOUND = 1;
    /** Represents that the directory was found but was not a directory */
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

        // Split the path into its directory counter parts
        String[] directories = path.startsWith("/") ? path.substring(1).split("/") : path.split("/");

        // Load the current directory from the volume
        Directory curDir = volume.getCurrentDir();

        int curPath = 0;
        boolean fileFound = false, treeEnd = false;
        // Loop through all of the directory entries in the current directory until it finds
        // the entry it's looking for or reaches the end of the tree
        while(!fileFound && !treeEnd) {
            for (int i = 0; i < curDir.getEntryCount(); i++) {
                if(directories[curPath].equals(curDir.getEntry(i).getName())) {
                    curPath++;
                    if(curPath == directories.length) {
                        fileFound = true;
                        inode = curDir.getEntry(i).getInode();
                        // If it finds it but is not a directory
                        // Set the foundType to a error value
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

        // Directory found get all of the entries associated
        if(fileFound) {
            initDirEntries(volume);
            // Set the foundType to a non error bit
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
     * @param volume The volume where the directory is located to read data from
     */
    private void initDirEntries(Volume volume) {
        ArrayList<DirectoryEntry> tmp = new ArrayList<>();

        int overallOffset = 0, currentOffset = 0;
        int curPointer = 0, ptrCount = 0;
        while (curPointer == 0) {
            curPointer = volume.getBlockPointer(inode, (ptrCount * volume.BLOCK_COUNT));
            ptrCount++;
        }
        // Loops until it loads all of the data
        // Inode size will give the amount of data needed to be loaded
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

        // Converts the temporary ArrayList to a fixed array
        entries = new DirectoryEntry[tmp.size()];
        tmp.toArray(entries);
    }

    /**
     * If the directory was found or not, used by the change directory (cd) command
     * @return 0 for found, 1 for not found and 2 for found but is a file
     * @see com.bulmanator.ext2.Terminal.Commands.ChangeDir
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
