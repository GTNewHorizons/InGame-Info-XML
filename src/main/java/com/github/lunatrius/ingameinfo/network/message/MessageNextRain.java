package com.github.lunatrius.ingameinfo.network.message;

import com.github.lunatrius.ingameinfo.tag.Tag;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class MessageNextRain implements IMessage, IMessageHandler<MessageNextRain, IMessage> {

    public int rainTime;

    public MessageNextRain() {
        this.rainTime = 0;
    }

    public MessageNextRain(int rainTime) {
        this.rainTime = rainTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.rainTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.rainTime);
    }

    @Override
    public IMessage onMessage(MessageNextRain message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            Tag.setNextRain(message.rainTime);
        }

        return null;
    }
}
