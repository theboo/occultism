/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.klikli_dev.occultism.common.tile;

import com.github.klikli_dev.occultism.common.container.DimensionalMineshaftContainer;
import com.github.klikli_dev.occultism.common.misc.WeightedIngredient;
import com.github.klikli_dev.occultism.crafting.recipe.MinerRecipe;
import com.github.klikli_dev.occultism.exceptions.ItemHandlerMissingException;
import com.github.klikli_dev.occultism.registry.OccultismRecipes;
import com.github.klikli_dev.occultism.registry.OccultismTiles;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DimensionalMineshaftTileEntity extends NetworkedTileEntity implements ITickableTileEntity, INamedContainerProvider {

    //region Fields
    public static final String MAX_MINING_TIME_TAG = "maxMiningTime";
    public static final int DEFAULT_MAX_MINING_TIME = 400;
    public static int DEFAULT_ROLLS_PER_OPERATION = 1;
    public static String ROLLS_PER_OPERATION_TAG = "rollsPerOperation";
    public LazyOptional<ItemStackHandler> inputHandler = LazyOptional.of(() -> new ItemStackHandler(1) {
        //region Overrides
        @Override
        protected void onContentsChanged(int slot) {
            DimensionalMineshaftTileEntity.this.setChanged();
        }
        //endregion Overrides
    });
    public LazyOptional<ItemStackHandler> outputHandler = LazyOptional.of(() -> new ItemStackHandler(9) {
        //region Overrides
        @Override
        protected void onContentsChanged(int slot) {
            DimensionalMineshaftTileEntity.this.setChanged();
        }
        //endregion Overrides
    });
    public LazyOptional<CombinedInvWrapper> combinedHandler =
            LazyOptional
                    .of(() -> new CombinedInvWrapper(this.inputHandler.orElseThrow(ItemHandlerMissingException::new),
                            this.outputHandler.orElseThrow(ItemHandlerMissingException::new)));
    public int miningTime;
    public int maxMiningTime = 0;
    public int rollsPerOperation = 0;
    protected Item currentInputType;
    protected List<WeightedIngredient> possibleResults;

    //endregion Fields
    //region Initialization
    public DimensionalMineshaftTileEntity() {
        super(OccultismTiles.DIMENSIONAL_MINESHAFT.get());
    }
    //endregion Initialization

    //region Static Methods
    public static void forceInitStackNBT(ItemStack stack, ServerWorld world) {
        stack.getItem().onCraftedBy(stack, world, FakePlayerFactory.getMinecraft(world));
    }

    public static int getMaxMiningTime(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return 0;
        int time = tag.getInt(MAX_MINING_TIME_TAG);
        return time <= 0 ? DEFAULT_MAX_MINING_TIME : time;
    }

    public static int getRollsPerOperation(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return 0;
        int rolls = tag.getInt(ROLLS_PER_OPERATION_TAG);
        return rolls <= 0 ? DEFAULT_ROLLS_PER_OPERATION : rolls;
    }

    //region Overrides
    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(this.getType().getRegistryName().getPath());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (direction == null) {
                //null is full access for machines or similar.
                return this.combinedHandler.cast();
            } else if (direction == Direction.UP) {
                return this.inputHandler.cast();
            } else {
                return this.outputHandler.cast();
            }
        }
        return super.getCapability(cap, direction);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.inputHandler.ifPresent((handler) -> handler.deserializeNBT(compound.getCompound("inputHandler")));
        this.outputHandler.ifPresent((handler) -> handler.deserializeNBT(compound.getCompound("outputHandler")));
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        this.inputHandler.ifPresent(handler -> compound.put("inputHandler", handler.serializeNBT()));
        this.outputHandler.ifPresent(handler -> compound.put("outputHandler", handler.serializeNBT()));
        return super.save(compound);
    }

    @Override
    public void readNetwork(CompoundNBT compound) {
        super.readNetwork(compound);
        this.miningTime = compound.getInt("miningTime");
        this.maxMiningTime = compound.getInt("maxMiningTime");
    }

    @Override
    public CompoundNBT writeNetwork(CompoundNBT compound) {
        compound.putInt("miningTime", this.miningTime);
        compound.putInt("maxMiningTime", this.maxMiningTime);
        return super.writeNetwork(compound);
    }
    //endregion Overrides

    @Override
    public void setRemoved() {
        this.inputHandler.invalidate();
        this.outputHandler.invalidate();
        super.setRemoved();
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide) {
            IItemHandler inputHandler = this.inputHandler.orElseThrow(ItemHandlerMissingException::new);
            ItemStack input = inputHandler.getStackInSlot(0);

            boolean dirty = false;
            if (this.miningTime > 0) {
                this.miningTime--;

                if (this.miningTime == 0 && !this.level.isClientSide) {
                    this.mine();
                }

                if (input.getItem() != this.currentInputType) {
                    //If the item was removed manually or consumed, set mining time to 0, which prevents further processing
                    //and sets up for starting the next operation in the next tick
                    this.miningTime = 0;

                    //if the item was used up or switched, we also delete our result cache
                    this.possibleResults = null;
                }
                if (this.miningTime % 10 == 0)
                    dirty = true;
            } else if (!input.isEmpty()) {
                //if we're done with the last mining job, and we have valid input, start the next one.
                this.currentInputType = input.getItem();
                //ensure nbt is initialized, fixes issues with spawned miner spirits
                forceInitStackNBT(input, (ServerWorld) this.level);
                this.maxMiningTime = getMaxMiningTime(input);
                this.rollsPerOperation = getRollsPerOperation(input);
                this.miningTime = this.maxMiningTime;
                dirty = true;
            }

            if (dirty) {
                this.markNetworkDirty();
            }
        } else {
            if (this.miningTime > 0 && this.level.getGameTime() % 10 == 0) {
                this.level.addParticle(ParticleTypes.PORTAL, this.worldPosition.getX() + 0.5f,
                        this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5f, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
        return new DimensionalMineshaftContainer(id, playerInventory, this);
    }
    //endregion Static Methods

    //region Methods
    public void mine() {
        ItemStackHandler inputHandler = this.inputHandler.orElseThrow(ItemHandlerMissingException::new);
        ItemStackHandler outputHandler = this.outputHandler.orElseThrow(ItemHandlerMissingException::new);

        if (this.possibleResults == null) {
            List<MinerRecipe> recipes = this.level.getRecipeManager()
                    .getRecipesFor(OccultismRecipes.MINER_TYPE.get(),
                            new RecipeWrapper(inputHandler), this.level);
            if (recipes == null || recipes.size() == 0) {
                this.possibleResults = new ArrayList<>();
            } else {
                this.possibleResults = recipes.stream().map(r -> r.getWeightedOutput()).collect(Collectors.toList());
            }
        }

        if (this.possibleResults.size() == 0)
            return;

        for (int i = 0; i < this.rollsPerOperation; i++) {
            WeightedIngredient result = WeightedRandom.getRandomItem(this.level.random, this.possibleResults);
            //Important: copy the result, don't use it raw!
            ItemHandlerHelper.insertItemStacked(outputHandler, result.getStack().copy(), false);
            //If there is no space, we simply continue. The otherworld miner spirit keeps working,
            // but the miner tile entity simply discards the results
        }

        //damage and eventually consume item.
        ItemStack input = inputHandler.getStackInSlot(0);
        if (input.hurt(1, this.level.random, null)) {
            input.shrink(1);
            input.setDamageValue(0);
        }
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        this.inputHandler.invalidate();
        this.outputHandler.invalidate();
        this.combinedHandler.invalidate();
    }

    //endregion Methods
}
