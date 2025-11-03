package com.gmail.nossr50.party;

import static java.util.Objects.requireNonNull;
import com.gmail.nossr50.datatypes.chat.ChatChannel;
import com.gmail.nossr50.datatypes.interactions.NotificationType;
import com.gmail.nossr50.datatypes.party.ItemShareType;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.party.PartyLeader;
import com.gmail.nossr50.datatypes.party.ShareMode;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.events.party.McMMOPartyAllianceChangeEvent;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent.EventReason;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.LogUtils;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.NotificationManager;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.sounds.SoundManager;
import com.gmail.nossr50.util.sounds.SoundType;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

public final class PartyManager {
    private final @NotNull List<Party> parties;
    private final @NotNull File partyFile;
    private final @NotNull mcMMO pluginRef;

    public PartyManager(@NotNull final mcMMO pluginRef) {
        this.pluginRef = pluginRef;
        final String partiesFilePath = mcMMO.getFlatFileDirectory() + "parties.yml";
        this.partyFile = new File(partiesFilePath);
        this.parties = new ArrayList<>();
    }

    /**
     * Checks if the player can join a party, parties can have a size limit, although there is a
     * permission to bypass this
     *
     * @param player      player who is attempting to join the party
     * @param targetParty the target party
     * @return true if party is full and cannot be joined
     */
    public boolean isPartyFull(@NotNull final Player player, @NotNull final Party targetParty) {
        requireNonNull(player, "player cannot be null!");
        requireNonNull(targetParty, "targetParty cannot be null!");
        return !Permissions.partySizeBypass(player) && pluginRef.getGeneralConfig()
                .getPartyMaxSize() >= 1
                && targetParty.getOnlineMembers().size() >= pluginRef.getGeneralConfig()
                .getPartyMaxSize();
    }

    public boolean areAllies(@NotNull final Player firstPlayer, @NotNull final Player secondPlayer) {
        requireNonNull(firstPlayer, "firstPlayer cannot be null!");
        requireNonNull(secondPlayer, "secondPlayer cannot be null!");

        //Profile not loaded
        final McMMOPlayer firstMmoPlayer = UserManager.getPlayer(firstPlayer);
        if (firstMmoPlayer == null) {
            return false;
        }

        //Profile not loaded
        final McMMOPlayer secondMmoPlayer = UserManager.getPlayer(secondPlayer);
        if (secondMmoPlayer == null) {
            return false;
        }

        final Party firstParty = firstMmoPlayer.getParty();
        final Party secondParty = secondMmoPlayer.getParty();

        if (firstParty == null || secondParty == null || firstParty.getAlly() == null || secondParty.getAlly() == null) {
            return false;
        }

        return firstParty.equals(secondParty.getAlly()) && secondParty.equals(firstParty.getAlly());
    }

    /**
     * Get the near party members.
     *
     * @param mmoPlayer The player to check
     * @return the near party members
     */
    public @NotNull List<Player> getNearMembers(@NotNull final McMMOPlayer mmoPlayer) {
        requireNonNull(mmoPlayer, "mmoPlayer cannot be null!");
        final List<Player> nearMembers = new ArrayList<>();
        final Party party = mmoPlayer.getParty();

        if (party != null) {
            final Player player = mmoPlayer.getPlayer();
            final double range = pluginRef.getGeneralConfig().getPartyShareRange();

            for (final Player member : party.getOnlineMembers()) {
                if (!player.equals(member) && member.isValid() && Misc.isNear(player.getLocation(),
                        member.getLocation(), range)) {
                    nearMembers.add(member);
                }
            }
        }

        return nearMembers;
    }

    public @NotNull List<Player> getNearVisibleMembers(@NotNull final McMMOPlayer mmoPlayer) {
        requireNonNull(mmoPlayer, "mmoPlayer cannot be null!");
        final List<Player> nearMembers = new ArrayList<>();
        final Party party = mmoPlayer.getParty();

        if (party != null) {
            final Player player = mmoPlayer.getPlayer();
            final double range = pluginRef.getGeneralConfig().getPartyShareRange();

            for (final Player member : party.getVisibleMembers(player)) {
                if (!player.equals(member) && member.isValid() && Misc.isNear(player.getLocation(),
                        member.getLocation(), range)) {
                    nearMembers.add(member);
                }
            }
        }

        return nearMembers;
    }

    /**
     * Get a list of all players in this player's party.
     *
     * @param player The player to check
     * @return all the players in the player's party
     */
    public @NotNull LinkedHashMap<UUID, String> getAllMembers(@NotNull final Player player) {
        requireNonNull(player, "player cannot be null!");
        final Party party = getParty(player);

        return party == null ? new LinkedHashMap<>() : party.getMembers();
    }

    /**
     * Get a list of all online players in this party.
     *
     * @param partyName The party to check
     * @return all online players in this party
     */
    public @NotNull List<Player> getOnlineMembers(@NotNull final String partyName) {
        requireNonNull(partyName, "partyName cannot be null!");
        return getOnlineMembers(getParty(partyName));
    }

    /**
     * Get a list of all online players in this party.
     *
     * @param player The player to check
     * @return all online players in this party
     */
    public @NotNull List<Player> getOnlineMembers(@NotNull final Player player) {
        requireNonNull(player, "player cannot be null!");
        return getOnlineMembers(getParty(player));
    }

    private List<Player> getOnlineMembers(@Nullable final Party party) {
        return party == null ? new ArrayList<>() : party.getOnlineMembers();
    }

    /**
     * Retrieve a party by its name
     *
     * @param partyName The party name
     * @return the existing party, null otherwise
     */
    public @Nullable Party getParty(@NotNull final String partyName) {
        requireNonNull(partyName, "partyName cannot be null!");
        for (final Party party : parties) {
            if (party.getName().equalsIgnoreCase(partyName)) {
                return party;
            }
        }

        return null;
    }

    /**
     * Retrieve a party by a members name
     *
     * @param playerName The members name
     * @return the existing party, null otherwise
     */
    @Deprecated
    public @Nullable Party getPlayerParty(@NotNull final String playerName) {
        requireNonNull(playerName, "playerName cannot be null!");
        for (final Party party : parties) {
            if (party.getMembers().containsValue(playerName)) {
                return party;
            }
        }

        return null;
    }

    /**
     * Retrieve a party by a members uuid
     *
     * @param uuid The members uuid
     * @return the existing party, null otherwise
     */
    public @Nullable Party getPlayerParty(@NotNull final String playerName, @NotNull final UUID uuid) {
        requireNonNull(playerName, "playerName cannot be null!");
        requireNonNull(uuid, "uuid cannot be null!");
        for (final Party party : parties) {
            final LinkedHashMap<UUID, String> members = party.getMembers();
            if (members.containsKey(uuid) || members.containsValue(playerName)) {

                // Name changes
                if (members.get(uuid) == null || !members.get(uuid).equals(playerName)) {
                    members.put(uuid, playerName);
                }

                return party;
            }
        }

        return null;
    }

    /**
     * Retrieve a party by member
     *
     * @param player The member
     * @return the existing party, null otherwise
     */
    public @Nullable Party getParty(@NotNull final Player player) {
        requireNonNull(player, "player cannot be null!");
        //Profile not loaded
        if (UserManager.getPlayer(player) == null) {
            return null;
        }

        final McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null) {
            return null;
        }

        return mmoPlayer.getParty();
    }

    /**
     * Get a list of all current parties.
     *
     * @return the list of parties.
     */
    public @NotNull List<Party> getParties() {
        return parties;
    }

    /**
     * Remove a player from a party.
     *
     * @param player The player to remove
     * @param party  The party
     */
    public void removeFromParty(@NotNull final OfflinePlayer player, @NotNull final Party party) {
        requireNonNull(player, "player cannot be null!");
        requireNonNull(party, "party cannot be null!");

        final LinkedHashMap<UUID, String> members = party.getMembers();
        final String playerName = player.getName();

        if (party.getLeader().getUniqueId().equals(player.getUniqueId())) {
            members.remove(player.getUniqueId());
            if (!members.isEmpty()) {
                for (final Entry<UUID, String> entry : members.entrySet()) {
                    final UUID memberUUID = entry.getKey();
                    final String memberName = entry.getValue();
                    if (!memberUUID.equals(party.getLeader().getUniqueId())) {
                        party.setLeader(new PartyLeader(memberUUID, memberName));
                        break;
                    }
                }
            }

        } else {
            members.remove(player.getUniqueId());
        }

        if (player.isOnline()) {
            party.getOnlineMembers().remove(player.getPlayer());
        }

        if (members.isEmpty()) {
            parties.remove(party);
        } else {
            informPartyMembersQuit(party, playerName);
        }
    }

    /**
     * Remove a player from a party.
     *
     * @param mmoPlayer The player to remove
     */
    public void removeFromParty(@NotNull final McMMOPlayer mmoPlayer) {
        requireNonNull(mmoPlayer, "mmoPlayer cannot be null!");
        if (mmoPlayer.getParty() == null) {
            return;
        }

        removeFromParty(mmoPlayer.getPlayer(), mmoPlayer.getParty());
        processPartyLeaving(mmoPlayer);
    }

    /**
     * Disband a party. Kicks out all members and removes the party.
     *
     * @param party The party to remove
     * @deprecated Use {@link #disbandParty(McMMOPlayer, Party)}
     */
    @Deprecated
    public void disbandParty(@NotNull final Party party) {
        requireNonNull(party, "party cannot be null!");
        disbandParty(null, party);
    }

    /**
     * Disband a party. Kicks out all members and removes the party.
     *
     * @param mmoPlayer The player to remove (can be null? lol)
     * @param party     The party to remove
     */
    public void disbandParty(@Nullable final McMMOPlayer mmoPlayer, @NotNull final Party party) {
        requireNonNull(party, "party cannot be null!");
        //TODO: Potential issues with unloaded profile?
        for (final Player member : party.getOnlineMembers()) {
            //Profile not loaded
            final McMMOPlayer mmoMember = UserManager.getPlayer(member);
            if (mmoMember == null) {
                continue;
            }

            processPartyLeaving(mmoMember);
        }

        // Disband the alliance between the disbanded party and it's ally
        if (party.getAlly() != null) {
            party.getAlly().setAlly(null);
        }

        parties.remove(party);
        if (mmoPlayer != null) {
            handlePartyChangeEvent(mmoPlayer.getPlayer(), party.getName(), null,
                    EventReason.DISBANDED_PARTY);
        }
    }

    /**
     * Create a new party
     *
     * @param mmoPlayer The player to add to the party
     * @param partyName The party to add the player to
     * @param password  The password for this party, null if there was no password
     */
    public void createParty(@NotNull final McMMOPlayer mmoPlayer, @NotNull final String partyName,
                            @Nullable final String password) {
        final Player player = mmoPlayer.getPlayer();

        final Party party = new Party(new PartyLeader(player.getUniqueId(), player.getName()),
                partyName.replace(".", ""),
                password);

        if (password != null) {
            player.sendMessage(LocaleLoader.getString("Party.Password.Set", password));
        }

        parties.add(party);

        player.sendMessage(LocaleLoader.getString("Commands.Party.Create", party.getName()));
        addToParty(mmoPlayer, party);
        handlePartyChangeEvent(player, null, partyName, EventReason.CREATED_PARTY);
    }

    /**
     * Check if a player can join a party
     *
     * @param player   The player trying to join a party
     * @param party    The party
     * @param password The password provided by the player
     * @return true if the player can join the party
     */
    public boolean checkPartyPassword(@NotNull final Player player, @NotNull final Party party,
                                      @Nullable final String password) {
        if (party.isLocked()) {
            final String partyPassword = party.getPassword();

            if (partyPassword == null) {
                player.sendMessage(LocaleLoader.getString("Party.Locked"));
                return false;
            }

            if (password == null) {
                player.sendMessage(LocaleLoader.getString("Party.Password.None"));
                return false;
            }

            if (!password.equals(partyPassword)) {
                player.sendMessage(LocaleLoader.getString("Party.Password.Incorrect"));
                return false;
            }
        }

        return true;
    }

    /**
     * Accept a party invitation
     *
     * @param mmoPlayer The player to add to the party
     */
    public void joinInvitedParty(@NotNull final McMMOPlayer mmoPlayer) {
        requireNonNull(mmoPlayer, "mmoPlayer cannot be null!");
        final Party invite = mmoPlayer.getPartyInvite();

        // Check if the party still exists, it might have been disbanded
        if (!parties.contains(invite)) {
            NotificationManager.sendPlayerInformation(mmoPlayer.getPlayer(),
                    NotificationType.PARTY_MESSAGE,
                    "Party.Disband");
            return;
        }

        /*
         * Don't let players join a full party
         */
        if (pluginRef.getGeneralConfig().getPartyMaxSize() > 0 && invite.getMembers()
                .size() >= pluginRef.getGeneralConfig().getPartyMaxSize()) {
            NotificationManager.sendPlayerInformation(mmoPlayer.getPlayer(),
                    NotificationType.PARTY_MESSAGE,
                    "Commands.Party.PartyFull.InviteAccept", invite.getName(),
                    String.valueOf(pluginRef.getGeneralConfig().getPartyMaxSize()));
            return;
        }

        NotificationManager.sendPlayerInformation(mmoPlayer.getPlayer(),
                NotificationType.PARTY_MESSAGE,
                "Commands.Party.Invite.Accepted", invite.getName());
        mmoPlayer.removePartyInvite();
        addToParty(mmoPlayer, invite);
    }

    /**
     * Accept a party alliance invitation
     *
     * @param mmoPlayer The player who accepts the alliance invite
     */
    public void acceptAllianceInvite(@NotNull final McMMOPlayer mmoPlayer) {
        requireNonNull(mmoPlayer, "mmoPlayer cannot be null!");
        final Party invite = mmoPlayer.getPartyAllianceInvite();
        final Player player = mmoPlayer.getPlayer();

        // Check if the party still exists, it might have been disbanded
        if (!parties.contains(invite)) {
            player.sendMessage(LocaleLoader.getString("Party.Disband"));
            return;
        }

        final Party ownParty = mmoPlayer.getParty();
        if (ownParty == null) {
            player.sendMessage(LocaleLoader.getString("Party.NoParty"));
            return;
        }

        if (!handlePartyChangeAllianceEvent(player, ownParty.getName(), invite.getName(),
                McMMOPartyAllianceChangeEvent.EventReason.FORMED_ALLIANCE)) {
            return;
        }

        player.sendMessage(LocaleLoader.getString("Commands.Party.Alliance.Invite.Accepted", invite.getName()));
        mmoPlayer.removePartyAllianceInvite();

        createAlliance(ownParty, invite);
    }

    public void createAlliance(@NotNull final Party firstParty, @NotNull final Party secondParty) {
        requireNonNull(firstParty, "firstParty cannot be null!");
        requireNonNull(secondParty, "secondParty cannot be null!");

        firstParty.setAlly(secondParty);
        secondParty.setAlly(firstParty);

        for (final Player member : firstParty.getOnlineMembers()) {
            member.sendMessage(
                    LocaleLoader.getString("Party.Alliance.Formed", secondParty.getName()));
        }

        for (final Player member : secondParty.getOnlineMembers()) {
            member.sendMessage(
                    LocaleLoader.getString("Party.Alliance.Formed", firstParty.getName()));
        }
    }

    public boolean disbandAlliance(@NotNull final Player player, @NotNull final Party firstParty,
                                   @NotNull final Party secondParty) {
        requireNonNull(player, "player cannot be null!");
        requireNonNull(firstParty, "firstParty cannot be null!");
        requireNonNull(secondParty, "secondParty cannot be null!");

        if (!handlePartyChangeAllianceEvent(player, firstParty.getName(), secondParty.getName(),
                McMMOPartyAllianceChangeEvent.EventReason.DISBAND_ALLIANCE)) {
            return false;
        }

        disbandAlliance(firstParty, secondParty);
        return true;
    }

    private void disbandAlliance(@NotNull final Party firstParty, @NotNull final Party secondParty) {
        requireNonNull(firstParty, "firstParty cannot be null!");
        requireNonNull(secondParty, "secondParty cannot be null!");
        firstParty.setAlly(null);
        secondParty.setAlly(null);

        for (final Player member : firstParty.getOnlineMembers()) {
            member.sendMessage(
                    LocaleLoader.getString("Party.Alliance.Disband", secondParty.getName()));
        }

        for (final Player member : secondParty.getOnlineMembers()) {
            member.sendMessage(
                    LocaleLoader.getString("Party.Alliance.Disband", firstParty.getName()));
        }
    }

    /**
     * Add a player to a party
     *
     * @param mmoPlayer The player to add to the party
     * @param party     The party
     */
    public void addToParty(@NotNull final McMMOPlayer mmoPlayer, @NotNull final Party party) {
        requireNonNull(mmoPlayer, "mmoPlayer cannot be null!");
        requireNonNull(party, "party cannot be null!");

        final Player player = mmoPlayer.getPlayer();
        final String playerName = player.getName();

        informPartyMembersJoin(party, playerName);
        mmoPlayer.setParty(party);
        party.getMembers().put(player.getUniqueId(), player.getName());
        party.getOnlineMembers().add(player);
    }

    /**
     * Get the leader of a party.
     *
     * @param partyName The party name
     * @return the leader of the party
     */
    public @Nullable String getPartyLeaderName(@NotNull final String partyName) {
        requireNonNull(partyName, "partyName cannot be null!");
        final Party party = getParty(partyName);

        return party == null ? null : party.getLeader().getPlayerName();
    }

    /**
     * Set the leader of a party.
     *
     * @param uuid  The uuid of the player to set as leader
     * @param party The party
     */
    public void setPartyLeader(@NotNull final UUID uuid, @NotNull final Party party) {
        requireNonNull(uuid, "uuid cannot be null!");
        requireNonNull(party, "party cannot be null!");
        final OfflinePlayer player = pluginRef.getServer().getOfflinePlayer(uuid);
        final UUID leaderUniqueId = party.getLeader().getUniqueId();

        for (final Player member : party.getOnlineMembers()) {
            final UUID memberUniqueId = member.getUniqueId();

            if (memberUniqueId.equals(player.getUniqueId())) {
                member.sendMessage(LocaleLoader.getString("Party.Owner.Player"));
            } else if (memberUniqueId.equals(leaderUniqueId)) {
                member.sendMessage(LocaleLoader.getString("Party.Owner.NotLeader"));
            } else {
                member.sendMessage(LocaleLoader.getString("Party.Owner.New", player.getName()));
            }
        }

        party.setLeader(new PartyLeader(player.getUniqueId(), player.getName()));
    }

    /**
     * Check if a player can invite others to his party.
     *
     * @return true if the player can invite
     */
    public boolean canInvite(@NotNull final McMMOPlayer mmoPlayer) {
        requireNonNull(mmoPlayer, "mmoPlayer cannot be null!");
        final Party party = mmoPlayer.getParty();
        if (party == null) {
            return false;
        }

        return !party.isLocked() || party.getLeader().getUniqueId()
                .equals(mmoPlayer.getPlayer().getUniqueId());
    }

    /**
     * Check if a party with a given name already exists.
     *
     * @param player    The player to notify
     * @param partyName The name of the party to check
     * @return true if a party with that name exists, false otherwise
     */
    public boolean checkPartyExistence(@NotNull final Player player, @NotNull final String partyName) {
        requireNonNull(player, "player cannot be null!");
        requireNonNull(partyName, "partyName cannot be null!");

        if (getParty(partyName) == null) {
            return false;
        }

        player.sendMessage(LocaleLoader.getString("Commands.Party.AlreadyExists", partyName));
        return true;
    }

    /**
     * Attempt to change parties or join a new party.
     *
     * @param mmoPlayer    The player changing or joining parties
     * @param newPartyName The name of the party being joined
     * @return true if the party was joined successfully, false otherwise
     */
    public boolean changeOrJoinParty(@NotNull final McMMOPlayer mmoPlayer, @NotNull final String newPartyName) {
        requireNonNull(mmoPlayer, "mmoPlayer cannot be null!");
        requireNonNull(newPartyName, "newPartyName cannot be null!");

        final Player player = mmoPlayer.getPlayer();

        if (mmoPlayer.getParty() != null && mmoPlayer.inParty()) {

            if (!handlePartyChangeEvent(player, mmoPlayer.getParty().getName(), newPartyName,
                    EventReason.CHANGED_PARTIES)) {
                return false;
            }

            removeFromParty(mmoPlayer);
        } else {
            return handlePartyChangeEvent(player, null, newPartyName, EventReason.JOINED_PARTY);
        }

        return true;
    }

    /**
     * Check if two online players are in the same party.
     *
     * @param firstPlayer  The first player
     * @param secondPlayer The second player
     * @return true if they are in the same party, false otherwise
     */
    public boolean inSameParty(@NotNull final Player firstPlayer, @NotNull final Player secondPlayer) {
        requireNonNull(firstPlayer, "firstPlayer cannot be null!");
        requireNonNull(secondPlayer, "secondPlayer cannot be null!");

        //Profile not loaded
        if (UserManager.getPlayer(firstPlayer) == null) {
            return false;
        }

        //Profile not loaded
        if (UserManager.getPlayer(secondPlayer) == null) {
            return false;
        }

        final McMMOPlayer firstMmoPlayer = UserManager.getPlayer(firstPlayer);
        if (firstMmoPlayer == null) {
            return false;
        }

        //Profile not loaded
        final McMMOPlayer secondMmoPlayer = UserManager.getPlayer(secondPlayer);
        if (secondMmoPlayer == null) {
            return false;
        }

        final Party firstParty = firstMmoPlayer.getParty();
        final Party secondParty = secondMmoPlayer.getParty();

        if (firstParty == null || secondParty == null) {
            return false;
        }

        return firstParty.equals(secondParty);
    }

    /**
     * Load party file.
     */
    public void loadParties() {
        if (!pluginRef.getPartyConfig().isPartyEnabled() || !partyFile.exists()) {
            return;
        }

        try {
            final YamlConfiguration partiesFile;
            partiesFile = YamlConfiguration.loadConfiguration(partyFile);

            final ArrayList<Party> hasAlly = new ArrayList<>();

            for (final String partyName : requireNonNull(partiesFile.getConfigurationSection("")).getKeys(false)) {
                try {
                    final Party party = new Party(partyName);

                    final String[] leaderSplit = requireNonNull(partiesFile.getString(partyName + ".Leader"))
                            .split("[|]");
                    party.setLeader(
                            new PartyLeader(UUID.fromString(leaderSplit[0]), leaderSplit[1]));
                    party.setPassword(partiesFile.getString(partyName + ".Password"));
                    party.setLocked(partiesFile.getBoolean(partyName + ".Locked"));
                    party.setLevel(partiesFile.getInt(partyName + ".Level"));
                    party.setXp(partiesFile.getInt(partyName + ".Xp"));

                    if (partiesFile.getString(partyName + ".Ally") != null) {
                        hasAlly.add(party);
                    }

                    party.setXpShareMode(
                            ShareMode.getShareMode(
                                    partiesFile.getString(partyName + ".ExpShareMode", "NONE")));
                    party.setItemShareMode(
                            ShareMode.getShareMode(
                                    partiesFile.getString(partyName + ".ItemShareMode", "NONE")));

                    for (final ItemShareType itemShareType : ItemShareType.values()) {
                        party.setSharingDrops(itemShareType,
                                partiesFile.getBoolean(
                                        partyName + ".ItemShareType." + itemShareType,
                                        true));
                    }

                    final LinkedHashMap<UUID, String> members = party.getMembers();

                    for (final String memberEntry : partiesFile.getStringList(partyName + ".Members")) {
                        final String[] memberSplit = memberEntry.split("[|]");
                        members.put(UUID.fromString(memberSplit[0]), memberSplit[1]);
                    }

                    parties.add(party);
                } catch (final Exception e) {
                    pluginRef.getLogger().log(Level.WARNING,
                            "An exception occurred while loading a party with name '" + partyName
                                    + "'. Skipped loading party.",
                            e);
                }
            }

            LogUtils.debug(pluginRef.getLogger(), "Loaded (" + parties.size() + ") Parties...");

            for (final Party party : hasAlly) {
                party.setAlly(getParty(requireNonNull(partiesFile.getString(party.getName() + ".Ally"))));
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save party file.
     */
    public void saveParties() {
        LogUtils.debug(pluginRef.getLogger(), "[Party Data] Saving...");

        if (partyFile.exists()) {
            if (!partyFile.delete()) {
                pluginRef.getLogger().warning("Could not delete party file. Party saving failed!");
                return;
            }
        }

        final YamlConfiguration partiesFile = new YamlConfiguration();

        for (final Party party : parties) {
            final String partyName = party.getName();
            final PartyLeader leader = party.getLeader();

            partiesFile.set(partyName + ".Leader",
                    leader.getUniqueId().toString() + "|" + leader.getPlayerName());
            partiesFile.set(partyName + ".Password", party.getPassword());
            partiesFile.set(partyName + ".Locked", party.isLocked());
            partiesFile.set(partyName + ".Level", party.getLevel());
            partiesFile.set(partyName + ".Xp", (int) party.getXp());
            partiesFile.set(partyName + ".Ally",
                    (party.getAlly() != null) ? party.getAlly().getName() : "");
            partiesFile.set(partyName + ".ExpShareMode", party.getXpShareMode().toString());
            partiesFile.set(partyName + ".ItemShareMode", party.getItemShareMode().toString());

            for (final ItemShareType itemShareType : ItemShareType.values()) {
                partiesFile.set(partyName + ".ItemShareType." + itemShareType.toString(),
                        party.sharingDrops(itemShareType));
            }

            final List<String> members = new ArrayList<>();

            for (final Entry<UUID, String> memberEntry : party.getMembers().entrySet()) {
                final String memberUniqueId =
                        memberEntry.getKey() == null ? "" : memberEntry.getKey().toString();
                final String memberName = memberEntry.getValue();

                if (!members.contains(memberName)) {
                    members.add(memberUniqueId + "|" + memberName);
                }
            }

            partiesFile.set(partyName + ".Members", members);
        }

        try {
            partiesFile.save(partyFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle party change event.
     *
     * @param player       The player changing parties
     * @param oldPartyName The name of the old party
     * @param newPartyName The name of the new party
     * @param reason       The reason for changing parties
     * @return true if the change event was successful, false otherwise
     */
    public boolean handlePartyChangeEvent(final Player player, final String oldPartyName, final String newPartyName,
                                          final EventReason reason) {
        final McMMOPartyChangeEvent event = new McMMOPartyChangeEvent(player, oldPartyName, newPartyName,
                reason);
        pluginRef.getServer().getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    /**
     * Handle party alliance change event.
     *
     * @param player      The player changing party alliances
     * @param oldAllyName The name of the old ally
     * @param newAllyName The name of the new ally
     * @param reason      The reason for changing allies
     * @return true if the change event was successful, false otherwise
     */
    public boolean handlePartyChangeAllianceEvent(final Player player, final String oldAllyName,
                                                  final String newAllyName,
                                                  final McMMOPartyAllianceChangeEvent.EventReason reason) {
        final McMMOPartyAllianceChangeEvent event = new McMMOPartyAllianceChangeEvent(player, oldAllyName,
                newAllyName,
                reason);
        pluginRef.getServer().getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    /**
     * Remove party data from the mmoPlayer.
     *
     * @param mmoPlayer The player to remove party data from.
     */
    public void processPartyLeaving(@NotNull final McMMOPlayer mmoPlayer) {
        mmoPlayer.removeParty();
        mmoPlayer.setChatMode(ChatChannel.NONE);
        mmoPlayer.setItemShareModifier(10);
    }

    /**
     * Notify party members when the party levels up.
     *
     * @param party        The concerned party
     * @param levelsGained The amount of levels gained
     * @param level        The current party level
     */
    public void informPartyMembersLevelUp(final Party party, final int levelsGained, final int level) {
        final boolean levelUpSoundsEnabled = pluginRef.getGeneralConfig().getLevelUpSoundsEnabled();
        for (final Player member : party.getOnlineMembers()) {
            member.sendMessage(LocaleLoader.getString("Party.LevelUp", levelsGained, level));

            if (levelUpSoundsEnabled) {
                SoundManager.sendSound(member, member.getLocation(), SoundType.LEVEL_UP);
            }
        }
    }

    /**
     * Notify party members when a player joins.
     *
     * @param party      The concerned party
     * @param playerName The name of the player that joined
     */
    private void informPartyMembersJoin(final Party party, final String playerName) {
        for (final Player member : party.getOnlineMembers()) {
            member.sendMessage(LocaleLoader.getString("Party.InformedOnJoin", playerName));
        }
    }

    /**
     * Notify party members when a party member quits.
     *
     * @param party      The concerned party
     * @param playerName The name of the player that left
     */
    private void informPartyMembersQuit(final Party party, final String playerName) {
        for (final Player member : party.getOnlineMembers()) {
            member.sendMessage(LocaleLoader.getString("Party.InformedOnQuit", playerName));
        }
    }
}