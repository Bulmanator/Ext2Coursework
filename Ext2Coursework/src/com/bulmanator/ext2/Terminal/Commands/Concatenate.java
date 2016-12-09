package com.bulmanator.ext2.Terminal.Commands;

import com.bulmanator.ext2.Structure.Ext2File;
import com.bulmanator.ext2.Structure.Volume;

public class Concatenate implements Command {

    private Ext2File file;
    private String path;

    public Concatenate(Ext2File file, String path) {
        this.file = file;
        this.path = path;
    }

    @Override
    public void invoke(Volume volume) {
        if(file.getFound() == Ext2File.NOT_FOUND) {
            System.out.println("cat: " + path + ": No such file or directory");
            return;
        }
        else if(file.getFound() == Ext2File.DIR) {
            System.out.println("cat: " + path  + ": Is a directory");
            return;
        }

        for (long i = 0; i < file.size; i += volume.BLOCK_SIZE * 16L) {
            byte[] data = file.read(i, volume.BLOCK_SIZE * 16L < (file.size - i) ? volume.BLOCK_SIZE * 16L : (file.size - i));
            for (int j = 0; j < data.length; j++) {
                if (data[j] > 9 && data[j] < 127) {
                    System.out.printf("%c", data[j]);
                }
            }
        }
    }
}
