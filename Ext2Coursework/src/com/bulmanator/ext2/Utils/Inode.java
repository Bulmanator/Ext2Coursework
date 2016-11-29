package com.bulmanator.ext2.Utils;

import com.bulmanator.ext2.Volume;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Inode {
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
    private int[] directBlocks;
    private int inderectBlock;
    private int doubleIndirectBlock;
    private int tripleIndirectBlock;

    public Inode(Volume volume, long position) {

        permissions = volume.readShort(position);
        userID = volume.readShort(position + 2);
        groupID = volume.readShort(position + 24);

        size = volume.readInt(position + 4) | volume.readInt(position + 108);
        creationTime = volume.readInt(position + 12);
        modificationTime = volume.readInt(position + 16);
        accessTime = volume.readInt(position + 8);

        directBlocks = new int[12];
        for(int i = 0; i < 12; i++) {
            directBlocks[i] = volume.readInt(position + 40L + (i * 4));
        }

        inderectBlock = volume.readInt(position + 88L);
        doubleIndirectBlock = volume.readInt(position + 92L);
        tripleIndirectBlock = volume.readInt(position + 96L);

        printPermissionsString();
        printInodeData();
        int tmpSize = size;
        int i = 0;
        while(directBlocks[i] != 0) {
            byte[] fileData1 = volume.read(directBlocks[i] * volume.BLOCK_SIZE, tmpSize > volume.BLOCK_SIZE ? volume.BLOCK_SIZE : tmpSize);
            tmpSize -= volume.BLOCK_SIZE;
            Helper.dumpHexBytes(fileData1);
            i++;
        }
    }

    public void printInodeData() {

        Date d = new Date(creationTime * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d hh:mm");

        System.out.println("[Inode Data]");
        System.out.println("-- Ownership");
        System.out.printf("   - Permissions: 0x%02x\n", permissions);
        System.out.println("   - User ID: " + userID);
        System.out.println("   - Group ID: " + groupID);
        System.out.println("-- Misc");
        System.out.println("   - Size (Bytes): " + size);
        System.out.println("   - Creation Time: " + sdf.format(d));
        d = new Date(modificationTime * 1000L);
        System.out.println("   - Last Modified: " + sdf.format(d));
        d = new Date(accessTime * 1000L);
        System.out.println("   - Last Accessed: " + sdf.format(d));
        System.out.println("-- Data");
        System.out.println("-- Direct Pointers: ");
        for(int i = 0; i < directBlocks.length; i++) {
            System.out.printf("   - DP%d: 0x%02x\n", i, directBlocks[i]);
        }
        System.out.printf(" - Single Indirect Pointer: 0x%02x\n", inderectBlock);
        System.out.printf(" - Double Indirect Pointer: 0x%02x\n", doubleIndirectBlock);
        System.out.printf(" - Triple Indirect Pointer: 0x%02x\n", tripleIndirectBlock);
        System.out.println();
    }

    public void printPermissionsString() {
        String per = "";

        int len = Permissions.PERMISSION_STRINGS.length;
        for(int i = 0; i < len; i++) {
            if((permissions & Permissions.PERMISSIONS[i]) == Permissions.PERMISSIONS[i]) {
                per += Permissions.PERMISSION_STRINGS[i];
            }
            else if(i > 6) {
                per += "-";
            }
        }


        System.out.println("Permissions: " + per);
    }
}