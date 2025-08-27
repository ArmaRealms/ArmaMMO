package com.gmail.nossr50.commands;

import com.gmail.nossr50.commands.database.ConvertDatabaseCommand;
import com.gmail.nossr50.commands.experience.ConvertExperienceCommand;
import com.gmail.nossr50.database.DatabaseManagerFactory;
import com.gmail.nossr50.datatypes.database.DatabaseType;
import com.gmail.nossr50.datatypes.experience.FormulaType;
import com.gmail.nossr50.mcMMO;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class McconvertCommand implements TabExecutor {
    private static final List<String> FORMULA_TYPES;
    private static final List<String> DATABASE_TYPES;
    private static final List<String> SUBCOMMANDS = List.of("database", "experience");

    static {
        ArrayList<String> formulaTypes = new ArrayList<>();
        ArrayList<String> databaseTypes = new ArrayList<>();

        for (FormulaType type : FormulaType.values()) {
            formulaTypes.add(type.toString());
        }

        for (DatabaseType type : DatabaseType.values()) {
            databaseTypes.add(type.toString());
        }

        // Custom stuff
        databaseTypes.remove(DatabaseType.CUSTOM.toString());

        if (mcMMO.getDatabaseManager().getDatabaseType() == DatabaseType.CUSTOM) {
            databaseTypes.add(DatabaseManagerFactory.getCustomDatabaseManagerClass().getName());
        }

        Collections.sort(formulaTypes);
        Collections.sort(databaseTypes);

        FORMULA_TYPES = List.copyOf(formulaTypes);
        DATABASE_TYPES = List.copyOf(databaseTypes);

    }

    private final CommandExecutor databaseConvertCommand = new ConvertDatabaseCommand();
    private final CommandExecutor experienceConvertCommand = new ConvertExperienceCommand();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("database") || args[0].equalsIgnoreCase("db")) {
                return databaseConvertCommand.onCommand(sender, command, label, args);
            } else if (args[0].equalsIgnoreCase("experience") || args[0].equalsIgnoreCase("xp") || args[1].equalsIgnoreCase("exp")) {
                return experienceConvertCommand.onCommand(sender, command, label, args);
            }

            return false;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        switch (args.length) {
            case 1 -> {
                return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0])).toList();
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("database") || args[0].equalsIgnoreCase("db")) {
                    return DATABASE_TYPES.stream().filter(s -> s.startsWith(args[1])).toList();
                }

                if (args[0].equalsIgnoreCase("experience") || args[0].equalsIgnoreCase("xp") || args[0].equalsIgnoreCase("exp")) {
                    return FORMULA_TYPES.stream().filter(s -> s.startsWith(args[1])).toList();
                }

                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }
}
