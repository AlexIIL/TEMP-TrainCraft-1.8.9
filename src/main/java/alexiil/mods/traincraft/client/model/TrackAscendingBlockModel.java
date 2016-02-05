package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPathStraight;
import alexiil.mods.traincraft.block.BlockAbstractTrack;
import alexiil.mods.traincraft.block.BlockAbstractTrack.EnumDirection;
import alexiil.mods.traincraft.block.BlockTrackAscending;
import alexiil.mods.traincraft.client.model.Plane.Face;
import alexiil.mods.traincraft.lib.BlockStateKeyWrapper;
import alexiil.mods.traincraft.property.BlockStatePropWrapper;

public class TrackAscendingBlockModel extends PerspAwareModelBase implements ISmartBlockModel {
    private Map<BlockStateKeyWrapper, IBakedModel> cache = new HashMap<>();
    private final BlockTrackAscending ascending;

    public TrackAscendingBlockModel(BlockTrackAscending ascending) {
        super(null, null, null, null);
        this.ascending = ascending;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        BlockStateKeyWrapper keyWrapper = new BlockStateKeyWrapper(state);
        if (!cache.containsKey(keyWrapper)) {
            ITrackPath path = ascending.path(state);
            if (path == null) return this;
            IBlockState materialState = null;
            if (state instanceof IExtendedBlockState) {
                IExtendedBlockState extended = (IExtendedBlockState) state;
                BlockStatePropWrapper wrapper = extended.getValue((IUnlistedProperty<BlockStatePropWrapper>) BlockTrackAscending.MATERIAL_TYPE);
                if (wrapper != null) materialState = wrapper.state;
            }
            IBakedModel material;
            if (materialState == null) {
                material = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
            } else material = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(materialState);

            IBakedModel baked = generateBlockModel(path, state, material);
            cache.put(keyWrapper, baked);
        }
        return cache.get(keyWrapper);
    }

    private IBakedModel generateBlockModel(ITrackPath path, IBlockState state, IBakedModel materialBaked) {
        List<BakedQuad> quads = new ArrayList<>();

        if (path instanceof TrackPathStraight) {
            EnumDirection dir = state.getValue(BlockTrackAscending.TRACK_DIRECTION);
            EnumFacing mf = dir.from;
            if (state.getValue(BlockTrackAscending.ASCEND_DIRECTION)) mf = dir.to;
            if (dir == EnumDirection.EAST_WEST) mf = mf.getOpposite();
            // It must be an ascending track that goes along a normal axis.
            TrackPathStraight straight = (TrackPathStraight) path;
            Vec3 planePoint = straight.interpolate(0.5).addVector(0, -BlockAbstractTrack.TRACK_HEIGHT, 0);
            Vec3 planeNormal = new Vec3(0, ascending.length, 0);
            planeNormal = planeNormal.add(new Vec3(BlockPos.ORIGIN.offset(mf, -1)));
            Plane plane = new Plane(planePoint, planeNormal);

            List<BakedQuad> materialQuads = new ArrayList<>(materialBaked.getGeneralQuads());
            for (EnumFacing face : EnumFacing.values())
                materialQuads.addAll(materialBaked.getFaceQuads(face));

            List<MutableQuad> mutableMaterial = ModelSplitter.makeMutable(materialQuads, DefaultVertexFormats.BLOCK);
            List<MutableQuad> allOffsets = new ArrayList<>();
            for (BlockPos offset : ascending.slaveOffsets(straight)) {
                allOffsets.addAll(ModelSplitter.offset(mutableMaterial, new Vec3(offset)));
            }
            List<MutableQuad>[] bisected = ModelSplitter.bisect(allOffsets, plane);
            List<MutableQuad> squishedQuadList = ModelSplitter.squashBisected(bisected, plane, Face.AWAY);
            quads.addAll(ModelSplitter.makeVanilla(squishedQuadList, DefaultVertexFormats.BLOCK));
        }
        
        quads.addAll(CommonModelSpriteCache.generateRails(path, CommonModelSpriteCache.INSTANCE.railSprite(false)));
        quads.addAll(CommonModelSpriteCache.generateSleepers(path, CommonModelSpriteCache.INSTANCE.loadSleepers()));

        return ModelUtil.wrapInBakedModel(quads, null);
    }
}
