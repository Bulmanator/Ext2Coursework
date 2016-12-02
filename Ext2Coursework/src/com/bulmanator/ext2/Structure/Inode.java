package com.bulmanator.ext2.Structure;

import com.bulmanator.ext2.Utils.Helper;
import com.bulmanator.ext2.Utils.Permissions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Inode {

    // The Volume on which this Inode is stored
    private Volume volume;

    // Ownership
    private int permissions;
    private int userID;
    private int groupID;

    // Misc
    private int size;
    private int creationTime;
    private int modificationTime;
    private int accessTime;

    // Data
    private int links;
    private int[] directBlocks;
    private int inderectBlock;
    private int doubleIndirectBlock;
    private int tripleIndirectBlock;

    public Inode(Volume volume, long position) {

        this.volume = volume;

        permissions = volume.readNum(position, Volume.SHORT);
        userID = volume.readNum(position + 2, Volume.SHORT);
        groupID = volume.readNum(position + 24, Volume.SHORT);

        size = volume.readNum(position + 4, Volume.INTEGER) | volume.readNum(position + 108, Volume.INTEGER);
        creationTime = volume.readNum(position + 12, Volume.INTEGER);
        modificationTime = volume.readNum(position + 16, Volume.INTEGER);
        accessTime = volume.readNum(position + 8, Volume.INTEGER);

        links = volume.readNum(position + 26, Volume.SHORT);
        directBlocks = new int[12];
        for(int i = 0; i < 12; i++) {
            directBlocks[i] = volume.readNum(position + 40L + (i * 4), Volume.INTEGER);
        }

        inderectBlock = volume.readNum(position + 88L, Volume.INTEGER);
        doubleIndirectBlock = volume.readNum(position + 92L, Volume.INTEGER);
        tripleIndirectBlock = volume.readNum(position + 96L, Volume.INTEGER);
    }

    public void getUsedPointers(Volume volume) {


        /* int tmpSize = size;
        int i = 0;
        byte[] data = new byte[size];
        while(directBlocks[i] != 0) {
            byte[] fileData1 = volume.read(directBlocks[i] * volume.BLOCK_SIZE, tmpSize > volume.BLOCK_SIZE ? volume.BLOCK_SIZE : tmpSize);
            tmpSize -= volume.BLOCK_SIZE;
            Helper.dumpHexBytes(fileData1);
            i++;
        }*/
    }

    public void printInodeData() {

        System.out.println("[Inode Data]");
        System.out.println("-- Ownership");
        System.out.printf("   - Permissions: %s (0x%02x)\n", getPermissionString(), permissions);
        System.out.println("   - User ID: " + userID + " (" + (userID == 0 ? "root" : userID == 1000 ? "asc" : "unknown") + ")");
        System.out.println("   - Group ID: " + groupID + " (" + (groupID == 0 ? "root" : groupID == 1000 ? "staff" : "unknown") + ")");
        System.out.println("-- Misc");
        System.out.println("   - Size (Bytes): " + size);
        System.out.println("   - Creation Time: " + Helper.toDate(creationTime * 1000L));
        System.out.println("   - Last Modified: " + Helper.toDate(modificationTime * 1000L));
        System.out.println("   - Last Accessed: " + Helper.toDate(accessTime * 1000L));
        System.out.println("-- Data");
        System.out.println("   - Hard Links: " + links);
        System.out.println("-- Direct Pointers: ");
        for(int i = 0; i < directBlocks.length; i++) {
            System.out.printf("   - DP%d: 0x%02x\n", i, directBlocks[i]);
        }
        System.out.printf(" - Single Indirect Pointer: 0x%02x\n", inderectBlock);
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

    public int getSize() { return size; }
    public int getCreationTime() { return creationTime; }
    public int getModificationTime() { return modificationTime; }
    public int getAccessTime() { return accessTime; }

    public int getLinks() { return links; }
    public int[] getDirectBlocks() { return directBlocks; }
    public int getInderectBlock() { return inderectBlock; }
    public int getDoubleIndirectBlock() { return doubleIndirectBlock; }

    public int getTripleIndirectBlock() { return tripleIndirectBlock; }
}