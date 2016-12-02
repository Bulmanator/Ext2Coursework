package com.bulmanator.ext2.Terminal.Commands;

public abstract class Command {

    protected String name;
    protected String[] args;

    public Command(String name, String...args) {
        this.name = name;
        this.args = args;
    }

    public abstract void invoke();

    public int getArgCount() { return args.length; }
    public String getArg(int index) { return args[index]; }
    public String getName() { return name; }
}
