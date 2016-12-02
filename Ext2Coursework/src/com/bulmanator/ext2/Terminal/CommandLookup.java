package com.bulmanator.ext2.Terminal;

import java.util.HashMap;

public class CommandLookup {

    public static final HashMap<String, String[]> commands;

    static {
        commands = new HashMap<>();
        commands.put("ls", new String[] { "-l", "-la" });
        commands.put("cd", null);
        commands.put("exit", null);
        commands.put("cat", null);
    }
}
