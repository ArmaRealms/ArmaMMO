package com.gmail.nossr50.runnables.skills;

import com.gmail.nossr50.datatypes.BlockSnapshot;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.CancellableRunnable;

import java.util.List;

public class DelayedHerbalismXPCheckTask extends CancellableRunnable {

    private final McMMOPlayer mcMMOPlayer;
    private final List<BlockSnapshot> chorusBlocks;

    public DelayedHerbalismXPCheckTask(McMMOPlayer mcMMOPlayer, List<BlockSnapshot> chorusBlocks) {
        this.mcMMOPlayer = mcMMOPlayer;
        this.chorusBlocks = chorusBlocks;
    }

    @Override
    public void run() {
        mcMMOPlayer.getHerbalismManager().awardXPForBlockSnapshots(chorusBlocks);
    }
}
