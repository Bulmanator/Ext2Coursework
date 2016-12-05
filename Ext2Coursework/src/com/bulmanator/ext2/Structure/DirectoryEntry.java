package com.bulmanator.ext2.Structure;

public class DirectoryEntry {

    /** Unknown Directory Entry */
    public static final int UNKNOWN = 0;
    /** Regular File Directory Entry */
    public static final int FILE = 1;
    /** Directory Directory Entry */
    public static final int DIRECTORY = 2;
    /** Character Device Directory Entry */
    public static final int CHR_DEVICE = 3;
    public static final int BLK_DEVICE = 4;
    public static final int FIFO = 5;
    public static final int SOCKET = 6;
    public static final int SYM_LINK = 7;

    private int inodeIndex;
    private Inode inode;
    private int length;
    private int nameLength;
    private int type;
    private String name;

    public DirectoryEntry(Volume v, long position) {
        inode = v.getInode(inodeIndex = v.readNum(position, Volume.INTEGER));
        length = v.readNum(position + 4, Volume.SHORT);
        nameLength = v.readNum(position + 6, Volume.BYTE);
        type = v.readNum(position + 7, Volume.BYTE);
        name = new String(v.read(position + 8, nameLength));
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
