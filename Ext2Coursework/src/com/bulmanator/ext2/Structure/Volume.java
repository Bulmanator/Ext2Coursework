package com.bulmanator.ext2.Structure;

import com.bulmanator.ext2.Utils.Helper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Vector;

public class Volume {

    public static final int INTEGER = 4;
    public static final int SHORT = 2;
    public static final int BYTE = 1;

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
        BLOCK_SIZE = 1024 * (int)(Math.pow(2, readNum(1048L, INTEGER)));
        // Inode Size
        INODE_SIZE = readNum(BLOCK_SIZE + 88L, INTEGER);

        // Inode Count
        INODE_COUNT = readNum(BLOCK_SIZE, INTEGER);
        // Block Count
        BLOCK_COUNT = readNum(BLOCK_SIZE + 4L, INTEGER);

        // Blocks per Group
        BLOCKS_PER_GROUP = readNum(BLOCK_SIZE + 32L, INTEGER);
        // Inodes per Group
        INODES_PER_GROUP = readNum(BLOCK_SIZE + 40L, INTEGER);

        // Ext2 Signature
        SIGNATURE = readNum(BLOCK_SIZE + 56, INTEGER);

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
        return readNum(offset, 4) * BLOCK_SIZE;
    }

    public void seek(long position) {
        try { fileSystem.seek(position); }
        catch (IOException ex) { ex.printStackTrace(); }
    }

    private int[] getUsedPtrs(int indPtr, int level) {
        ArrayList<Integer> pointers = new ArrayList<>();
        if(level == 1) {
            for(int i = 0; i < BLOCK_SIZE / INTEGER; i++) {
                int ptr = readNum(indPtr * BLOCK_SIZE + (i * 4), INTEGER);
                if(ptr != 0) pointers.add(ptr);
            }
        }
        else {
            for(int i = 0; i < BLOCK_SIZE / INTEGER; i++) {
                int ptr = readNum(indPtr * BLOCK_SIZE + (i * 4), INTEGER);
                if(ptr != 0) {
                    int[] tmp = getUsedPtrs(ptr, level - 1);
                    for (int j = 0; j < tmp.length; j++) {
                        pointers.add(tmp[j]);
                    }
                }
            }
        }

        int[] ind = new int[pointers.size()];
        for(int i = 0; i < ind.length; i++) {
            ind[i] = pointers.get(i);
        }

        return ind;
    }
    public int[] getUsedPtrs(Inode inode) {
        ArrayList<Integer> pointers = new ArrayList<>();

        if(inode.getTripleIndirectBlock() != 0) {
            int[] trplPtrs = getUsedPtrs(inode.getTripleIndirectBlock(), 3);
            for(int i : trplPtrs) { pointers.add(i); }
        }

        if(inode.getDoubleIndirectBlock() != 0) {
            int[] dblPtrs = getUsedPtrs(inode.getDoubleIndirectBlock(), 2);
            for(int i : dblPtrs) { pointers.add(i); }
        }
        if(inode.getInderectBlock() != 0) {
            int[] snglPtrs = getUsedPtrs(inode.getInderectBlock(), 1);
            for(int i : snglPtrs) { pointers.add(i); }
        }

        for(int i = 0; i < 12; i++) {
            if(inode.getDirectBlocks()[i] != 0)
                pointers.add(inode.getDirectBlocks()[i]);
        }

        // Convert ArrayList to int array;
        int[] used = new int[pointers.size()];
        for(int i = 0; i < used.length; i++) {
            used[i] = pointers.get(i);
        }
        return used;
    }

    public int readNum(long start, int size) {
        int result = -1;
        try {
            fileSystem.seek(start);
            switch (size) {
                case BYTE:
                    result = fileSystem.readByte() & 0x000000FF;
                    break;
                case SHORT:
                    result = Short.reverseBytes(fileSystem.readShort()) & 0x0000FFFF;
                    break;
                case INTEGER:
                    result = Integer.reverseBytes(fileSystem.readInt());
                    break;
                default:
                    System.err.println("Error: Unknown Byte size \"" + size + "\"");
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }

        return result;
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
