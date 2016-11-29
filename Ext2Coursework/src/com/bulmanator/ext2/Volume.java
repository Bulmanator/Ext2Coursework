package com.bulmanator.ext2;

import com.bulmanator.ext2.Utils.Helper;

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

    public void seek(long position) {
        try { fileSystem.seek(position); }
        catch (IOException ex) { ex.printStackTrace(); }
    }

    public long getPosition() {
        try { return fileSystem.getFilePointer(); }
        catch (IOException ex) {
            ex.printStackTrace();
            return -1L;
        }
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
            return fileSystem.readByte();
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
