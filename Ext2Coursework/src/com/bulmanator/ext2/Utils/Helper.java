package com.bulmanator.ext2.Utils;

import java.nio.ByteOrder;

public class Helper {

    public static void dumpHexBytes(byte[] bytes) {
        int remain = bytes.length % 16;
        int len = (bytes.length - remain) / 16;
        for(int i = 0; i < len; i++) {
            if(i + 1 < 10) {
                System.out.printf((i + 1) + ".   ");
            }
            else if(i + 1 < 100) {
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
                            System.out.printf("-");
                        }
                        if ((k + 1) % 8 == 0) System.out.print(" | ");
                    }
                }
            }
            System.out.println();
        }

        if(remain > 0) {
            if (len + 1 < 10) {
                System.out.print((len + 1) + ".   ");
            } else if (len + 1 < 100) {
                System.out.print((len + 1) + ".  ");
            } else {
                System.out.print((len + 1) + ". ");
            }

            int loop = len + 16;

            for (int j = 0; j < 2; j++) {
                for (int i = len; i < loop; i++) {
                    if (j == 0) {
                        if (i < len + remain) {
                            if (i % 2 == 0) {
                                System.out.printf("%02x ", bytes[i]);
                            } else {
                                System.out.printf("%02x", bytes[i]);
                            }
                        } else {
                            if (i % 2 == 0) {
                                System.out.print("XX ");
                            } else {
                                System.out.print("XX");
                            }
                        }
                        if (((i + 1) - len) % 8 == 0) System.out.print("| ");
                    } else {
                        if (i < len + remain) {
                            if (bytes[i] > 31 && bytes[i] < 127) {
                                System.out.printf("%c", bytes[i]);
                            } else {
                                System.out.print("-");
                            }
                        } else {
                            System.out.print("X");
                        }
                        if (((i + 1) - len) % 8 == 0) System.out.print(" | ");
                    }
                }
            }
        }
        System.out.println();
    }
}
