package com.gmail.nossr50.commands.party;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum PartySubcommandType {
    JOIN("entrar"),
    ACCEPT("aceitar"),
    CREATE("criar"),
    HELP("ajuda"),
    INFO("info"),
    QUIT("sair"),
    XPSHARE("xpshare"),
    ITEMSHARE("itemshare"),
    INVITE("convidar"),
    KICK("expulsar"),
    DISBAND("debandar"),
    OWNER("dono"),
    LOCK("travar"),
    UNLOCK("destravar"),
    PASSWORD("senha"),
    RENAME("renomear"),
    TELEPORT("teleportar"),
    CHAT("chat"),
    ALLIANCE("alianca");

    private static final List<String> HELP_ALIAS = List.of("?", "ajuda");
    private static final List<String> QUIT_ALIAS = List.of("q", "leave", "sair");
    private static final List<String> OWNER_ALIAS = List.of("leader", "lider");
    private static final List<String> XPSHARE_ALIAS = List.of("xpshare", "shareexp", "sharexp");
    private static final List<String> ITEMSHARE_ALIAS = List.of("shareitem", "shareitems");
    private static final List<String> ALLIANCE_ALIAS = List.of("ally", "aliado");
    private final String commandName;

    PartySubcommandType(String commandName) {
        this.commandName = commandName;
    }

    public static @NotNull List<String> getSubcommands() {
        List<String> subcommands = new ArrayList<>();
        for (PartySubcommandType subcommand : PartySubcommandType.values()) {
            subcommands.add(subcommand.string());
        }

        return subcommands;
    }

    public static @Nullable PartySubcommandType getSubcommand(String commandName) {
        for (PartySubcommandType command : values()) {
            if (command.string().equalsIgnoreCase(commandName)) {
                return command;
            }
        }

        if (HELP_ALIAS.contains(commandName)) {
            return HELP;
        }

        if (QUIT_ALIAS.contains(commandName)) {
            return QUIT;
        }

        if (OWNER_ALIAS.contains(commandName)) {
            return OWNER;
        }

        if (XPSHARE_ALIAS.contains(commandName)) {
            return XPSHARE;
        }

        if (ITEMSHARE_ALIAS.contains(commandName)) {
            return ITEMSHARE;
        }

        if (ALLIANCE_ALIAS.contains(commandName)) {
            return ALLIANCE;
        }

        return null;
    }

    public String string() {
        return commandName;
    }
}
