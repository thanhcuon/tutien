package org.github.tutien;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TuTien extends JavaPlugin {
    private LinhKhiManager linhKhiManager;
    private LinhKhiZoneManager linhKhiZoneManager;
    private CanhGioiManager canhGioiManager;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.linhKhiManager = new LinhKhiManager(this);
        this.linhKhiZoneManager = new LinhKhiZoneManager(this);
        this.canhGioiManager = new CanhGioiManager(this);
        if (getCommand("tutien") != null) {
            getCommand("tutien").setExecutor(new TuTienCommand(this.linhKhiManager,this.canhGioiManager));
        } else {
            getLogger().warning("Không thể tìm thấy lệnh 'tutien' trong plugin.yml!");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LinhKhiExpansion(this).register();
        }


    }

    @Override
    public void onDisable() {

    }
    public LinhKhiManager getLinhKhiManager() {
        return linhKhiManager;
    }
    public LinhKhiZoneManager getLinhKhiZoneManager() {
        return linhKhiZoneManager;
    }
    public CanhGioiManager getCanhGioiManager(){
        return canhGioiManager;
    }

}
