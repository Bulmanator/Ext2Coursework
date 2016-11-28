package com.bulmanator.ext2;

import com.bulmanator.ext2.Utils.Helper;

import java.nio.ByteOrder;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }

    private void run() {
        Scanner s = new Scanner(System.in);
        Volume v = null;
        try {
            v = new Volume("resources/ext2fs");
        }
        catch (Exception e) {
            System.err.println("Error: Failed to create File System!");
            System.exit(-1);
        }

        v.printSuperblock();

        Ext2File file = new Ext2File(v, "/home/james/Documents/List.txt");

        int inodePtr = v.readInt(4, 2, 8);

        int blockNo = 2;
        byte[] b = v.readRootInode(inodePtr); //v.readBlock(blockNo);

        int tmp = v.readInt(2, inodePtr, 128);
        System.out.printf("Permissions: 0x%02x\n", tmp);

        tmp = v.readInt(2, inodePtr, 130);
        System.out.println("User ID: " + tmp);

        tmp = v.readInt(2, inodePtr, 132);
        System.out.println("Size: " + tmp);

        System.out.println();

        byte[] inodeTable = v.readBlock(inodePtr + 1);

        int rootPtr = v.readInt(4, inodePtr, 168);
        byte[] root = v.readBlock(rootPtr);

        int inode, length = 0, nameLength = 0, type = 0;
        String name;

        int overallOffset = 0;

        while (true) {
            inode = v.readInt(4, rootPtr, overallOffset);
            if(inode == 0) break;

            length = v.readInt(2, rootPtr, overallOffset + 4);
            nameLength = v.readInt(1, rootPtr, overallOffset + 6);
            type = v.readInt(1, rootPtr, overallOffset + 7);
            name = new String(v.read(nameLength, rootPtr, overallOffset + 8));

            printDirectory(inode, length, nameLength, type, name);

            overallOffset += length;
            if(overallOffset >= v.BLOCK_SIZE) break;
        }

        int bGroup = (11) / v.INODES_PER_GROUP;
        int index = (11) % v.INODES_PER_GROUP;

        int cBlock = (index * v.INODE_SIZE) / v.BLOCK_SIZE;
        int offset = (v.INODE_SIZE) * (index % (v.BLOCK_SIZE / v.INODE_SIZE));

        System.out.println("[Info: Block Group: " + bGroup + ", Index: " + index + ", Containing Block: " + cBlock + "]\n\n");

        byte[] twoCititesInode = v.read(v.INODE_SIZE, inodePtr + cBlock, offset);


        System.out.print("Dump Hex bytes of Root (Y/N): ");
        String in = s.next();

        int permissions = 0x8000 | 0x0100 | 0x0080 | 0x0020 | 0x0004;

        System.out.printf("Permissions: 0x%02x\n\n", permissions);

        int citiesDataPtr = v.readInt(4, inodePtr + cBlock, offset + 40);
        int citiesDataPtr2 = v.readInt(4, inode + cBlock, offset + 44);

        int citiesLength = v.readInt(4, inodePtr + cBlock, offset + 4) | v.readInt(4, inodePtr + cBlock, offset + 108);

        System.out.println("Two Cities File Length: " + citiesLength);

        byte[] citiesData = v.readBlock(citiesDataPtr);
        byte[] citiesData2 = v.readBlock(citiesDataPtr + 1);

        if(in.toUpperCase().equals("Y")) {
            Helper.dumpHexBytes(citiesData);
            System.out.println();
            Helper.dumpHexBytes(citiesData2);
        }
    }


    private void printDirectory(int i, int len, int nameLen, int type, String name) {
        System.out.println("Inode: " + i);
        System.out.println("Length: " + len);
        System.out.println("Name Length: " + nameLen);
        System.out.println("File Type: " + type);
        System.out.println("Directory Name: " + name);
        System.out.println();
    }
}
