package com.bulmanator.ext2;

import com.bulmanator.ext2.Utils.Helper;
import com.bulmanator.ext2.Utils.Inode;

import java.io.IOException;

public class Ext2File {

    private byte[] data;

    public Ext2File(Volume volume, String path) {
        String[] directories = path.substring(1).split("/");
        Inode currentInode = volume.getRoot();

        int size = currentInode.getSize();
        boolean fileFound = false;
        int overallOffset = 0, currentPtr = currentInode.getDirectBlocks()[0] * volume.BLOCK_SIZE;
        int block = 0;

        boolean finalFile = false;
        for(int i = 0; i < directories.length; i++) {
            while (overallOffset < currentInode.getSize()) {
                int inode = volume.readInt(currentPtr + overallOffset);
                int length = volume.readShort(currentPtr + overallOffset + 4);
                int nameLen = volume.read(currentPtr + overallOffset + 6);
                String name = new String(volume.read(currentPtr + overallOffset + 8, nameLen));

                if(name.equals(directories[i])) {
                    System.out.println(inode);
                    currentInode = volume.getInode(inode);
                    currentPtr = currentInode.getDirectBlocks()[0] * volume.BLOCK_SIZE;
                    break;
                }

                overallOffset += length;
                if(overallOffset == volume.BLOCK_SIZE) {
                    block++;
                    currentPtr = currentInode.getDirectBlocks()[block] * volume.BLOCK_SIZE;
                }
            }

            overallOffset = 0;
        }

        currentInode.printInodeData();
    }
}