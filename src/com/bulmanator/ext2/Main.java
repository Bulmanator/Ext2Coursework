package com.bulmanator.ext2;

import com.bulmanator.ext2.Utils.Helper;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Volume v = null;
        Helper h = new Helper();
        try {
            v = new Volume("resources/ext2fs");
        }
        catch (Exception e) {
            System.err.println("Error: Failed to create File System!");
            System.exit(-1);
        }

        v.getInformation().printInfo();

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

        int rootPtr = v.readInt(4, inodePtr, 168);
        byte[] root = v.readBlock(rootPtr);

        int inode = v.readInt(4, rootPtr, 0);
        int len = v.readInt(2, rootPtr, 4);
        int nLen = v.readInt(1, rootPtr, 6);

        byte[] name = v.read(nLen, rootPtr, 8);

        String dirName = new String(name);

        System.out.println("Inode: " + inode);
        System.out.println("Length: " + len);
        System.out.println("Name Length: " + nLen);
        System.out.println("Directory Name: " + dirName);

        Scanner s = new Scanner(System.in);
        System.out.print("Dump Hex bytes of Root (Y/N): ");
        String in = s.next();

        if(in.toUpperCase().equals("Y")) {
            h.dumpHexBytes(root);
        }
    }
}
