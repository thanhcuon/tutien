package org.github.tutien;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class LinhKhiZoneManager {
    private final TuTien plugin;
    private final File file;
    private final FileConfiguration data;
    private final HashMap<String, Integer> cachedLinhKhi = new HashMap<>();
    private final Random random = new Random();

    public LinhKhiZoneManager(TuTien plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "linhkhi_zones.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }


    public int getLinhKhiAt(Location location) {
        Chunk chunk = location.getChunk();
        String key = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();

        if (cachedLinhKhi.containsKey(key)) {
            return cachedLinhKhi.get(key);
        }

        // Nếu chưa có dữ liệu, random cấp độ linh khí và lưu lại
        int linhKhiLevel = randomLinhKhi();
        data.set("chunks." + key, linhKhiLevel);
        saveFile();
        cachedLinhKhi.put(key, linhKhiLevel);
        return linhKhiLevel;
    }


    private int randomLinhKhi() {
        int roll = random.nextInt(100);
        if (roll < 5) return 3;  // 10% - Linh khí cao
        if (roll < 15) return 2;  // 20% - Linh khí trung bình
        if (roll < 60) return 1;  // 30% - Linh khí thấp
        return 0;                 // 40% - Không có linh khí
    }

    private void saveFile() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
