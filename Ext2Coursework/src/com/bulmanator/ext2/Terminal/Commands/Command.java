package com.bulmanator.ext2.Terminal.Commands;

import com.bulmanator.ext2.Structure.Volume;

public interface Command {
    void invoke(Volume volume);
}
