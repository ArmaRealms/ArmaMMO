package com.gmail.nossr50.datatypes.party;

import static java.util.logging.Logger.getLogger;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gmail.nossr50.MMOTestEnvironment;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.experience.FormulaType;
import com.gmail.nossr50.events.party.McMMOPartyLevelUpEvent;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.experience.FormulaManager;
import org.bukkit.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.logging.Logger;

class PartyTest extends MMOTestEnvironment {
    private static final Logger logger = getLogger(PartyTest.class.getName());

    private FormulaManager formulaManager;

    @BeforeEach
    public void setUp() {
        mockBaseEnvironment(logger);

        formulaManager = mock(FormulaManager.class);
        mockedMcMMO.when(mcMMO::getFormulaManager).thenReturn(formulaManager);

        when(ExperienceConfig.getInstance().getFormulaType()).thenReturn(FormulaType.LINEAR);
        // XP required to reach level 1 from level 0
        when(formulaManager.getXPtoNextLevel(0, FormulaType.LINEAR)).thenReturn(1000);
        // XP required to reach level 2 from level 1
        when(formulaManager.getXPtoNextLevel(1, FormulaType.LINEAR)).thenReturn(1500);

        // Curve multiplier of 1 with 0 online members → XP to level = baseXP * 1
        when(generalConfig.getPartyXpCurveMultiplier()).thenReturn(1);
        // Party level cap well above test range
        when(generalConfig.getPartyLevelCap()).thenReturn(100);
        // Don't inform all members (avoids LocaleLoader and PartyManager calls)
        when(generalConfig.getPartyInformAllMembers()).thenReturn(false);
        // No leader online, so the leader notification branch is skipped
        when(server.getPlayer(any(UUID.class))).thenReturn(null);
    }

    @AfterEach
    public void tearDown() {
        cleanUpStaticMocks();
    }

    @Test
    void partyApplyXpGainLevelsUpWhenXpIsSufficient() {
        final Party party = new Party(new PartyLeader(UUID.randomUUID(), "Leader"), "TestParty");

        // Apply exactly enough XP to level up once (XP to level = 1000 * 1 = 1000)
        party.applyXpGain(1000);

        assertThat(party.getLevel()).isEqualTo(1);
        assertThat(party.getXp()).isEqualTo(0f);
    }

    @Test
    void partyApplyXpGainDoesNotLevelUpWhenXpIsInsufficient() {
        final Party party = new Party(new PartyLeader(UUID.randomUUID(), "Leader"), "TestParty");

        party.applyXpGain(500);

        assertThat(party.getLevel()).isEqualTo(0);
        assertThat(party.getXp()).isEqualTo(500f);
    }

    @Test
    void partyApplyXpGainRevertsLevelAndXpWhenLevelUpEventIsCancelled() {
        final Party party = new Party(new PartyLeader(UUID.randomUUID(), "Leader"), "TestParty");

        // Cancel only McMMOPartyLevelUpEvent, let XP gain event pass through
        doAnswer(invocation -> {
            final Object arg = invocation.getArgument(0);
            if (arg instanceof McMMOPartyLevelUpEvent levelUpEvent) {
                levelUpEvent.setCancelled(true);
            }
            return null;
        }).when(pluginManager).callEvent(any(Event.class));

        party.applyXpGain(1000);

        // Level-up was cancelled: party should revert to level 0 with the original XP restored
        assertThat(party.getLevel()).isEqualTo(0);
        assertThat(party.getXp()).isEqualTo(1000f);
    }

    @Test
    void partyApplyXpGainHandlesMultipleLevelUpsInOneGain() {
        final Party party = new Party(new PartyLeader(UUID.randomUUID(), "Leader"), "TestParty");

        // Enough XP for two level-ups: 1000 (level 0→1) + 1500 (level 1→2) = 2500
        party.applyXpGain(2500);

        assertThat(party.getLevel()).isEqualTo(2);
        assertThat(party.getXp()).isEqualTo(0f);
    }
}
