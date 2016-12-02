package com.bulmanator.ext2.Structure;

public class Ext2File {

    private byte[] data;
    private Inode inode;

    public Ext2File(Volume volume, String path) {
        String[] directories = path.substring(1).split("/");
        Inode currentInode = volume.getRoot();

        int size = currentInode.getSize();
        boolean fileFound = true;
        int overallOffset = 0, currentOffset = 0;
        int currentPtr = 0;

        for(int i = 0; i < 15; i++) {
            if(i < 12) { currentPtr = currentInode.getDirectBlocks()[0] * volume.BLOCK_SIZE; }
            else if(i == 12) { currentPtr = currentInode.getInderectBlock() * volume.BLOCK_SIZE; }
            else if(i == 13) { currentPtr = currentInode.getDoubleIndirectBlock() * volume.BLOCK_SIZE; }
            else { currentPtr = currentInode.getTripleIndirectBlock() * volume.BLOCK_SIZE; }

            if(currentPtr != 0) break;
        }

        int block = 0;

        for(int i = 0; i < directories.length; i++) {
            while (overallOffset < currentInode.getSize()) {
                int inode = volume.readNum(currentPtr + currentOffset, Volume.INTEGER);
                int length = volume.readNum(currentPtr + currentOffset + 4, Volume.SHORT);
                int nameLen = volume.readNum(currentPtr + currentOffset + 6, Volume.BYTE);
                String name = new String(volume.read(currentPtr + currentOffset + 8, nameLen));

                if(name.equals(directories[i])) {
                    currentInode = volume.getInode(inode);
                    System.out.println("Inode: " + inode);
                    System.out.println("Current Inode Size: " + currentInode.getSize());
                    currentPtr = currentInode.getDirectBlocks()[0] * volume.BLOCK_SIZE;
                    break;
                }

                overallOffset += length;
                currentOffset += length;
                if(overallOffset == volume.BLOCK_SIZE) {
                    block++;
                    if(block < 12) {
                        currentPtr = currentInode.getDirectBlocks()[block] * volume.BLOCK_SIZE;
                    }
                    else if(block == 12) {

                    }

                    currentOffset = 0;
                }
            }

            if(overallOffset == currentInode.getSize()) {
                System.out.println("Ext2File: File not found");
                fileFound = false;
                break;
            }

            currentOffset = 0;
            overallOffset = 0;
        }

        if(fileFound) {
            inode = currentInode;
            if(inode.isDirectory()) {
                System.out.println("Ext2File: " + path + ": Is a directory");
                return;
            }
            inode.printInodeData();
        }


    }
}