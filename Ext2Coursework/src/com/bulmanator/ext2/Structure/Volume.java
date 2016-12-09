package com.bulmanator.ext2.Structure;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Volume {

    /** The total number of inodes */
    public final int INODE_COUNT;
    /** The total number of blocks */
    public final int BLOCK_COUNT;

    /** The size of one inode */
    public final int INODE_SIZE;
    /** The size of one block */
    public final int BLOCK_SIZE;

    /** The number of inodes per group */
    public final int INODES_PER_GROUP;
    /** The number of blocks per group */
    public final int BLOCKS_PER_GROUP;

    /** The Signature ('Magic number') of Ext2, should equal 0xEF53 */
    public final short SIGNATURE;

    /** The name of the volume */
    public final String VOLUME_NAME;

    private int indirectSize;
    private RandomAccessFile fileSystem;
    private Directory current;

    /**
     * Loads a entire volume
     * @param file The file location of the volume
     * @throws IOException If the file fails to load the file or fails to read the Super Block
     */
    public Volume(String file) throws IOException {
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

        if(SIGNATURE != (short)0xEF53)
            throw new IOException(String.format("Error: Signature not defined correctly, expected: 0xEF53 got 0x%02X", SIGNATURE));

        // Volume Label
        byte[] buffer = new byte[16];
        buf.position(120);
        buf.get(buffer);
        VOLUME_NAME = new String(buffer);

        current = new Directory(this, getInode(2));

        indirectSize = BLOCK_SIZE / 4;
    }

    /**
     * Prints the super block information
     */
    public void printSuperblock() {
        System.out.println("[Volume Information]");

        System.out.println(" - Volume Label: " + VOLUME_NAME);

        System.out.println(" - Inode Count: " + INODE_COUNT);
        System.out.println(" - Block Count: " + BLOCK_COUNT);

        System.out.println(" - Block Size: " + BLOCK_SIZE);
        System.out.println(" - Inode Size: " + INODE_SIZE);

        System.out.println(" - Inodes Per Group: " + INODES_PER_GROUP);
        System.out.println(" - Blocks Per Group: " + BLOCKS_PER_GROUP);

        System.out.printf(" - Signature: 0x%02X\n", SIGNATURE);
    }

    /**
     * Prints a block group descriptor for a specific block group
     * @param groupNo The group number
     */
    public void printBlockGroupDescriptor(int groupNo) {
        int ptr = (2 * BLOCK_SIZE) + (32 * groupNo);
        ByteBuffer buf = ByteBuffer.wrap(read(ptr, 32)).order(ByteOrder.LITTLE_ENDIAN);

        System.out.println("[Block Group Descriptor: Group " + groupNo + "]");
        System.out.printf(" - Block Bitmap Pointer: 0x%02x\n", buf.getInt(0));
        System.out.printf(" - Inode Bitmap Pointer: 0x%02x\n", buf.getInt(4));
        System.out.printf(" - Inode Table Pointer: 0x%02x\n", buf.getInt(8));

        System.out.printf(" - Free Block Count: %d\n", buf.getShort(12));
        System.out.printf(" - Free Inode Count: %d\n", buf.getShort(14));
        System.out.printf(" - Used Directory Count: %d\n", buf.getShort(16));
    }

    /**
     * Gets an Inode from an index
     * @param index The index of the inode
     * @return The Inode
     * @see Inode
     */
    public Inode getInode(int index) {
        int offset = ((index - 1) % INODES_PER_GROUP) * INODE_SIZE;
        offset += getInodeTablePtr((index - 1) / INODES_PER_GROUP);

        return new Inode(this, offset);
    }

    /**
     * Gets the inode table pointer for a specific block group
     * @param group The group number
     * @return A pointer to the inode table pointer for the block group
     */
    public int getInodeTablePtr(int group) {
        int offset = 2 * BLOCK_SIZE + (32 * group);
        offset += 8;
        return ByteBuffer.wrap(read(offset, 4)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;
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

    /**
     * Will read data from the given inode, from the given position in the inode and length specified
     * @param inode The inode containing data pointers
     * @param start The starting position within the file
     * @param length The length to read
     * @return A byte array containing all of the data
     */
    public byte[] read(Inode inode, long start, long length) {

        if(length >= (Integer.MAX_VALUE - 5)) throw new OutOfMemoryError("Error: Cannot read as arrays can only hold " + (Integer.MAX_VALUE - 5));

        // Makes a byte array to store all of the data
        // Will truncate the long if it's size > Integer.MAX_VALUE because arrays physically cannot hold more
        byte[] data = new byte[(int)length];

        // Works out the offset from the block
        long offset = start % BLOCK_SIZE;

        // using the offset it will work out whether or not to read length or to the end of block
        int totalRead = (BLOCK_SIZE - (int) offset) > length ? (int)length : (BLOCK_SIZE - (int)offset);

        // Gets the initial block pointer and reads
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

        // Loops until all the data has been read
        // Will read one block at a time until there is less than a blocks worth of bytes left
        // When this is the case it will read any bytes left
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

            if(totalRead >= BLOCK_SIZE) {
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
        // If the index is [12, 268) then its in the indirect block pointer
        // Find the index in the indirect block and return the data pointer
        else if((index - 12) < indirectSize) {
            if(inode.getIndirectBlock() == 0) return 0;
            ptr = ByteBuffer.wrap(read(inode.getIndirectBlock() * BLOCK_SIZE
                    + ((index - 12) * 4), 4)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;
        }
        // If the index is [267, 65804) then it is using the double indirect block pointer
        // Find the correct indexes for both layers of indirection and return the data pointer
        else if((index - indirectSize - 12) < (indirectSize * indirectSize)) {
            if(inode.getDoubleIndirectBlock() == 0) return 0;
            // Align the index so its in the range 0 - 65535
            index = index - indirectSize - 12;

            // The index to get the single indirect block is given by (index / 256)
            int dblIndex = index / indirectSize;
            int snglBlkPtr = ByteBuffer.wrap(read(inode.getDoubleIndirectBlock()
                    * BLOCK_SIZE + (dblIndex * 4), 4)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;

            ptr = ByteBuffer.wrap(read(snglBlkPtr + ((index % indirectSize) * 4),
                    4)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;
        }
        // If the index is [65804, 16843020) then it is using the triple indirect block pointer
        // Find the three indexes and return the data pointer
        else if((index - (indirectSize * indirectSize) - indirectSize - 12) < (indirectSize * indirectSize * indirectSize)) {
            // 0 indicates no data so return 0 instead of searching
            if(inode.getTripleIndirectBlock() == 0) return 0;
            // Align the index so its in the range 0 - 16777215
            index = index - (indirectSize * indirectSize) - indirectSize - 12;

            // The index to get the double indirect block is given by index / (256 * 256)
            int trplIndex = index / (indirectSize * indirectSize);
            int dblBlkPtr = ByteBuffer.wrap(read(inode.getTripleIndirectBlock()
                    * BLOCK_SIZE + (trplIndex * 4), 4)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;

            // The index to get the single indirect block is given by (index / 256) % 256
            int dblIndex = (index / indirectSize) % indirectSize;
            int snglBlkPtr = ByteBuffer.wrap(read(dblBlkPtr +
                    (dblIndex * 4), 4)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;

            ptr = ByteBuffer.wrap(read(snglBlkPtr + ((index % indirectSize) * 4),
                    4)).order(ByteOrder.LITTLE_ENDIAN).getInt() * BLOCK_SIZE;
        }
        else {
            // At this point we have run out of block pointers so throw error
            throw new OutOfMemoryError("Error: File size too big!");
        }
        return ptr;
    }

    /**
     * Reads data for the specified length at the specified position into a byte array
     * @param start The start location within the file system (in bytes)
     * @param length The length to read (in bytes)
     * @return A byte array containing the data
     */
    public byte[] read(long start, int length) {
        byte[] ret = new byte[length];

        try {
            fileSystem.seek(start);
            fileSystem.read(ret);
        }
        catch (Exception ex) { ex.printStackTrace(); }

        return ret;
    }

    /**
     * Gets the directory which the volume is currently situated on
     * @return The current directory
     */
    public Directory getCurrentDir() { return current; }

    /**
     * Changes the current directory to the one given, will not change if the directory is not found
     * @param dir The new current directory
     */
    public void setCurrentDir(Directory dir) {
        if(dir.getFoundType() != 0) return;
        current = dir;
    }

    /**
     * Closes the file system safely
     */
    public void close() {
        try { fileSystem.close(); }
        catch (IOException ex) { ex.printStackTrace(); }
    }
}

