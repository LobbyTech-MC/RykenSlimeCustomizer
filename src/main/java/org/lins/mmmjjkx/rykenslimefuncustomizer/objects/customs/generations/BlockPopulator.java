package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.generations;

import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.Range;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;


public class BlockPopulator extends org.bukkit.generator.BlockPopulator {
    private static final List<String> blockedWorlds = List.of(
            "CAsteroidBelt",
            "CMars",
            "CMoon",
            "dimensionalhome",
            "ft_world",
            "ne_muspelheim",
            "ne_niflheim",
            "SmallSpace",
            "space",
            "world_galactifun_earth_orbit",
            "world_galactifun_enceladus",
            "world_galactifun_europa",
            "world_galactifun_io",
            "world_galactifun_mars",
            "world_galactifun_the_moon",
            "world_galactifun_titan",
            "world_galactifun_venus",
            "world_void",
            "corporate_dimension",
            "logispace");
	private static Map<String, PlayerSkin> skinCache = new HashMap<>();
	public static void setSkull121(Location loc, String textureUrl) {
	    // 1. 构造 Base64 纹理
	    String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";
	    String base64 = Base64.getEncoder().encodeToString(json.getBytes());

	    com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(loc.getWorld());

	    try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
	        // 2. 构建符合 1.21 结构的 NBT (用于 Block Entity Data)
	        // 1.21 中，头颅的属性结构嵌套在 profile 标签下
	        CompoundTag textureValue = CompoundTagBuilder.create()
	            .put("Value", new StringTag(base64))
	            .build();

	        ListTag texturesList = new ListTag(CompoundTag.class, Collections.singletonList(textureValue));
	        CompoundTag properties = CompoundTagBuilder.create()
	            .put("textures", texturesList)
	            .build();

	        CompoundTag profile = CompoundTagBuilder.create()
	            .put("id", new IntArrayTag(new int[]{1, 2, 3, 4})) // 1.21 依然支持 IntArray UUID
	            .put("properties", properties)
	            .build();

	        // 3. 包装进最终的方块实体数据
	        CompoundTag nbt = CompoundTagBuilder.create()
	            .put("profile", profile) // 注意：1.21 中很多字段改为了小写
	            .build();

	        // 4. 应用到方块
	        BaseBlock skullBlock = BlockTypes.PLAYER_HEAD.getDefaultState().toBaseBlock(nbt);
	        editSession.setBlock(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()), skullBlock);
	    }
	}

    @Override
    public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk source) {
        if (blockedWorlds.contains(world.getName())) {
            return;
        }

        List<ProjectAddon> addons = RykenSlimefunCustomizer.addonManager.getAllValues();

        for (ProjectAddon addon : addons) {
            List<GenerationInfo> generationInfos = addon.getGenerationInfos();

            for (GenerationInfo generationInfo : generationInfos) {
                List<GenerationArea> areas = generationInfo.getAreas();

                for (GenerationArea area : areas) {
                    if (area.getEnvironment() != world.getEnvironment()) continue;

                    for (int i = 0; i < area.getAmount(); i++)
                        generateNext(source.getX(), source.getZ(), world, random, generationInfo, area);
                }
            }
        }
    }

    
    
    private void generateNext(
            int chunkX,
            int chunkZ,
            @Nonnull World world,
            @Nonnull Random random,
            @Nonnull GenerationInfo generationInfo,
            @Nonnull GenerationArea area) {
        Range height = area.getHeight();
        int h = height.getDistance() + 1;
        int r;

        if (h < 0) {
            h = 1;
        }

        double s2 = random.nextDouble(0, h);

        double sTop = (height.max() - area.getMost() + 1);
        if (s2 < sTop) {
            int h2MaxHeight = (int) (s2 * 2);
            r = height.max() - h2MaxHeight;
        } else {
            s2 -= sTop;
            int h2MinHeight = (int) (s2 * 2);
            r = height.min() + h2MinHeight;
        }

        int centerX = (chunkX << 4) + random.nextInt(16);
        int centerY = r;
        int centerZ = (chunkZ << 4) + random.nextInt(16);

        for (int i = 0; i < area.getSize().getRandomBetween(random); i++) {
            Location location = new Location(world, centerX, centerY, centerZ);
            Block block = world.getBlockAt(centerX, centerY, centerZ);
            if (!(centerX >= (chunkX << 4)
                    && centerX < (chunkX << 4) + 16
                    && centerZ >= (chunkZ << 4)
                    && centerZ < (chunkZ << 4) + 16)) {
                break;
            }
            if (block.getType() != area.getReplacement()) break;

            SlimefunItemStack slimefunItemStack = generationInfo.getSlimefunItemStack();

            Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
            	
            	block.setType(slimefunItemStack.getType(), false);
                if (block.getType() == Material.PLAYER_HEAD && slimefunItemStack.getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) slimefunItemStack.getItemMeta();
                    PlayerProfile profile = meta.getPlayerProfile();
                    if (profile != null) {
                        PlayerTextures textures = profile.getTextures();
                        URL skin = textures.getSkin();
                        if (skin != null && block.getType() == Material.PLAYER_HEAD) {
                        	setSkull121(block.getLocation(), skin.toString());
                        }
                    }
                }
            });

            		
            		
            

            
            Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
            	try {
            		BlockDataController controller = Slimefun.getDatabaseManager().getBlockDataController();

            		// 1. 将 Bukkit 的 World 转为 WorldEdit 的 World
            		com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());
            		// 2. 创建 EditSession（建议使用 try-with-resources 自动关闭并刷新队列）
            		try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
            		    // 3. 设置方块 (坐标使用 BlockVector3)
            		    BlockVector3 position = BlockVector3.at(location.getX(), location.getY(), location.getZ());
            		    if (editSession.getBlock(position).getBlockType() != BlockTypes.AIR) {
            		    	editSession.setBlock(position, BlockTypes.AIR); 
            		    }
            		    
            		    
            		    // 4. (可选) 如果不使用 try-with-resources，必须手动执行 editSession.close();
            		}
            		
            		if (controller.getBlockData(location) != null) {
            		    controller.removeBlock(location);
            		}
                		
            		if (location.getBlock().getType() == Material.AIR && controller.getBlockData(location) == null) {
            			controller.createBlock(location, generationInfo.getSlimefunItemStack().getItemId());
            		}

                	
            	} catch  (IllegalStateException e) {
        			e.printStackTrace();
        		}
            	
            	
            });
            

            r = random.nextInt(0, 3);
            if (r == 0) {
                centerX++;
            } else if (r == 1) {
                centerY++;
            } else if (r == 2) {
                centerZ++;
            }
        }
    }
}
