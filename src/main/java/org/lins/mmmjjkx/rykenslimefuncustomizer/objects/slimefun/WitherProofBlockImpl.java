package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun;

import javax.annotation.Nonnull;

import org.bukkit.block.Block;
import org.bukkit.entity.Wither;

import io.github.thebusybiscuit.slimefun4.core.attributes.WitherProof;

public interface WitherProofBlockImpl extends WitherProof {
    default void onAttack(@Nonnull Block var1, @Nonnull Wither var2) {}
}
