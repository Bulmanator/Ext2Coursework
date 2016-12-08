package com.bulmanator.ext2;

import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Terminal.Interpeter;

public class Main {

    private Volume volume;

    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }

    private void run() {
        volume = null;
        try { volume = new Volume("resources/lite"); }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: Failed to create File System!");
            System.exit(-1);
        }

        new Interpeter(volume);

        volume.close();
    }
}
