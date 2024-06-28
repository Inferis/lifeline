package org.inferis.lifeline.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

import org.inferis.lifeline.EntityTracker;
import org.inferis.lifeline.LifeLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<E extends LivingEntity, EM extends EntityModel<E>> extends EntityRenderer<E> implements FeatureRendererContext<E, EM> {

	protected LivingEntityRendererMixin(Context ctx) {
		super(ctx);
	}

    @Inject(at=@At("TAIL"), method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
    public void render(E livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo callbackInfo) {
		if (!EntityTracker.INSTANCE.isTracking(livingEntity)) {
			LifeLine.LOGGER.info("Not tracking + " + livingEntity.getUuid() + " (" + livingEntity.getName() + ")");
			return;
		}

        // var distance = this.dispatcher.getSquaredDistanceToCamera(livingEntity);

		var health = MathHelper.ceil(livingEntity.getHealth());
        var maxHealth = MathHelper.ceil(livingEntity.getMaxHealth());
        var absorption = MathHelper.ceil(livingEntity.getAbsorptionAmount());
		var label = (health + absorption) + "/" + maxHealth;

		matrixStack.push();

		matrixStack.translate(0, livingEntity.getHeight() + 0.5, 0);
		matrixStack.multiply(this.dispatcher.getRotation());

		var client = MinecraftClient.getInstance();
		var textRenderer = client.textRenderer;
        var x = -textRenderer.getWidth(label) / 2.0f;
        var model = matrixStack.peek().getPositionMatrix();
        textRenderer.draw(label, x, 0, Color.WHITE.getRGB(), true, model, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, 0, light);
		LifeLine.LOGGER.info(livingEntity.getUuid() + " (" + livingEntity.getName() + ") " + label);

		matrixStack.pop();
	}
}
