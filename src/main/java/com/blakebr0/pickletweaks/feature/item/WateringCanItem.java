package com.blakebr0.pickletweaks.feature.item;

import com.blakebr0.cucumber.helper.NBTHelper;
import com.blakebr0.cucumber.item.BaseItem;
import com.blakebr0.cucumber.util.Utils;
import com.blakebr0.pickletweaks.config.ModConfigs;
import com.blakebr0.pickletweaks.lib.ModTooltips;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;
import java.util.UUID;

public class WateringCanItem extends BaseItem {
	protected final int range;
	protected final double chance;

	public WateringCanItem(int range, double chance) {
		super(p -> p.stacksTo(1));
		this.range = range;
		this.chance = chance;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		var stack = player.getItemInHand(hand);
		if (NBTHelper.getBoolean(stack, "Water"))
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);

		var trace = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (trace.getType() != HitResult.Type.BLOCK)
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);

		var pos = trace.getBlockPos();
		var direction = trace.getDirection();

		if (level.mayInteract(player, pos) && player.mayUseItemAt(pos.relative(direction), direction, stack)) {
			var state = level.getBlockState(pos);

			// TODO: 1.20 - is this as helpful as Material.WATER?
			if (state.getFluidState().is(Fluids.WATER)) {
				NBTHelper.setString(stack, "ID", UUID.randomUUID().toString());
				NBTHelper.setBoolean(stack, "Water", true);

				player.playSound(SoundEvents.BUCKET_FILL, 1.0F, 1.0F);

				return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			}
		}

		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		var player = context.getPlayer();
		if (player == null)
			return InteractionResult.FAIL;

		var level = context.getLevel();
		var pos = context.getClickedPos();
		var direction = context.getClickedFace();

		if (!player.mayUseItemAt(pos.relative(direction), direction, stack))
			return InteractionResult.FAIL;

		if (!NBTHelper.getBoolean(stack, "Water"))
			return InteractionResult.PASS;

		return this.doWater(stack, level, player, pos, direction);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag advanced) {
		if (NBTHelper.getBoolean(stack, "Water")) {
			tooltip.add(ModTooltips.FILLED.build());
		} else {
			tooltip.add(ModTooltips.EMPTY.build());
		}
	}

	protected InteractionResult doWater(ItemStack stack, Level level, Player player, BlockPos pos, Direction direction) {
		if (player == null)
			return InteractionResult.FAIL;

		if (!player.mayUseItemAt(pos.relative(direction), direction, stack))
			return InteractionResult.FAIL;

		if (!NBTHelper.getBoolean(stack, "Water"))
			return InteractionResult.PASS;

		if (!ModConfigs.FAKE_PLAYER_WATERING.get() && player instanceof FakePlayer)
			return InteractionResult.PASS;

		if (!level.isClientSide()) {
			var cooldowns = player.getCooldowns();
			var item = stack.getItem();

			if (!cooldowns.isOnCooldown(item)) {
				cooldowns.addCooldown(item, getThrottleTicks(player));
			} else {
				return InteractionResult.PASS;
			}
		}

		int range = (this.range - 1) / 2;
		var blocks = BlockPos.betweenClosedStream(pos.offset(-range, -range, -range), pos.offset(range, range, range));

		blocks.forEach(aoePos -> {
			var aoeState = level.getBlockState(aoePos);
			if (aoeState.getBlock() instanceof FarmBlock) {
				int moisture = aoeState.getValue(FarmBlock.MOISTURE);
				if (moisture < 7) {
					level.setBlock(aoePos, aoeState.setValue(FarmBlock.MOISTURE, 7), 3);
				}
			}
		});

		for (int x = -range; x <= range; x++) {
			for (int z = -range; z <= range; z++) {
				double d0 = pos.offset(x, 0, z).getX() + level.getRandom().nextFloat();
				double d1 = pos.offset(x, 0, z).getY() + 1.0D;
				double d2 = pos.offset(x, 0, z).getZ() + level.getRandom().nextFloat();

				var state = level.getBlockState(pos);
				if (state.canOcclude() || state.getBlock() instanceof FarmBlock)
					d1 += 0.3D;

				level.addParticle(ParticleTypes.RAIN, d0, d1, d2, 0.0D, 0.0D, 0.0D);
			}
		}

		if (!level.isClientSide()) {
			if (Math.random() <= this.chance) {
				blocks = BlockPos.betweenClosedStream(pos.offset(-range, -range, -range), pos.offset(range, range, range));
				blocks.forEach(aoePos -> {
					var state = level.getBlockState(aoePos);
					var plantBlock = state.getBlock();

					if (plantBlock instanceof BonemealableBlock || plantBlock instanceof IPlantable || plantBlock == Blocks.MYCELIUM || plantBlock == Blocks.CHORUS_FLOWER) {
						state.randomTick((ServerLevel) level, aoePos, Utils.RANDOM);
					}
				});

				return InteractionResult.PASS;
			}
		}

		return InteractionResult.PASS;
	}

	private static int getThrottleTicks(Player player) {
		return player instanceof FakePlayer ? 10 : 5;
	}
}
