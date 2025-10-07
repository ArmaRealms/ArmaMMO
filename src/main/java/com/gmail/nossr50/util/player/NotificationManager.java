package com.gmail.nossr50.util.player;

import com.gmail.nossr50.datatypes.LevelUpBroadcastPredicate;
import com.gmail.nossr50.datatypes.PowerLevelUpBroadcastPredicate;
import com.gmail.nossr50.datatypes.interactions.NotificationType;
import com.gmail.nossr50.datatypes.notifications.SensitiveCommandType;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.events.skills.McMMOPlayerNotificationEvent;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.sounds.SoundManager;
import com.gmail.nossr50.util.sounds.SoundType;
import com.gmail.nossr50.util.text.McMMOMessageType;
import com.gmail.nossr50.util.text.TextComponentFactory;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NotificationManager {

    public static final String HEX_BEIGE_COLOR = "#c2a66e";
    public static final String HEX_LIME_GREEN_COLOR = "#8ec26e";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Sends players notifications from mcMMO Does so by sending out an event so other plugins can
     * cancel it
     *
     * @param player           target player
     * @param notificationType notifications defined type
     * @param key              the locale key for the notifications defined message
     */
    public static void sendPlayerInformation(final Player player, final NotificationType notificationType,
                                             final String key) {
        final McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null || !mmoPlayer.useChatNotifications()) {
            return;
        }

        final McMMOMessageType destination
                = mcMMO.p.getAdvancedConfig().doesNotificationUseActionBar(notificationType)
                ? McMMOMessageType.ACTION_BAR : McMMOMessageType.SYSTEM;

        final Component message = TextComponentFactory.getNotificationTextComponentFromLocale(key);
        final McMMOPlayerNotificationEvent customEvent = checkNotificationEvent(player, notificationType,
                destination, message);

        sendNotification(player, customEvent);
    }

    public static boolean doesPlayerUseNotifications(final Player player) {
        final McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null) return false;
        else return mmoPlayer.useChatNotifications();
    }

    /**
     * Sends players notifications from mcMMO This does this by sending out an event so other
     * plugins can cancel it This event in particular is provided with a source player, and players
     * near the source player are sent the information
     *
     * @param targetPlayer     the recipient player for this message
     * @param notificationType type of notification
     * @param key              Locale Key for the string to use with this event
     * @param values           values to be injected into the locale string
     */
    public static void sendNearbyPlayersInformation(final Player targetPlayer,
                                                    final NotificationType notificationType, final String key,
                                                    final String... values) {
        sendPlayerInformation(targetPlayer, notificationType, key, values);
    }

    public static void sendPlayerInformationChatOnly(final Player player, final String key, final String... values) {
        final McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null || !mmoPlayer.useChatNotifications()) {
            return;
        }

        final String preColoredString = LocaleLoader.getString(key, (Object[]) values);
        player.sendMessage(preColoredString);
    }

    public static void sendPlayerInformationChatOnlyPrefixed(final Player player, final String key,
                                                             final String... values) {
        final McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null || !mcMMOPlayer.useChatNotifications()) {
            return;
        }

        final String preColoredString = LocaleLoader.getString(key, (Object[]) values);
        final String prefixFormattedMessage = LocaleLoader.getString("mcMMO.Template.Prefix",
                preColoredString);
        player.sendMessage(prefixFormattedMessage);
    }

    public static void sendPlayerInformation(final Player player, final NotificationType notificationType,
                                             final String key,
                                             final String... values) {
        final McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null || !mmoPlayer.useChatNotifications()) {
            return;
        }

        final McMMOMessageType destination =
                mcMMO.p.getAdvancedConfig().doesNotificationUseActionBar(notificationType)
                        ? McMMOMessageType.ACTION_BAR : McMMOMessageType.SYSTEM;

        final Component message = TextComponentFactory.getNotificationMultipleValues(key, values);
        final McMMOPlayerNotificationEvent customEvent = checkNotificationEvent(player, notificationType,
                destination, message);

        sendNotification(player, customEvent);
    }

    private static void sendNotification(final Player player, final McMMOPlayerNotificationEvent customEvent) {
        if (customEvent.isCancelled()) {
            return;
        }

        final Audience audience = mcMMO.getAudiences().player(player);

        final Component notificationTextComponent = customEvent.getNotificationTextComponent();
        if (customEvent.getChatMessageType() == McMMOMessageType.ACTION_BAR) {
            audience.sendActionBar(notificationTextComponent);

            // If the message is being sent to the action bar we need to check if a copy is also sent to the chat system
            if (customEvent.isMessageAlsoBeingSentToChat()) {
                //Send copy to chat system
                audience.sendMessage(notificationTextComponent);
            }
        } else {
            audience.sendMessage(notificationTextComponent);
        }
    }

    private static McMMOPlayerNotificationEvent checkNotificationEvent(final Player player,
                                                                       final NotificationType notificationType,
                                                                       final McMMOMessageType destination,
                                                                       final Component message) {
        //Init event
        final McMMOPlayerNotificationEvent customEvent = new McMMOPlayerNotificationEvent(player,
                notificationType, message, destination,
                mcMMO.p.getAdvancedConfig().doesNotificationSendCopyToChat(notificationType));

        //Call event
        Bukkit.getServer().getPluginManager().callEvent(customEvent);
        return customEvent;
    }

    /**
     * Handles sending level up notifications to a mmoPlayer
     *
     * @param mmoPlayer target mmoPlayer
     * @param skillName skill that leveled up
     * @param newLevel  new level of that skill
     */
    public static void sendPlayerLevelUpNotification(final McMMOPlayer mmoPlayer,
                                                     final PrimarySkillType skillName,
                                                     final int levelsGained, final int newLevel) {
        if (!mmoPlayer.useChatNotifications()) {
            return;
        }

        final McMMOMessageType destination
                = mcMMO.p.getAdvancedConfig()
                .doesNotificationUseActionBar(NotificationType.LEVEL_UP_MESSAGE)
                ? McMMOMessageType.ACTION_BAR : McMMOMessageType.SYSTEM;

        final Component levelUpTextComponent = TextComponentFactory.getNotificationLevelUpTextComponent(
                skillName, levelsGained, newLevel);
        final McMMOPlayerNotificationEvent customEvent = checkNotificationEvent(
                mmoPlayer.getPlayer(),
                NotificationType.LEVEL_UP_MESSAGE,
                destination,
                levelUpTextComponent);

        sendNotification(mmoPlayer.getPlayer(), customEvent);
    }

    public static void broadcastTitle(final Server server, final String title, final String subtitle, final int i1, final int i2,
                                      final int i3) {
        for (final Player player : server.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, i1, i2, i3);
        }
    }

    public static void sendPlayerUnlockNotification(final McMMOPlayer mmoPlayer,
                                                    final SubSkillType subSkillType) {
        if (!mmoPlayer.useChatNotifications()) {
            return;
        }

        //CHAT MESSAGE
        mcMMO.getAudiences().player(mmoPlayer.getPlayer()).sendMessage(Identity.nil(),
                TextComponentFactory.getSubSkillUnlockedNotificationComponents(
                        mmoPlayer.getPlayer(), subSkillType));

        //Unlock Sound Effect
        SoundManager.sendCategorizedSound(mmoPlayer.getPlayer(),
                mmoPlayer.getPlayer().getLocation(),
                SoundType.SKILL_UNLOCKED, SoundCategory.MASTER);
    }

    /**
     * Sends a message to all admins with the admin notification formatting from the locale Admins
     * are currently players with either Operator status or Admin Chat permission
     *
     * @param msg message fetched from locale
     */
    private static void sendAdminNotification(final String msg) {
        //If its not enabled exit
        if (!mcMMO.p.getGeneralConfig().adminNotifications()) {
            return;
        }

        for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.isOp() || Permissions.adminChat(player)) {
                player.sendMessage(
                        LocaleLoader.getString("Notifications.Admin.Format.Others", msg));
            }
        }

        //Copy it out to Console too
        mcMMO.p.getLogger().info(LocaleLoader.getString("Notifications.Admin.Format.Others", msg));
    }

    /**
     * Sends a confirmation message to the CommandSender who just executed an admin command
     *
     * @param commandSender target command sender
     * @param msg           message fetched from locale
     */
    private static void sendAdminCommandConfirmation(final CommandSender commandSender, final String msg) {
        commandSender.sendMessage(LocaleLoader.getString("Notifications.Admin.Format.Self", msg));
    }

    /**
     * Convenience method to report info about a command sender using a sensitive command
     *
     * @param commandSender        the command user
     * @param sensitiveCommandType type of command issued
     */
    public static void processSensitiveCommandNotification(final CommandSender commandSender,
                                                           final SensitiveCommandType sensitiveCommandType, final String... args) {
        /*
         * Determine the 'identity' of the one who executed the command to pass as a parameters
         */
        String senderName = LocaleLoader.getString("Server.ConsoleName");

        if (commandSender instanceof final Player player) {
            senderName = player.getDisplayName() + ChatColor.RESET + "-" + player.getUniqueId();
        }

        //Send the notification
        switch (sensitiveCommandType) {
            case XPRATE_MODIFY -> {
                sendAdminNotification(
                        LocaleLoader.getString("Notifications.Admin.XPRate.Start.Others",
                                addItemToFirstPositionOfArray(senderName, args)));
                sendAdminCommandConfirmation(
                        commandSender,
                        LocaleLoader.getString("Notifications.Admin.XPRate.Start.Self", args));
            }

            case XPRATE_END -> {
                sendAdminNotification(
                        LocaleLoader.getString(
                                "Notifications.Admin.XPRate.End.Others",
                                addItemToFirstPositionOfArray(senderName, args)));
                sendAdminCommandConfirmation(commandSender,
                        LocaleLoader.getString("Notifications.Admin.XPRate.End.Self", args));
            }
        }
    }

    /**
     * Takes an array and an object, makes a new array with object in the first position of the new
     * array, and the following elements in this new array being a copy of the existing array
     * retaining their order
     *
     * @param itemToAdd     the string to put at the beginning of the new array
     * @param existingArray the existing array to be copied to the new array at position [0]+1
     *                      relative to their original index
     * @return the new array combining itemToAdd at the start and existing array elements following
     * while retaining their order
     */
    public static String[] addItemToFirstPositionOfArray(final String itemToAdd,
                                                         final String... existingArray) {
        final String[] newArray = new String[existingArray.length + 1];
        newArray[0] = itemToAdd;

        System.arraycopy(existingArray, 0, newArray, 1, existingArray.length);

        return newArray;
    }

    public static void processLevelUpBroadcasting(@NotNull final McMMOPlayer mmoPlayer,
                                                  @NotNull final PrimarySkillType primarySkillType, final int level) {
        if (level <= 0) {
            return;
        }

        //Check if broadcasting is enabled
        if (mcMMO.p.getGeneralConfig().shouldLevelUpBroadcasts()) {
            //Permission check
            if (!Permissions.levelUpBroadcast(mmoPlayer.getPlayer())) {
                return;
            }

            final int levelInterval = mcMMO.p.getGeneralConfig().getLevelUpBroadcastInterval();
            final int remainder = level % levelInterval;

            if (remainder == 0) {
                //Grab appropriate audience
                final Audience audience = mcMMO.getAudiences()
                        .filter(getLevelUpBroadcastPredicate(mmoPlayer.getPlayer()));
                //TODO: Make prettier
                final HoverEvent<Component> levelMilestoneHover = Component.text(
                                mmoPlayer.getPlayer().getName())
                        .append(Component.newline())
                        .append(Component.text("Data: " + LocalDate.now().format(DATE_FORMATTER))).color(TextColor.fromHexString(HEX_LIME_GREEN_COLOR))
                        .append(Component.newline())
                        .append(Component.text(
                                mcMMO.p.getSkillTools().getLocalizedSkillName(primarySkillType)
                                        + " alcançou nível " + level)).color(TextColor.fromHexString(HEX_BEIGE_COLOR))
                        .asHoverEvent();

                final String localeMessage = LocaleLoader.getString(
                        "Broadcasts.LevelUpMilestone", mmoPlayer.getPlayer().getDisplayName(),
                        level,
                        mcMMO.p.getSkillTools().getLocalizedSkillName(primarySkillType));
                final Component component = LegacyComponentSerializer
                        .legacySection()
                        .deserialize(localeMessage)
                        .hoverEvent(levelMilestoneHover);

                // TODO: Update system msg API
                mcMMO.p.getFoliaLib().getScheduler().runNextTick(
                        t -> audience.sendMessage(component));
            }
        }
    }

    //TODO: Remove the code duplication, am lazy atm
    //TODO: Fix broadcasts being skipped for situations where a player skips over the milestone like with the addlevels command
    public static void processPowerLevelUpBroadcasting(@NotNull final McMMOPlayer mmoPlayer,
                                                       final int powerLevel) {
        if (powerLevel <= 0) {
            return;
        }

        //Check if broadcasting is enabled
        if (mcMMO.p.getGeneralConfig().shouldPowerLevelUpBroadcasts()) {
            //Permission check
            if (!Permissions.levelUpBroadcast(mmoPlayer.getPlayer())) {
                return;
            }

            final int levelInterval = mcMMO.p.getGeneralConfig().getPowerLevelUpBroadcastInterval();
            final int remainder = powerLevel % levelInterval;

            if (remainder == 0) {
                //Grab appropriate audience
                final Audience audience = mcMMO.getAudiences()
                        .filter(getPowerLevelUpBroadcastPredicate(mmoPlayer.getPlayer()));
                //TODO: Make prettier
                final HoverEvent<Component> levelMilestoneHover = Component.text(
                                mmoPlayer.getPlayer().getName())
                        .append(Component.newline())
                        .append(Component.text("Data: " + LocalDate.now().format(DATE_FORMATTER)).color(TextColor.fromHexString(HEX_LIME_GREEN_COLOR)))
                        .append(Component.newline())
                        .append(Component.text("Nível de Poder: " + powerLevel))
                        .color(TextColor.fromHexString(HEX_BEIGE_COLOR))
                        .asHoverEvent();

                final String localeMessage = LocaleLoader.getString("Broadcasts.PowerLevelUpMilestone",
                        mmoPlayer.getPlayer().getDisplayName(), powerLevel);
                final Component message = LegacyComponentSerializer.legacySection()
                        .deserialize(localeMessage).hoverEvent(levelMilestoneHover);

                mcMMO.p.getFoliaLib().getScheduler()
                        .runNextTick(t -> audience.sendMessage(message));
            }
        }
    }

    //TODO: Could cache
    public static @NotNull LevelUpBroadcastPredicate<CommandSender> getLevelUpBroadcastPredicate(
            @NotNull final CommandSender levelUpPlayer) {
        return new LevelUpBroadcastPredicate<>(levelUpPlayer);
    }

    public static @NotNull PowerLevelUpBroadcastPredicate<CommandSender> getPowerLevelUpBroadcastPredicate(
            @NotNull final CommandSender levelUpPlayer) {
        return new PowerLevelUpBroadcastPredicate<>(levelUpPlayer);
    }

}
