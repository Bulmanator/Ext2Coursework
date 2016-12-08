package com.bulmanator.ext2.Utils;

public class Permissions {

    /** Socket type */
    public static final int SOCKET = 0xC000;
    /** Symbolic Link type */
    public static final int SYM_LINK = 0xA000;
    /** Regular File type */
    public static final int FILE = 0x8000;
    /** Block Device type */
    public static final int BLK_DEV = 0x6000;
    /** Directory Type */
    public static final int DIR = 0x4000;
    /** Character Device type */
    public static final int CHR_DEV = 0x2000;
    /** Pipe/ FIFO type */
    public static final int FIFO = 0x1000;

    /** Set User ID bit mask */
    public static final int SET_UID = 0x0800;
    /** Set Group ID bit mask */
    public static final int SET_GID = 0x0400;
    /** Sticky bit mask */
    public static final int STICKY = 0x0200;

    /** User read bit */
    public static final int USR_READ = 0x0100;
    /** User write bit */
    public static final int USR_WRITE = 0x0080;
    /** User execute bit */
    public static final int USR_EXE = 0x0040;

    // Group Permissions
    public static final int GRP_READ = 0x0020;
    public static final int GRP_WRITE = 0x0010;
    public static final int GRP_EXE = 0x0008;

    // Other Permissions
    public static final int OTHR_READ = 0x0004;
    public static final int OTHR_WRITE = 0x0002;
    public static final int OTHR_EXE = 0x0001;

    public static final int[] PERMISSIONS = {
            SOCKET, SYM_LINK, FILE, BLK_DEV, DIR, CHR_DEV, FIFO,
            SET_UID, SET_GID, STICKY,
            USR_READ, USR_WRITE, USR_EXE,
            GRP_READ, GRP_WRITE, GRP_EXE,
            OTHR_READ, OTHR_WRITE, OTHR_EXE
    };

    public static final String[] PERMISSION_STRINGS = {
            "s", "l", "-", "b", "d", "c", "p",
            "", "", "",
            "r", "w", "x",
            "r", "w", "x",
            "r", "w", "x"
    };
}
