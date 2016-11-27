package com.bulmanator.ext2.Utils;

/**
 * Created by james on 26/11/16.
 */
public class Helper {

    public void dumpHexBytes(byte[] bytes) {
        int remain = bytes.length % 16;
        int len = (bytes.length - remain) / 16;
        for(int i = 0; i < len; i++) {
            if(i + 1 < 10) {
                System.out.printf((i + 1) + ".  ");
            }
            else {
                System.out.printf((i + 1) + ". ");
            }
            for (int j = 0; j < 2; j++) {
                for(int k = 0; k < 16; k++) {
                    byte current = bytes[(i * 16) + k];
                    if(j == 0) {
                        if(k % 2 != 0) {
                            System.out.format("%02x ", current);
                        }
                        else {
                            System.out.format("%02x", current);
                        }
                        if ((k + 1) % 8 == 0) System.out.print("| ");
                    }
                    else {
                        if(current > 31 && current < 127) {
                            System.out.printf("%c", current);
                        }
                        else {
                            System.out.printf(".");
                        }
                        if ((k + 1) % 8 == 0) System.out.print(" | ");
                    }
                }
            }
            System.out.println();
        }
        for(int i = 0; i < remain; i++) {
            System.out.println("XX ");
        }
    }

    public static int intFromBytes(byte[] b) {
        byte[] tmp = new byte[4];

        if(b.length == 1) {
            tmp[0] = 0;
            tmp[1] = 0;
            tmp[2] = 0;
            tmp[3] = b[0];
        }
        else if(b.length == 2) {
            tmp[0] = 0;
            tmp[1] = 0;
            tmp[2] = b[1];
            tmp[3] = b[0];
        }
        else {
            tmp[0] = b[3];
            tmp[1] = b[2];
            tmp[2] = b[1];
            tmp[3] = b[0];
        }

        int value = 0;
        for(int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (tmp[i] & 0x000000FF) << shift;
        }

        return value;
    }

    public static byte[] bytesFromInt(int a) {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);


        return ret;
    }
}
