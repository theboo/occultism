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

package com.github.klikli_dev.occultism.common.block.storage;

import com.github.klikli_dev.occultism.common.tile.StableWormholeTileEntity;
import com.github.klikli_dev.occultism.registry.OccultismTiles;
import com.github.klikli_dev.occultism.util.TileEntityUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

public class StableWormholeBlock extends Block {

    //region Fields
    public static final Property<Boolean> LINKED = BooleanProperty.create("linked");
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(
            ImmutableMap.<Direction, VoxelShape>builder()
                    .put(Direction.EAST, Stream.of(
                            Block.box(0, 4, 4, 1, 12, 12),
                            Block.box(0, 4, 1, 1, 12, 4),
                            Block.box(0, 4, 12, 1, 12, 15),
                            Block.box(0, 6, 0, 1, 10, 1),
                            Block.box(0, 6, 15, 1, 10, 16),
                            Block.box(0, 2, 12, 1, 4, 13),
                            Block.box(0, 12, 12, 1, 14, 13),
                            Block.box(0, 2, 3, 1, 4, 4),
                            Block.box(0, 3, 2, 1, 4, 3),
                            Block.box(0, 12, 2, 1, 13, 3),
                            Block.box(0, 3, 13, 1, 4, 14),
                            Block.box(0, 12, 13, 1, 13, 14),
                            Block.box(0, 12, 3, 1, 14, 4),
                            Block.box(0, 1, 4, 1, 4, 12),
                            Block.box(0, 12, 4, 1, 15, 12),
                            Block.box(0, 0, 6, 1, 1, 10),
                            Block.box(0, 15, 6, 1, 16, 10)
                    ).reduce((v1, v2) -> {
                        return VoxelShapes.join(v1, v2, IBooleanFunction.OR);
                    }).get())
                    .put(Direction.WEST, Stream.of(
                            Block.box(15, 4, 4, 16, 12, 12),
                            Block.box(15, 4, 1, 16, 12, 4),
                            Block.box(15, 4, 12, 16, 12, 15),
                            Block.box(15, 6, 0, 16, 10, 1),
                            Block.box(15, 6, 15, 16, 10, 16),
                            Block.box(15, 2, 12, 16, 4, 13),
                            Block.box(15, 12, 12, 16, 14, 13),
                            Block.box(15, 2, 3, 16, 4, 4),
                            Block.box(15, 3, 2, 16, 4, 3),
                            Block.box(15, 12, 2, 16, 13, 3),
                            Block.box(15, 3, 13, 16, 4, 14),
                            Block.box(15, 12, 13, 16, 13, 14),
                            Block.box(15, 12, 3, 16, 14, 4),
                            Block.box(15, 1, 4, 16, 4, 12),
                            Block.box(15, 12, 4, 16, 15, 12),
                            Block.box(15, 0, 6, 16, 1, 10),
                            Block.box(15, 15, 6, 16, 16, 10)
                    ).reduce((v1, v2) -> {
                        return VoxelShapes.join(v1, v2, IBooleanFunction.OR);
                    }).get())
                    .put(Direction.NORTH, Stream.of(
                            Block.box(4, 4, 15, 12, 12, 16),
                            Block.box(1, 4, 15, 4, 12, 16),
                            Block.box(12, 4, 15, 15, 12, 16),
                            Block.box(0, 6, 15, 1, 10, 16),
                            Block.box(15, 6, 15, 16, 10, 16),
                            Block.box(12, 2, 15, 13, 4, 16),
                            Block.box(12, 12, 15, 13, 14, 16),
                            Block.box(3, 2, 15, 4, 4, 16),
                            Block.box(2, 3, 15, 3, 4, 16),
                            Block.box(2, 12, 15, 3, 13, 16),
                            Block.box(13, 3, 15, 14, 4, 16),
                            Block.box(13, 12, 15, 14, 13, 16),
                            Block.box(3, 12, 15, 4, 14, 16),
                            Block.box(4, 1, 15, 12, 4, 16),
                            Block.box(4, 12, 15, 12, 15, 16),
                            Block.box(6, 0, 15, 10, 1, 16),
                            Block.box(6, 15, 15, 10, 16, 16)
                    ).reduce((v1, v2) -> {
                        return VoxelShapes.join(v1, v2, IBooleanFunction.OR);
                    }).get())
                    .put(Direction.SOUTH, Stream.of(
                            Block.box(4, 4, 0, 12, 12, 1),
                            Block.box(1, 4, 0, 4, 12, 1),
                            Block.box(12, 4, 0, 15, 12, 1),
                            Block.box(0, 6, 0, 1, 10, 1),
                            Block.box(15, 6, 0, 16, 10, 1),
                            Block.box(12, 2, 0, 13, 4, 1),
                            Block.box(12, 12, 0, 13, 14, 1),
                            Block.box(3, 2, 0, 4, 4, 1),
                            Block.box(2, 3, 0, 3, 4, 1),
                            Block.box(2, 12, 0, 3, 13, 1),
                            Block.box(13, 3, 0, 14, 4, 1),
                            Block.box(13, 12, 0, 14, 13, 1),
                            Block.box(3, 12, 0, 4, 14, 1),
                            Block.box(4, 1, 0, 12, 4, 1),
                            Block.box(4, 12, 0, 12, 15, 1),
                            Block.box(6, 0, 0, 10, 1, 1),
                            Block.box(6, 15, 0, 10, 16, 1)
                    ).reduce((v1, v2) -> {
                        return VoxelShapes.join(v1, v2, IBooleanFunction.OR);
                    }).get())
                    .put(Direction.UP, Stream.of(
                            Block.box(4, 0, 4, 12, 1, 12),
                            Block.box(1, 0, 4, 4, 1, 12),
                            Block.box(12, 0, 4, 15, 1, 12),
                            Block.box(0, 0, 6, 1, 1, 10),
                            Block.box(15, 0, 6, 16, 1, 10),
                            Block.box(12, 0, 12, 13, 1, 14),
                            Block.box(12, 0, 2, 13, 1, 4),
                            Block.box(3, 0, 12, 4, 1, 14),
                            Block.box(2, 0, 12, 3, 1, 13),
                            Block.box(2, 0, 3, 3, 1, 4),
                            Block.box(13, 0, 12, 14, 1, 13),
                            Block.box(13, 0, 3, 14, 1, 4),
                            Block.box(3, 0, 2, 4, 1, 4),
                            Block.box(4, 0, 12, 12, 1, 15),
                            Block.box(4, 0, 1, 12, 1, 4),
                            Block.box(6, 0, 15, 10, 1, 16),
                            Block.box(6, 0, 0, 10, 1, 1)
                    ).reduce((v1, v2) -> {
                        return VoxelShapes.join(v1, v2, IBooleanFunction.OR);
                    }).get())
                    .put(Direction.DOWN, Stream.of(
                            Block.box(4, 15, 4, 12, 16, 12),
                            Block.box(1, 15, 4, 4, 16, 12),
                            Block.box(12, 15, 4, 15, 16, 12),
                            Block.box(0, 15, 6, 1, 16, 10),
                            Block.box(15, 15, 6, 16, 16, 10),
                            Block.box(12, 15, 12, 13, 16, 14),
                            Block.box(12, 15, 2, 13, 16, 4),
                            Block.box(3, 15, 12, 4, 16, 14),
                            Block.box(2, 15, 12, 3, 16, 13),
                            Block.box(2, 15, 3, 3, 16, 4),
                            Block.box(13, 15, 12, 14, 16, 13),
                            Block.box(13, 15, 3, 14, 16, 4),
                            Block.box(3, 15, 2, 4, 16, 4),
                            Block.box(4, 15, 12, 12, 16, 15),
                            Block.box(4, 15, 1, 12, 16, 4),
                            Block.box(6, 15, 15, 10, 16, 16),
                            Block.box(6, 15, 0, 10, 16, 1)
                    ).reduce((v1, v2) -> {
                        return VoxelShapes.join(v1, v2, IBooleanFunction.OR);
                    }).get()).build());

    //endregion Fields
    //region Initialization
    public StableWormholeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LINKED, false));
    }
    //endregion Initialization

    //region Overrides
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.getValue(BlockStateProperties.FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
                                        ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntityUtil.onBlockChangeDropWithNbt(this, state, worldIn, pos, newState);
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                Hand handIn, BlockRayTraceResult rayTraceResult) {
        if (!world.isClientSide) {
            TileEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof StableWormholeTileEntity) {
                StableWormholeTileEntity wormhole = (StableWormholeTileEntity) tileEntity;
                if (wormhole.getLinkedStorageController() != null)
                    NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, pos);
                else {
                    world.setBlock(pos, state.setValue(LINKED, false), 2);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace());
        if (context.getItemInHand().getOrCreateTag().getCompound("BlockEntityTag")
                .contains("linkedStorageControllerPosition")) {
            state = state.setValue(LINKED, true);
        }
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return TileEntityUtil.getItemWithNbt(this, worldIn, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LINKED, BlockStateProperties.FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return OccultismTiles.STABLE_WORMHOLE.get().create();
    }

    //endregion Overrides
}


