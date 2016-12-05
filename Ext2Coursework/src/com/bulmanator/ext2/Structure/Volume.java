package com.bulmanator.ext2.Structure;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

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

    private int indSize;
    private RandomAccessFile fileSystem;

   // private int inodeTablePtr;
    private Directory root;

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

        root = new Directory(this, getInode(2));

        indSize = BLOCK_SIZE / INTEGER;
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
    public Directory getRoot() { return root; }

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

    /**
     * Pads the data with zeros from offset to length
     * @param data The byte array to add the zeros to
     * @param offset The starting position within the array to put zeros into
     * @param length The number of zeros to add
     */
    private void padZero(byte[] data, int offset, int length) {
        for(int i = 0; i < length; i++) {
            data[offset + i] = 0x00;
        }
    }


    public byte[] read(Inode inode, long start, long length) {
        byte[] data = new byte[(int)length];

        long offset = start % BLOCK_SIZE;

        int totalRead = (BLOCK_SIZE - (int) offset) > length ? (int)length : (BLOCK_SIZE - (int)offset);
        read(data, getBlockPointer(inode, start) + offset, 0, totalRead);
        length -= totalRead;

        int curIndex = 1;
        while (length > 0) {
            int curRead = length > BLOCK_SIZE ? BLOCK_SIZE : (int)length;

            int ptr = getBlockPointer(inode, start + (curIndex * BLOCK_SIZE));
            if(ptr != 0) System.out.printf("0x%02x\n", ptr);

            read(data, ptr,
                        (BLOCK_SIZE - (int) offset) + ((curIndex - 1) * BLOCK_SIZE), curRead);


            totalRead += curRead;
            length -= curRead;

            if(totalRead == BLOCK_SIZE) {
                totalRead = 0;
                curIndex++;
            }
        }

        return data;
    }

    private void read(byte[] b, long start, int offset, int len) {
        try {
            if(start == 0) {
                padZero(b, offset, len);
            }
            else {
                fileSystem.seek(start);
                fileSystem.read(b, offset, len);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Failed to read!");
            System.exit(-1);
        }
    }

    /*public byte[] getData(Inode inode, long startByte, long size) {
        byte[] data = new byte[(int)size];

        long tmp = size % BLOCK_SIZE;
        int aligned = (int) tmp;
        size -= aligned;

        tmp = (size + aligned) / BLOCK_SIZE;
        int blockCount = (int)tmp;

        try {
            int i;
            for (i = 0; i < blockCount; i++) {
                boolean pad = true;
                int ptr = getBlockPointer(inode, i * BLOCK_SIZE);
                if(ptr != 0) {
                    fileSystem.seek(ptr);
                    fileSystem.read(data, (i * BLOCK_SIZE), BLOCK_SIZE);
                    pad = false;
                }

                if(pad) padZero(data, (i * BLOCK_SIZE), BLOCK_SIZE);
            }

            if(aligned != 0) {
                int ptr = getBlockPointer(inode, i * BLOCK_SIZE);
                if(ptr != 0) {
                    fileSystem.seek(ptr);
                    fileSystem.read(data, (i * BLOCK_SIZE), aligned);
                }
                else {
                    padZero(data, (i * BLOCK_SIZE), aligned);
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        return data;
    }*/

    public int getBlockPointer(Inode inode, long position) {
        int ptr;

        int align = (int)(position % BLOCK_SIZE);
        long tmp = (position - align) / BLOCK_SIZE;
        int index = (int) tmp;

       // System.out.println("Position: " + position + " Index: " + index);

        if(index < 0) {
            throw new IllegalArgumentException("Error: Index cannot be < 0");
        }

        if(index < 12) {
            if(inode.getDirectBlocks()[index] == 0) return 0;
            ptr = inode.getDirectBlocks()[index] * BLOCK_SIZE;
        }
        else if((index - 12) < indSize) {
            if(inode.getIndirectBlock() == 0) return 0;
            ptr = readNum(inode.getIndirectBlock() * BLOCK_SIZE + ((index - 12) * INTEGER), INTEGER) * BLOCK_SIZE;
        }
        else if((index - indSize - 12) < (indSize * indSize)) {
            if(inode.getDoubleIndirectBlock() == 0) return 0;
            index = index - indSize - 12;

            int dblIndex = index / indSize;
            int snglBlkPtr = readNum(inode.getDoubleIndirectBlock() * BLOCK_SIZE + (dblIndex * INTEGER), INTEGER) * BLOCK_SIZE;
            ptr = readNum(snglBlkPtr + ((index % indSize) * INTEGER), INTEGER) * BLOCK_SIZE;

            System.out.println("Position: " + position + " -> Double: " + (inode.getDoubleIndirectBlock() * BLOCK_SIZE + (dblIndex * INTEGER))
                    + " -> Single: " + (snglBlkPtr + ((index % indSize) * INTEGER)) + " -> Data: " + ptr);
        }
        else if((index - (indSize * indSize) - indSize - 12) < (indSize * indSize * indSize)) {
            if(inode.getTripleIndirectBlock() == 0) return 0;
            index = index - (indSize * indSize) - indSize - 12;

            int trplIndex = index / (indSize * indSize);
            int dblBlkPtr = readNum(inode.getTripleIndirectBlock() * BLOCK_SIZE + (trplIndex * INTEGER), INTEGER) * BLOCK_SIZE;

            int dblIndex = (index % (indSize * indSize)) % indSize;
            int snglBlkPtr = readNum(dblBlkPtr + (dblIndex * INTEGER), INTEGER) * BLOCK_SIZE;

            int dataIndex = (index % indSize);

            ptr = readNum(snglBlkPtr + (dataIndex * INTEGER), INTEGER) * BLOCK_SIZE;
            System.out.println("Position: " + position + " -> Triple: " + (inode.getTripleIndirectBlock() * BLOCK_SIZE + (trplIndex * INTEGER))
                    + " -> Double: " + (dblBlkPtr + (dblIndex * INTEGER)) + " -> Single: " + (snglBlkPtr + (dataIndex * INTEGER)) + " -> Data: " + ptr);
        }
        else {
            throw new OutOfMemoryError("Error: File size too big!");
        }
        return ptr;
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

