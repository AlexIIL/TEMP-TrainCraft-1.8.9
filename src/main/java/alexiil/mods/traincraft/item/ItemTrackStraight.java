package alexiil.mods.traincraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.block.BlockTrackStraight;
import alexiil.mods.traincraft.block.EnumDirection;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.track.TrackBehaviourStraightState;

public class ItemTrackStraight extends ItemBlockTrack {
    public ItemTrackStraight(Block block) {
        super(block);
    }

    @Override
    protected IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        EnumFacing entFacing = player.getHorizontalFacing();
        IBlockState state = TCBlocks.TRACK_STRAIGHT.getBlock().getDefaultState();
        if (entFacing.getAxis() == Axis.X) return state.withProperty(BlockTrackStraight.TRACK_DIRECTION, EnumDirection.EAST_WEST);
        else return state.withProperty(BlockTrackStraight.TRACK_DIRECTION, EnumDirection.NORTH_SOUTH);
    }

    @Override
    protected TrackBehaviourStateful statefulState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX,
            float hitY, float hitZ) {
        TrackBehaviourStraightState state = new TrackBehaviourStraightState(world, pos);
        EnumFacing entFacing = player.getHorizontalFacing();
        if (entFacing.getAxis() == Axis.X) state.setDir(EnumDirection.EAST_WEST);
        else state.setDir(EnumDirection.NORTH_SOUTH);
        return state;
    }
}
