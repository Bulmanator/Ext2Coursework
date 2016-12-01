package com.bulmanator.ext2;

import com.bulmanator.ext2.Utils.Helper;
import com.bulmanator.ext2.Utils.Inode;

import java.io.IOException;
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
        try { v = new Volume("resources/ext2fs"); }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: Failed to create File System!");
            System.exit(-1);
        }

        v.printSuperblock();

        Ext2File file = new Ext2File(v, "/deep/down/in/the/filesystem/there/lived/a/file");


        //System.out.println(((1722 - 1) % v.INODES_PER_GROUP) * v.INODE_SIZE);

      // Inode i = new Inode(v, v.getInodeTablePtr(1) + ((1722 - 1) % v.INODES_PER_GROUP) * v.INODE_SIZE);
       // Inode i = new Inode(v, v.getInodeTablePtr(0) + (11 * v.INODE_SIZE));
        //i.printInodeData();



        //System.out.printf("Direct Block 1: 0x%02x\n", i.getDirectBlocks()[0]);
       //byte[] d = v.read(i.getDirectBlocks()[0] * v.BLOCK_SIZE, v.BLOCK_SIZE);
       //Helper.dumpHexBytes(d);

        v.close();
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
