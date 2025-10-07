package com.gmail.nossr50.commands.skills;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.util.skills.CombatUtils;
import com.gmail.nossr50.util.skills.SkillUtils;
import com.gmail.nossr50.util.text.TextComponentFactory;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.gmail.nossr50.datatypes.skills.SubSkillType.TRIDENTS_IMPALE;
import static com.gmail.nossr50.datatypes.skills.SubSkillType.TRIDENTS_TRIDENTS_LIMIT_BREAK;

public class TridentsCommand extends SkillCommand {

    public TridentsCommand() {
        super(PrimarySkillType.TRIDENTS);
    }

    @Override
    protected void dataCalculations(final Player player, final float skillValue) {
    }

    @Override
    protected void permissionsCheck(final Player player) {
    }

    @Override
    protected List<String> statsDisplay(final Player player, final float skillValue, final boolean hasEndurance,
                                        final boolean isLucky) {
        final List<String> messages = new ArrayList<>();

        if (SkillUtils.canUseSubskill(player, TRIDENTS_TRIDENTS_LIMIT_BREAK)) {
            messages.add(getStatMessage(TRIDENTS_TRIDENTS_LIMIT_BREAK,
                    String.valueOf(CombatUtils.getLimitBreakDamageAgainstQuality(player,
                            TRIDENTS_TRIDENTS_LIMIT_BREAK, 1000))));
        }

        if (SkillUtils.canUseSubskill(player, TRIDENTS_IMPALE)) {
            messages.add(getStatMessage(TRIDENTS_IMPALE,
                    String.valueOf(mmoPlayer.getTridentsManager().impaleDamageBonus())));
        }

        return messages;
    }

    @Override
    protected List<Component> getTextComponents(final Player player) {
        final List<Component> textComponents = new ArrayList<>();

        TextComponentFactory.getSubSkillTextComponents(player, textComponents,
                PrimarySkillType.TRIDENTS);

        return textComponents;
    }
}
