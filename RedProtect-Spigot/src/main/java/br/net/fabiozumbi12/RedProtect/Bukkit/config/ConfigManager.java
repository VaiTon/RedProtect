/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Core.config.category.FlagGuiCategory;
import br.net.fabiozumbi12.RedProtect.Core.config.category.GlobalFlagsCategory;
import br.net.fabiozumbi12.RedProtect.Core.config.category.MainCategory;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.google.common.reflect.TypeToken.of;

public class ConfigManager {

    public final List<String> AdminFlags = Arrays.asList(
            "spawn-wither",
            "cropsfarm",
            "keep-inventory",
            "keep-levels",
            "can-drop",
            "can-pickup",
            "cmd-onhealth",
            "can-death",
            "max-players",
            "forcefly",
            "gamemode",
            "player-damage",
            "can-hunger",
            "can-projectiles",
            "allow-place",
            "allow-break",
            "can-pet",
            "allow-cmds",
            "deny-cmds",
            "allow-create-portal",
            "portal-exit",
            "portal-enter",
            "allow-mod",
            "allow-enter-items",
            "deny-enter-items",
            "pvparena",
            "player-enter-command",
            "server-enter-command",
            "player-exit-command",
            "server-exit-command",
            "invincible",
            "effects",
            "treefarm",
            "minefarm",
            "pvp",
            "sign",
            "enderpearl",
            "enter",
            "up-skills",
            "can-back",
            "for-sale",
            "set-portal",
            "exit",
            "particles",
            "dynmap",
            "deny-exit-items");
    public ConfigurationNode ecoCfgs;
    private ConfigurationLoader<CommentedConfigurationNode> ecoLoader;
    private ConfigurationNode signCfgs;
    private ConfigurationLoader<CommentedConfigurationNode> signsLoader;
    private ConfigurationNode guiCfgRoot;
    private ConfigurationLoader<CommentedConfigurationNode> guiCfgLoader;
    private FlagGuiCategory guiRoot;
    private ConfigurationLoader<CommentedConfigurationNode> gFlagsLoader;
    private ConfigurationNode gflagsRoot;
    private GlobalFlagsCategory globalFlagsRoot;
    private ConfigurationNode configRoot;
    private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    private MainCategory root;

    //init
    public ConfigManager() throws ObjectMappingException {
        try {
            if (!RedProtect.get().getDataFolder().exists()) {
                RedProtect.get().getDataFolder().mkdir();
            }
            if (!new File(RedProtect.get().getDataFolder(), "data").exists()) {
                new File(RedProtect.get().getDataFolder(), "data").mkdir();
            }

            /*--------------------- config.yml ---------------------------*/
            String header = ""
                    + "+--------------------------------------------------------------------+ #\n"
                    + "<               RedProtect World configuration File                  > #\n"
                    + "<--------------------------------------------------------------------> #\n"
                    + "<       This is the configuration file, feel free to edit it.        > #\n"
                    + "<        For more info about cmds and flags, check our Wiki:         > #\n"
                    + "<         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
                    + "+--------------------------------------------------------------------+ #\n"
                    + "\n"
                    + "Notes:\n"
                    + "Lists are [object1, object2, ...]\n"
                    + "Strings containing the char & always need to be quoted";


            cfgLoader = HoconConfigurationLoader.builder().setFile(new File(RedProtect.get().getDataFolder(), "config.conf")).build();

            if (new File(RedProtect.get().getDataFolder(), "config.yml").exists()) {
                File defConfig = new File(RedProtect.get().getDataFolder(), "config.yml");
                ConfigurationLoader<ConfigurationNode> cfgLoader = YAMLConfigurationLoader.builder().setFile(defConfig).build();
                configRoot = cfgLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(header));
                defConfig.renameTo(new File(RedProtect.get().getDataFolder(), "config_BKP.yml"));
            } else {
                configRoot = cfgLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(header));
            }
            this.root = configRoot.getValue(of(MainCategory.class), new MainCategory(Bukkit.getOnlineMode()));

            //Defaults per server
            if (this.root.private_cat.allowed_blocks.isEmpty()) {
                this.root.private_cat.allowed_blocks = Arrays.asList(
                        "ANVIL",
                        "DAMAGED_ANVIL",
                        "CHIPPED_ANVIL",
                        "DISPENSER",
                        "DISPENSER",
                        "NOTE_BLOCK",
                        "BED_BLOCK",
                        "CHEST",
                        "CRAFTING_TABLE",
                        "FURNACE",
                        "JUKEBOX",
                        "ENCHANTING_TABLE",
                        "BREWING_STAND",
                        "CAULDRON",
                        "ENDER_CHEST",
                        "BEACON",
                        "TRAPPED_CHEST",
                        "HOPPER",
                        "DROPPER",
                        "[A-Z_]+_SHULKER_BOX");
            }
            if (this.root.needed_claim_to_build.allow_break_blocks.isEmpty()) {
                this.root.needed_claim_to_build.allow_break_blocks = Arrays.asList(Material.GRASS.name(), Material.DIRT.name());
            }
            if (this.root.region_settings.block_id.isEmpty()) {
                this.root.region_settings.block_id = "FENCE";
            }
            if (this.root.region_settings.border.material.isEmpty()) {
                this.root.region_settings.border.material = Material.GLOWSTONE.name();
            }
            if (this.root.wands.adminWandID.isEmpty()) {
                this.root.wands.adminWandID = Material.GLASS_BOTTLE.name();
            }
            if (this.root.wands.infoWandID.isEmpty()) {
                this.root.wands.infoWandID = Material.PAPER.name();
            }
            if (root.debug_messages.isEmpty()) {
                for (LogLevel level : LogLevel.values()) {
                    root.debug_messages.put(level.name().toLowerCase(), false);
                }
            }

            /*--------------------- end config.yml ---------------------------*/

            /*--------------------- globalflags.yml ---------------------------*/
            String headerg = ""
                    + "+--------------------------------------------------------------------+ #\n"
                    + "<          RedProtect Global Flags configuration File                > #\n"
                    + "<--------------------------------------------------------------------> #\n"
                    + "<         This is the global flags configuration file.               > #\n"
                    + "<                       Feel free to edit it.                        > #\n"
                    + "<         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
                    + "+--------------------------------------------------------------------+ #\n"
                    + "\n"
                    + "Notes:\n"
                    + "Lists are [object1, object2, ...]\n"
                    + "Strings containing the char & always need to be quoted";

            gFlagsLoader = HoconConfigurationLoader.builder().setFile(new File(RedProtect.get().getDataFolder(), "globalflags.conf")).build();

            if (new File(RedProtect.get().getDataFolder(), "globalflags.yml").exists()) {
                File gFlagsConfig = new File(RedProtect.get().getDataFolder(), "globalflags.yml");
                ConfigurationLoader<ConfigurationNode> gFlagsLoader = YAMLConfigurationLoader.builder().setFile(gFlagsConfig).build();
                gflagsRoot = gFlagsLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerg));
                gFlagsConfig.renameTo(new File(RedProtect.get().getDataFolder(), "globalflags_BKP.yml"));
            } else {
                gflagsRoot = gFlagsLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerg));
            }
            this.globalFlagsRoot = gflagsRoot.getValue(of(GlobalFlagsCategory.class), new GlobalFlagsCategory());

            /*--------------------- end globalflags.yml ---------------------------*/

            /*--------------------- guiconfig.yml ---------------------------*/
            String headerGui = ""
                    + "+--------------------------------------------------------------------+ #\n"
                    + "<             RedProtect Gui Flags configuration File                > #\n"
                    + "<--------------------------------------------------------------------> #\n"
                    + "<            This is the gui flags configuration file.               > #\n"
                    + "<                       Feel free to edit it.                        > #\n"
                    + "<         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
                    + "+--------------------------------------------------------------------+ #\n"
                    + "\n"
                    + "Notes:\n"
                    + "Lists are [object1, object2, ...]\n"
                    + "Strings containing the char & always need to be quoted";

            String guiFileName = "guiconfig" + configRoot().language + ".conf";
            if (new File(RedProtect.get().getDataFolder(), guiFileName).exists()){
                new File(RedProtect.get().getDataFolder(), guiFileName).renameTo(new File(RedProtect.get().getDataFolder(), "guiconfig.conf"));
            }
            guiCfgLoader = HoconConfigurationLoader.builder().setFile(new File(RedProtect.get().getDataFolder(), "guiconfig.conf")).build();

            if (new File(RedProtect.get().getDataFolder(), "guiconfig.yml").exists()) {
                File guiOldCfg = new File(RedProtect.get().getDataFolder(), "guiconfig.yml");
                ConfigurationLoader<ConfigurationNode> guiLoader = YAMLConfigurationLoader.builder().setFile(guiOldCfg).build();
                guiCfgRoot = guiLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerGui));
                guiOldCfg.renameTo(new File(RedProtect.get().getDataFolder(), "guiconfig_BKP.yml"));
            } else {
                guiCfgRoot = guiCfgLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerGui));
            }
            this.guiRoot = guiCfgRoot.getValue(of(FlagGuiCategory.class), new FlagGuiCategory());

            if (guiCfgRoot.getNode("gui-strings").getValue() != null){
                guiCfgRoot.removeChild("gui-strings");
            }
            for (ConfigurationNode key:guiCfgRoot.getNode("gui-flags").getChildrenList()){
                if (key.getNode("name").getValue() != null){
                    key.removeChild("name");
                }
                if (key.getNode("description").getValue() != null){
                    key.removeChild("description");
                }
                if (key.getNode("description1").getValue() != null){
                    key.removeChild("description1");
                }
                if (key.getNode("description2").getValue() != null){
                    key.removeChild("description2");
                }
            }

            if (this.guiRoot.gui_separator.material.isEmpty())
                this.guiRoot.gui_separator.material = "WHITE_STAINED_GLASS_PANE";

            if (this.guiRoot.gui_flags.isEmpty()) {
                this.guiRoot.gui_flags.put("allow-effects", new FlagGuiCategory.GuiFlag("BLAZE_ROD", 16));
                this.guiRoot.gui_flags.put("allow-fly", new FlagGuiCategory.GuiFlag("FEATHER", 8));
                this.guiRoot.gui_flags.put("allow-home", new FlagGuiCategory.GuiFlag("COMPASS", 2));
                this.guiRoot.gui_flags.put("allow-potions", new FlagGuiCategory.GuiFlag("POTION", 26));
                this.guiRoot.gui_flags.put("allow-spawner", new FlagGuiCategory.GuiFlag("LEASH", 10));
                this.guiRoot.gui_flags.put("build", new FlagGuiCategory.GuiFlag("GRASS", 13));
                this.guiRoot.gui_flags.put("button", new FlagGuiCategory.GuiFlag("STONE_BUTTON", 6));
                this.guiRoot.gui_flags.put("can-grow", new FlagGuiCategory.GuiFlag("WHEAT", 27));
                this.guiRoot.gui_flags.put("chest", new FlagGuiCategory.GuiFlag("TRAPPED_CHEST", 3));
                this.guiRoot.gui_flags.put("door", new FlagGuiCategory.GuiFlag("ACACIA_DOOR", 0));
                this.guiRoot.gui_flags.put("ender-chest", new FlagGuiCategory.GuiFlag("ENDER_CHEST", 22));
                this.guiRoot.gui_flags.put("fire", new FlagGuiCategory.GuiFlag("BLAZE_POWDER", 9));
                this.guiRoot.gui_flags.put("fishing", new FlagGuiCategory.GuiFlag("FISHING_ROD", 28));
                this.guiRoot.gui_flags.put("flow", new FlagGuiCategory.GuiFlag("WATER_BUCKET", 29));
                this.guiRoot.gui_flags.put("flow-damage", new FlagGuiCategory.GuiFlag("LAVA_BUCKET", 30));
                this.guiRoot.gui_flags.put("iceform-player", new FlagGuiCategory.GuiFlag("PACKED_ICE", 4));
                this.guiRoot.gui_flags.put("iceform-world", new FlagGuiCategory.GuiFlag("ICE", 31));
                this.guiRoot.gui_flags.put("leaves-decay", new FlagGuiCategory.GuiFlag("LEAVES", 18));
                this.guiRoot.gui_flags.put("lever", new FlagGuiCategory.GuiFlag("LEVER", 5));
                this.guiRoot.gui_flags.put("minecart", new FlagGuiCategory.GuiFlag("MINECART", 25));
                this.guiRoot.gui_flags.put("mob-loot", new FlagGuiCategory.GuiFlag("MYCEL", 32));
                this.guiRoot.gui_flags.put("passives", new FlagGuiCategory.GuiFlag("SADDLE", 33));
                this.guiRoot.gui_flags.put("press-plate", new FlagGuiCategory.GuiFlag("GOLD_PLATE", 17));
                this.guiRoot.gui_flags.put("pvp", new FlagGuiCategory.GuiFlag("STONE_SWORD", 20));
                this.guiRoot.gui_flags.put("smart-door", new FlagGuiCategory.GuiFlag("IRON_DOOR", 1));
                this.guiRoot.gui_flags.put("spawn-animals", new FlagGuiCategory.GuiFlag("EGG", 34));
                this.guiRoot.gui_flags.put("spawn-monsters", new FlagGuiCategory.GuiFlag("PUMPKIN", 35));
                this.guiRoot.gui_flags.put("teleport", new FlagGuiCategory.GuiFlag("ENDER_PEARL", 19));
                this.guiRoot.gui_flags.put("use-potions", new FlagGuiCategory.GuiFlag("GLASS_BOTTLE", 26));
            }

            for (String key : getDefFlagsValues().keySet()) {
                if (!guiRoot.gui_flags.containsKey(key)) {
                    guiRoot.gui_flags.put(key, new FlagGuiCategory.GuiFlag("GOLDEN_APPLE", getGuiMaxSlot()));
                }
            }

            //Economy file
            ecoLoader = HoconConfigurationLoader.builder().setPath(new File(RedProtect.get().getDataFolder(), "economy.conf").toPath()).build();
            if (new File(RedProtect.get().getDataFolder(), "economy.yml").exists()) {
                File ecoConfig = new File(RedProtect.get().getDataFolder(), "economy.yml");
                ConfigurationLoader<ConfigurationNode> ecoLoader = YAMLConfigurationLoader.builder().setPath(ecoConfig.toPath()).build();
                ecoCfgs = ecoLoader.load();
                ecoConfig.renameTo(new File(RedProtect.get().getDataFolder(), "economy_BKP.yml"));
            } else {
                if (!new File(RedProtect.get().getDataFolder(), "economy.conf").exists()) {
                    RedProtect.get().saveResource("economy.conf", false);
                }
                ecoCfgs = ecoLoader.load();
            }

            if (ecoCfgs.getNode("items", "values").getChildrenList().size() != Material.values().length) {
                for (Material mat : Material.values()) {
                    if (ecoCfgs.getNode("items", "values", mat.name()).getValue() == null) {
                        ecoCfgs.getNode("items", "values", mat.name()).setValue(0.0);
                    }
                }
            }
            if (ecoCfgs.getNode("enchantments", "values").getChildrenList().size() != Enchantment.values().length) {
                for (Enchantment ench : Enchantment.values()) {
                    if (ecoCfgs.getNode("enchantments", "values", ench.getName()).getValue() == null) {
                        ecoCfgs.getNode("enchantments", "values", ench.getName()).setValue(0.0);
                    }
                }
            }

            //Signs file
            signsLoader = HoconConfigurationLoader.builder().setPath(new File(RedProtect.get().getDataFolder(), "signs.conf").toPath()).build();
            if (new File(RedProtect.get().getDataFolder(), "signs.yml").exists()) {
                File signFile = new File(RedProtect.get().getDataFolder(), "signs.yml");
                ConfigurationLoader<ConfigurationNode> signsLoader = YAMLConfigurationLoader.builder().setPath(signFile.toPath()).build();
                signCfgs = signsLoader.load();
                signFile.renameTo(new File(RedProtect.get().getDataFolder(), "signs_BKP.yml"));
            } else {
                signCfgs = signsLoader.load();
            }
        } catch (IOException e1) {
            RedProtect.get().logger.severe("The default configuration could not be loaded or created!");
            e1.printStackTrace();
        }

        RedProtect.get().logger.info("Server version: " + Bukkit.getServer().getBukkitVersion());

        //Load configs per world
        for (World w : RedProtect.get().getServer().getWorlds()) {
            this.addWorldProperties(w);
        }

        /*------------- ---- Add default config for not updatable configs ------------------*/

        //update new player flags according version

        int update = 0;
        if (root.config_version < 6.8D) {
            root.config_version = 6.8D;

            if (!root.flags_configuration.enabled_flags.contains("smart-door")) {
                root.flags_configuration.enabled_flags.add("smart-door");
            }
            if (!root.flags_configuration.enabled_flags.contains("allow-potions")) {
                root.flags_configuration.enabled_flags.add("allow-potions");
            }
            if (!root.flags_configuration.enabled_flags.contains("mob-loot")) {
                root.flags_configuration.enabled_flags.add("mob-loot");
            }
            if (!root.flags_configuration.enabled_flags.contains("flow-damage")) {
                root.flags_configuration.enabled_flags.add("flow-damage");
            }
            update++;
        }

        if (root.config_version < 6.9D) {
            root.config_version = 6.9D;
            if (!root.flags_configuration.enabled_flags.contains("iceform-player")) {
                root.flags_configuration.enabled_flags.add("iceform-player");
                root.flags.put("iceform-player", false);
            }
            if (!root.flags_configuration.enabled_flags.contains("iceform-entity")) {
                root.flags_configuration.enabled_flags.add("iceform-entity");
                root.flags.put("iceform-entity", true);
            }
            update++;
        }

        if (root.config_version < 7.0D) {
            root.config_version = 7.0D;
            if (!root.flags_configuration.enabled_flags.contains("allow-fly")) {
                root.flags_configuration.enabled_flags.add("allow-fly");
                root.flags.put("allow-fly", true);
            }
            update++;
        }

        if (root.config_version < 7.1D) {
            root.config_version = 7.1D;
            if (!root.flags_configuration.enabled_flags.contains("teleport")) {
                root.flags_configuration.enabled_flags.add("teleport");
                root.flags.put("teleport", true);
            }
            update++;
        }

        if (root.config_version < 7.3D) {
            root.config_version = 7.3D;
            if (!root.flags_configuration.enabled_flags.contains("ender-chest")) {
                root.flags_configuration.enabled_flags.add("ender-chest");
                root.flags.put("ender-chest", false);
            }
            update++;
        }

        if (root.config_version < 7.4D) {
            root.config_version = 7.4D;
            root.private_cat.allowed_blocks.add("[A-Z_]+_SHULKER_BOX");
            update++;
        }

        if (root.config_version < 7.5D) {
            root.config_version = 7.5D;
            if (!root.flags_configuration.enabled_flags.contains("can-grow")) {
                root.flags_configuration.enabled_flags.add("can-grow");
                root.flags.put("can-grow", true);
            }
            update++;
        }

        if (root.config_version < 7.6D) {
            root.config_version = 7.6D;
            if (!root.flags_configuration.enabled_flags.contains("allow-effects")) {
                root.flags_configuration.enabled_flags.add("allow-effects");
                root.flags.put("allow-effects", true);
            }
            update++;
        }

        if (root.config_version < 7.7D) {
            root.config_version = 7.7D;
            if (!root.flags_configuration.enabled_flags.contains("allow-spawner")) {
                root.flags_configuration.enabled_flags.add("allow-spawner");
                root.flags.put("allow-spawner", false);
            }
            update++;
        }

        if (root.config_version < 7.8D) {
            root.config_version = 7.8D;
            if (!root.flags_configuration.enabled_flags.contains("leaves-decay")) {
                root.flags_configuration.enabled_flags.add("leaves-decay");
                root.flags.put("leaves-decay", true);
            }
            update++;
        }

        if (root.config_version < 7.9D) {
            root.config_version = 7.9D;
            if (!root.flags_configuration.enabled_flags.contains("build")) {
                root.flags_configuration.enabled_flags.add("build");
                root.flags.put("build", false);
            }
            update++;
        }

        if (root.config_version < 8.3D) {
            root.config_version = 8.3D;
            if (!root.flags_configuration.enabled_flags.contains("press-plate")) {
                root.flags_configuration.enabled_flags.add("press-plate");
                root.flags.put("press-plate", false);
            }
            if (!root.flags_configuration.enabled_flags.contains("fishing")) {
                root.flags_configuration.enabled_flags.add("fishing");
                root.flags.put("fishing", false);
            }
            update++;
        }

        if (update > 0) {
            RedProtect.get().logger.warning("Configuration UPDATED!");
        }

        //////////////////////

        //create logs folder
        File logs = new File(RedProtect.get().getDataFolder(), File.separator + "logs");
        if (root.log_actions && !logs.exists()) {
            logs.mkdir();
            RedProtect.get().logger.info("Created folder: " + RedProtect.get().getDataFolder() + File.separator + "logs");
        }

        save();
        RedProtect.get().logger.info("All configurations loaded!");

    }

    public void addWorldProperties(World w) {
        //add allowed claim worlds to config
        if (!root.allowed_claim_worlds.contains(w.getName())) {
            root.allowed_claim_worlds.add(w.getName());
            RedProtect.get().logger.warning("Added world to allowed claim list " + w.getName());
        }
        //add worlds to claim types list
        if (!root.region_settings.claim.world_types.containsKey(w.getName())) {
            root.region_settings.claim.world_types.put(w.getName(), "BLOCK");
            RedProtect.get().logger.warning("Added world to claim types list " + w.getName());
        }
        //add worlds to color list
        if (!root.region_settings.world_colors.containsKey(w.getName())) {
            switch (w.getEnvironment()) {
                case NORMAL:
                    root.region_settings.world_colors.put(w.getName(), "&a&l");
                case NETHER:
                    root.region_settings.world_colors.put(w.getName(), "&c&l");
                case THE_END:
                    root.region_settings.world_colors.put(w.getName(), "&5&l");
                default:
                    root.region_settings.world_colors.put(w.getName(), "&a&l");
            }
            RedProtect.get().logger.warning("Added world to colors list " + w.getName());
        }
        //add world to globalflags
        if (!globalFlagsRoot.worlds.containsKey(w.getName())) {
            globalFlagsRoot.worlds.put(w.getName(), new GlobalFlagsCategory.WorldProperties());
            saveGFlags();
        }
    }

    public FlagGuiCategory guiRoot() {
        return this.guiRoot;
    }

    public GlobalFlagsCategory globalFlagsRoot() {
        return this.globalFlagsRoot;
    }

    public MainCategory configRoot() {
        return this.root;
    }

    public int getGuiSlot(String flag) {
        return guiRoot.gui_flags.get(flag).slot;
    }

    public void setGuiSlot(/*String mat, */String flag, int slot) {
        guiRoot.gui_flags.get(flag).slot = slot;
        saveGui();
    }

    public ItemStack getGuiSeparator() {
        ItemStack separator = new ItemStack(Material.getMaterial(guiRoot.gui_separator.material), 1, (short) guiRoot.gui_separator.data);
        ItemMeta meta = separator.getItemMeta();
        meta.setDisplayName(RedProtect.get().guiLang.getFlagString("separator"));
        meta.setLore(Arrays.asList("", RedProtect.get().guiLang.getFlagString("separator")));
        separator.setItemMeta(meta);
        return separator;
    }

    public int getGuiMaxSlot() {
        SortedSet<Integer> slots = new TreeSet<>(new ArrayList<>());
        for (FlagGuiCategory.GuiFlag key : guiRoot.gui_flags.values()) {
            slots.add(key.slot);
        }
        return Collections.max(slots);
    }

    public HashMap<String, Object> getDefFlagsValues() {
        HashMap<String, Object> flags = new HashMap<>();
        for (Object oflag : root.flags.keySet()) {
            if (oflag instanceof String && isFlagEnabled(((String) oflag).replace("flags.", ""))) {
                String flag = (String) oflag;
                if (flag.equals("pvp") && !root.flags_configuration.enabled_flags.contains("pvp")) {
                    continue;
                }
                flags.put(flag, root.flags.get(flag));
            }
        }
        return flags;
    }

    public boolean isFlagEnabled(String flag) {
        return root.flags_configuration.enabled_flags.contains(flag) || AdminFlags.contains(flag);
    }

    public SortedSet<String> getDefFlags() {
        return new TreeSet<>(getDefFlagsValues().keySet());
    }

    public Material getBorderMaterial() {
        if (Material.getMaterial(guiRoot.gui_separator.material) != null) {
            return Material.getMaterial(guiRoot.gui_separator.material);
        }
        return Material.GLOWSTONE;
    }

    private void saveConfig() {
        try {
            configRoot.setValue(of(MainCategory.class), root);
            cfgLoader.save(configRoot);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private void saveGFlags() {
        try {
            gflagsRoot.setValue(of(GlobalFlagsCategory.class), globalFlagsRoot);
            gFlagsLoader.save(gflagsRoot);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            saveConfig();
            saveGFlags();

            ecoLoader.save(ecoCfgs);
            signsLoader.save(signCfgs);
            saveGui();
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            e.printStackTrace();
        }
    }

    public void saveGui() {
        try {
            guiCfgRoot.setValue(of(FlagGuiCategory.class), guiRoot);
            guiCfgLoader.save(guiCfgRoot);
        } catch (IOException | ObjectMappingException e) {
            RedProtect.get().logger.severe("Problems during save gui file:");
            e.printStackTrace();
        }
    }

    public boolean isAllowedWorld(Player p) {
        return root.allowed_claim_worlds.contains(p.getWorld().getName()) || p.hasPermission("redprotect.bypass.world");
    }

    public boolean addFlag(String flag, boolean defaultValue, boolean isAdmin) {
        if (isAdmin) {
            if (!AdminFlags.contains(flag)) {
                AdminFlags.add(flag);
                return true;
            }
        } else {
            if (!root.flags.containsKey(flag)) {
                root.flags.put(flag, defaultValue);
                root.flags_configuration.enabled_flags.add(flag);
                saveConfig();
                return true;
            }
        }
        return false;
    }

    public int getBlockCost(String itemName) {
        return ecoCfgs.getNode("items", "values", itemName).getInt();
    }

    public int getEnchantCost(String enchantment) {
        return ecoCfgs.getNode("enchantments", "values", enchantment).getInt(-1);
    }

    public String getEcoString(String key) {
        return ecoCfgs.getNode(key).getString("&4Missing economy string for &c" + key);
    }

    public Integer getEcoInt(String key) {
        return ecoCfgs.getNode(key).getInt();
    }

    public boolean getEcoBool(String key) {
        return ecoCfgs.getNode(key).getBoolean();
    }

    public String getWorldClaimType(String w) {
        return root.region_settings.claim.world_types.getOrDefault(w, "");
    }

    public boolean needClaimToBuild(Player p, Block b) {
        boolean bool = root.needed_claim_to_build.worlds.contains(p.getWorld().getName());
        if (bool) {
            if (b != null && root.needed_claim_to_build.allow_only_protections_blocks &&
                    (getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BLOCK") ||
                            getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BOTH"))) {
                boolean blocks = b.getType().name().contains(root.region_settings.block_id) ||
                        root.needed_claim_to_build.allow_break_blocks.stream().anyMatch(str -> str.equalsIgnoreCase(b.getType().name()));
                if (!blocks) {
                    RedProtect.get().lang.sendMessage(p, "need.claim.blockids");
                } else {
                    return false;
                }
            }
            RedProtect.get().lang.sendMessage(p, "need.claim.tobuild");
        }
        return bool;
    }

    public List<Location> getSigns(String rid) {
        List<Location> locs = new ArrayList<>();
        try {
            for (String s : signCfgs.getNode(rid).getList(of(String.class))) {
                String[] val = s.split(",");
                if (Bukkit.getServer().getWorld(val[0]) == null) {
                    continue;
                }
                locs.add(new Location(Bukkit.getServer().getWorld(val[0]), Double.valueOf(val[1]), Double.valueOf(val[2]), Double.valueOf(val[3])));
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return locs;
    }

    public void putSign(String rid, Location loc) {
        try {
            List<String> lsigns = signCfgs.getNode(rid).getList(of(String.class));
            String locs = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
            if (!lsigns.contains(locs)) {
                lsigns.add(locs);
                saveSigns(rid, lsigns);
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public void removeSign(String rid, Location loc) {
        try {
            List<String> lsigns = signCfgs.getNode(rid).getList(of(String.class));
            String locs = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
            if (lsigns.contains(locs)) {
                lsigns.remove(locs);
                saveSigns(rid, lsigns);
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private void saveSigns(String rid, List<String> locs) {
        if (locs.isEmpty()) {
            signCfgs.getNode(rid).setValue(null);
        } else {
            signCfgs.getNode(rid).setValue(locs);
        }
        try {
            signsLoader.save(signCfgs);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            e.printStackTrace();
        }
    }
}
   
