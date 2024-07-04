package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;

public class DohRecordEncoder extends ChannelOutboundHandlerAdapter {
    private final DohQueryEncoder dohQueryEncoder = new DohQueryEncoder();

    private final DohProviders.DohProvider dohProvider;

    public DohRecordEncoder(DohProviders.DohProvider dohProvider) {
        this.dohProvider = dohProvider;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf content = ctx.alloc().heapBuffer();
        dohQueryEncoder.encode(ctx, (DnsQuery) msg, content);

        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, dohProvider.uri(),
                content);

        request.headers().set(HttpHeaderNames.HOST, dohProvider.host());
        request.headers().set(HttpHeaderNames.ACCEPT, "application/dns-message");
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/dns-message");
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        super.write(ctx, request, promise);
    }
}
