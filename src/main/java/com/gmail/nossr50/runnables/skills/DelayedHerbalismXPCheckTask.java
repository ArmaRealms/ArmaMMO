package com.gmail.nossr50.runnables.skills;

import com.gmail.nossr50.datatypes.BlockSnapshot;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.CancellableRunnable;

import java.util.List;

public class DelayedHerbalismXPCheckTask extends CancellableRunnable {

    private final McMMOPlayer mmoPlayer;
    private final List<BlockSnapshot> chorusBlocks;

    public DelayedHerbalismXPCheckTask(McMMOPlayer mmoPlayer, List<BlockSnapshot> chorusBlocks) {
        this.mmoPlayer = mmoPlayer;
        this.chorusBlocks = chorusBlocks;
    }

    @Override
    public void run() {
        mmoPlayer.getHerbalismManager().awardXPForBlockSnapshots(chorusBlocks);
    }
}
