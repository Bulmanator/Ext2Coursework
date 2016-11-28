package com.bulmanator.ext2;

import java.io.IOException;

public class Ext2File {

    private byte[] data;

    public Ext2File(Volume volume, String path) {
        String[] directories = path.split("/");

        for(int i = 0; i < directories.length; i++) {
            System.out.println("/" + directories[i]);
        }
    }
}
