package alexiil.mods.traincraft.api.track.behaviour;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.common.util.INBTSerializable;

import alexiil.mods.traincraft.api.INetSerialisable;
import alexiil.mods.traincraft.api.lib.MathUtil;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;

import io.netty.buffer.ByteBuf;

/** This controls how a train interacts with a tracks and can get a path that a train can use to follow this track. */
public abstract class TrackBehaviour {
    /** An immutable set of just the origin point to be used for all tracks that have paths which are completly
     * contained within a single block. */
    public static final ImmutableSet<BlockPos> SINGLE_BLOCK_SLAVES = ImmutableSet.of(BlockPos.ORIGIN);

    /** Private constructor to force either of the below types to be used as they are specially coded for. */
    private TrackBehaviour() {}

    /** @return A path that will traverse over this behaviour. So long as a rolling stock is traversing this path
     *         {@link #onStockPass(IRollingStock)} will be called. */
    public abstract ITrackPath getPath(World world, BlockPos pos, IBlockState state);

    /** @return An identifier that can identify this behaviour verses any other. */
    public abstract TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state);

    /** Checks to see if the appropriate block/tile for this track still exists in the world. */
    public boolean isValid(World world, BlockPos pos, IBlockState state) {
        return true;
    }

    /** Tests to see if the given track is allowed to overlap with this track. Note that you do not need to check to see
     * if the two {@link #getPath(World, BlockPos, IBlockState)} are actually allowed over each other.
     * 
     * @param otherTrack The track to test
     * @return True if you can overlap, false if not. */
    public abstract boolean canOverlap(TrackBehaviour otherTrack);

    /** @return All of the positions that this track passes over. This should include the ORIGIN as an offset. It is
     *         recommended that you create a set with {@link #createSlaveOffsets(ITrackPath)} */
    public abstract Set<BlockPos> getSlaveOffsets(World world, BlockPos pos, IBlockState state);

    public static Set<BlockPos> createSlaveOffsets(ITrackPath path) {
        ImmutableSet.Builder<BlockPos> slaves = ImmutableSet.builder();
        // Calculate slaves
        for (int i = 0; i < path.length() * 5; i++) {
            double offset = i;
            offset += 0.5;
            offset /= path.length();
            Vec3 pos = path.interpolate(offset);
            Vec3 dir = path.direction(offset);
            dir = MathUtil.cross(dir, new Vec3(0, 1, 0)).normalize();

            slaves.add(new BlockPos(pos.add(MathUtil.scale(dir, 0.2))));
            slaves.add(new BlockPos(pos.add(MathUtil.scale(dir, -0.2))));
        }
        return slaves.build();
    }

    /** Called once per tick by a stock to let the track intract with the stock. */
    public abstract void onStockPass(World world, BlockPos pos, IBlockState state, IRollingStock stock);

    /** A behaviour that is completly stored within a single block. It is recommended that you also provide a
     * {@link TrackBehaviourStateful} instance to use for allowing your track to overlap with other tracks. */
    public static abstract class TrackBehaviourNative extends TrackBehaviour {
        /** Attempts to convert this track into a stateful instance- that can be used to create points or a crossing. */
        public abstract TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state);
    }

    /** A behaviour that can save and load itself from inside a tile entity. If a subclass implements {@link ITickable}
     * then the wrapping tile used will call {@link ITickable#update()} every tick. getPath, getIdentifier and isValid
     * will ignore all of the arguments you give them. */
    public static abstract class TrackBehaviourStateful extends TrackBehaviour implements INBTSerializable<NBTTagCompound>, INetSerialisable {
        /** @return A factory that will create new instances of this behaviour for client->server or world->disk->world
         *         transfer */
        public abstract StatefulFactory factory();

        /** Attempts to convert this track (in position) to its native version (if one exists)
         * 
         * @param owner The tile entity that currently owns this state.
         * 
         * @return True if this was converted into block-only format in the world, false. */
        public abstract boolean convertToNative(TileEntity owner);

        @Override
        public final ITrackPath getPath(World world, BlockPos pos, IBlockState state) {
            return getPath();
        }

        public abstract ITrackPath getPath();

        @Override
        public final TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state) {
            return getIdentifier();
        }

        public abstract TrackIdentifier getIdentifier();

        @Override
        public final Set<BlockPos> getSlaveOffsets(World world, BlockPos pos, IBlockState state) {
            return getSlaveOffsets();
        }

        public abstract Set<BlockPos> getSlaveOffsets();

        @Override
        public final void onStockPass(World world, BlockPos pos, IBlockState state, IRollingStock stock) {
            onStockPass(stock);
        }

        public abstract void onStockPass(IRollingStock stock);

        // Block related methods
        public void onNeighbourChange(TileEntity owner) {}
    }

    public interface StatefulFactory {
        /** @return A globally unique identifier for this factory. It is recommended this is in the form
         *         "modid:moduniquename" to ensure this is globally unique. */
        String identifier();

        /** @return A new instance of the state that can be loaded from either {@link NBTTagCompound} of a
         *         {@link ByteBuf} or is usable in its default configuration */
        TrackBehaviourStateful create(World world, BlockPos pos);
    }
}
