package com.bulmanator.ext2.Utils;

import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Helper {

    /**
     * Will print a byte array printed out in formatted hexadecimal and ascii representation (if present)
     * @param bytes The byte array to dump
     */
    public static void dumpHexBytes(byte[] bytes) {
        int length = bytes.length + (16 - (bytes.length % 16)); //((bytes.length % 16 != 0) ? (16 - (bytes.length % 16)) : 0);

        for(int i = 0; i < length; i++) {

            if(i < bytes.length) {
                System.out.printf("%02x", bytes[i]);
            }
            else {
                System.out.print("XX");
            }

            if((i + 1) % 2 == 0) System.out.print(" ");
            if((i + 1) % 8 == 0) System.out.print("| ");

            if((i + 1) % 16 == 0) {
                for(int j = i - 15; j <= i; j++) {
                    if(j < bytes.length) {
                        // Checks ASCII range otherwise prints '-'
                        if (bytes[j] > 31 && bytes[j] < 127) {
                            System.out.printf("%c", bytes[j]);
                        } else {
                            System.out.print("-");
                        }
                    }
                    else {
                        System.out.print("X");
                    }

                    if((j + 1) % 8 == 0) System.out.print(" | ");
                }
                System.out.println();
            }
        }
    }


    /**
     * Converts a time in milliseconds to a date as a formatted string
     * @param ms The time, in milliseconds
     * @return The formatted date
     */
    public static String toDate(long ms) {
        Date d = new Date(ms);
        Date today = new Date();

        SimpleDateFormat sdf;
        if((today.getTime() - ms) > 31556952000L) {
            sdf = new SimpleDateFormat("MMM d YYYY");
        }
        else {
            sdf = new SimpleDateFormat("MMM d hh:mm");
        }


        return sdf.format(d);
    }

}
