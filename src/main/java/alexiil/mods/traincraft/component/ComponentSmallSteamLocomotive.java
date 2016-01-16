package alexiil.mods.traincraft.component;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.component.ComponentResting;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.render.RenderRollingStockBase;

public class ComponentSmallSteamLocomotive extends ComponentResting {
    private static final ResourceLocation modelLocation = new ResourceLocation("traincraft:models/trains/steam_small.obj");
    private static final ResourceLocation textureLocation = new ResourceLocation("traincraft:trains/steam_small");

    public ComponentSmallSteamLocomotive(IRollingStock stock, IComponent childFront, IComponent childBack, List<IComponent> childMiddle,
            double frontBack) {
        super(stock, childFront, childBack, childMiddle, frontBack);
    }

    @Override
    public double frontArea() {
        return super.frontArea() + 1.2;
    }

    @SideOnly(Side.CLIENT)
    public static void textureStitch(TextureStitchEvent.Pre event) {
        event.map.registerSprite(textureLocation);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IRollingStock stock, float partialTicks) {
        childFront.render(stock, partialTicks);
        childBack.render(stock, partialTicks);

        GlStateManager.pushMatrix();
        preRenderOffsets(stock, partialTicks);

        RenderRollingStockBase.renderModel(modelLocation);

        GlStateManager.popMatrix();
    }

    @Override
    public IComponent createNew(IRollingStock stock) {
        List<IComponent> middle = childMiddle.stream().map(c -> c.createNew(stock)).collect(Collectors.toList());
        return new ComponentSmallSteamLocomotive(stock, childFront.createNew(stock), childBack.createNew(stock), middle, frontBack);
    }

    @Override
    protected AxisAlignedBB box() {
        return new AxisAlignedBB(-1, 0.2, 0, 1, 1.25, 1);
    }
}
