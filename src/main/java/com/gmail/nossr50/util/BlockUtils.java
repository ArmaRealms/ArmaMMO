package com.gmail.nossr50.util;

import static java.util.Objects.requireNonNull;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.meta.BonusDropMeta;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.repair.Repair;
import com.gmail.nossr50.skills.salvage.Salvage;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.random.ProbabilityUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class BlockUtils {

    public static final String SHORT_GRASS = "SHORT_GRASS";
    public static final String GRASS = "GRASS";

    private BlockUtils() {
    }

    /**
     * Mark a block for giving bonus drops, double drops are used if triple is false
     *
     * @param blockState target blockstate
     * @param triple     marks the block to give triple drops
     */
    @Deprecated(forRemoval = true, since = "2.2.024")
    public static void markDropsAsBonus(final BlockState blockState, final boolean triple) {
        if (triple) {
            blockState.getBlock().setMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS,
                    new BonusDropMeta(2, mcMMO.p));
        } else {
            blockState.getBlock().setMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS,
                    new BonusDropMeta(1, mcMMO.p));
        }
    }

    public static void markDropsAsBonus(final Block block, final boolean triple) {
        if (triple) {
            block.setMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS,
                    new BonusDropMeta(2, mcMMO.p));
        } else {
            block.setMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS,
                    new BonusDropMeta(1, mcMMO.p));
        }
    }

    /**
     * Util method for compatibility across Minecraft versions, grabs the {@link Material} enum for
     * short_grass
     *
     * @return the {@link Material} enum for short_grass
     */
    public static Material getShortGrass() {
        if (Material.getMaterial(SHORT_GRASS) != null) {
            return Material.getMaterial(SHORT_GRASS);
        } else if (Material.getMaterial(GRASS) != null) {
            return Material.getMaterial(GRASS);
        } else {
            throw new UnsupportedOperationException("Unable to find short grass material");
        }
    }

    /**
     * Set up the state for a block to be seen as unnatural and cleanup any unwanted metadata from
     * the block
     *
     * @param block target block
     */
    public static void setUnnaturalBlock(@NotNull final Block block) {
        mcMMO.getUserBlockTracker().setIneligible(block);

        // Failsafe against lingering metadata
        if (block.hasMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS)) {
            block.removeMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS, mcMMO.p);
        }
    }

    /**
     * Cleans up some block metadata when a block breaks and the metadata is no longer needed This
     * also sets the blocks coords to false in our chunk store
     *
     * @param block target block
     */
    public static void cleanupBlockMetadata(final Block block) {
        if (block.hasMetadata(MetadataConstants.METADATA_KEY_REPLANT)) {
            block.removeMetadata(MetadataConstants.METADATA_KEY_REPLANT, mcMMO.p);
        }

        mcMMO.getUserBlockTracker().setEligible(block);
    }

    /**
     * Marks a block to drop extra copies of items
     *
     * @param blockState target blockstate
     * @param amount     amount of extra items to drop
     */
    @Deprecated(forRemoval = true, since = "2.2.024")
    public static void markDropsAsBonus(final BlockState blockState, final int amount) {
        blockState.setMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS,
                new BonusDropMeta(amount, mcMMO.p));
    }

    /**
     * Marks a block to drop extra copies of items
     *
     * @param block  target block
     * @param amount the number of extra items to drop
     */
    public static void markDropsAsBonus(final Block block, final int amount) {
        block.setMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS,
                new BonusDropMeta(amount, mcMMO.p));
    }

    /**
     * Checks if a player successfully passed the double drop check
     *
     * @param blockState the blockstate
     * @return true if the player succeeded in the check
     * @deprecated Use {@link #checkDoubleDrops(McMMOPlayer, BlockState, SubSkillType)} instead
     */
    @Deprecated(forRemoval = true, since = "2.2.010")
    public static boolean checkDoubleDrops(final Player player, final BlockState blockState,
                                           final PrimarySkillType ignored, final SubSkillType subSkillType) {
        return checkDoubleDrops(UserManager.getPlayer(player), blockState, subSkillType);
    }

    /**
     * Checks if a player successfully passed the double drop check
     *
     * @param mmoPlayer    the player involved in the check
     * @param blockState   the blockstate of the block
     * @param subSkillType the subskill involved
     * @return true if the player succeeded in the check
     */
    @Deprecated(forRemoval = true, since = "2.2.024")
    public static boolean checkDoubleDrops(@Nullable final McMMOPlayer mmoPlayer,
                                           @NotNull final BlockState blockState,
                                           @NotNull final SubSkillType subSkillType) {
        requireNonNull(blockState, "blockState cannot be null");
        requireNonNull(subSkillType, "subSkillType cannot be null");
        if (mcMMO.p.getGeneralConfig()
                .getDoubleDropsEnabled(subSkillType.getParentSkill(), blockState.getType())
                && Permissions.isSubSkillEnabled(mmoPlayer, subSkillType)) {
            return ProbabilityUtil.isSkillRNGSuccessful(subSkillType, mmoPlayer);
        }

        return false;
    }

    /**
     * Checks if a player successfully passed the double drop check
     *
     * @param mmoPlayer    the player involved in the check
     * @param block        the block
     * @param subSkillType the subskill involved
     * @return true if the player succeeded in the check
     */
    public static boolean checkDoubleDrops(@Nullable final McMMOPlayer mmoPlayer, @NotNull final Block block,
                                           @NotNull final SubSkillType subSkillType) {
        requireNonNull(block, "block cannot be null");
        requireNonNull(subSkillType, "subSkillType cannot be null");
        if (mcMMO.p.getGeneralConfig()
                .getDoubleDropsEnabled(subSkillType.getParentSkill(), block.getType())
                && Permissions.isSubSkillEnabled(mmoPlayer, subSkillType)) {
            return ProbabilityUtil.isSkillRNGSuccessful(subSkillType, mmoPlayer);
        }

        return false;
    }

    /**
     * Checks to see if a given block awards XP.
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block awards XP, false otherwise
     */
    public static boolean shouldBeWatched(final BlockState blockState) {
        return shouldBeWatched(blockState.getType());
    }

    public static boolean shouldBeWatched(final Material material) {
        return affectedByGigaDrillBreaker(material)
                || affectedByGreenTerra(material)
                || affectedBySuperBreaker(material)
                || hasWoodcuttingXP(material)
                || mcMMO.p.getGeneralConfig()
                .getDoubleDropsEnabled(PrimarySkillType.MINING, material)
                || mcMMO.p.getGeneralConfig()
                .getDoubleDropsEnabled(PrimarySkillType.EXCAVATION, material)
                || mcMMO.p.getGeneralConfig()
                .getDoubleDropsEnabled(PrimarySkillType.WOODCUTTING, material)
                || mcMMO.p.getGeneralConfig()
                .getDoubleDropsEnabled(PrimarySkillType.SMELTING, material);
    }

    /**
     * Check if a given block should allow for the activation of abilities
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block should allow ability activation, false otherwise
     */
    @Deprecated(forRemoval = true, since = "2.2.024")
    public static boolean canActivateAbilities(final BlockState blockState) {
        return !mcMMO.getMaterialMapStore().isAbilityActivationBlackListed(blockState.getType());
    }

    public static boolean canActivateAbilities(final Block block) {
        return !mcMMO.getMaterialMapStore().isAbilityActivationBlackListed(block.getType());
    }

    /**
     * Check if a given block should allow for the activation of tools Activating a tool is step 1
     * of a 2 step process for super ability activation
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block should allow ability activation, false otherwise
     */
    public static boolean canActivateTools(final BlockState blockState) {
        return !mcMMO.getMaterialMapStore().isToolActivationBlackListed(blockState.getType())
                && blockState.getType() != Repair.anvilMaterial
                && blockState.getType() != Salvage.anvilMaterial;
    }

    /**
     * Check if a given block is an ore
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block is an ore, false otherwise
     */
    public static boolean isOre(final BlockState blockState) {
        return isOre(blockState.getType());
    }

    /**
     * Check if a given block is an ore
     *
     * @param block The {@link Block} to check
     * @return true if the block is an ore, false otherwise
     */
    public static boolean isOre(final Block block) {
        return isOre(block.getType());
    }

    /**
     * Check if a given material is an ore
     *
     * @param material The {@link Material} to check
     * @return true if the material is an ore, false otherwise
     */
    public static boolean isOre(final Material material) {
        return MaterialUtils.isOre(material);
    }

    /**
     * Determine if a given block can be made mossy
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block can be made mossy, false otherwise
     */
    @Deprecated(since = "2.2.024")
    public static boolean canMakeMossy(final BlockState blockState) {
        return mcMMO.getMaterialMapStore().isMossyWhiteListed(blockState.getType());
    }

    public static boolean canMakeMossy(final Block block) {
        return mcMMO.getMaterialMapStore().isMossyWhiteListed(block.getType());
    }

    /**
     * Determine if a given block should be affected by Green Terra
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block should affected by Green Terra, false otherwise
     */
    public static boolean affectedByGreenTerra(final BlockState blockState) {
        return affectedByGreenTerra(blockState.getType());
    }

    public static boolean affectedByGreenTerra(final Block block) {
        return affectedByGreenTerra(block.getType());
    }

    public static boolean affectedByGreenTerra(final Material material) {
        return ExperienceConfig.getInstance()
                .doesBlockGiveSkillXP(PrimarySkillType.HERBALISM, material);
    }

    public static boolean affectedBySuperBreaker(final BlockState blockState) {
        return affectedBySuperBreaker(blockState.getType());
    }

    /**
     * Determine if a given block should be affected by Super Breaker
     *
     * @param block The {@link Block} to check
     * @return true if the block should affected by Super Breaker, false otherwise
     */
    public static boolean affectedBySuperBreaker(final Block block) {
        return affectedBySuperBreaker(block.getType());
    }

    public static boolean affectedBySuperBreaker(final Material material) {
        if (mcMMO.getMaterialMapStore().isIntendedToolPickaxe(material)) {
            return true;
        }

        return ExperienceConfig.getInstance()
                .doesBlockGiveSkillXP(PrimarySkillType.MINING, material);
    }

    /**
     * Determine if a given block should be affected by Giga Drill Breaker
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block should be affected by Giga Drill Breaker, false otherwise
     */
    public static boolean affectedByGigaDrillBreaker(@NotNull final BlockState blockState) {
        return affectedByGigaDrillBreaker(blockState.getType());
    }

    public static boolean affectedByGigaDrillBreaker(@NotNull final Block block) {
        return affectedByGigaDrillBreaker(block.getType());
    }

    public static boolean affectedByGigaDrillBreaker(@NotNull final Material material) {
        return ExperienceConfig.getInstance()
                .doesBlockGiveSkillXP(PrimarySkillType.EXCAVATION, material);
    }

    /**
     * Check if a given block is a log
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block is a log, false otherwise
     */
    public static boolean hasWoodcuttingXP(@NotNull final BlockState blockState) {
        return hasWoodcuttingXP(blockState.getType());
    }

    public static boolean hasWoodcuttingXP(@NotNull final Block block) {
        return hasWoodcuttingXP(block.getType());
    }

    public static boolean hasWoodcuttingXP(@NotNull final Material material) {
        return ExperienceConfig.getInstance()
                .doesBlockGiveSkillXP(PrimarySkillType.WOODCUTTING, material);
    }

    /**
     * Check if a given block is a leaf
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block is a leaf, false otherwise
     */
    public static boolean isNonWoodPartOfTree(@NotNull final BlockState blockState) {
        return isNonWoodPartOfTree(blockState.getType());
    }

    public static boolean isNonWoodPartOfTree(@NotNull final Block block) {
        return isNonWoodPartOfTree(block.getType());
    }

    public static boolean isNonWoodPartOfTree(@NotNull final Material material) {
        return mcMMO.getMaterialMapStore().isTreeFellerDestructible(material);
    }

//    /**
//     * Determine if a given block should be affected by Flux Mining
//     *
//     * @param blockState The {@link BlockState} of the block to check
//     * @return true if the block should affected by Flux Mining, false otherwise
//     */
//    public static boolean affectedByFluxMining(BlockState blockState) {
//        switch (blockState.getType()) {
//            case IRON_ORE:
//            case GOLD_ORE:
//                return true;
//
//            default:
//                return false;
//        }
//    }

    /**
     * Determine if a given block can activate Herbalism abilities
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block can be activate Herbalism abilities, false otherwise
     */
    public static boolean canActivateHerbalism(final BlockState blockState) {
        return mcMMO.getMaterialMapStore().isHerbalismAbilityWhiteListed(blockState.getType());
    }

    /**
     * Determine if a given block should be affected by Block Cracker
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block should affected by Block Cracker, false otherwise
     */
    public static boolean affectedByBlockCracker(final BlockState blockState) {
        return affectedByBlockCracker(blockState.getType());
    }

    public static boolean affectedByBlockCracker(final Block block) {
        return affectedByBlockCracker(block.getType());
    }

    public static boolean affectedByBlockCracker(final Material material) {
        return mcMMO.getMaterialMapStore().isBlockCrackerWhiteListed(material);
    }

    /**
     * Determine if a given block can be made into Mycelium
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block can be made into Mycelium, false otherwise
     */
    public static boolean canMakeShroomy(final BlockState blockState) {
        return mcMMO.getMaterialMapStore().isShroomyWhiteListed(blockState.getType());
    }

    /**
     * Determine if a given block is an mcMMO anvil
     *
     * @param blockState The {@link BlockState} of the block to check
     * @return true if the block is an mcMMO anvil, false otherwise
     */
    public static boolean isMcMMOAnvil(final BlockState blockState) {
        final Material type = blockState.getType();

        return type == Repair.anvilMaterial || type == Salvage.anvilMaterial;
    }

    public static boolean isPistonPiece(final BlockState blockState) {
        final Material type = blockState.getType();

        return type == Material.MOVING_PISTON || type == Material.AIR;
    }

    /**
     * Get a HashSet containing every transparent block
     *
     * @return HashSet with the IDs of every transparent block
     */
    public static HashSet<Material> getTransparentBlocks() {
        final HashSet<Material> transparentBlocks = new HashSet<>();

        for (final Material material : Material.values()) {
            if (material.isTransparent()) {
                transparentBlocks.add(material);
            }
        }

        return transparentBlocks;
    }

    public static boolean isFullyGrown(final BlockState blockState) {
        final BlockData data = blockState.getBlockData();
        if (data.getMaterial() == Material.CACTUS || data.getMaterial() == Material.SUGAR_CANE) {
            return true;
        }
        if (data instanceof final Ageable ageable) {
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return true;
    }

    public static boolean isPartOfTree(final Block block) {
        return hasWoodcuttingXP(block.getState()) || isNonWoodPartOfTree(block.getType());
    }

    /**
     * Checks to see if a Block is within the world bounds Prevent processing blocks from other
     * plugins (or perhaps odd spigot anomalies) from sending blocks that can't exist within the
     * world
     *
     * @param block
     * @return
     */
    public static boolean isWithinWorldBounds(@NotNull final Block block) {
        final World world = block.getWorld();

        //World min height = inclusive | World max height = exclusive
        return block.getY() >= world.getMinHeight() && block.getY() < world.getMaxHeight();
    }

}
