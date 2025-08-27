package com.gmail.nossr50.party;

import static java.util.logging.Logger.getLogger;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gmail.nossr50.MMOTestEnvironment;
import com.gmail.nossr50.datatypes.interactions.NotificationType;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.party.PartyLeader;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.NotificationManager;
import com.gmail.nossr50.util.player.UserManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PartyManagerTest extends MMOTestEnvironment {
    private static final Logger logger = getLogger(PartyManagerTest.class.getName());

    @BeforeEach
    public void setUp() {
        mockBaseEnvironment(logger);

        // currently unnecessary, but may be needed for future tests
        when(partyConfig.isPartyEnabled()).thenReturn(true);
    }

    @AfterEach
    public void tearDown() {
        cleanUpStaticMocks();

        // disable parties in config for other tests
        when(partyConfig.isPartyEnabled()).thenReturn(false);
    }

    @Test
    public void createPartyWithoutPasswordShouldSucceed() {
        // Given
        final PartyManager partyManager = new PartyManager(mcMMO.p);
        final String partyName = "TestParty";

        final Player player = mock(Player.class);
        final McMMOPlayer mmoPlayer = mock(McMMOPlayer.class);
        when(mmoPlayer.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(new UUID(0, 0));

        // When & Then
        partyManager.createParty(mmoPlayer, partyName, null);
    }

    @Test
    public void createPartyWithPasswordShouldSucceed() {
        // Given
        final PartyManager partyManager = new PartyManager(mcMMO.p);
        final String partyName = "TestParty";
        final String partyPassword = "somePassword";

        final Player player = mock(Player.class);
        final McMMOPlayer mmoPlayer = mock(McMMOPlayer.class);
        when(mmoPlayer.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(new UUID(0, 0));

        // When & Then
        partyManager.createParty(mmoPlayer, partyName, partyPassword);
    }

    @Test
    public void createPartyWithoutNameShouldFail() {
        // Given
        final PartyManager partyManager = new PartyManager(mcMMO.p);
        final String partyPassword = "somePassword";

        final Player player = mock(Player.class);
        final McMMOPlayer mmoPlayer = mock(McMMOPlayer.class);
        when(mmoPlayer.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(new UUID(0, 0));

        // When & Then
        assertThrows(NullPointerException.class,
                () -> partyManager.createParty(mmoPlayer, null, partyPassword));
    }

    @Test
    public void createPartyWithoutPlayerShouldFail() {
        // Given
        final PartyManager partyManager = new PartyManager(mcMMO.p);
        final String partyName = "TestParty";
        final String partyPassword = "somePassword";

        // When & Then
        assertThrows(NullPointerException.class,
                () -> partyManager.createParty(null, partyName, partyPassword));
    }

    @Test
    public void checkPartyPasswordFailsWithIncorrectPassword() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final Party party = Mockito.mock(Party.class);
        final Player player = Mockito.mock(Player.class);

        when(party.isLocked()).thenReturn(true);
        when(party.getPassword()).thenReturn("correctPassword");

        final boolean result = partyManager.checkPartyPassword(player, party, "wrongPassword");

        assertThat(result).isFalse();
        verify(player).sendMessage(contains("Party password is incorrect"));
    }

    @Test
    public void checkPartyPasswordFailsWithNullInput() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final Party party = Mockito.mock(Party.class);
        final Player player = Mockito.mock(Player.class);

        when(party.isLocked()).thenReturn(true);
        when(party.getPassword()).thenReturn("secure");

        final boolean result = partyManager.checkPartyPassword(player, party, null);

        assertThat(result).isFalse();
        verify(player).sendMessage(
                contains("This party is password protected. Please provide a password to join."));
    }

    @Test
    public void checkPartyExistenceReturnsTrueIfExists() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final Party party = Mockito.mock(Party.class);
        Mockito.when(party.getName()).thenReturn("ExistingParty");

        partyManager.getParties().add(party);

        final boolean result = partyManager.checkPartyExistence(player, "ExistingParty");

        assertThat(result).isTrue();
        Mockito.verify(player).sendMessage(Mockito.contains("Party ExistingParty already exists!"));
    }

    @Test
    public void inSamePartyShouldReturnTrueIfSameParty() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final Party party = Mockito.mock(Party.class);
        final Player playerA = mock(Player.class);
        final Player playerB = mock(Player.class);

        final McMMOPlayer mmoA = mock(McMMOPlayer.class);
        final McMMOPlayer mmoB = mock(McMMOPlayer.class);

        mockedUserManager.when(() -> UserManager.getPlayer(playerA)).thenReturn(mmoA);
        mockedUserManager.when(() -> UserManager.getPlayer(playerB)).thenReturn(mmoB);

        when(mmoA.getParty()).thenReturn(party);
        when(mmoB.getParty()).thenReturn(party);

        assertThat(partyManager.inSameParty(playerA, playerB)).isTrue();
    }

    @Test
    public void areAlliesShouldReturnTrueIfMutuallyAllied() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final Player p1 = mock(Player.class);
        final Player p2 = mock(Player.class);

        final McMMOPlayer mmo1 = mock(McMMOPlayer.class);
        final McMMOPlayer mmo2 = mock(McMMOPlayer.class);

        final Party party1 = mock(Party.class);
        final Party party2 = mock(Party.class);

        mockedUserManager.when(() -> UserManager.getPlayer(p1)).thenReturn(mmo1);
        mockedUserManager.when(() -> UserManager.getPlayer(p2)).thenReturn(mmo2);

        when(mmo1.getParty()).thenReturn(party1);
        when(mmo2.getParty()).thenReturn(party2);
        when(party1.getAlly()).thenReturn(party2);
        when(party2.getAlly()).thenReturn(party1);

        assertTrue(partyManager.areAllies(p1, p2));
    }

    @Test
    public void removeFromPartyDoesNothing() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final McMMOPlayer mmoPlayer = mock(McMMOPlayer.class);
        when(mmoPlayer.getParty()).thenReturn(null);

        partyManager.removeFromParty(mmoPlayer);
    }

    @Test
    public void removeFromPartyWithPartyRemovesCorrectly() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final McMMOPlayer mmoPlayer = mock(McMMOPlayer.class);
        final Player player = mock(Player.class);
        final Party party = mock(Party.class);
        final UUID uuid = UUID.randomUUID();

        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("PlayerName");
        when(player.isOnline()).thenReturn(true);
        when(player.getPlayer()).thenReturn(player);

        when(mmoPlayer.getPlayer()).thenReturn(player);
        when(mmoPlayer.getParty()).thenReturn(party);

        when(party.getMembers()).thenReturn(new LinkedHashMap<>(Map.of(uuid, "PlayerName")));
        when(party.getOnlineMembers()).thenReturn(new ArrayList<>(List.of(player)));
        when(party.getLeader()).thenReturn(new PartyLeader(uuid, "PlayerName"));

        partyManager.getParties().add(party);
        partyManager.removeFromParty(mmoPlayer);

        // Party should be removed since it had only one member
        assertFalse(partyManager.getParties().contains(party));
    }

    @Test
    public void changeOrJoinPartyNotInPartyTriggersEventAndReturnsTrue() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final McMMOPlayer mmoPlayer = mock(McMMOPlayer.class);
        final Player player = mock(Player.class);

        when(mmoPlayer.getPlayer()).thenReturn(player);
        when(mmoPlayer.inParty()).thenReturn(false);

        assertTrue(partyManager.changeOrJoinParty(mmoPlayer, "NewParty"));
    }

    @Test
    public void removeFromPartyLeaderLeavesNewLeaderIsAssigned() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final UUID oldLeaderUUID = UUID.randomUUID();
        final UUID newLeaderUUID = UUID.randomUUID();

        // Setup players
        final OfflinePlayer oldLeader = mock(OfflinePlayer.class);
        when(oldLeader.getUniqueId()).thenReturn(oldLeaderUUID);
        when(oldLeader.getName()).thenReturn("OldLeader");
        when(oldLeader.isOnline()).thenReturn(true);
        // required for party.getOnlineMembers()
        when(oldLeader.getPlayer()).thenReturn(mock(Player.class));

        final OfflinePlayer newLeader = mock(OfflinePlayer.class);
        when(newLeader.getUniqueId()).thenReturn(newLeaderUUID);
        when(newLeader.getName()).thenReturn("NewLeader");

        // Setup party and members
        final Party party = new Party(new PartyLeader(oldLeaderUUID, "OldLeader"), "SomeParty", null);
        party.getMembers().put(oldLeaderUUID, "OldLeader");
        party.getMembers().put(newLeaderUUID, "NewLeader");

        final Player newLeaderOnline = mock(Player.class);
        when(newLeaderOnline.getUniqueId()).thenReturn(newLeaderUUID);
        // simulate second member online
        party.getOnlineMembers().add(newLeaderOnline);

        partyManager.getParties().add(party);

        // Act
        partyManager.removeFromParty(oldLeader, party);

        // Assert
        final PartyLeader newLeaderObj = party.getLeader();
        assertThat(newLeaderUUID).isEqualTo(newLeaderObj.getUniqueId());
        assertThat("NewLeader").isEqualTo(newLeaderObj.getPlayerName());
    }

    @Test
    public void joinInvitedPartyPartyDoesNotExistDoesNotJoin() {
        final PartyManager partyManager = new PartyManager(mcMMO.p);

        final McMMOPlayer mmoPlayer = mock(McMMOPlayer.class);
        final Player player = mock(Player.class);
        final Party partyWhichNoLongerExists = mock(Party.class);

        when(mmoPlayer.getPartyInvite()).thenReturn(partyWhichNoLongerExists);
        when(mmoPlayer.getPlayer()).thenReturn(player);

        assertFalse(partyManager.getParties().contains(partyWhichNoLongerExists));

        partyManager.joinInvitedParty(mmoPlayer);

        // Should have sent disband message
        notificationManager.verify(() ->
                NotificationManager.sendPlayerInformation(player, NotificationType.PARTY_MESSAGE,
                        "Party.Disband"));
    }

}