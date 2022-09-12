package org.valkyrienskies.addon.control.tileentity;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.config.VSControlConfig;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import valkyrienwarfare.api.TransformType;

import java.util.Locale;

public class TileEntityGyroscopeDampener extends TileEntity implements SimpleComponent, ManagedPeripheral {

    private Vector3dc levelVector;

    public TileEntityGyroscopeDampener() {
        this.levelVector = new Vector3d(0, 1, 0);
    }

    @Override
    public String getComponentName() {
        return "gyroscope_dampener";
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
        if (compound.hasKey("vec")) {
            NBTTagCompound vec = compound.getCompoundTag("vec");
            this.levelVector = new Vector3d(
                    vec.getDouble("x"),
                    vec.getDouble("y"),
                    vec.getDouble("z")
            );
        } else {
            this.levelVector = new Vector3d(0,1,0);
        }
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

    @Override
    public String[] methods() {
        return new String[]{"get_vector_level", "set_vector_level"};
    }

    @Override
    public Object[] invoke(String s, Context context, Arguments args) throws Exception {
        switch (s.toLowerCase(Locale.ROOT)) {
            case "get_vector_level": {
                Vector3dc vec = getLevelVector();
                return new Object[]{vec.x(),vec.y(),vec.z()};
            }
            case "set_vector_level": {
                setLevelVector(args.checkDouble(0), args.checkDouble(1), args.checkDouble(2));
                return new Object[]{true};
            }
            default: throw new NoSuchMethodException();
        }
    }

    public Vector3dc getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        Vector3d shipLevelNormal = new Vector3d(levelVector);
        physicsCalculations.getParent().getShipTransformationManager().getCurrentPhysicsTransform()
            .transformDirection(shipLevelNormal, TransformType.SUBSPACE_TO_GLOBAL);

        double dampingComponent = shipLevelNormal.dot(new Vector3d(physicsCalculations.getAngularVelocity()));
        Vector3d angularChangeAllowed = shipLevelNormal
            .mul(shipLevelNormal.dot(new Vector3d(physicsCalculations.getAngularVelocity())), new Vector3d());
        Vector3d angularVelocityToDamp = new Vector3d(physicsCalculations.getAngularVelocity())
            .sub(angularChangeAllowed);

        Vector3d dampingTorque = angularVelocityToDamp
            .mul(physicsCalculations.getPhysicsTimeDeltaPerPhysTick());


        Vector3d dampingTorqueWithRespectToInertia = physicsCalculations.getPhysMOITensor().transform(dampingTorque);

        double dampingTorqueRespectMagnitude = dampingTorqueWithRespectToInertia.length();
        if (dampingTorqueRespectMagnitude > VSControlConfig.dampenerMaxTorque) {
            dampingTorqueWithRespectToInertia
                .mul(VSControlConfig.dampenerMaxTorque / dampingTorqueRespectMagnitude);

        }

        return dampingTorqueWithRespectToInertia.mul(-1);
    }
}
