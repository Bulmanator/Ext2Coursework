package com.bulmanator.ext2;

import com.bulmanator.ext2.Structure.Ext2File;
import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Terminal.Interpeter;
import com.bulmanator.ext2.Utils.Helper;
import com.bulmanator.ext2.Structure.Inode;

import java.util.Scanner;

public class Main {

    private Volume v;

    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }

    private void run() {
        Scanner s = new Scanner(System.in);
        v = null;
        try { v = new Volume("resources/ext2fs"); }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: Failed to create File System!");
            System.exit(-1);
        }

    //    v.printSuperblock();

      // Ext2File file = new Ext2File(v, "/files/ind-e");


       //
       // System.out.println("Size: " + v.BLOCK_SIZE / Volume.INTEGER);
        Inode ind = v.getInode(1721);
        ind.printInodeData();

        long sTime = System.nanoTime();
        int[] ptrs = v.getUsedPtrs(ind);
        long eTime = System.nanoTime();

        System.out.println("Time Taken: " + (eTime - sTime));
        for(int i = 0; i < ptrs.length; i++) {
            System.out.printf("0x%02x\n", ptrs[i]);
            byte[] d = v.read(ptrs[i] * v.BLOCK_SIZE, v.BLOCK_SIZE);
            Helper.dumpHexBytes(d);

        }
        //byte[] d = v.read(ind.getInderectBlock() * v.BLOCK_SIZE, v.BLOCK_SIZE);
        //Helper.dumpHexBytes(d);

      //  int[] ptrs = v.getSingleIndirectPtrs(ind.getInderectBlock());
      //  for(int i = 0; i < ptrs.length; i++) {
       //     System.out.printf("0x%02x\n", ptrs[i]);
      //  }

        //long sTime = System.nanoTime();
      //  readTrplIndBlock(ind.getTripleIndirectBlock());
       // long eTime = System.nanoTime();

       // System.out.println("Time: " + (eTime - sTime));

       // Inode i = v.getInode(12);
       // i.printInodeData();

//        byte[] data = v.read(i.getDirectBlocks()[0] * v.BLOCK_SIZE, v.BLOCK_SIZE);
 //       Helper.dumpHexBytes(data);

        //System.out.printf("Direct Block 1: 0x%02x\n", i.getDirectBlocks()[0]);
       //byte[] d = v.read(i.getDirectBlocks()[0] * v.BLOCK_SIZE, v.BLOCK_SIZE);
       //Helper.dumpHexBytes(d);

      //  Interpeter interpeter = new Interpeter();
      //  interpeter.run();


      /*  while(true) {
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
        }*/

        v.close();
    }

    long processed = 0;
    private void readIndBlock(int indPtr) {
        System.out.println("Ind Ptr: " + indPtr);
        for(int i = 0; i < (v.BLOCK_SIZE / Volume.INTEGER); i++) {
            int ptr = v.readNum((indPtr * v.BLOCK_SIZE) + (i * Volume.INTEGER), Volume.INTEGER);
            if(ptr != 0) {
                byte[] d = v.read(ptr, v.BLOCK_SIZE);
                Helper.dumpHexBytes(d);
                processed += 1;
            }
        }
    }

    private void readDblIndBlock(int dblIndPtr) {
        System.out.println("Dbl Ptr: " + dblIndPtr);
        for(int i = 0; i < (v.BLOCK_SIZE / Volume.INTEGER); i++) {
            int ptr = v.readNum((dblIndPtr * v.BLOCK_SIZE) + (i * Volume.INTEGER), Volume.INTEGER);
            if(ptr != 0) {
                readIndBlock(ptr);
            }
        }
    }

    private void readTrplIndBlock(int trplIndPtr) {
        processed = 0;
        System.out.println("Trpl Ptr: " + trplIndPtr);
        for(int i = 0; i < (v.BLOCK_SIZE / Volume.INTEGER); i++) {
            int ptr = v.readNum((trplIndPtr * v.BLOCK_SIZE) + (i * Volume.INTEGER), Volume.INTEGER);
            if(ptr != 0) {
                readDblIndBlock(ptr);
            }
        }
        System.out.println("Processed Pointers: " + processed);
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
