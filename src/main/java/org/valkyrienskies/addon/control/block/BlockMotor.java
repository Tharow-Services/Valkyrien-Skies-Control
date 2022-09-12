package org.valkyrienskies.addon.control.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.tileentity.TileEntityMotor;
import org.valkyrienskies.addon.control.util.BaseBlock;

import javax.annotation.Nullable;
import java.util.List;

public class BlockMotor extends BaseBlock implements ITileEntityProvider {

    public BlockMotor() {
        super("motor", Material.IRON, 0.0F, true);
        this.setHardness(6.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player,
        List<String> itemInformation,
        ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.BLUE + I18n.format("tooltip.vs_control.motor"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMotor();
    }
}
