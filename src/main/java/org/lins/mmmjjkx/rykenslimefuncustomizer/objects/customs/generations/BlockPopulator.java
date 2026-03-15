package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.generations;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.meta.SkullMeta;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.Range;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;



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
	


    @Override
    public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk source) {
        if (blockedWorlds.contains(world.getName())) {
            return;
        }
        
        com.sk89q.worldedit.world.World faweworld = BukkitAdapter.adapt(world);
        
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
    			.world(faweworld)
                .maxBlocks(-1)
                .fastMode(true)
                .build()) {
        	
        	
        	List<ProjectAddon> addons = RykenSlimefunCustomizer.addonManager.getAllValues();

            for (ProjectAddon addon : addons) {
                List<GenerationInfo> generationInfos = addon.getGenerationInfos();

                for (GenerationInfo generationInfo : generationInfos) {
                    List<GenerationArea> areas = generationInfo.getAreas();

                    for (GenerationArea area : areas) {
                        if (area.getEnvironment() != world.getEnvironment()) continue;

                        for (int i = 0; i < area.getAmount(); i++)
                            generateNext(source.getX(), source.getZ(), world, random, generationInfo, area, editSession);
                    }
                }
            }
            
            editSession.flushQueue();
            //Bukkit.getLogger().log(Level.INFO, "批量设置头颅成功！");
        	
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("批量设置头颅失败", e);
        }

        
        
    }

    
    
    private void generateNext(
            int chunkX,
            int chunkZ,
            @Nonnull World world,
            @Nonnull Random random,
            @Nonnull GenerationInfo generationInfo,
            @Nonnull GenerationArea area,
            EditSession editSession) {
    	
    	// 更新Slimefun数据库
        BlockDataController controller = Slimefun.getDatabaseManager().getBlockDataController();
        
    	Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
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
    	            if (controller.getBlockData(location) != null) break;
    	        
    	            SlimefunItemStack slimefunItemStack = generationInfo.getSlimefunItemStack();
    	        
    	            BlockVector3 pos = BlockVector3.at(centerX, centerY, centerZ);
    	            if (slimefunItemStack.getType() == Material.PLAYER_HEAD) {
    	                SkullMeta meta = (SkullMeta) slimefunItemStack.getItemMeta();
    	                PlayerProfile profile = meta.getPlayerProfile();
    	                if (profile != null) {
    	                    String texture = getTexture(profile);
    	                    if (texture != null) {
    	                        // 使用FAWE的NBT构建方式
    	                    	// 1.21.1 正确的 NBT 结构
    	                    	com.sk89q.jnbt.CompoundTag nbt = new com.sk89q.jnbt.CompoundTag(Map.of(
    	                                "profile", new com.sk89q.jnbt.CompoundTag(Map.of(
    	                                    // 生成一个随机UUID，使用IntArrayTag表示
    	                                    "id", new IntArrayTag(new int[]{
    	                                        UUID.randomUUID().hashCode(),
    	                                        UUID.randomUUID().hashCode(),
    	                                        UUID.randomUUID().hashCode(),
    	                                        UUID.randomUUID().hashCode()
    	                                    }),
    	                                    "properties", new ListTag(com.sk89q.jnbt.CompoundTag.class, List.of(
    	                                        new com.sk89q.jnbt.CompoundTag(Map.of(
    	                                            "name", new StringTag("textures"),
    	                                            "value", new StringTag(texture)
    	                                            // 注意：1.21.1中通常不需要 signature，除非你处理的是正版玩家的签名数据
    	                                        ))
    	                                    ))
    	                                ))
    	                            ));
    	                        
    	                        // 创建带NBT的BaseBlock
    	                        
    	                        BaseBlock skullBlock = new BaseBlock(BlockTypes.PLAYER_HEAD.getDefaultState(), nbt);
    	                        
    	                        editSession.setBlock(pos, skullBlock);
    	                    } else {
    	                        editSession.setBlock(pos, BlockTypes.PLAYER_HEAD.getDefaultState());
    	                    }
    	                }
    	            } else {
    	                // 非头颅方块直接设置
    	                BlockState blockState = BlockTypes.parse(slimefunItemStack.getType().name()).getDefaultState();
    	                editSession.setBlock(pos, blockState);
    	            }
    	            
    	            /*
                     * Fix: There already a block in this location.
                     */
    	            
    	            synchronized (controller) {
    	            	if (controller.getBlockData(location) == null) {

                            
                            try {
                            	controller.createBlock(location, slimefunItemStack.getItemId());
                            } catch (IllegalStateException illegalStateException) {
                                // ignore
                            }
        	            }
    	            }
    	            
    	            //Bukkit.getLogger().log(Level.INFO, "X:" + centerX + " Y:" + centerY + " Z:" + centerZ);                        
                    r = random.nextInt(0, 3);
                    if (r == 0) {
                        centerX++;
                    } else if (r == 1) {
                        centerY++;
                    } else if (r == 2) {
                        centerZ++;
                    }
                }
                
                
            
    		
    	});
    	
    	
        
    }
                		
    private String getTexture(PlayerProfile profile) {
        if (profile == null) return null;
        for (ProfileProperty property : profile.getProperties()) {
            if ("textures".equals(property.getName())) {
                return property.getValue();
            }
        }
        return null;
    }
                
}
