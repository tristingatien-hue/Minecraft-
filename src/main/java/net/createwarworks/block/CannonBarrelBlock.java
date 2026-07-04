package net.createwarworks.block;

import net.minecraft.world.level.block.RotatedPillarBlock;

/**
 * A section of big cannon barrel. Place barrels in a straight line in front of
 * a cannon breech (matching the breech's facing) — each barrel adds muzzle
 * velocity, up to the configured cap. No assembly step required: the breech
 * counts its barrels every time it fires.
 */
public class CannonBarrelBlock extends RotatedPillarBlock {
    public CannonBarrelBlock(Properties properties) {
        super(properties);
    }
}
