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

package com.github.klikli_dev.occultism.network;

import com.github.klikli_dev.occultism.common.container.storage.SatchelContainer;
import com.github.klikli_dev.occultism.common.item.storage.SatchelItem;
import com.github.klikli_dev.occultism.util.CuriosUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

public class MessageOpenSatchel extends MessageBase {

    //region Initialization

    public MessageOpenSatchel(PacketBuffer buf) {
        this.decode(buf);
    }

    public MessageOpenSatchel() {

    }
    //endregion Initialization

    //region Overrides

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayerEntity player,
                                 NetworkEvent.Context context) {

        int selectedSlot = -1;
        //first attempt to get backpack from curios slot
        ItemStack backpackStack = CuriosUtil.getBackpack(player);

        //if not found, try to get from player inventory
        if (!(backpackStack.getItem() instanceof SatchelItem)) {
            selectedSlot = CuriosUtil.getFirstBackpackSlot(player);
            backpackStack = selectedSlot > 0 ? player.inventory.getItem(selectedSlot) : ItemStack.EMPTY;
        }
        //now, if we have a satchel, proceed
        if (backpackStack.getItem() instanceof SatchelItem) {
            ItemStack finalBackpackStack = backpackStack;
            int finalSelectedSlot = selectedSlot;
            NetworkHooks.openGui(player,
                    new SimpleNamedContainerProvider((id, playerInventory, unused) -> {
                        return new SatchelContainer(id, playerInventory,
                                ((SatchelItem) finalBackpackStack.getItem()).getInventory(player, finalBackpackStack),
                                finalSelectedSlot);
                    }, backpackStack.getHoverName()), buffer -> {
                        buffer.writeVarInt(finalSelectedSlot);
                    });
        }
    }

    @Override
    public void encode(PacketBuffer buf) {

    }

    @Override
    public void decode(PacketBuffer buf) {

    }
    //endregion Overrides
}
