package org.github.tutien;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CanhGioiManager {
    private final TuTien plugin;
    private File file;
    private FileConfiguration data;
    private final Random random = new Random();

    public final String[] REALMS = {"Phàm Nhân", "Luyện Khí", "Trúc Cơ", "Kim Đan", "Nguyên Anh", "Hóa Thần", "Luyện Hư", "Hợp Thể", "Đại Thừa", "Độ Kiếp", "Phi Tiên"};
    public final int[] LINH_KHI_LIMITS = {50, 100, 500, 1000, 5000, 10000, 50000, 100000, 500000, 1000000, 5000000};
    private static final Map<String, Integer> LINH_KHI_REQUIREMENTS = new HashMap<>() {{
        put("Phàm Nhân", 50);
        put("Luyện Khí", 100);
        put("Trúc Cơ", 500);
        put("Kim Đan", 1000);
        put("Nguyên Anh", 5000);
        put("Hóa Thần", 10000);
        put("Luyện Hư", 50000);
        put("Hợp Thể", 100000);
        put("Đại Thừa", 500000);
        put("Độ Kiếp", 1000000);
        put("Phi Tiên", 5000000);
    }};
    public final int[] DOT_PHA_TIME = {10, 15, 20, 30, 45, 60, 90, 120, 150, 180, 240};

    public CanhGioiManager(TuTien plugin) {
        this.plugin = plugin;
        loadFile();
    }

    private void loadFile() {
        file = new File(plugin.getDataFolder(), "canhgioi.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    private void saveFile() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCanhGioi(Player player) {
        return data.getString("players." + player.getUniqueId() + ".canhGioi", "Phàm Nhân");
    }

    public int getTang(Player player) {
        return data.getInt("players." + player.getUniqueId() + ".tang", 1);
    }

    public void setCanhGioi(Player player, String canhGioi, int tang) {
        data.set("players." + player.getUniqueId() + ".canhGioi", canhGioi);
        data.set("players." + player.getUniqueId() + ".tang", tang);
        saveFile();
    }

    public boolean dotPha(Player player) {
        String currentRealm = getCanhGioi(player);
        int currentTang = getTang(player);
        int linhKhi = plugin.getLinhKhiManager().getLinhKhi(player);

        int index = -1;
        for (int i = 0; i < REALMS.length; i++) {
            if (REALMS[i].equals(currentRealm)) {
                index = i;
                break;
            }
        }


        if (index == -1 || (index == REALMS.length - 1 && currentTang == 10)) {
            player.sendMessage("§eBạn đã đạt đến giới hạn cảnh giới!");
            return false;
        }

        // Xác định số linh khí cần để đột phá theo cảnh giới hiện tại
        String currentRealmName = REALMS[index];
        int requiredLinhKhi = LINH_KHI_REQUIREMENTS.getOrDefault(currentRealmName, 50); // Lấy số linh khí cần, nếu không có thì mặc định là 50

        // Kiểm tra linh khí có đủ không
        if (linhKhi < requiredLinhKhi) {
            player.sendMessage("§cKhông đủ linh khí để đột phá! Cần: " + requiredLinhKhi);
            return false;
        }


        plugin.getLinhKhiManager().setLinhKhi(player, linhKhi - requiredLinhKhi);

        double baseSuccessRate = 0.3; // Tỷ lệ đột phá mặc định

        // Giảm tỷ lệ dựa vào cảnh giới hiện tại (cảnh giới càng cao, tỷ lệ càng thấp)
        double realmPenalty = index * 0.05; // Mỗi cảnh giới cao hơn giảm 5%
        double successRate = Math.max(0.05, baseSuccessRate - realmPenalty); // Đảm bảo không thấp hơn 5%

        if (currentTang == 10) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    String itemName = item.getItemMeta().getDisplayName();
                    if (itemName.equals("§aĐột Phá Đan Hạ Phẩm")) {
                        successRate += 0.2; // +20% nếu dùng đan hạ phẩm
                        item.setAmount(item.getAmount() - 1);
                        break;
                    } else if (itemName.equals("§bĐột Phá Đan Trung Phẩm")) {
                        successRate += 0.4; // +40% nếu dùng đan trung phẩm
                        item.setAmount(item.getAmount() - 1);
                        break;
                    } else if (itemName.equals("§cĐột Phá Đan Thượng Phẩm")) {
                        successRate += 0.6; // +60% nếu dùng đan thượng phẩm
                        item.setAmount(item.getAmount() - 1);
                        break;
                    }
                }
            }
        }

        // Giới hạn tỷ lệ tối đa là 95% (không có gì chắc chắn là 100% hết hiểu ko ní)
        successRate = Math.min(successRate, 0.95);
        // Thông báo tỷ lệ đột phá cho người chơi
        player.sendMessage("§6Tỷ lệ đột phá của bạn là: " + (successRate * 100) + "%");

        final int realmIndex = index;
        final double finalSuccessRate = successRate;


        if (player.hasMetadata("dang_dot_pha")) {
            player.sendMessage("§cBạn đang trong quá trình đột phá, hãy chờ!");
            return false;
        }


        player.setMetadata("dang_dot_pha", new FixedMetadataValue(plugin, true));

        boolean isRealmBreakthrough = (currentTang == 10);
        if (isRealmBreakthrough) {
            player.sendMessage("§6Đang đột phá... Hãy chờ " + DOT_PHA_TIME[index] + " giây!");

            new BukkitRunnable() {
                int timeLeft = DOT_PHA_TIME[realmIndex];

                @Override
                public void run() {
                    if (timeLeft <= 0) {
                        cancel();
                        return;
                    }

                    Location loc = player.getLocation();
                    World world = player.getWorld();
                    world.strikeLightningEffect(loc);

                    double currentHealth = player.getHealth();
                    double maxDamage = Math.max(2, currentHealth * 0.15); // Thiên lôi gây 15% máu hiện tại, ít nhất 2 sát thương

                    if (currentHealth > 2) {
                        player.damage(Math.min(maxDamage, currentHealth - 2)); // Không trừ quá mức, đảm bảo còn 2HP
                    }

                    if (player.getHealth() <= 2) {
                        cancel();
                    }

                    timeLeft--;
                }
            }.runTaskTimer(plugin, 0L, 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.removeMetadata("dang_dot_pha", plugin);
                if (random.nextDouble() < finalSuccessRate) {
                    setCanhGioi(player, REALMS[realmIndex + 1], 1);
                    player.setHealth(2);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2400, 1));
                    player.sendMessage("§aĐột phá thành công! Bạn đã tiến vào " + getCanhGioi(player) + " tầng 1!");
                    Location loc = player.getLocation();
                    World world = player.getWorld();
                    world.strikeLightning(loc);
                } else {
                    int fallBackTang = Math.max(1, currentTang - random.nextInt(5));
                    setCanhGioi(player, currentRealm, fallBackTang);
                    player.sendMessage("§cĐột phá thất bại! Bạn bị giáng xuống " + currentRealm + " tầng " + fallBackTang);
                }
            }, DOT_PHA_TIME[realmIndex] * 20L);
        } else {
            setCanhGioi(player, currentRealm, currentTang + 1);
            player.sendMessage("§aĐột phá thành công lên " + getCanhGioi(player) + " tầng " + getTang(player) + "!");
        }
        int[] MAX_HEALTH_BY_REALM = {20, 25, 30, 40, 50, 60, 80, 100, 120, 150, 200};

        if (random.nextDouble() < finalSuccessRate) {
            setCanhGioi(player, REALMS[realmIndex + 1], 1);
            double newMaxHealth = MAX_HEALTH_BY_REALM[realmIndex + 1];
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
            player.setHealth(newMaxHealth);
            player.sendMessage("§aĐột phá thành công! Máu tối đa của bạn đã tăng lên " + newMaxHealth + " HP!");
        }
        // Định nghĩa sát thương theo cảnh giới
        double[] DAMAGE_BY_REALM = {1.0, 3.0, 5.0, 8.0, 12.0, 15.0, 20.0, 30.0, 40.0, 50.0, 70.0};

        if (random.nextDouble() < finalSuccessRate) {
            setCanhGioi(player, REALMS[realmIndex + 1], 1);

            // Cập nhật sát thương
            double newAttackDamage = DAMAGE_BY_REALM[realmIndex + 1];
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(newAttackDamage);

            player.sendMessage("§aĐột phá thành công! Sát thương của bạn đã tăng lên " + newAttackDamage + "!");
        }
        double[] SPEED_BY_REALM = {0.1, 0.12, 0.14, 0.16, 0.18, 0.20, 0.22, 0.25, 0.28, 0.3, 0.35};

        if (random.nextDouble() < finalSuccessRate) {
            setCanhGioi(player, REALMS[realmIndex + 1], 1);

            // Cập nhật tốc độ di chuyển
            double newSpeed = SPEED_BY_REALM[realmIndex + 1];
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(newSpeed);

            player.sendMessage("§aĐột phá thành công! Tốc độ di chuyển của bạn đã tăng lên " + newSpeed + "!");
        }


        return true;

    }
}
