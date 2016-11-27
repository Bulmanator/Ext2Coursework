package com.bulmanator.ext2;

import com.bulmanator.ext2.Utils.Helper;
import com.bulmanator.ext2.Utils.VolumeInfo;

import java.io.File;
import java.io.RandomAccessFile;

public class Volume {

    private VolumeInfo information;
    private RandomAccessFile fileSystem;

    public Volume(String file) throws Exception {
        fileSystem = new RandomAccessFile(new File(file), "r");
       // fileSystem.seek(1024);
        information = new VolumeInfo();
        VolumeInfo.init(this, information);
    }

    public RandomAccessFile getRandomAccess() { return fileSystem; }

    public byte[] readBlock(int block) {
        byte[] data = new byte[information.BLOCK_SIZE];
        try {
            int offset = information.BLOCK_SIZE * block;
            fileSystem.seek(offset);
            fileSystem.read(data, 0, information.BLOCK_SIZE);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        return data;
    }

    public byte[] readRootInode(int block) {
        byte[] ret = new byte[information.INODE_SIZE];
        try {
            fileSystem.seek(information.BLOCK_SIZE * block + information.INODE_SIZE);
            fileSystem.read(ret);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public VolumeInfo getInformation() { return information; }


    public byte[] read(int nBytes, int blockNumber, int offset) {
        byte[] ret = new byte[nBytes];
        try {
            fileSystem.seek(information.BLOCK_SIZE * blockNumber + offset);
            fileSystem.read(ret);
        }
        catch (Exception ex) { ex.printStackTrace(); }

        return ret;
    }

    public int readInt(int nBytes, int blockNumber, int offset) {
        int value = 0;
        try {
            byte[] b = new byte[nBytes];

            fileSystem.seek(information.BLOCK_SIZE * blockNumber + offset);
            fileSystem.read(b);

            value = Helper.intFromBytes(b);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return value;
    }
}
