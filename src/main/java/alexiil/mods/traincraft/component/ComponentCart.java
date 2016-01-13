package alexiil.mods.traincraft.component;

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

public class ComponentCart extends ComponentResting {
    private static final ResourceLocation modelLocation = new ResourceLocation("traincraft:models/trains/cart.obj");
    private static final ResourceLocation textureLocation = new ResourceLocation("traincraft:trains/cart");

    public ComponentCart(IRollingStock stock, IComponent childFront, IComponent childBack, double frontBack) {
        super(stock, childFront, childBack, frontBack);
    }

    @Override
    public double frontArea() {
        return super.frontArea() + 0.5;
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
        return new ComponentCart(stock, childFront.createNew(stock), childBack.createNew(stock), frontBack);
    }

    @Override
    protected AxisAlignedBB box() {
        return new AxisAlignedBB(-0.5, 0.2, 0, 0.5, 1, 1);
    }
}
