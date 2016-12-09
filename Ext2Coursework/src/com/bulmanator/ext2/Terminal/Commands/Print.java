package com.bulmanator.ext2.Terminal.Commands;

import com.bulmanator.ext2.Structure.Ext2File;
import com.bulmanator.ext2.Structure.Volume;
import com.bulmanator.ext2.Utils.Helper;

public class Print implements Command {

    private Ext2File file;
    private String[] args;

    public Print(Ext2File file, String...args) {
        this.file = file;
        this.args = args;
    }

    @Override
    public void invoke(Volume volume) {
        if(args.length < 1) return;

        if(args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "inode":
                    if(file == null) return;
                    file.getInode().printInodeData();
                    break;
                case "superblock":
                    volume.printSuperblock();
                    break;
                default:
                    System.out.println("print: unknown argument: " + args[0]);
            }
        }
        else if(args.length == 2) {
            int index = 0;
            if(args[index].toLowerCase().equals("blockgroup")) {
                int grpNo = Integer.parseInt(args[index + 1]);
                volume.printBlockGroupDescriptor(grpNo);
            }
            else {
                if(file == null) return;
                try {
                    long start = Long.parseLong(args[0]);
                    long len = Long.parseLong(args[1]);


                    byte[] data = file.read(start, len);
                    for(int i = 0; i < data.length; i++) {
                        if(data[i] > 9 && data[i] < 127) {
                            System.out.printf("%c", data[i]);
                        }
                    }
                    if(data[data.length - 1] != '\n') System.out.println();
                }
                catch (NumberFormatException ex) {
                    String print = "print: expected number: ";
                    for(int i = 0; i < args.length; i++) {
                        print += args[i] + " ";
                    }
                    System.out.println(print + " received");
                }
            }
        }
        else if(args.length == 3) {
            if(file == null) return;
            try {
                long start = Long.parseLong(args[1]);
                long len = Long.parseLong(args[2]);

                System.out.println("Start: " + start + " Length: " + len);


                byte[] data = file.read(start, len);
                Helper.dumpHexBytes(data);
            }
            catch (NumberFormatException ex) {
                String print = "print: expected number: ";
                for(int i = 0; i < args.length; i++) {
                    print += args[i] + " ";
                }
                System.out.println(print + "received");}
        }
    }
}
