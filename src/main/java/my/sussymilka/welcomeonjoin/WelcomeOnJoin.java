package my.sussymilka.welcomeonjoin;


import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.bukkit.ChatColor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class WelcomeOnJoin extends JavaPlugin implements @NotNull Listener, TabCompleter {

    public boolean papi;
    public void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    // Overloaded method with default timing
    public void title_sendboth(Player player, String title, String subtitle) {
        showTitle(player, title, subtitle, 10, 70, 20); // default timings
    }

    // Method to show just the title
    public void title_showTitle(Player player, String title) {
        showTitle(player, title, "", 10, 70, 20);
    }
    public void title_showSubtitle(Player player, String subtitle) {
        showTitle(player, "", subtitle, 10, 70, 20);

    }




    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();


        Bukkit.getServer().getPluginManager().registerEvents(this,this);
        getLogger().info("WOJ has been enabled (: | Made by SussyMilka <3");


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papi = true;
            getLogger().info("Initialized PAPI support");

            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            papi = false;
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            getLogger().severe("Could not find PlaceholderAPI! This plugin is optional, but placeholders in messages wont work!");

        }


    }
    public int keysum() {
        int key_sum=0;
        FileConfiguration config = getConfig();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        for (String key : messagesSection.getKeys(false)) {
            key_sum++;


        }
        return key_sum;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getLogger().info("WOJ has been disabled, see ya!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.reloadConfig();

        Player player = event.getPlayer();
        FileConfiguration config = getConfig();

        for (String key : Objects.requireNonNull(config.getConfigurationSection("messages")).getKeys(false)) {
            if(papi) {
                String not_prepared_message = config.getString("messages." + key);
                String message = PlaceholderAPI.setPlaceholders(event.getPlayer(), not_prepared_message);

                if (!message.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }





            } else {

                String message = config.getString("messages." + key);
                if (message.length() > 3) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }

            }

        }

        if(config.getBoolean("titles_enabled")){
            if (papi){
                String bigTitle = config.getString("big_title");
                String subtitle = config.getString("subtitle");
                showTitle(player, PlaceholderAPI.setPlaceholders(event.getPlayer(), bigTitle), PlaceholderAPI.setPlaceholders(event.getPlayer(), subtitle), 20,70, 20);


            } else {
                String bigTitle = config.getString("big_title");
                String subtitle = config.getString("subtitle");
                showTitle(player, bigTitle, subtitle, 20, 70, 20);

            }

        }
        //playSound(p.getLocation(), Sound.GLASS, 10, 29);
        if(config.getBoolean("soundeffect_enabled")) {
            FileConfiguration configg = getConfig();
            String sound_id_aaaa = configg.getString("sound_effect");
            if (sound_id_aaaa != null && !sound_id_aaaa.isEmpty()) {

                player.playSound(player.getLocation(), sound_id_aaaa, 10, 1);

            }
        }




    }
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            // Example: Provide completion options for the first argument
            completions.add("set");
            completions.add("list");
            completions.add("remove");
            completions.add("reload");
        }
        // Filter and return suggestions based on what the player has typed
        return completions;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("woj")) {
            if (args.length >= 3 && args[0].equalsIgnoreCase("set")) {
                try {
                    int linenum = Integer.parseInt(args[1]);
                    String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if(80 <= linenum && getConfig().getBoolean("limit")) {
                        sender.sendMessage("Theres a minecraft message limit, limiting max messages to 80.");
                        sender.sendMessage("The message wont be set because of that, but you can");
                        sender.sendMessage("disable this by setting limit to false in config.yml");

                    } else {
                        getConfig().set("messages." + linenum, message);
                        saveConfig();

                        sender.sendMessage("Welcome msg line: " + linenum + " was set to: " + message);
                        return true;

                    }




                } catch (NumberFormatException e) {
                    sender.sendMessage("Line number must be valid");

                }
            } if (args[0].equalsIgnoreCase("list")) {
                FileConfiguration config = getConfig();
                ConfigurationSection messagesSection = config.getConfigurationSection("messages");

                if (messagesSection != null) {
                    int msgNumber = 0;
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "Message List("+keysum()+"):"));
                    for (String key : messagesSection.getKeys(false)) {
                        msgNumber++;
                        String message = messagesSection.getString(key);
                        if (message != null) {
                            TextComponent removeComponent = new TextComponent(ChatColor.RED + "REMOVE");
                            removeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/woj remove_but_list_after_exec " + key));
                            sender.sendMessage(new TextComponent(key + ": " + message + " "), removeComponent);

                        }
                    }
                }
                return true;


            } else if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
                try {
                    String key = args[1];
                    FileConfiguration config = getConfig();
                    if (config.contains("messages." + key)) {
                        config.set("messages." + key, null);
                        saveConfig();
                        sender.sendMessage("Message removed.");

                    } else {
                        sender.sendMessage("Message with that number does not exist.");
                    }
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage("Number must be valid. You sure you typed it correctly?");

                }

            } else if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                this.reloadConfig();
                sender.sendMessage("Config reloaded");


            }

            if (args.length >= 2 && args[0].equalsIgnoreCase("remove_but_list_after_exec")) {
                try {
                    String key = args[1];
                    FileConfiguration config = getConfig();
                    if (config.contains("messages." + key)) {
                        config.set("messages." + key, null);
                        saveConfig();
                        Bukkit.dispatchCommand(sender, "woj list");
                        sender.sendMessage("Message removed.");

                    } else {
                        sender.sendMessage("Message with that number does not exist.");
                    }
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage("Number must be valid. You sure you typed it correctly?");

                }


            }



        }

        return false;
    }
}
