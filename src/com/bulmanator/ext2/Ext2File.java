package com.bulmanator.ext2;

import java.io.IOException;

public class Ext2File {

    private byte[] data;

    public Ext2File(Volume volume, String path) {
        try {
            System.out.println("File: " + volume.getRandomAccess().length());
            data = new byte[500];
            volume.getRandomAccess().read(data, 0, 500);
        }
        catch (IOException ex) {
            try { volume.getRandomAccess().close(); }
            catch (Exception e) {}
            ex.printStackTrace();
            System.exit(-1);
        }

        for(int i = 0; i < data.length; i++) {
            System.out.print((char)data[i]);
        }
    }
}
