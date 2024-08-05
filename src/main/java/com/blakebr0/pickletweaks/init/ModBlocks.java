package com.blakebr0.pickletweaks.init;

import com.blakebr0.cucumber.item.BaseBlockItem;
import com.blakebr0.pickletweaks.PickleTweaks;
import com.blakebr0.pickletweaks.feature.block.ColoredCobblestoneBlock;
import com.blakebr0.pickletweaks.feature.block.SmoothGlowstoneBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(Registries.BLOCK, PickleTweaks.MOD_ID);
	public static final Map<String, Supplier<BlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();

	public static final DeferredHolder<Block, Block> WHITE_COBBLESTONE = register("white_cobblestone", () -> new ColoredCobblestoneBlock(16383998));
	public static final DeferredHolder<Block, Block> ORANGE_COBBLESTONE = register("orange_cobblestone", () -> new ColoredCobblestoneBlock(16351261));
	public static final DeferredHolder<Block, Block> MAGENTA_COBBLESTONE = register("magenta_cobblestone", () -> new ColoredCobblestoneBlock(13061821));
	public static final DeferredHolder<Block, Block> LIGHT_BLUE_COBBLESTONE = register("light_blue_cobblestone", () -> new ColoredCobblestoneBlock(3847130));
	public static final DeferredHolder<Block, Block> YELLOW_COBBLESTONE = register("yellow_cobblestone", () -> new ColoredCobblestoneBlock(16701501));
	public static final DeferredHolder<Block, Block> LIME_COBBLESTONE = register("lime_cobblestone", () -> new ColoredCobblestoneBlock(8439583));
	public static final DeferredHolder<Block, Block> PINK_COBBLESTONE = register("pink_cobblestone", () -> new ColoredCobblestoneBlock(15961002));
	public static final DeferredHolder<Block, Block> GRAY_COBBLESTONE = register("gray_cobblestone", () -> new ColoredCobblestoneBlock(4673362));
	public static final DeferredHolder<Block, Block> LIGHT_GRAY_COBBLESTONE = register("light_gray_cobblestone", () -> new ColoredCobblestoneBlock(10329495));
	public static final DeferredHolder<Block, Block> CYAN_COBBLESTONE = register("cyan_cobblestone", () -> new ColoredCobblestoneBlock(1481884));
	public static final DeferredHolder<Block, Block> PURPLE_COBBLESTONE = register("purple_cobblestone", () -> new ColoredCobblestoneBlock(8991416));
	public static final DeferredHolder<Block, Block> BLUE_COBBLESTONE = register("blue_cobblestone", () -> new ColoredCobblestoneBlock(3949738));
	public static final DeferredHolder<Block, Block> BROWN_COBBLESTONE = register("brown_cobblestone", () -> new ColoredCobblestoneBlock(8606770));
	public static final DeferredHolder<Block, Block> GREEN_COBBLESTONE = register("green_cobblestone", () -> new ColoredCobblestoneBlock(6192150));
	public static final DeferredHolder<Block, Block> RED_COBBLESTONE = register("red_cobblestone", () -> new ColoredCobblestoneBlock(11546150));
	public static final DeferredHolder<Block, Block> BLACK_COBBLESTONE = register("black_cobblestone", () -> new ColoredCobblestoneBlock(1908001));

	public static final DeferredHolder<Block, Block> SMOOTH_GLOWSTONE = register("smooth_glowstone", SmoothGlowstoneBlock::new);

	private static DeferredHolder<Block, Block> register(String name, Supplier<Block> block) {
		return register(name, block, b -> () -> new BaseBlockItem(b.get()));
	}

	private static DeferredHolder<Block, Block> register(String name, Supplier<Block> block, Function<DeferredHolder<Block, ? extends Block>, Supplier<? extends BlockItem>> item) {
		var reg = REGISTRY.register(name, block);
		BLOCK_ITEMS.put(name, () -> item.apply(reg).get());
		return reg;
	}
}
