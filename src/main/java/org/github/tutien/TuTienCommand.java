package org.github.tutien;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;


public class TuTienCommand implements CommandExecutor {
    private final LinhKhiManager linhKhiManager;
    private final CanhGioiManager canhGioiManager;
    public TuTienCommand(LinhKhiManager linhKhiManager,CanhGioiManager canhGioiManager) {
        this.linhKhiManager = linhKhiManager;
        this.canhGioiManager = canhGioiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Lệnh này chỉ có thể sử dụng bởi người chơi.");
            return true;
        }

        Player player = (Player) sender;

        // Kiểm tra nếu có tham số
        if (args.length == 0) {
            player.sendMessage("Vui lòng nhập lệnh đúng: /tutien linhkhi");
            return true;
        }

        if (args[0].equalsIgnoreCase("linhkhi")) {
            int linhKhi = linhKhiManager.getLinhKhi(player);
            player.sendMessage("Bạn đang có " + linhKhi + " linh khí.");
            return true;
        }

        if (args[0].equalsIgnoreCase("tuluyen")) {
            linhKhiManager.toggleTuLuyen(player);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String type = args[1];
            ItemStack item = null;
            String name = "";
            String lore = "";

            if (type.equalsIgnoreCase("ha")) {
                item = new ItemStack(Material.EMERALD);
                name = "§aLinh Thạch Hạ Phẩm";
                lore = "§7Dùng chuột phải để hấp thụ Linh Khí.";
            } else if (type.equalsIgnoreCase("trung")) {
                item = new ItemStack(Material.DIAMOND);
                name = "§bLinh Thạch Trung Phẩm";
                lore = "§7Dùng chuột phải để hấp thụ Linh Khí.";
            } else if (type.equalsIgnoreCase("thuong")) {
                item = new ItemStack(Material.NETHERITE_INGOT);
                name = "§cLinh Thạch Thượng Phẩm";
                lore = "§7Dùng chuột phải để hấp thụ Linh Khí.";
            } else if (type.equalsIgnoreCase("dotphadan_ha")) {
                item = new ItemStack(Material.POTION);
                name = "§aĐột Phá Đan Hạ Phẩm";
                lore = "§7Dùng để đột phá cảnh giới.";
            } else if (type.equalsIgnoreCase("dotphadan_trung")) {
                item = new ItemStack(Material.POTION);
                name = "§bĐột Phá Đan Trung Phẩm";
                lore = "§7Dùng để đột phá cảnh giới.";
            }else if (type.equalsIgnoreCase("dotphadan_thuong")) {
                item = new ItemStack(Material.POTION);
                name = "§cĐột Phá Đan Thượng Phẩm";
                lore = "§7Dùng để đột phá cảnh giới.";
            }

            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(Collections.singletonList(lore));
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
                player.sendMessage("§aBạn đã nhận " + name);
            } else {
                player.sendMessage("§cLoại Linh Thạch không hợp lệ!");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("canhgioi")) {
            player.sendMessage("§aCảnh giới hiện tại: " + canhGioiManager.getCanhGioi(player) + " tầng " + canhGioiManager.getTang(player));
            return true;
        }

        if (args[0].equalsIgnoreCase("dotpha")) {
            canhGioiManager.dotPha(player);
            return true;
        }
        player.sendMessage("Lệnh không hợp lệ!");
        return true;
    }
}

