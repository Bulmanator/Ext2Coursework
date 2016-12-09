package com.bulmanator.ext2.Structure;

import com.bulmanator.ext2.Utils.Helper;
import com.bulmanator.ext2.Utils.Permissions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Inode {

    // Ownership
    private short permissions;
    private short userID;
    private short groupID;

    // Misc
    private long size;
    private int creationTime;
    private int modificationTime;
    private int accessTime;

    // Data
    private short links;
    private int[] directBlocks;
    private int indirectBlock;
    private int doubleIndirectBlock;
    private int tripleIndirectBlock;

    /**
     * Contains information about a single inode
     * @param volume The volume from which to read the inode data from
     * @param position The starting position of the inode data
     */
    public Inode(Volume volume, long position) {

        ByteBuffer buffer = ByteBuffer.wrap(volume.read(position, volume.INODE_SIZE));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        permissions = buffer.getShort(0);
        userID = buffer.getShort(2);
        groupID = buffer.getShort(24);

        // The size is stored as two ints so convert them to one long
        size = (((long)buffer.getInt(108) << 32) | ((long)buffer.getInt(4) & 0xFFFFFFFFL));

        creationTime = buffer.getInt(12);
        modificationTime = buffer.getInt(16);
        accessTime = buffer.getInt(8);

        links = buffer.getShort(26);
        directBlocks = new int[12];
        for(int i = 0; i < 12; i++) {
            directBlocks[i] = buffer.getInt(40 + (i * 4));
        }

        indirectBlock = buffer.getInt(88);
        doubleIndirectBlock = buffer.getInt(92);
        tripleIndirectBlock = buffer.getInt(96);
    }

    /**
     * Prints out all of the inode data in a proper format
     */
    public void printInodeData() {
        System.out.println("[Inode Data]");
        System.out.println("-- Ownership");
        System.out.printf("   - Permissions: %s (0x%02x)\n", getPermissionString(), permissions);
        System.out.println("   - User ID: " + userID + " (" + (userID == 0 ? "root" : userID == 1000 ? "scc211" : "unknown") + ")");
        System.out.println("   - Group ID: " + groupID + " (" + (groupID == 0 ? "root" : groupID == 1000 ? "scc211" : "unknown") + ")");
        System.out.println("-- Misc");
        System.out.println("   - Size (Bytes): " + size);
        System.out.println("   - Creation Time: " + Helper.toDate(creationTime * 1000L) + " (" + creationTime + ")");
        System.out.println("   - Last Modified: " + Helper.toDate(modificationTime * 1000L) + " (" + modificationTime + ")");
        System.out.println("   - Last Accessed: " + Helper.toDate(accessTime * 1000L) + " (" + accessTime + ")");
        System.out.println("-- Data");
        System.out.println("   - Hard Links: " + links);
        System.out.println("-- Direct Pointers: ");
        for(int i = 0; i < directBlocks.length; i++) {
            System.out.printf("   - DP%d: 0x%02x\n", i, directBlocks[i]);
        }
        System.out.printf(" - Single Indirect Pointer: 0x%02x\n", indirectBlock);
        System.out.printf(" - Double Indirect Pointer: 0x%02x\n", doubleIndirectBlock);
        System.out.printf(" - Triple Indirect Pointer: 0x%02x\n", tripleIndirectBlock);
    }

    /**
     * Converts the number representation of the file mode/ permissions into its corresponding string counterpart
     * @return The permissions in string format (i.e. -rwxr--r--)
     * @see Permissions
     */
    public String getPermissionString() {
        String per = "";

        for(int i = 0; i < Permissions.PERMISSION_STRINGS.length; i++) {
            if((permissions & Permissions.PERMISSIONS[i]) == Permissions.PERMISSIONS[i]) {
                per += Permissions.PERMISSION_STRINGS[i];
            }
            else if(i > 9) {
                per += "-";
            }
        }
        return per;
    }

    /**
     * Whether or not this inode describes a directory
     * @return True if this describes a directory, otherwise false
     */
    public boolean isDirectory() { return (permissions & Permissions.DIR) == Permissions.DIR; }

    /**
     * Whether or not this inode describes a regular file
     * @return True if this describes a regular file, otherwise false
     */
    public boolean isFile() { return (permissions & Permissions.FILE) == Permissions.FILE; }

    /**
     * Gets the file mode/ permissions in number form
     * @return The file mode/ permissions
     */
    public int getPermissions() { return permissions; }

    /**
     * Gets the User ID of the inode
     * @return The User ID
     */
    public int getUserID() { return userID; }

    /**
     * Gets the Group ID of the inode
     * @return The Group ID
     */
    public int getGroupID() { return groupID; }

    /**
     * Gets the size (in bytes) of the inode
     * @return The size, in bytes
     */
    public long getSize() { return size; }

    /**
     * Gets the time of creation in seconds
     * @return The creation time
     */
    public int getCreationTime() { return creationTime; }

    /**
     * Gets the Last time modified in seconds
     * @return The last modification time
     */
    public int getModificationTime() { return modificationTime; }

    /**
     * Gets the last time accessed
     * @return The last access time
     */
    public int getAccessTime() { return accessTime; }

    /**
     * Gets the number of hard links to the inode
     * @return The hard link count
     */
    public int getLinks() { return links; }

    /**
     * Gets the 12 direct block pointers associated with the inode
     * @return The 12 direct block pointers
     */
    public int[] getDirectBlocks() { return directBlocks; }

    /**
     * Gets the singly indirect block pointer associated with the inode
     * @return The singly indirect block pointer
     */
    public int getIndirectBlock() { return indirectBlock; }

    /**
     * Gets the Doubly indirect block pointer associated with the inode
     * @return The doubly indirect block pointer
     */
    public int getDoubleIndirectBlock() { return doubleIndirectBlock; }

    /**
     * Gets the triply indirect block pointer associated with the inode
     * @return The triply indirect block pointer
     */
    public int getTripleIndirectBlock() { return tripleIndirectBlock; }
}