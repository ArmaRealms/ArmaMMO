package com.gmail.nossr50.util;

import com.gmail.nossr50.commands.party.PartySubcommandType;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.ItemType;
import com.gmail.nossr50.datatypes.skills.MaterialType;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.skills.RankUtils;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class Permissions {
    private static final Map<PrimarySkillType, String> SKILL_ENABLED_NODES =
            perSkillNodes("mcmmo.skills.");
    private static final Map<PrimarySkillType, String> LUCKY_PERK_NODES =
            perSkillNodes("mcmmo.perks.lucky.");
    private static final Map<PrimarySkillType, String> XP_QUADRUPLE_NODES =
            perSkillNodes("mcmmo.perks.xp.quadruple.");
    private static final Map<PrimarySkillType, String> XP_TRIPLE_NODES =
            perSkillNodes("mcmmo.perks.xp.triple.");
    private static final Map<PrimarySkillType, String> XP_150_PERCENT_NODES =
            perSkillNodes("mcmmo.perks.xp.150percentboost.");
    private static final Map<PrimarySkillType, String> XP_DOUBLE_NODES =
            perSkillNodes("mcmmo.perks.xp.double.");
    private static final Map<PrimarySkillType, String> XP_50_PERCENT_NODES =
            perSkillNodes("mcmmo.perks.xp.50percentboost.");
    private static final Map<PrimarySkillType, String> XP_25_PERCENT_NODES =
            perSkillNodes("mcmmo.perks.xp.25percentboost.");
    private static final Map<PrimarySkillType, String> XP_10_PERCENT_NODES =
            perSkillNodes("mcmmo.perks.xp.10percentboost.");
    private static final Map<PrimarySkillType, String> XP_CUSTOM_BOOST_NODES =
            perSkillNodes("mcmmo.perks.xp.customboost.");
    private static final Map<PrimarySkillType, String> VANILLA_XP_BOOST_NODES =
            perSkillNodes("mcmmo.ability.", ".vanillaxpboost");
    private static final Map<ItemType, String> REPAIR_ITEM_TYPE_NODES = perEnumNodes(
            ItemType.class, type -> "mcmmo.ability.repair."
                    + type.toString().toLowerCase(Locale.ENGLISH) + "repair");
    private static final Map<MaterialType, String> REPAIR_MATERIAL_TYPE_NODES = perEnumNodes(
            MaterialType.class, type -> "mcmmo.ability.repair."
                    + type.toString().toLowerCase(Locale.ENGLISH) + "repair");
    private static final Map<ItemType, String> SALVAGE_ITEM_TYPE_NODES = perEnumNodes(
            ItemType.class, type -> "mcmmo.ability.salvage."
                    + type.toString().toLowerCase(Locale.ENGLISH) + "salvage");
    private static final Map<MaterialType, String> SALVAGE_MATERIAL_TYPE_NODES = perEnumNodes(
            MaterialType.class, type -> "mcmmo.ability.salvage."
                    + type.toString().toLowerCase(Locale.ENGLISH) + "salvage");
    private static final Map<EntityType, String> CALL_OF_THE_WILD_NODES = perEnumNodes(
            EntityType.class, type -> "mcmmo.ability.taming.callofthewild."
                    + type.toString().toLowerCase(Locale.ENGLISH));
    // Material is too large to precompute every node eagerly; these fill lazily and are read
    // from region threads on Folia
    private static final Map<Material, String> GREEN_THUMB_BLOCK_NODES =
            new ConcurrentHashMap<>();
    private static final Map<Material, String> GREEN_THUMB_PLANT_NODES =
            new ConcurrentHashMap<>();

    private Permissions() {
    }

    private static Map<PrimarySkillType, String> perSkillNodes(String prefix) {
        return perSkillNodes(prefix, "");
    }

    private static Map<PrimarySkillType, String> perSkillNodes(String prefix, String suffix) {
        final Map<PrimarySkillType, String> nodes = new EnumMap<>(PrimarySkillType.class);

        for (PrimarySkillType skill : PrimarySkillType.values()) {
            nodes.put(skill, prefix + skill.toString().toLowerCase(Locale.ENGLISH) + suffix);
        }

        return nodes;
    }

    private static <T extends Enum<T>> Map<T, String> perEnumNodes(Class<T> enumClass,
            Function<T, String> nodeBuilder) {
        final Map<T, String> nodes = new EnumMap<>(enumClass);

        for (T constant : enumClass.getEnumConstants()) {
            nodes.put(constant, nodeBuilder.apply(constant));
        }

        return nodes;
    }

    /*
     * GENERAL
     */
    public static boolean motd(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.motd");
    }

    public static boolean levelUpBroadcast(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.broadcast.levelup");
    }

    public static boolean updateNotifications(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.tools.updatecheck");
    }

    public static boolean chimaeraWing(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.item.chimaerawing");
    }

    public static boolean showversion(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.showversion");
    }

    /* BYPASS */
    public static boolean hardcoreBypass(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.bypass.hardcoremode");
    }

    public static boolean arcaneBypass(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.bypass.arcanebypass");
    }

    /* CHAT */
    public static boolean partyChat(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.chat.partychat");
    }

    public static boolean adminChat(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.chat.adminchat");
    }

    public static boolean colorChat(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.chat.colors");
    }

    /*
     * COMMANDS
     */

    public static boolean mmoinfo(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mmoinfo");
    }

    public static boolean addlevels(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.addlevels");
    }

    public static boolean addlevelsOthers(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.addlevels.others");
    }

    public static boolean addxp(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.addxp");
    }

    public static boolean addxpOthers(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.addxp.others");
    }

    public static boolean hardcoreModify(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.hardcore.modify");
    }

    public static boolean hardcoreToggle(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.hardcore.toggle");
    }

    public static boolean inspect(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.inspect"));
    }

    public static boolean inspectFar(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.inspect.far"));
    }

    public static boolean inspectHidden(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.inspect.hidden"));
    }

    public static boolean mcability(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.mcability"));
    }

    public static boolean mcabilityOthers(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.mcability.others"));
    }

    public static boolean adminChatSpy(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mcchatspy");
    }

    public static boolean adminChatSpyOthers(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mcchatspy.others");
    }

    public static boolean mcgod(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mcgod");
    }

    public static boolean mcgodOthers(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mcgod.others");
    }

    public static boolean mcmmoDescription(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mcmmo.description");
    }

    public static boolean mcmmoHelp(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mcmmo.help");
    }

    public static boolean mcrank(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.mcrank"));
    }

    public static boolean mcrankOthers(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.mcrank.others"));
    }

    public static boolean mcrankFar(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.mcrank.others.far"));
    }

    public static boolean mcrankOffline(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.mcrank.others.offline"));
    }

    public static boolean mcrefresh(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.mcrefresh"));
    }

    public static boolean mcrefreshOthers(final Permissible permissible) {
        return (permissible.hasPermission("mcmmo.commands.mcrefresh.others"));
    }

    public static boolean mctop(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission(
                "mcmmo.commands.mctop." + skill.toString().toLowerCase(Locale.ENGLISH));
    }

    public static boolean mmoedit(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mmoedit");
    }

    public static boolean mmoeditOthers(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mmoedit.others");
    }

    public static boolean skillreset(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.skillreset");
    }

    public static boolean skillreset(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission(
                "mcmmo.commands.skillreset." + skill.toString().toLowerCase(Locale.ENGLISH));
    }

    public static boolean skillresetOthers(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.skillreset.others");
    }

    public static boolean skillresetOthers(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission(
                "mcmmo.commands.skillreset.others." + skill.toString().toLowerCase(Locale.ENGLISH));
    }

    public static boolean xplock(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission(
                "mcmmo.commands.xplock." + skill.toString().toLowerCase(Locale.ENGLISH));
    }

    public static boolean xprateSet(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.xprate.set");
    }

    public static boolean xprateReset(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.xprate.reset");
    }

    public static boolean xprateShow(Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.xprate.show");
    }

    public static boolean mcpurge(Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mcpurge");
    }

    public static boolean mcremove(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mcremove");
    }

    public static boolean mmoupdate(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.mmoupdate");
    }

    public static boolean reloadlocale(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.reloadlocale");
    }

    /*
     * PERKS
     */

    /* BYPASS PERKS */

    public static boolean hasRepairEnchantBypassPerk(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.perks.bypass.repairenchant");
    }

    public static boolean hasSalvageEnchantBypassPerk(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.perks.bypass.salvageenchant");
    }

    public static boolean lucky(Permissible permissible, PrimarySkillType skill) {
        return permissible.hasPermission(LUCKY_PERK_NODES.get(skill));
    }

    /* XP PERKS */
    @Deprecated
    public static boolean quadrupleXp(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission("mcmmo.perks.xp.quadruple.all")
                || permissible.hasPermission(XP_QUADRUPLE_NODES.get(skill));
    }

    @Deprecated
    public static boolean tripleXp(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission("mcmmo.perks.xp.triple.all")
                || permissible.hasPermission(XP_TRIPLE_NODES.get(skill));
    }

    @Deprecated
    public static boolean doubleAndOneHalfXp(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission("mcmmo.perks.xp.150percentboost.all")
                || permissible.hasPermission(XP_150_PERCENT_NODES.get(skill));
    }

    @Deprecated
    public static boolean doubleXp(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission("mcmmo.perks.xp.double.all")
                || permissible.hasPermission(XP_DOUBLE_NODES.get(skill));
    }

    @Deprecated
    public static boolean oneAndOneHalfXp(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission("mcmmo.perks.xp.50percentboost.all")
                || permissible.hasPermission(XP_50_PERCENT_NODES.get(skill));
    }

    @Deprecated
    public static boolean oneAndAQuarterXp(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission("mcmmo.perks.xp.25percentboost.all")
                || permissible.hasPermission(XP_25_PERCENT_NODES.get(skill));
    }

    @Deprecated
    public static boolean oneAndOneTenthXp(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission("mcmmo.perks.xp.10percentboost.all")
                || permissible.hasPermission(XP_10_PERCENT_NODES.get(skill));
    }

    @Deprecated
    public static boolean customXpBoost(final Permissible permissible, final PrimarySkillType skill) {
        return permissible.hasPermission("mcmmo.perks.xp.customboost.all")
                || permissible.hasPermission(XP_CUSTOM_BOOST_NODES.get(skill));
    }

    /* ACTIVATION PERKS */
    public static boolean twelveSecondActivationBoost(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.perks.activationtime.twelveseconds");
    }

    public static boolean eightSecondActivationBoost(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.perks.activationtime.eightseconds");
    }

    public static boolean fourSecondActivationBoost(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.perks.activationtime.fourseconds");
    }

    /* COOLDOWN PERKS */
    public static boolean halvedCooldowns(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.perks.cooldowns.halved");
    }

    public static boolean thirdedCooldowns(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.perks.cooldowns.thirded");
    }

    public static boolean quarteredCooldowns(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.perks.cooldowns.quartered");
    }

    /*
     * SKILLS
     */

    public static boolean skillEnabled(Permissible permissible, PrimarySkillType skill) {
        return permissible.hasPermission(SKILL_ENABLED_NODES.get(skill));
    }

    public static boolean vanillaXpBoost(Permissible permissible, PrimarySkillType skill) {
        return permissible.hasPermission(VANILLA_XP_BOOST_NODES.get(skill));
    }

    public static boolean isSubSkillEnabled(@Nullable final Permissible permissible,
                                            @NotNull final SubSkillType subSkillType) {
        if (permissible == null) {
            return false;
        }
        return permissible.hasPermission(subSkillType.getPermissionNodeAddress());
    }

    public static boolean isSubSkillEnabled(@Nullable final McMMOPlayer permissible,
                                            @NotNull final SubSkillType subSkillType) {
        if (permissible == null) {
            return false;
        }

        return isSubSkillEnabled(permissible.getPlayer(), subSkillType);
    }

    /* ACROBATICS */
    public static boolean dodge(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.acrobatics.dodge");
    }

    public static boolean gracefulRoll(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.acrobatics.gracefulroll");
    }

    public static boolean roll(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.acrobatics.roll");
    }

    /* ALCHEMY */
    public static boolean catalysis(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.alchemy.catalysis");
    }

    public static boolean concoctions(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.alchemy.concoctions");
    }

    /* ARCHERY */
    public static boolean arrowRetrieval(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.archery.trackarrows");
    }

    public static boolean daze(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.archery.daze");
    }

    /* AXES */
    public static boolean skullSplitter(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.axes.skullsplitter");
    }

    /* EXCAVATION */
    public static boolean gigaDrillBreaker(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.excavation.gigadrillbreaker");
    }

    /* HERBALISM */
    public static boolean greenTerra(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.herbalism.greenterra");
    }

    public static boolean greenThumbBlock(Permissible permissible, Material material) {
        return permissible.hasPermission(GREEN_THUMB_BLOCK_NODES.computeIfAbsent(material,
                mat -> "mcmmo.ability.herbalism.greenthumb.blocks."
                        + mat.toString().replace("_", "").toLowerCase(Locale.ENGLISH)));
    }

    public static boolean greenThumbPlant(Permissible permissible, Material material) {
        return permissible.hasPermission(GREEN_THUMB_PLANT_NODES.computeIfAbsent(material,
                mat -> "mcmmo.ability.herbalism.greenthumb.plants."
                        + mat.toString().replace("_", "").toLowerCase(Locale.ENGLISH)));
    }

    /* MINING */
    public static boolean biggerBombs(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.mining.blastmining.biggerbombs");
    }

    public static boolean demolitionsExpertise(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.mining.blastmining.demolitionsexpertise");
    }

    public static boolean remoteDetonation(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.mining.blastmining.detonate");
    }

    public static boolean superBreaker(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.mining.superbreaker");
    }

    /* REPAIR */
    public static boolean repairItemType(Permissible permissible, ItemType repairItemType) {
        return permissible.hasPermission(REPAIR_ITEM_TYPE_NODES.get(repairItemType));
    }

    public static boolean repairMaterialType(Permissible permissible,
            MaterialType repairMaterialType) {
        return permissible.hasPermission(REPAIR_MATERIAL_TYPE_NODES.get(repairMaterialType));
    }

    /* SALVAGE */
    public static boolean arcaneSalvage(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.salvage.arcanesalvage");
    }

    public static boolean salvageItemType(Permissible permissible, ItemType salvageItemType) {
        return permissible.hasPermission(SALVAGE_ITEM_TYPE_NODES.get(salvageItemType));
    }

    public static boolean salvageMaterialType(Permissible permissible,
            MaterialType salvageMaterialType) {
        return permissible.hasPermission(SALVAGE_MATERIAL_TYPE_NODES.get(salvageMaterialType));
    }

    /* SMELTING */
    public static boolean fluxMining(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.smelting.fluxmining");
    }

    public static boolean fuelEfficiency(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.smelting.fuelefficiency");
    }

    /* SWORDS */
    public static boolean serratedStrikes(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.swords.serratedstrikes");
    }

    /* TAMING */
    public static boolean callOfTheWild(Permissible permissible, EntityType type) {
        return permissible.hasPermission(CALL_OF_THE_WILD_NODES.get(type));
    }

    /* UNARMED */
    public static boolean berserk(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.unarmed.berserk");
    }

    /* WOODCUTTING */
    public static boolean treeFeller(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.woodcutting.treefeller");
    }

    /* CROSSBOWS */
    public static boolean trickShot(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.crossbows.trickshot");
    }

    public static boolean poweredShot(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.crossbows.poweredshot");
    }

    /* TRIDENTS */
    public static boolean tridentsLimitBreak(Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.tridents.tridentslimitbreak");
    }

    /* MACES */
    public static boolean macesLimitBreak(Permissible permissible) {
        return permissible.hasPermission("mcmmo.ability.maces.maceslimitbreak");
    }

    /*
     * PARTY
     */
    public static boolean partySizeBypass(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.bypass.partylimit");
    }

    public static boolean party(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.party");
    }

    public static boolean partySubcommand(final Permissible permissible, final PartySubcommandType subcommand) {
        return permissible.hasPermission(
                "mcmmo.commands.party." + subcommand.toString().toLowerCase(Locale.ENGLISH));
    }

    public static boolean friendlyFire(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.party.friendlyfire");
    }

    /* TELEPORT */
    public static boolean partyTeleportSend(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.ptp.send");
    }

    public static boolean partyTeleportAccept(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.ptp.accept");
    }

    public static boolean partyTeleportAcceptAll(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.ptp.acceptall");
    }

    public static boolean partyTeleportToggle(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.ptp.toggle");
    }

    public static boolean partyTeleportAllWorlds(final Permissible permissible) {
        return permissible.hasPermission("mcmmo.commands.ptp.world.all");
    }

    public static boolean partyTeleportWorld(final Permissible permissible, final World world) {
        return permissible.hasPermission("mcmmo.commands.ptp.world." + world.getName());
    }

    public static void generateWorldTeleportPermissions() {
        final Server server = mcMMO.p.getServer();
        final PluginManager pluginManager = server.getPluginManager();

        for (final World world : server.getWorlds()) {
            addDynamicPermission("mcmmo.commands.ptp.world." + world.getName(),
                    PermissionDefault.OP, pluginManager);
        }
    }

    private static void addDynamicPermission(final String permissionName,
                                             final PermissionDefault permissionDefault, final PluginManager pluginManager) {
        final Permission permission = new Permission(permissionName);
        permission.setDefault(permissionDefault);
        pluginManager.addPermission(permission);
    }

    /**
     * Checks if a player can use a skill
     *
     * @param player       target player
     * @param subSkillType target subskill
     * @return true if the player has permission and has the skill unlocked
     */
    public static boolean canUseSubSkill(@NotNull final Player player,
                                         @NotNull final SubSkillType subSkillType) {
        return isSubSkillEnabled(player, subSkillType) && RankUtils.hasUnlockedSubskill(player,
                subSkillType);
    }
}
