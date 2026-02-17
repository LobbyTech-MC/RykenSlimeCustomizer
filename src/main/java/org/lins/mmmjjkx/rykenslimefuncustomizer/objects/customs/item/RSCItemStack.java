package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.item;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;

public class RSCItemStack extends CustomItemStack {
    public RSCItemStack(ItemStack item, String name, List<String> lore) {
        super(item, meta -> {
            if (name != null && !name.isBlank()) {
                meta.setDisplayName(name);
            }

            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
        });
    }
}
