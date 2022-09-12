package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.block.multiblocks.IMultiblockSchematic;
import org.valkyrienskies.addon.control.block.multiblocks.TileEntityValkyriumEnginePart;
import org.valkyrienskies.addon.control.block.multiblocks.ValkyriumEngineMultiblockSchematic;
import org.valkyrienskies.addon.control.block.torque.IRotationNode;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeProvider;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorld;
import org.valkyrienskies.addon.control.block.torque.ImplRotationNode;
import org.valkyrienskies.addon.control.block.torque.custom_torque_functions.ValkyriumEngineTorqueFunction;
import org.valkyrienskies.addon.control.util.ValkyrienSkiesControlUtil;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.VSMath;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.List;
import java.util.Optional;

public class TileEntityMotor extends TileEntity implements IRotationNodeProvider<TileEntityMotor>, ITickable {

    private static final int ROTATION_NODE_SORT_PRIORITY = 10000;
    @SuppressWarnings("WeakerAccess")
    protected final IRotationNode rotationNode;
    private boolean firstUpdate;

    @SuppressWarnings("WeakerAccess")
    public TileEntityMotor() {
        super();
        this.rotationNode = new ImplRotationNode<>(this, 50, ROTATION_NODE_SORT_PRIORITY);
        this.firstUpdate = true;
    }

    @Override
    public void update() {
        if (!this.getWorld().isRemote) {
            if (firstUpdate) {
                this.rotationNode.markInitialized();
                firstUpdate = false;
            }

            if (true) {
                Optional<PhysicsObject> physicsObjectOptional = ValkyrienUtils
                        .getPhysoManagingBlock(getWorld(), getPos());

                IRotationNodeWorld nodeWorld;
                if (physicsObjectOptional.isPresent()) {
                    nodeWorld = ValkyrienSkiesControlUtil
                            .getRotationWorldFromShip(physicsObjectOptional.get());
                } else {
                    nodeWorld = ValkyrienSkiesControlUtil.getRotationWorldFromWorld(getWorld());
                }
                if (physicsObjectOptional.isPresent() && !rotationNode.hasBeenPlacedIntoNodeWorld()) {
                    nodeWorld.enqueueTaskOntoWorld(
                            () -> nodeWorld.setNodeFromPos(getPos(), rotationNode));
                }

                BlockPos torqueOutputPos = getPos();
                TileEntity tileEntity = this.getWorld().getTileEntity(torqueOutputPos);
                if (tileEntity instanceof TileEntityMotor) {
                    if (((TileEntityMotor) tileEntity).getRotationNode()
                            .isPresent()) {

                        double radiansRotatedThisTick =
                                ((TileEntityMotor) tileEntity).getRotationNode().get()
                                        .getAngularVelocityUnsynchronized() / 20D;
                        // Thats about right, although the x1.3 multiplier tells me the world node math is wrong.

                    }
                }
                VSNetwork.sendTileToAllNearby(this);
            }
            this.markDirty();
        }  // Client keyframe interpolating logic, use .85 to smoothly slide towards actual value
        // to appear more fluid when the server lags.

    }


    // The following methods are basically just here because interfaces can't have fields.
    @Override
    public Optional<IRotationNode> getRotationNode() {
        if (rotationNode.isInitialized()) {
            return Optional.of(rotationNode);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (this.getWorld() == null || !this.getWorld().isRemote) {
            rotationNode.readFromNBT(compound);
        }
//        rotationNode.markInitialized();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        rotationNode.writeToNBT(compound);
        return compound;
    }
}
