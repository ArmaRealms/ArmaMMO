package com.gmail.nossr50.datatypes.party;

import com.gmail.nossr50.commands.party.PartySubcommandType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.text.StringUtils;
import org.bukkit.entity.Player;

public enum PartyFeature {
    CHAT,
    TELEPORT,
    ALLIANCE,
    ITEM_SHARE,
    XP_SHARE;

    public String getLocaleString() {
        return LocaleLoader.getString("Party.Feature." + StringUtils.getPrettyPartyFeatureString(this).replace(" ", ""));
    }

    public String getFeatureLockedLocaleString() {
        return LocaleLoader.getString("Ability.Generic.Template.Lock", LocaleLoader.getString("Party.Feature.Locked." + StringUtils.getPrettyPartyFeatureString(this).replace(" ", ""), mcMMO.p.getGeneralConfig().getPartyFeatureUnlockLevel(this)));
    }

    public boolean hasPermission(Player player) {
        PartySubcommandType partySubCommandType;
        switch (this) {
            case CHAT -> partySubCommandType = PartySubcommandType.CHAT;
            case TELEPORT -> partySubCommandType = PartySubcommandType.TELEPORT;
            case ALLIANCE -> partySubCommandType = PartySubcommandType.ALLIANCE;
            case ITEM_SHARE -> partySubCommandType = PartySubcommandType.ITEMSHARE;
            case XP_SHARE -> partySubCommandType = PartySubcommandType.XPSHARE;
            default -> {
                return false;
            }
        }

        return Permissions.partySubcommand(player, partySubCommandType);
    }
}
