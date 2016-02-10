package alexiil.mods.traincraft.api;

import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.IRollingStock.Face;
import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.track.ITrackPath;

/** Provides a way for trains to find the next path dependant on a previous path. */
public interface ITrainMovementManager {
    /** Finds the next path that follows on from a given path, in a given direction (The returned path will either be
     * null or {@link ITrackPath#start()} will equal then given path's {@link ITrackPath#end()} */
    ITrackPath next(IBlockAccess access, ITrackPath from);

    ITrackPath closest(ComponentTrackFollower comp, Face direction);
}
