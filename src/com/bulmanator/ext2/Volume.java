package com.bulmanator.ext2;

import com.bulmanator.ext2.Utils.Helper;

import java.io.File;
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

        // Block Size -- Has to be done differently because
        // readInt() depends on BLOCK_SIZE
        byte[] b = new byte[4];
        fileSystem.seek(1048);
        fileSystem.read(b);
        BLOCK_SIZE = 1024 * (int)Math.pow(2, Helper.intFromBytes(b));
        // Inode Size
        INODE_SIZE = readInt(4, 1, 88);

        // Inode Count
        INODE_COUNT = readInt(4, 1, 0);
        // Block Count
        BLOCK_COUNT = readInt(4, 1, 4);

        // Blocks per Group
        BLOCKS_PER_GROUP = readInt(4, 1, 32);
        // Inodes per Group
        INODES_PER_GROUP = readInt(4, 1, 40);

        SIGNATURE = readInt(2, 1, 56);

        VOLUME_NAME = new String(read(16, 1, 120));
    }

    public RandomAccessFile getRandomAccess() { return fileSystem; }

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

    public byte[] readBlock(int block) {
        byte[] data = new byte[BLOCK_SIZE];
        try {
            int offset = BLOCK_SIZE * block;
            fileSystem.seek(offset);
            fileSystem.read(data, 0, BLOCK_SIZE);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        return data;
    }
    public byte[] readRootInode(int block) {
        byte[] ret = new byte[INODE_SIZE];
        try {
            fileSystem.seek(BLOCK_SIZE * block + INODE_SIZE);
            fileSystem.read(ret);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }
    public byte[] read(int nBytes, int blockNumber, int offset) {
        byte[] ret = new byte[nBytes];
        try {
            fileSystem.seek(BLOCK_SIZE * blockNumber + offset);
            fileSystem.read(ret);
        }
        catch (Exception ex) { ex.printStackTrace(); }

        return ret;
    }
    public int readInt(int nBytes, int blockNumber, int offset) {
        int value = 0;
        try {
            byte[] b = new byte[nBytes];

            fileSystem.seek(BLOCK_SIZE * blockNumber + offset);
            fileSystem.read(b);

            value = Helper.intFromBytes(b);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return value;
    }
}
