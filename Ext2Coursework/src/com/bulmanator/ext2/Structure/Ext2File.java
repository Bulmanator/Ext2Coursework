package com.bulmanator.ext2.Structure;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Ext2File {

    private Volume volume;
    private Inode inode;
    public final long size;

    private long position;

    private int found;

    public Ext2File(Volume volume, String path) {
        this.volume = volume;
        String[] directories = path.substring(1).split("/");
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
                        if(inode.isDirectory()) {
                            found = 2;
                            size = -1;
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

        if(treeEnd) {
            found = 1;
        }
        else  found = 0;

        position = 0;
        if(found == 0) {
            size = inode.getSize();
        }
        else {
            size = -1;
        }
    }

    public void seek(long position) { this.position = position; }

    public byte[] read(long start, long length) {
        seek(start);
        return read(length);
    }

    public byte[] read(long length) {
        try {
            if (found != 0)
                throw new FileNotFoundException("Error: Non-existent file!");
            else if(position >= size)
                throw new IOException("Error: Cannot read from outside of the file bounds (" + position + " >= " + size + ")");
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }


        long correct = (inode.getSize() - position < length) ? (inode.getSize() - position) : length;
        return volume.read(inode, position, correct);
    }

    public long getPosition() { return position; }

    public int getFound() { return found; }
    public Inode getInode() { return inode; }
}