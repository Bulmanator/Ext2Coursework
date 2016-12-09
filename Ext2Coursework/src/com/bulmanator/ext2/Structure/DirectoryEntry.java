package com.bulmanator.ext2.Structure;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectoryEntry {

    private int inodeIndex;
    private Inode inode;
    private int length;
    private int type;
    private String name;

    /**
     * Creates a single directory entry
     * @param volume The volume used to read
     * @param position The start position within the volume to read from
     */
    public DirectoryEntry(Volume volume, long position) {
        ByteBuffer buffer = ByteBuffer.wrap(volume.read(position, volume.BLOCK_SIZE)).order(ByteOrder.LITTLE_ENDIAN);

        inode = volume.getInode(inodeIndex = buffer.getInt(0));
        length = buffer.getShort(4);
        type = buffer.get(7);
        name = new String(volume.read(position + 8, buffer.get(6)));
    }

    /**
     * Gets the inode index
     * @return The inode index
     */
    public int getInodeIndex() { return inodeIndex;}

    /**
     * Gets the inode associated with the entry
     * @return The Inode
     */
    public Inode getInode() { return inode; }

    /**
     * Gets the length of the entry
     * @return The length
     */
    public int getLength() { return length; }

    /**
     * Gets the Length of the entry name
     * @return The name length
     */
    public int getNameLength() { return name.length(); }

    /**
     * Gets the entry type
     * @return The type
     */
    public int getType() { return type; }

    /**
     * Gets the entry name
     * @return The name of this entry
     */
    public String getName() { return name; }
}
