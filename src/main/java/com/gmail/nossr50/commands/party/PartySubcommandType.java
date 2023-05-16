package com.gmail.nossr50.commands.party;

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

    private final String commandName;

    PartySubcommandType(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String toString() {
        return commandName;
    }

    public static PartySubcommandType getSubcommand(String commandName) {
        for (PartySubcommandType command : values()) {
            if (command.toString().equalsIgnoreCase(commandName)) {
                return command;
            }
        }

        if (commandName.equalsIgnoreCase("?")) {
            return HELP;
        }
        else if (commandName.equalsIgnoreCase("q") || commandName.equalsIgnoreCase("leave")) {
            return QUIT;
        }
        else if (commandName.equalsIgnoreCase("lider")) {
            return OWNER;
        }
        else if (commandName.equalsIgnoreCase("xpshare") || commandName.equalsIgnoreCase("shareexp") || commandName.equalsIgnoreCase("sharexp")) {
            return XPSHARE;
        }
        else if (commandName.equalsIgnoreCase("shareitem") || commandName.equalsIgnoreCase("shareitems")) {
            return ITEMSHARE;
        }
        else if (commandName.equalsIgnoreCase("ally") || commandName.equalsIgnoreCase("aliado")) {
            return ALLIANCE;
        }

        return null;
    }
}
