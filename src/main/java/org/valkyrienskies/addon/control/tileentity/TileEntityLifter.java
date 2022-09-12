package org.valkyrienskies.addon.control.tileentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.config.VSControlConfig;
import org.valkyrienskies.addon.control.fuel.IValkyriumEngine;
import org.valkyrienskies.addon.control.nodenetwork.IForceTile;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nonnull;
import java.util.Optional;


public class TileEntityLifter extends TileEntityEnvironment implements IValkyriumEngine, IForceTile, ITickable {

    private double thrustMultiplierGoal;
    private double maxThrust;
    private Vector3dc forceNormal;

    private double targetYPosition;
    private boolean isYTarget;

    public TileEntityLifter(double maxThrust) {
        super();
        this.maxThrust =maxThrust;
        this.thrustMultiplierGoal=0;
        this.targetYPosition=20.0F;
        this.isYTarget=false;
        this.forceNormal= new Vector3d(0,1,0);
        this.node = Network.newNode(this, Visibility.Network).withComponent("vs_lifter", Visibility.Network).create();
    }

    public TileEntityLifter() {
        this(VSControlConfig.ENGINE_THRUST.compressorMaxThrust);
    }

    @Override
    public void update() {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
                .getPhysoManagingBlock(getWorld(), getPos());

        if (physicsObject.isPresent() && this.isYTarget) {
            // The linear velocity of the ship
            Vector3d linearVel = physicsObject.get()
                    .getPhysicsCalculations()
                    .getVelocityAtPoint(new Vector3d());
            // The global coordinates of this tile entity
            Vector3d tilePos = new Vector3d(getPos().getX() + .5, getPos().getY() + .5,
                    getPos().getZ() + .5);
            physicsObject.get()
                    .getShipTransformationManager()
                    .getCurrentPhysicsTransform()
                    .transformPosition(tilePos, TransformType.SUBSPACE_TO_GLOBAL);

            // Utilizing a proper PI controller for very smooth control.
            double heightWithIntegral = tilePos.y + linearVel.y * .3D;
            double heightDelta = targetYPosition - heightWithIntegral;
            double multiplier = heightDelta / 2D;
            multiplier = Math.max(0, Math.min(1, multiplier));
            setThrustMultiplierGoal(multiplier);
        }
    }

    @Override
    public double getMaxThrust() {
        return maxThrust;
    }

    @Override
    public void setMaxThrust(double maxThrust) {
        this.maxThrust = maxThrust;
        markDirty();
    }

    public double getTargetYPosition() {
        return targetYPosition;
    }

    public void setTargetYPosition(double targetYPosition) {
        this.targetYPosition = targetYPosition;
        markDirty();
    }

    public Vector3dc getForceNormal() {
        return forceNormal;
    }

    public void setForceNormal(Vector3dc forceNormal) {
        this.forceNormal = forceNormal;
        markDirty();
    }

    public boolean isYTarget() {
        return isYTarget;
    }

    public void setYTarget(boolean YTarget) {
        isYTarget = YTarget;
        markDirty();
    }

    @Override
    public double getThrustMultiplierGoal() {
        return thrustMultiplierGoal;
    }

    @Override
    public void setThrustMultiplierGoal(double thrustMultiplierGoal) {
        this.thrustMultiplierGoal = thrustMultiplierGoal;
        markDirty();
    }

    @Override
    public Vector3dc getForceOutputNormal(double secondsToApply, PhysicsObject object) {
        return forceNormal;
    }


    @Override
    public double getThrustMagnitude(PhysicsObject physicsObject) {
        return this.getMaxThrust() * this.getThrustMultiplierGoal() * this.getCurrentValkyriumEfficiency(physicsObject);

    }

    @Override
    public double getCurrentValkyriumEfficiency(@Nonnull PhysicsObject physicsObject) {
        Vector3d tilePos = new Vector3d(getPos().getX() + .5D, getPos().getY() + .5D,
                getPos().getZ() + .5D);
        physicsObject
                .getShipTransformationManager()
                .getCurrentPhysicsTransform()
                .transformPosition(tilePos, TransformType.SUBSPACE_TO_GLOBAL);
        double yPos = tilePos.y;
        return IValkyriumEngine.getValkyriumEfficiencyFromHeight(yPos);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);
        toReturn.setDouble("thrustMultiplierGoal", thrustMultiplierGoal);
        toReturn.setDouble("targetYPosition", targetYPosition);
        toReturn.setBoolean("isYTarget", isYTarget);
        toReturn.setDouble("maxThrust", maxThrust);
        NBTTagCompound vec = new NBTTagCompound();
        vec.setDouble("x", forceNormal.x());
        vec.setDouble("y", forceNormal.y());
        vec.setDouble("z", forceNormal.z());
        toReturn.setTag("vec", vec);
        return toReturn;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.thrustMultiplierGoal = compound.getDouble("thrustMultiplierGoal");
        this.targetYPosition = compound.getDouble("targetYPosition");
        this.isYTarget = compound.getBoolean("isYTarget");
        this.maxThrust = compound.getDouble("maxThrust");
        NBTTagCompound vec = compound.getCompoundTag("vec");
        this.forceNormal = new Vector3d(
                vec.getDouble("x"),
                vec.getDouble("y"),
                vec.getDouble("z")
        );
    }

    // Open Computers //


    @Callback(value = "get_max_thrust")
    public Object[] getMaxThrust(Context con, Arguments args) {
        return new Object[]{this.getMaxThrust()};
    }

    @Callback(value = "set_max_thrust")
    public Object[] setMaxThrust(Context con, Arguments args) {
        this.setMaxThrust(args.checkDouble(0));
        return new Object[0];
    }

    @Callback(value = "get_thrust_multi")
    public Object[] getThrustMultiplierGoal(Context con, Arguments args) {
        return new Object[]{getThrustMultiplierGoal()};
    }

    @Callback(value = "set_thrust_multi")
    public Object[] setThrustMultiplierGoal(Context con, Arguments args) {
        setThrustMultiplierGoal(args.checkDouble(0));
        return new Object[0];
    }

    @Callback(value = "get_thrust_vec")
    public Object[] getThrustVector(Context con, Arguments args) {
        return new Object[]{getForceNormal()};
    }

    @Callback(value = "set_thrust_vec")
    public Object[] setThrustVector(Context con, Arguments args) {
        setForceNormal(new Vector3d(
                args.checkDouble(0),
                args.checkDouble(1),
                args.checkDouble(2)
        ));
        return new Object[0];
    }

    @Callback(value = "get_target_y_position")
    public Object[] getTargetYPosition(Context con, Arguments args) {
        return new Object[]{getTargetYPosition()};
    }

    @Callback(value = "set_target_y_position")
    public Object[] setTargetYPosition(Context con, Arguments args) {
        setTargetYPosition(args.checkDouble(0));
        return new Object[0];
    }

    @Callback(value = "is_hover")
    public Object[] getIsYTarget(Context con, Arguments args) {
        return new Object[]{isYTarget()};
    }

    @Callback(value = "set_hover")
    public Object[] setIsYTarget(Context con, Arguments args) {
        setYTarget(args.checkBoolean(0));
        return new Object[0];
    }

}
