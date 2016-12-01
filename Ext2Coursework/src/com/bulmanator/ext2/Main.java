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

    //    v.printSuperblock();

       // Ext2File file = new Ext2File(v, "/deep/down/");

       // Inode i = v.getInode(12);
       // i.printInodeData();

//        byte[] data = v.read(i.getDirectBlocks()[0] * v.BLOCK_SIZE, v.BLOCK_SIZE);
 //       Helper.dumpHexBytes(data);

        //System.out.printf("Direct Block 1: 0x%02x\n", i.getDirectBlocks()[0]);
       //byte[] d = v.read(i.getDirectBlocks()[0] * v.BLOCK_SIZE, v.BLOCK_SIZE);
       //Helper.dumpHexBytes(d);


        while(true) {
            System.out.print("user@scc211:/$ ");
            String next = s.nextLine();
            if(next.equals("ls -l")) {
                int offset = 0;
                while(offset != v.BLOCK_SIZE) {
                    int index = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + offset, 4);
                    int nameLen = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 6 + offset, 1);
                    int len = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 4 + offset, 2);
                    String name = new String(v.read(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 8 + offset, nameLen));
                    Inode current = v.getInode(index);

                    if(!name.contains(".")) {
                        System.out.printf("%s %d %d %d %d %s %s\n", current.getPermissionString(),
                                current.getLinks(), current.getUserID(), current.getGroupID(), current.getSize(),
                                Helper.toDate(current.getModificationTime() * 1000L), name);

                    }
                    offset += len;
                }
            }
            else if(next.equals("ls")) {
                int offset = 0;
                while(offset != v.BLOCK_SIZE) {
                    int index = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + offset, 4);
                    int nameLen = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 6 + offset, 1);
                    int len = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 4 + offset, 2);
                    String name = new String(v.read(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 8 + offset, nameLen));
                    Inode current = v.getInode(index);

                    System.out.printf("%s ", name);

                    offset += len;
                }
                System.out.println();
            }
            else if(next.equals("ls -la")) {
                int offset = 0;
                while(offset != v.BLOCK_SIZE) {
                    int index = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + offset, 4);
                    int nameLen = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 6 + offset, 1);
                    int len = v.readNum(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 4 + offset, 2);
                    String name = new String(v.read(v.getRoot().getDirectBlocks()[0] * v.BLOCK_SIZE + 8 + offset, nameLen));
                    Inode current = v.getInode(index);

                    System.out.printf("%s %d %d %d %d %s %s\n", current.getPermissionString(),
                            current.getLinks(), current.getUserID(), current.getGroupID(), current.getSize(),
                            Helper.toDate(current.getModificationTime() * 1000L), name);


                    offset += len;
                }
            }
            else if(next.contains("echo") && next.contains(" ")) {
                String[] split = next.split(" ");
                if(split[0].equals("echo")) {
                    for(int j = 1; j < split.length; j++) {
                        System.out.print(split[j] + " ");
                    }
                }
                System.out.println();
            }
            else if(next.equals("exit")) {
                break;
            }
            else {
                String[] split = next.split(" ");
                System.out.println(split[0] + ": command not found");
            }
        }

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
