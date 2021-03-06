/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>, Emi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.queerbric.inspecio.tooltip;

import io.github.queerbric.inspecio.Inspecio;
import io.github.queerbric.inspecio.SignTooltipMode;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SignItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.SignType;
import net.minecraft.util.math.Matrix4f;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class SignTooltipComponent implements ConvertibleTooltipData, TooltipComponent {
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final SignTooltipMode tooltipMode = Inspecio.get().getConfig().getSignTooltipMode();
	private final SignType type;
	private final Text[] text;
	private final DyeColor color;
	private SignBlockEntityRenderer.SignModel model;

	public SignTooltipComponent(SignType type, Text[] text, DyeColor color) {
		this.type = type;
		this.text = text;
		this.color = color;
		this.model = SignBlockEntityRenderer.method_32157(this.client.getEntityModelLoader(), this.type);
	}

	public static Optional<TooltipData> fromItemStack(ItemStack stack) {
		if (!Inspecio.get().getConfig().getSignTooltipMode().isEnabled())
			return Optional.empty();

		if (stack.getItem() instanceof SignItem) {
			Block block = ((SignItem) stack.getItem()).getBlock();
			CompoundTag tag = stack.getSubTag("BlockEntityTag");
			if (tag != null) return Optional.of(fromTag(SignBlockEntityRenderer.method_32155(block), tag));
		}
		return Optional.empty();
	}

	public static SignTooltipComponent fromTag(SignType type, CompoundTag tag) {
		DyeColor color = DyeColor.byName(tag.getString("Color"), DyeColor.BLACK);

		Text[] lines = new Text[4];
		for (int i = 0; i < 4; ++i) {
			String serialized = tag.getString("Text" + (i + 1));
			Text text = Text.Serializer.fromJson(serialized.isEmpty() ? "\"\"" : serialized);
			lines[i] = text;
		}

		return new SignTooltipComponent(type, lines, color);
	}

	@Override
	public TooltipComponent getComponent() {
		return this;
	}

	@Override
	public int getHeight() {
		if (this.tooltipMode == SignTooltipMode.FANCY)
			return 48;
		return this.text.length * 10;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		if (this.tooltipMode == SignTooltipMode.FANCY)
			return 94;
		return Arrays.stream(this.text).map(textRenderer::getWidth).max(Comparator.naturalOrder()).orElse(94);
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {
		if (this.tooltipMode != SignTooltipMode.FAST)
			return;

		for (Text text : this.text) {
			textRenderer.draw(text, x, y, this.color.getSignColor(), true, matrix4f, immediate, false, 0, 15728880);
			y += 10;
		}
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
		if (this.tooltipMode != SignTooltipMode.FANCY)
			return;

		DiffuseLighting.disableGuiDepthLighting();
		matrices.push();
		matrices.translate(x + 2, y, z);

		matrices.push();
		matrices.translate(45, 56, 0);
		matrices.scale(65, 65, -65);
		VertexConsumerProvider.Immediate immediate = this.client.getBufferBuilders().getEntityVertexConsumers();
		SpriteIdentifier spriteIdentifier = TexturedRenderLayers.method_33082(this.type);
		VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(immediate, this.model::getLayer);
		this.model.foot.visible = false;
		this.model.field_27756.visible = true;
		this.model.field_27756.render(matrices, vertexConsumer, 15728880, OverlayTexture.DEFAULT_UV);
		immediate.draw();
		matrices.pop();

		matrices.translate(0, 4, 10);

		for (int i = 0; i < this.text.length; i++) {
			Text text = this.text[i];
			textRenderer.draw(matrices, text, 45 - textRenderer.getWidth(text) / 2.f, i * 10, this.color.getSignColor());
			y += textRenderer.fontHeight + 2;
		}
		matrices.pop();

		DiffuseLighting.enableGuiDepthLighting();
	}
}
