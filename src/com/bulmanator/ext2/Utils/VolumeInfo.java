package com.bulmanator.ext2.Utils;

import com.bulmanator.ext2.Volume;

public class VolumeInfo {

    public int INODE_COUNT = -1;
    public int BLOCK_COUNT = -1;

    public int BLOCK_SIZE = -1;
    public int INODE_SIZE = -1;

    public int BLOCKS_PER_GROUP = -1;
    public int INODES_PER_GROUP = -1;

    public int SIGNATURE = -1;

    public String VOLUME_NAME = "NULL";

    public void printInfo() {
        System.out.println("[Volume Information]");

        System.out.println(" - Volume Label: " + VOLUME_NAME);

        System.out.println(" - Inode Count: " + INODE_COUNT);
        System.out.println(" - Block Count: " + BLOCK_COUNT);

        System.out.println(" - Block Size: " + BLOCK_SIZE);
        System.out.println(" - Inode Size: " + INODE_SIZE);

        System.out.println(" - Inodes Per Group: " + INODES_PER_GROUP);
        System.out.println(" - Blocks Per Group: " + BLOCKS_PER_GROUP);

        System.out.printf(" - Signature: 0x%02X\n\n", SIGNATURE);
    }

    public static void init(Volume v, VolumeInfo info) {
        try {

            // Block Size -- Has to be done differently because
            // readInt() depends on BLOCK_SIZE
            byte[] b = new byte[4];
            v.getRandomAccess().seek(1048);
            v.getRandomAccess().read(b);
            info.BLOCK_SIZE = 1024 * (int)Math.pow(2, Helper.intFromBytes(b));

            // Inode Count
            info.INODE_COUNT = v.readInt(4, 1, 0);
            // Block Count
            info.BLOCK_COUNT = v.readInt(4, 1, 4);

            // Inode Size
            info.INODE_SIZE = v.readInt(4, 1, 88);

            // Blocks per Group
            info.BLOCKS_PER_GROUP = v.readInt(4, 1, 32);
            // Inodes per Group
            info.INODES_PER_GROUP = v.readInt(4, 1, 40);

            info.SIGNATURE = v.readInt(2, 1, 56);

            info.VOLUME_NAME = new String(v.read(16, 1, 120));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
