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

        int inode = v.readInt(2056) * v.BLOCK_SIZE;
       // byte[] b = v.read(inode + , 8 * v.INODE_SIZE);
        System.out.println();
       // Helper.dumpHexBytes(b);

        Inode i = new Inode(v, inode + 11 * v.INODE_SIZE);

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
