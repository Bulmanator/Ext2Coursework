package com.bulmanator.ext2.Structure;

import java.util.HashMap;
import java.util.Vector;

public class Directory {

    private HashMap<String, DirectoryEntry> entries;
    private Inode inode;

    public Directory(Volume v, Inode i) {
        Vector<DirectoryEntry> dir = new Vector<>();

        inode = i;
        if(!i.isDirectory()) {
            System.err.println("Directory: Not a directory");
            System.exit(-1);
        }

        int currentPtr = 0;
        int totalProcessed = 0;

    }
}
