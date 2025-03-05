package org.github.tutien;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
public class LinhKhiExpansion extends PlaceholderExpansion {
    private final TuTien plugin;

    public LinhKhiExpansion(TuTien plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "tutien";
    }

    @Override
    public String getAuthor() {
        return "athanh";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.isOnline()) {
            return "0";
        }

        Player p = player.getPlayer();
        LinhKhiManager linhKhiManager = plugin.getLinhKhiManager();

        if (params.equalsIgnoreCase("linhkhi")) {
            return String.valueOf(linhKhiManager.getLinhKhi(p));
        }

        if (params.equalsIgnoreCase("linhkhi_limit")) {
            return String.valueOf(linhKhiManager.getLinhKhiLimit(p));
        }
        CanhGioiManager canhGioiManager = plugin.getCanhGioiManager();
        // Lấy cảnh giới hiện tại của người chơi
        if (params.equalsIgnoreCase("canhgioi")) {
            return canhGioiManager.getCanhGioi(p);
        }

        // Lấy tầng hiện tại của người chơi
        if (params.equalsIgnoreCase("tang")) {
            return String.valueOf(canhGioiManager.getTang(p));
        }

        return null;
    }
}
