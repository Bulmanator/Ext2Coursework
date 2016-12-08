package com.bulmanator.ext2.Structure;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectoryEntry {

    private int inodeIndex;
    private Inode inode;
    private int length;
    private int nameLength;
    private int type;
    private String name;

    public DirectoryEntry(Volume volume, long position) {
        ByteBuffer buffer = ByteBuffer.wrap(volume.read(position, volume.BLOCK_SIZE)).order(ByteOrder.LITTLE_ENDIAN);

        inode = volume.getInode(inodeIndex = buffer.getInt(0));
        length = buffer.getShort(4);
        nameLength = buffer.get(6);
        type = buffer.get(7);
        name = new String(volume.read(position + 8, nameLength));
    }

    public int getInodeIndex() { return inodeIndex;}
    public Inode getInode() { return inode; }
    public int getLength() { return length; }
    public int getNameLength() { return nameLength; }
    public int getType() { return type; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "[Name: " + name + ", Inode: " + inode + ", Length: " + length + ", Type: " + type + "]";
    }
}
