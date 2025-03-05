package org.github.tutien;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LinhKhiManager implements Listener {
    private final TuTien plugin;
    private File file;
    private FileConfiguration data;
    private final Set<UUID> playersTuluen = new HashSet<>();
    private final HashMap<UUID, BukkitRunnable> absorptionTasks = new HashMap<>();

    public LinhKhiManager(TuTien plugin) {
        this.plugin = plugin;
        loadFile();
        startLinhKhiTask();
        startActionBarTask();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadFile() {
        file = new File(plugin.getDataFolder(), "linhkhi.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public int getLinhKhi(Player player) {
        return data.getInt("players." + player.getUniqueId() + ".linhKhi", 0);
    }

    public void setLinhKhi(Player player, int amount) {
        data.set("players." + player.getUniqueId() + ".linhKhi", amount);
        saveFile();
    }
    public int getLinhKhiLimit(Player player) {
        String canhGioi = plugin.getCanhGioiManager().getCanhGioi(player);
        for (int i = 0; i < plugin.getCanhGioiManager().REALMS.length; i++) {
            if (plugin.getCanhGioiManager().REALMS[i].equals(canhGioi)) {
                return plugin.getCanhGioiManager().LINH_KHI_LIMITS[i];
            }
        }
        return 50; // Mặc định là Phàm Nhân
    }
    public void addLinhKhi(Player player, int amount) {
        int current = getLinhKhi(player);
        int maxLinhKhi = getLinhKhiLimit(player); // Lấy giới hạn từ cảnh giới
        int newAmount = Math.min(current + amount, maxLinhKhi);
        setLinhKhi(player, newAmount);
    }

    private void saveFile() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startLinhKhiTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : playersTuluen) {
                    Player player = Bukkit.getPlayer(uuid);
                    int linhKhiZone = plugin.getLinhKhiZoneManager().getLinhKhiAt(player.getLocation());
                    if (linhKhiZone > 0) {
                        addLinhKhi(player, Math.min(linhKhiZone, plugin.getConfig().getInt("linhKhiPerTick", 1)));
                    }
                }
            }
        }.runTaskTimer(plugin, 0, plugin.getConfig().getInt("tickInterval", 200));
    }

    public void toggleTuLuyen(Player player) {
        UUID uuid = player.getUniqueId();
        if (playersTuluen.contains(uuid)) {
            playersTuluen.remove(uuid);
            player.setSneaking(false);
            player.sendMessage("§cBạn đã dừng tu luyện.");
        } else {
            playersTuluen.add(uuid);
            player.setSneaking(true);
            player.sendMessage("§aBạn đã bắt đầu tu luyện.");
        }
    }
    public boolean isTuLuyen(Player player) {
        return playersTuluen.contains(player.getUniqueId());
    }
    private void startActionBarTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int linhKhi = plugin.getLinhKhiZoneManager().getLinhKhiAt(player.getLocation());
                    player.sendActionBar("§eKhu vực linh khí: " + linhKhi);
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Cập nhật mỗi 2 giây
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean hasMovedXZ = event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ();
        if (hasMovedXZ && (playersTuluen.contains(uuid) || absorptionTasks.containsKey(uuid))) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerUseLinhThach(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName();
            final int absorbedLinhKhi;
            final int absorptionDuration;
            if (name.equals("§aLinh Thạch Hạ Phẩm")) {
                absorbedLinhKhi = 100;
                absorptionDuration = 60;
            } else if (name.equals("§bLinh Thạch Trung Phẩm")) {
                absorbedLinhKhi = 1000;
                absorptionDuration = 300;
            } else if (name.equals("§cLinh Thạch Thượng Phẩm")) {
                absorbedLinhKhi = 10000;
                absorptionDuration = 600;
            } else {
                return;
            }

            if (absorbedLinhKhi > 0) {
                if (absorptionTasks.containsKey(player.getUniqueId())) {
                    player.sendMessage("§cBạn đang hấp thụ linh thạch, không thể dùng thêm!");
                    return;
                }
                player.sendMessage("§aBắt đầu hấp thụ " + name + " trong " + (absorptionDuration / 60) + " phút...");
                item.setAmount(item.getAmount() - 1);
                UUID uuid = player.getUniqueId();

                BukkitRunnable task = new BukkitRunnable() {
                    int timeLeft = absorptionDuration;
                    @Override
                    public void run() {
                        if (timeLeft <= 0) {
                            addLinhKhi(player, absorbedLinhKhi);
                            player.sendMessage("§aBạn đã hấp thụ thành công " + name + "!");
                            absorptionTasks.remove(uuid);
                            cancel();
                        } else {
                            player.sendActionBar("§eĐang hấp thụ " + name + ": " + timeLeft + " giây...");
                            timeLeft--;
                        }
                    }
                };
                absorptionTasks.put(uuid, task);
                task.runTaskTimer(plugin, 0, 20);
            }
        }
    }
}
