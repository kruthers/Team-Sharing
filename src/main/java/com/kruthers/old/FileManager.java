/*
 * Team Sharing - A Spigot plugin thats allows plays on the same team to share there inventory
 * Copyright (C) 2020 kruthers
 *
 * This Program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The program  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kruthers.old;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class FileManager {
    private static final TeamSharing plugin = TeamSharing.getPlugin(TeamSharing.class);

    private static final String PLUGIN_FILE = plugin.getDataFolder().getPath()+"/";
    private static final Logger LOGGER = plugin.getLogger();

    private static File dataFile = new File(PLUGIN_FILE+"inventories.yml");
    private static FileConfiguration savedData;

    public static boolean init(){
        if (dataFile.exists()){
            savedData = YamlConfiguration.loadConfiguration(dataFile);
        } else {
            try {
                dataFile.createNewFile();
                savedData = YamlConfiguration.loadConfiguration(dataFile);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.severe("Failed to load user data, aborting");
                return false;
            }
        }

        return true;
    }

}
