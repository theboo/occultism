
/*
 * MIT License
 *
 * Copyright 2020 klikli-dev, McJty
 *
 * Based on https://github.com/McJty/YouTubeModding14/blob/master/src/main/java/com/mcjty/mytutorial/datagen/BaseLootTableProvider.java
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

package com.github.klikli_dev.occultism.datagen;

import com.github.klikli_dev.occultism.Occultism;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraft.world.storage.loot.functions.CopyName;
import net.minecraft.world.storage.loot.functions.CopyNbt;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseLootTableProvider extends LootTableProvider {

    //region Fields
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // Filled by subclasses
    protected final Map<Block, LootTable.Builder> lootTables = new HashMap<>();

    private final DataGenerator generator;
    protected final BlockLootTables blockLootTables;
    //endregion Fields

    //region Initialization
    public BaseLootTableProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
        this.generator = dataGeneratorIn;
        this.blockLootTables = new BlockLootTables();
    }
    //endregion Initialization

    //region Overrides
    @Override
    // Entry point
    public void act(DirectoryCache cache) {
        addTables();
        Map<ResourceLocation, LootTable> tables = new HashMap<>();
        for (Map.Entry<Block, LootTable.Builder> entry : lootTables.entrySet()) {
            tables.put(entry.getKey().getLootTable(),
                    entry.getValue().setParameterSet(LootParameterSets.BLOCK).build());
        }
        writeTables(cache, tables);
    }

    @Override
    public String getName() {
        return "Occultism LootTables";
    }
    //endregion Overrides

    //region Methods
    // Subclasses can override this to fill the 'lootTables' map.
    protected abstract void addTables();

    // Actually write out the tables in the output folder
    private void writeTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                IDataProvider.save(GSON, cache, LootTableManager.toJson(lootTable), path);
            } catch (IOException e) {
                Occultism.LOGGER.error("Couldn't write loot table {}", path, e);
            }
        });
    }
    //endregion Methods
}