package org.inferis.lifeline.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

import org.inferis.lifeline.EntityTracker;
import org.inferis.lifeline.HeartType;
import org.inferis.lifeline.LifeLine;
import org.inferis.lifeline.config.LifeLineConfig;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<E extends LivingEntity, EM extends EntityModel<E>> extends EntityRenderer<E> implements FeatureRendererContext<E, EM> {
	public class Scale {
		static final float world = 0.025f;
		static final float text = 1.f;
	}

	protected LivingEntityRendererMixin(Context ctx) {
		super(ctx);
	}

    @Inject(at=@At("TAIL"), method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
    public void render(E livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo callbackInfo) {
		if (!EntityTracker.INSTANCE.isTracking(livingEntity)) {
			return;
		}

		matrixStack.push();

		// position above entity
		matrixStack.translate(0, livingEntity.getHeight() + 0.5, 0);

		// adjustments
		adjustMatrixForLabel(matrixStack, livingEntity);
		adjustMatrixForScoreBoard(matrixStack, livingEntity);

		// also face the camera
		matrixStack.multiply(this.dispatcher.getRotation());
		// we need to scale down a bit too.
		matrixStack.scale(Scale.world, Scale.world, Scale.world);
		// Apply configured scale
		matrixStack.scale(LifeLine.CONFIG.scale, LifeLine.CONFIG.scale, LifeLine.CONFIG.scale);

		switch (LifeLine.CONFIG.displayMode) {
			case LifeLineConfig.DisplayMode.BOTH: 
				drawLabel(livingEntity, matrixStack, vertexConsumerProvider, light);
				// Move up a bit
				matrixStack.translate(0, 10.0, 0);
				drawHearts(livingEntity, matrixStack);
				break;
			case LifeLineConfig.DisplayMode.HEARTS:
				drawHearts(livingEntity, matrixStack);
				break;
			case LifeLineConfig.DisplayMode.LABEL:
				drawLabel(livingEntity, matrixStack, vertexConsumerProvider, light);
				break;
		}

		matrixStack.pop();
	}

	private void drawLabel(E livingEntity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
		matrixStack.push();

		// flip the vertical scale because text draws upside down
		matrixStack.scale(Scale.text, -Scale.text, Scale.text);

		final var health = MathHelper.ceil(livingEntity.getHealth()) / 2.0;
        final var maxHealth = MathHelper.ceil(livingEntity.getMaxHealth()) / 2.0;
        final var absorption = MathHelper.ceil(livingEntity.getAbsorptionAmount()) / 2.0;

		// Some tricky to get the numbers right. We don't want "2.0" but "2", 
		// but we do want "2.5".
		var label = "%1.0f".formatted(maxHealth);
		if ((int)(health + absorption) == (health + absorption)) {
			label = "%1.0f/%s".formatted(health + absorption, label);
		}
		else {
			label = "%1.1f/%s".formatted(health + absorption, label);
		}

		final var client = MinecraftClient.getInstance();
		final var textRenderer = client.textRenderer;
        final var x = -textRenderer.getWidth(label) / 2.0f;
        final var model = matrixStack.peek().getPositionMatrix();
        textRenderer.draw(label, 
			x, 0, 
			Color.WHITE.brighter().getRGB(), 
			true, 
			model, 
			vertexConsumerProvider, 
			TextRenderer.TextLayerType.NORMAL, 
			0,
			light);
		
		matrixStack.pop();
	}

	private void drawHearts(E livingEntity, MatrixStack matrixStack) {
		matrixStack.push();

		final var health = MathHelper.ceil(livingEntity.getHealth());
        final var maxHealth = MathHelper.ceil(livingEntity.getMaxHealth());
        final var absorption = MathHelper.ceil(livingEntity.getAbsorptionAmount());

		final var redHearts = MathHelper.ceil(health / 2.0);
        final var halfRedHeart = (health % 2) == 1;
        final var fullHearts = MathHelper.ceil(maxHealth / 2.0);
        final var yellowHearts = MathHelper.ceil(absorption / 2.0);
        final var halfYellowHeart = (absorption % 2) == 1;
        final var totalHearts = fullHearts + yellowHearts;

        final var tessellator = Tessellator.getInstance();
        final var heartsPerRow = 10;
		final var spacing = 8;

		final var totalWide = Math.min(totalHearts, heartsPerRow) * spacing + 1;
		final var maxX = totalWide / 2.0f;

		matrixStack.scale(-1, 1, 1);

		for (var isDrawingHearts: new boolean[] { false, true }) {
			for (var heart = 0; heart<totalHearts; ++heart) {
				matrixStack.push();

				final var level = MathHelper.floor(heart / heartsPerRow);
				matrixStack.translate(0, level * 10.0, 0);

				final var bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				final var model = matrixStack.peek().getPositionMatrix();
				final var x = maxX - (heart % heartsPerRow) * spacing;

				var type = HeartType.EMPTY;
				if (isDrawingHearts) {
                    if (heart < redHearts) {
                        type = HeartType.REGULAR_FULL;
                        if (halfRedHeart && heart == redHearts-1) {
                            type = HeartType.REGULAR_HALF;
                        }
                    } 
					else if (heart >= fullHearts) {
                        type = HeartType.ABSORBING_FULL;
                        if (halfYellowHeart && heart == fullHearts-1) {
                            type = HeartType.ABSORBING_HALF;
                        }
                    }
				}
				drawHeart(type, x, model, bufferBuilder);
				matrixStack.pop();
				
				try {
					final var builtBuffer = bufferBuilder.end();
					if (builtBuffer != null) {
						BufferRenderer.drawWithGlobalProgram(builtBuffer);
						builtBuffer.close();
					}
				}
				catch (Exception e){
					LifeLine.LOGGER.error("Could not build vertex buffer", e);
				}
			}
		}

		matrixStack.pop();
	}

	private void drawHeart(HeartType type, Float x, Matrix4f model, BufferBuilder buffer) {
        final var minU = 0.f;
        final var maxU = 1.f;
        final var minV = 0.f;
        final var maxV = 1.f;
        final var heartSize = 9.f;

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, type.icon);
        RenderSystem.enableDepthTest();

		buffer.vertex(model, x, 0.f - heartSize, 0.f).texture(minU, maxV);
        buffer.vertex(model, x - heartSize, 0.f - heartSize, 0.0F).texture(maxU, maxV);
        buffer.vertex(model, x - heartSize, 0.f, 0.f).texture(maxU, minV);
        buffer.vertex(model, x, 0.f, 0.f).texture(minU, minV);
	}

	private void adjustMatrixForLabel(MatrixStack matrixStack, E livingEntity) {
		if (this.hasLabel(livingEntity)) {
			// move up a bit more since we have a label
			matrixStack.translate(0, 10 * Scale.world, 0);
		}
	}

	private void adjustMatrixForScoreBoard(MatrixStack matrixStack, E livingEntity) {
        var distance = this.dispatcher.getSquaredDistanceToCamera(livingEntity);
		if (distance < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
			// move up a bit more to accomodate the scoreboard label
			matrixStack.translate(0, 10 * Scale.world, 0);
		}
	}
}
