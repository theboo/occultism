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

package com.github.klikli_dev.occultism.registry;

import com.github.klikli_dev.occultism.Occultism;
import com.github.klikli_dev.occultism.common.tile.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class OccultismTiles {
    //region Fields
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(
            ForgeRegistries.TILE_ENTITIES, Occultism.MODID);

    public static final RegistryObject<TileEntityType<StorageControllerTileEntity>> STORAGE_CONTROLLER = TILES.register(
            "storage_controller", () -> TileEntityType.Builder.of(StorageControllerTileEntity::new,
                    OccultismBlocks.STORAGE_CONTROLLER.get()).build(null));

    public static final RegistryObject<TileEntityType<StableWormholeTileEntity>> STABLE_WORMHOLE = TILES.register(
            "stable_wormhole", () -> TileEntityType.Builder.of(StableWormholeTileEntity::new,
                    OccultismBlocks.STABLE_WORMHOLE.get()).build(null));

    public static final RegistryObject<TileEntityType<SacrificialBowlTileEntity>> SACRIFICIAL_BOWL = TILES.register(
            "sacrificial_bowl", () -> TileEntityType.Builder.of(SacrificialBowlTileEntity::new,
                    OccultismBlocks.SACRIFICIAL_BOWL.get()).build(null));

    public static final RegistryObject<TileEntityType<GoldenSacrificialBowlTileEntity>> GOLDEN_SACRIFICIAL_BOWL =
            TILES.register(
                    "golden_sacrificial_bowl", () -> TileEntityType.Builder.of(GoldenSacrificialBowlTileEntity::new,
                            OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL.get()).build(null));

    public static final RegistryObject<TileEntityType<DimensionalMineshaftTileEntity>> DIMENSIONAL_MINESHAFT =
            TILES.register(
                    "dimensional_mineshaft", () -> TileEntityType.Builder.of(DimensionalMineshaftTileEntity::new,
                            OccultismBlocks.DIMENSIONAL_MINESHAFT.get()).build(null));

    //endregion Fields

}
