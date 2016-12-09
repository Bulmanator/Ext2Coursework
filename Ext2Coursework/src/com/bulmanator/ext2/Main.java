package com.bulmanator.ext2;

import com.bulmanator.ext2.Structure.Ext2File;
import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Terminal.Interpreter;
import com.bulmanator.ext2.Utils.Helper;

public class Main {

    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }

    /** Loads the volume and starts the interpreter */
    private void run() {
        Volume volume = null;
        try { volume = new Volume("resources/ext2fs"); }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: Failed to create File System!");
            System.exit(-1);
        }

        System.out.println("SCC211: Operating Systems - Coursework 2");
        System.out.println(" - Type 'help' for available commands");
        new Interpreter(volume);

        volume.close();
    }
}
