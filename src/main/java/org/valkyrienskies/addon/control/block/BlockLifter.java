package org.valkyrienskies.addon.control.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.config.VSControlConfig;
import org.valkyrienskies.addon.control.tileentity.TileEntityGyroscopeDampener;
import org.valkyrienskies.addon.control.tileentity.TileEntityLifter;
import org.valkyrienskies.addon.control.util.BaseBlock;
import org.valkyrienskies.mod.common.block.IBlockForceProvider;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import javax.annotation.Nullable;
import java.util.List;

public class BlockLifter extends BaseBlock implements IBlockForceProvider, ITileEntityProvider {

    public BlockLifter() {
        super("lifter", Material.GLASS, 4.0F, true);
    }

    /**
     * The force Vector this block gives within its local space (Not within World space).
     */
    @Nullable
    @Override
    public Vector3dc getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state,
                                              PhysicsObject physicsObject, double secondsToApply) {

        TileEntity thisTile = ValkyrienUtils.getTileEntitySafe(world, pos);
        if (thisTile instanceof TileEntityLifter) {
            TileEntityLifter tileLifter = (TileEntityLifter) thisTile;

            return tileLifter.getForceOutputUnoriented(secondsToApply, physicsObject);
        }
        return null;
    }

    /**
     * Blocks that shouldn't have their force rotated (Like Valkyrium Compressors) must return false.
     */
    @Override
    public boolean shouldLocalForceBeRotated(World world, BlockPos pos, IBlockState state,
        double secondsToApply) {
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World player,
                               List<String> itemInformation,
                               @NotNull ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + "" + TextFormatting.BOLD +
            I18n.format("tooltip.vs_control.lifter", VSControlConfig.ENGINE_THRUST.compressorMaxThrust));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityLifter(VSControlConfig.ENGINE_THRUST.compressorMaxThrust);
    }
}
