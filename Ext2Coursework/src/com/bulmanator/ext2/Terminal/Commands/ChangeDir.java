package com.bulmanator.ext2.Terminal.Commands;

import com.bulmanator.ext2.Structure.Directory;
import com.bulmanator.ext2.Structure.Volume;

public class ChangeDir implements Command {

    private Directory directory;
    private String path;

    /**
     * Creates an invokable command which will change the working directory to the one specified
     * @param directory The directory to change to
     * @param path The path of the directory
     */
    public ChangeDir(Directory directory, String path) {
        this.directory = directory;
        this.path = path;
    }

    /**
     * Performs the Change Directory Command
     * @param volume The volume which the directories are located
     */
    public void invoke(Volume volume) {
        if(directory.getFoundType() == Directory.NOT_FOUND) {
            System.out.println("cd: " + path + ": No such file or directory");
            return;
        }
        else if(directory.getFoundType() == Directory.FILE) {
            System.out.println("cd: " + path + ": Not a directory");
            return;
        }

        volume.setCurrentDir(directory);
    }
}
