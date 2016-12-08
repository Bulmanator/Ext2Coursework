package com.bulmanator.ext2.Structure;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    public final short SIGNATURE;

    public final String VOLUME_NAME;

    private int indSize;
    private RandomAccessFile fileSystem;

    private Directory current;

    public Volume(String file) throws Exception {
        fileSystem = new RandomAccessFile(new File(file), "r");

        fileSystem.seek(1024);
        byte[] data = new byte[1024];
        fileSystem.read(data);

        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.nativeOrder());

        // Block Size
        BLOCK_SIZE = 1024 * (int)(Math.pow(2, buf.getInt(24)));
        // Inode Size
        INODE_SIZE = buf.getInt(88);

        // Inode Count
        INODE_COUNT = buf.getInt(0);
        // Block Count
        BLOCK_COUNT = buf.getInt(4);

        // Blocks per Group
        BLOCKS_PER_GROUP = buf.getInt(32);
        // Inodes per Group
        INODES_PER_GROUP = buf.getInt(40);

        // Ext2 Signature
        SIGNATURE = buf.getShort(56);

        // Volume Label
        byte[] buffer = new byte[16];
        buf.position(120);
        buf.get(buffer);
        VOLUME_NAME = new String(buffer);

        current = new Directory(this, getInode(2));

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

    public Inode getInode(int index) {
        int offset = ((index - 1) % INODES_PER_GROUP) * INODE_SIZE;
        offset += getInodeTablePtr((index - 1) / INODES_PER_GROUP);

        return new Inode(this, offset);
    }
    public int getInodeTablePtr(int group) {
        int offset = 2 * BLOCK_SIZE + (32 * group);
        offset += 8;
        return ByteBuffer.wrap(read(offset, 4)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;
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

        int ptr = getBlockPointer(inode, start) + (int)offset;
        if(ptr == 0) {
            padZero(data, 0, totalRead);
        }
        else {
            try {
                fileSystem.seek(ptr);
                fileSystem.read(data, 0, totalRead);
            }
            catch (IOException ex) { ex.printStackTrace(); }
        }

        length -= totalRead;

        int curIndex = 1;
        while (length > 0) {
            int curRead = length > BLOCK_SIZE ? BLOCK_SIZE : (int)length;

            ptr = getBlockPointer(inode, start + (curIndex * BLOCK_SIZE));

            if(ptr == 0) {
                padZero(data, (BLOCK_SIZE - (int) offset) + ((curIndex - 1) * BLOCK_SIZE), curRead);
            }
            else {
                try {
                    fileSystem.seek(ptr);
                    fileSystem.read(data, (BLOCK_SIZE - (int) offset) + ((curIndex - 1) * BLOCK_SIZE), curRead);
                }
                catch (IOException ex) { ex.printStackTrace(); }
            }

            totalRead += curRead;
            length -= curRead;

            if(totalRead == BLOCK_SIZE) {
                totalRead = 0;
                curIndex++;
            }
        }

        return data;
    }

    /**
     * This will convert an inode and a position within a file into the correct data pointer whether or not it is indirect
     * @param inode The inode which contains the data pointers
     * @param position The position within the file
     * @return The pointer
     */
    public int getBlockPointer(Inode inode, long position) {
        int ptr;

        // This will align the position in the file to the beginning of its block
        int align = (int)(position % BLOCK_SIZE);
        long tmp = (position - align) / BLOCK_SIZE;
        int index = (int) tmp;

        if(index < 0) {
            // Index cannot less than 0 so throw exception
            throw new IllegalArgumentException("Error: Index cannot be < 0");
        }

        // If the index is less than 12 then use the direct block pointers
        if(index < 12) {
            if(inode.getDirectBlocks()[index] == 0) return 0;
            ptr = inode.getDirectBlocks()[index] * BLOCK_SIZE;
        }
        // If the index is [12, 267) then its in the indirect block pointer
        // Find the index in the indirect block and return the data pointer
        else if((index - 12) < indSize) {
            if(inode.getIndirectBlock() == 0) return 0;
            ptr = ByteBuffer.wrap(read(inode.getIndirectBlock() * BLOCK_SIZE
                    + ((index - 12) * INTEGER), INTEGER)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;
        }
        // If the index is [267, 65535) then it is using the double indirect block pointer
        // Find the correct indexes for both layers of indirection and return the data pointer
        else if((index - indSize - 12) < (indSize * indSize)) {
            if(inode.getDoubleIndirectBlock() == 0) return 0;
            index = index - indSize - 12;

            int dblIndex = index / indSize;
            int snglBlkPtr = ByteBuffer.wrap(read(inode.getDoubleIndirectBlock()
                    * BLOCK_SIZE + (dblIndex * INTEGER), INTEGER)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;

            ptr = ByteBuffer.wrap(read(snglBlkPtr + ((index % indSize) * INTEGER),
                    INTEGER)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;
        }
        // If the index is [65536, 16777216)
        else if((index - (indSize * indSize) - indSize - 12) < (indSize * indSize * indSize)) {
            if(inode.getTripleIndirectBlock() == 0) return 0;
            index = index - (indSize * indSize) - indSize - 12;

            int trplIndex = index / (indSize * indSize);
            int dblBlkPtr = ByteBuffer.wrap(read(inode.getTripleIndirectBlock()
                    * BLOCK_SIZE + (trplIndex * INTEGER), INTEGER)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;

            int dblIndex = (index / indSize) % indSize;
            int snglBlkPtr = ByteBuffer.wrap(read(dblBlkPtr +
                    (dblIndex * INTEGER), INTEGER)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;

            int dataIndex = (index % indSize);

            ptr = ByteBuffer.wrap(read(snglBlkPtr + (dataIndex * INTEGER),
                    INTEGER)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;
        }
        else {
            throw new OutOfMemoryError("Error: File size too big!");
        }
        return ptr;
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

    public Directory getCurrentDir() { return current; }
    public void setCurrentDir(Directory dir) {
        if(dir.getFoundType() != 0) return;
        current = dir;
    }

    public void close() {
        try { fileSystem.close(); }
        catch (IOException ex) { ex.printStackTrace(); }
    }
}

