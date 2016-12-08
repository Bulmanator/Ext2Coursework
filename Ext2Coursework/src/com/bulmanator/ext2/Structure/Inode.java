package com.bulmanator.ext2.Structure;

import com.bulmanator.ext2.Utils.Helper;
import com.bulmanator.ext2.Utils.Permissions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Inode {

    // Ownership
    private short permissions;
    private short userID;
    private short groupID;

    // Misc
    private int index;
    private long size;
    private int creationTime;
    private int modificationTime;
    private int accessTime;

    // Data
    private short links;
    private int[] directBlocks;
    private int indirectBlock;
    private int doubleIndirectBlock;
    private int tripleIndirectBlock;

    public Inode(Volume volume, long position) {

        ByteBuffer buffer = ByteBuffer.wrap(volume.read(position, volume.INODE_SIZE));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        permissions = buffer.getShort(0);
        userID = buffer.getShort(2);
        groupID = buffer.getShort(24);

        size = (((long)buffer.getInt(108) << 32) | ((long)buffer.getInt(4) & 0xFFFFFFFFL));

        creationTime = buffer.getInt(12);
        modificationTime = buffer.getInt(16);
        accessTime = buffer.getInt(8);

        links = buffer.getShort(26);
        directBlocks = new int[12];
        for(int i = 0; i < 12; i++) {
            directBlocks[i] = buffer.getInt(40 + (i * 4));
        }

        indirectBlock = buffer.getInt(88);
        doubleIndirectBlock = buffer.getInt(92);
        tripleIndirectBlock = buffer.getInt(96);
    }

    public void printInodeData() {

        System.out.println("[Inode Data]");
        System.out.println("-- Ownership");
        System.out.printf("   - Permissions: %s (0x%02x)\n", getPermissionString(), permissions);
        System.out.println("   - User ID: " + userID + " (" + (userID == 0 ? "root" : userID == 1000 ? "asc" : "unknown") + ")");
        System.out.println("   - Group ID: " + groupID + " (" + (groupID == 0 ? "root" : groupID == 1000 ? "staff" : "unknown") + ")");
        System.out.println("-- Misc");
        System.out.println("   - Size (Bytes): " + size);
        System.out.println("   - Creation Time: " + Helper.toDate(creationTime * 1000L) + " (" + creationTime + ")");
        System.out.println("   - Last Modified: " + Helper.toDate(modificationTime * 1000L) + " (" + modificationTime + ")");
        System.out.println("   - Last Accessed: " + Helper.toDate(accessTime * 1000L) + " (" + accessTime + ")");
        System.out.println("-- Data");
        System.out.println("   - Hard Links: " + links);
        System.out.println("-- Direct Pointers: ");
        for(int i = 0; i < directBlocks.length; i++) {
            System.out.printf("   - DP%d: 0x%02x\n", i, directBlocks[i]);
        }
        System.out.printf(" - Single Indirect Pointer: 0x%02x\n", indirectBlock);
        System.out.printf(" - Double Indirect Pointer: 0x%02x\n", doubleIndirectBlock);
        System.out.printf(" - Triple Indirect Pointer: 0x%02x\n", tripleIndirectBlock);
        System.out.println();
    }

    public String getPermissionString() {
        String per = "";

        for(int i = 0; i < Permissions.PERMISSION_STRINGS.length; i++) {
            if((permissions & Permissions.PERMISSIONS[i]) == Permissions.PERMISSIONS[i]) {
                per += Permissions.PERMISSION_STRINGS[i];
            }
            else if(i > 9) {
                per += "-";
            }
        }
        return per;
    }

    public boolean isDirectory() { return (permissions & Permissions.DIR) == Permissions.DIR; }
    public boolean isFile() { return (permissions & Permissions.FILE) == Permissions.FILE; }

    public int getPermissions() { return permissions; }
    public int getUserID() { return userID; }
    public int getGroupID() { return groupID; }

    public long getSize() { return size; }

    public int getCreationTime() { return creationTime; }
    public int getModificationTime() { return modificationTime; }
    public int getAccessTime() { return accessTime; }

    public int getLinks() { return links; }
    public int[] getDirectBlocks() { return directBlocks; }
    public int getIndirectBlock() { return indirectBlock; }
    public int getDoubleIndirectBlock() { return doubleIndirectBlock; }

    public int getTripleIndirectBlock() { return tripleIndirectBlock; }
}