package org.github.tutien;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
public class CanhGioiExpansion extends PlaceholderExpansion {
    private final TuTien plugin;
    public CanhGioiExpansion (TuTien plugin){
        this.plugin = plugin;
    }
    @Override
    public @NotNull String getIdentifier() {
        return "tutien";
    }

    @Override
    public String getAuthor() {
        return "athanh";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        Player p = player.getPlayer();
        CanhGioiManager canhGioiManager = plugin.getCanhGioiManager();
        // Lấy cảnh giới hiện tại của người chơi
        if (identifier.equalsIgnoreCase("canhgioi")) {
            return canhGioiManager.getCanhGioi(p);
        }

        // Lấy tầng hiện tại của người chơi
        if (identifier.equalsIgnoreCase("tang")) {
            return String.valueOf(canhGioiManager.getTang(p));
        }

        return null;
    }

}
