package com.bulmanator.ext2.Structure;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Ext2File {

    /** Represents that the file was found */
    public static final int FOUND = 0;
    /** Represents that the file was not found */
    public static final int NOT_FOUND = 1;
    /** Represents that the file was found but was not a file */
    public static final int DIR = 2;

    private Volume volume;
    private Inode inode;
    /** The size of the file */
    public final long size;
    private long position;
    private int found;

    /**
     * Creates a single file from the path given
     * @param volume The volume which the file is on
     * @param path The path to the file
     */
    public Ext2File(Volume volume, String path) {
        this.volume = volume;

        // Splits the path into it directory counterparts
        String[] directories = path.startsWith("/") ? path.substring(1).split("/") : path.split("/");
        Directory curDir = volume.getCurrentDir();

        int curPath = 0;
        boolean fileFound = false, treeEnd = false;
        // Loops through the entire tree structure to look for the file
        while(!fileFound && !treeEnd) {
            for (int i = 0; i < curDir.getEntryCount(); i++) {
                if(directories[curPath].equals(curDir.getEntry(i).getName())) {
                    curPath++;
                    if(curPath == directories.length) {
                        fileFound = true;
                        inode = curDir.getEntry(i).getInode();
                        if(!inode.isFile()) {
                            // If found but not a file, set error and return
                            found = DIR;
                            size = -1;
                            return;
                        }
                    }
                    else {
                        // Moves onto the next directory
                        curDir = new Directory(volume, curDir.getEntry(i).getInode());
                    }
                    break;
                }

                treeEnd = (i == curDir.getEntryCount() - 1);
            }
        }

        // If it gets to the end of the tree then it was not found
        if(treeEnd) { found = NOT_FOUND; }
        else { found = FOUND; }

        position = 0;

        // Size is -1 if not found otherwise the same as the inode size
        if(found == FOUND) { size = inode.getSize(); }
        else { size = -1; }
    }

    /**
     * Moves the position to the one specified, legal to move the position outside of the file
     * @param position The new position
     */
    public void seek(long position) { this.position = position; }

    /**
     * Reads a byte array from the position specified in the file to, at most, length in bytes
     * @param start The starting position within the file, must be 0 <= start < file.size
     * @param length The length in bytes to read
     * @return The data read in a byte array
     */
    public byte[] read(long start, long length) {
        seek(start);
        return read(length);
    }

    /**
     * Reads the length amount of data into a byte array from the current position in the file
     * @param length The length (in bytes) to read
     * @return The data read in a byte array
     */
    public byte[] read(long length) {
        try {
            if (found != FOUND)
                throw new FileNotFoundException("Error: Non-existent file!");
            else if(position >= size)
                throw new IOException("Error: Cannot read from outside of the file bounds (" + position + " >= " + size + ")");
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }


        // Correct the length incase it is too long
        long corrected = (inode.getSize() - position < length) ? (inode.getSize() - position) : length;
        corrected = corrected > size ? size : corrected;

        // Read from the volume
        byte[] data = volume.read(inode, position, corrected);
        position += corrected;
        return data;
    }

    /**
     * Gets the current position within the file
     * @return The current position
     */
    public long getPosition() { return position; }

    /**
     * Gets whether or not the file was found
     * @return 0 for found, 1 for not found and 2 for found but wasn't a regular file
     */
    public int getFound() { return found; }

    /**
     * Gets the inode associated to the file
     * @return The inode
     */
    public Inode getInode() { return inode; }
}