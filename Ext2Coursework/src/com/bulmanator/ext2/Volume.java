package com.bulmanator.ext2;

import com.bulmanator.ext2.Utils.Helper;
import com.bulmanator.ext2.Utils.Inode;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Volume {

    public final int INODE_COUNT;
    public final int BLOCK_COUNT;

    public final int BLOCK_SIZE;
    public final int INODE_SIZE;

    public final int BLOCKS_PER_GROUP;
    public final int INODES_PER_GROUP;

    public final int SIGNATURE;

    public final String VOLUME_NAME;

    private RandomAccessFile fileSystem;

   // private int inodeTablePtr;
    private Inode root;

    public Volume(String file) throws Exception {
        fileSystem = new RandomAccessFile(new File(file), "r");

        // Block Size
        BLOCK_SIZE = 1024 * (int)(Math.pow(2, readInt(1048L)));
        // Inode Size
        INODE_SIZE = readInt(BLOCK_SIZE + 88L);

        // Inode Count
        INODE_COUNT = readInt(BLOCK_SIZE);
        // Block Count
        BLOCK_COUNT = readInt(BLOCK_SIZE + 4L);

        // Blocks per Group
        BLOCKS_PER_GROUP = readInt(BLOCK_SIZE + 32L);
        // Inodes per Group
        INODES_PER_GROUP = readInt(BLOCK_SIZE + 40L);

        // Ext2 Signature
        SIGNATURE = readInt(BLOCK_SIZE + 56);

        // Volume Label
        VOLUME_NAME = new String(read(BLOCK_SIZE + 120L, 16));

        //inodeTablePtr = readInt(2056) * BLOCK_SIZE;
        root = new Inode(this, getInodeTablePtr(0) + INODE_SIZE);
    }

    public void printSuperblock() {
        System.out.println("[Volume Information]");

        System.out.println(" - Volume Label: " + VOLUME_NAME);

        System.out.println(" - Inode Count: " + INODE_COUNT);
        System.out.println(" - Block Count: " + BLOCK_COUNT);

        System.out.println(" - Block Size: " + BLOCK_SIZE);
        System.out.println(" - Inode Size: " + INODE_SIZE);

        System.out.println(" - Inodes Per Group: " + INODES_PER_GROUP);
        System.out.println(" - Blocks Per Group: " + BLOCKS_PER_GROUP);

        System.out.printf(" - Signature: 0x%02X\n\n", SIGNATURE);
    }
    public long getPosition() {
        try { return fileSystem.getFilePointer(); }
        catch (IOException ex) {
            ex.printStackTrace();
            return -1L;
        }
    }
    public Inode getRoot() { return root; }

    public Inode getInode(int index) {
        int offset = ((index - 1) % INODES_PER_GROUP) * INODE_SIZE;
        offset += getInodeTablePtr((index - 1) / INODES_PER_GROUP);

        return new Inode(this, offset);
    }

    public int getInodeTablePtr(int group) {
        int offset = BLOCK_SIZE + (32 * group);
        offset += BLOCK_SIZE;
        offset += 8;
        return readInt(offset) * BLOCK_SIZE;
    }

    public void seek(long position) {
        try { fileSystem.seek(position); }
        catch (IOException ex) { ex.printStackTrace(); }
    }

    public int readShort(long start) {
        try {
            fileSystem.seek(start);
            return  Short.reverseBytes(fileSystem.readShort()) & 0x0000FFFF;
        } catch(IOException ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    public int readInt(long start) {
        try {
            fileSystem.seek(start);
            return Integer.reverseBytes(fileSystem.readInt());
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public byte read(long start) {
        try {
            fileSystem.seek(start);
            byte b = fileSystem.readByte();
            return b;
        }
        catch(IOException ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    public byte[] read(long start, int length) {
        byte[] ret = new byte[length];
        try {
            fileSystem.seek(start);
            fileSystem.read(ret);
        }
        catch (Exception ex) { ex.printStackTrace(); }

        return ret;
    }

    public void close() {
        try {
            fileSystem.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

