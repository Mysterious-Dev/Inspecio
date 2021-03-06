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

import com.mojang.datafixers.util.Pair;
import io.github.queerbric.inspecio.Inspecio;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.DyeColor;

import java.util.List;
import java.util.Optional;

public class BannerTooltipComponent implements ConvertibleTooltipData, TooltipComponent {
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final BannerPattern pattern;
	private final ModelPart bannerField;

	private BannerTooltipComponent(BannerPattern pattern) {
		this.pattern = pattern;
		this.bannerField = this.client.getEntityModelLoader().getModelPart(EntityModelLayers.BANNER).getChild("flag");
	}

	public static Optional<TooltipData> of(BannerPattern pattern) {
		if (!Inspecio.get().getConfig().hasBannerPattern())
			return Optional.empty();
		return Optional.of(new BannerTooltipComponent(pattern));
	}

	@Override
	public TooltipComponent getComponent() {
		return this;
	}

	@Override
	public int getHeight() {
		return 32;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 16;
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
		DiffuseLighting.disableGuiDepthLighting();
		matrices.push();
		matrices.translate(x + 8, y + 8, z);
		ItemStack itemStack = new ItemStack(Items.GRAY_BANNER);
		CompoundTag compoundTag = itemStack.getOrCreateSubTag("BlockEntityTag");
		ListTag listTag = (new BannerPattern.Patterns()).add(BannerPattern.BASE, DyeColor.GRAY).add(this.pattern, DyeColor.WHITE).toTag();
		compoundTag.put("Patterns", listTag);
		matrices.push();
		matrices.translate(0.5, 16, 0);
		matrices.scale(6, -6, 1);
		matrices.scale(2, -2, -2);
		VertexConsumerProvider.Immediate immediate = this.client.getBufferBuilders().getEntityVertexConsumers();
		this.bannerField.pitch = 0.f;
		this.bannerField.pivotY = -32.f;
		List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.method_24280(DyeColor.GRAY, BannerBlockEntity.getPatternListTag(itemStack));
		BannerBlockEntityRenderer.method_29999(matrices, immediate, 15728880, OverlayTexture.DEFAULT_UV, this.bannerField, ModelLoader.BANNER_BASE, true, list);
		matrices.pop();
		immediate.draw();
		matrices.pop();
		DiffuseLighting.enableGuiDepthLighting();
	}
}
