package org.valkyrienskies.addon.control.tileentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.*;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.config.VSControlConfig;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import valkyrienwarfare.api.TransformType;

public class TileEntityGyroscopeStabilizer extends TileEntityEnvironment {


    // The direction we are want to align to.
    private Vector3dc levelVector;

    public TileEntityGyroscopeStabilizer(Vector3dc vec) {
        super();
        this.levelVector = vec;
        this.node = Network.newNode(this, Visibility.Network)
                .withComponent("gyroscope_stabilizer", Visibility.Network).create();

    }

    public TileEntityGyroscopeStabilizer() {
        this(new Vector3d(0, 1, 0));
    }



    public Vector3dc getLevelVector() {
        return this.levelVector;
    }

    public void setLevelVector(double x, double y, double z) {
        this.levelVector = new Vector3d(x,y,z);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

            NBTTagCompound vec = compound.getCompoundTag("vec");
            this.levelVector = new Vector3d(
                    vec.getDouble("x"),
                    vec.getDouble("y"),
                    vec.getDouble("z")
            );
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound vec = new NBTTagCompound();
        vec.setDouble("x", this.levelVector.x());
        vec.setDouble("y", this.levelVector.y());
        vec.setDouble("z", this.levelVector.z());
        compound.setTag("vec", vec);
        return super.writeToNBT(compound);
    }

    @Callback(doc = "get_vector_level():[x:double, y:double, z:double]; Get the current leveling vector.", value = "getvectorlevel")
    public Object[] getVectorLevel(Context con, Arguments args) {
        Vector3dc vec = getLevelVector();
        return new Object[]{vec.x(),vec.y(),vec.z()};
    }

    @Callback(doc = "set_vector_level(x:double, y:double, z:double):boolean; set the current leveling vector.", value = "setvectorlevel")
    public Object[] setVectorLevel(Context con, Arguments args) {
        setLevelVector(args.checkDouble(0), args.checkDouble(1), args.checkDouble(2));
        return new Object[]{true};
    }
    public Vector3dc getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        Vector3d shipLevelNormal = new Vector3d(levelVector);
        physicsCalculations.getParent().getShipTransformationManager().getCurrentPhysicsTransform()
            .transformDirection(shipLevelNormal, TransformType.SUBSPACE_TO_GLOBAL);
        Vector3d torqueDir = levelVector.cross(shipLevelNormal, new Vector3d());

        if (torqueDir.lengthSquared() < .0000000001) {
            // The ship is already level, don't try to divide by 0
            return new Vector3d();
        }

        double angleBetween = Math.toDegrees(levelVector.angle(shipLevelNormal));
        torqueDir.normalize();

        double torquePowerFactor = angleBetween / 5;

        torquePowerFactor = Math.max(Math.min(1, torquePowerFactor), 0);

        // System.out.println(angleBetween);

        return torqueDir.mul(VSControlConfig.stabilizerMaxTorque * torquePowerFactor * physicsCalculations
            .getPhysicsTimeDeltaPerPhysTick() * -1D);
    }
    public String getComponentName() {
        return "gyroscope_stabilizer";
    }
}
