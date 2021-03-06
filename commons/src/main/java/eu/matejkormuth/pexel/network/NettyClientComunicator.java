// @formatter:off
/*
 * Pexel Project - Minecraft minigame server platform. 
 * Copyright (C) 2014 Matej Kormuth <http://www.matejkormuth.eu>
 * 
 * This file is part of Pexel.
 * 
 * Pexel is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Pexel is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
// @formatter:on
package eu.matejkormuth.pexel.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import eu.matejkormuth.pexel.commons.Logger;

public class NettyClientComunicator extends MessageComunicator {
    private Bootstrap                     b;
    private Channel                       channelToMaster;
    private final ServerInfo              master;
    private final Logger                  log;
    
    // Queue.
    protected BlockingQueue<NettyMessage> payloads = new PriorityBlockingQueue<NettyMessage>(
                                                           100,
                                                           new NettyMessageComparator());
    
    public NettyClientComunicator(final PayloadHandler handler, final int port,
            final String host, final String authKey, final SlaveServer server) {
        super(handler);
        
        this.master = server.getMasterServerInfo();
        this.log = server.getLogger().getChild("Netty");
        
        try {
            this.init(port, host, server.getName(), authKey);
        } catch (SSLException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void init(final int port, final String host, final String thisSlaveName,
            final String authKey) throws InterruptedException, SSLException {
        this.log.info("Setting up SSL...");
        // Configure SSL.
        final SslContext sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            this.b = new Bootstrap();
            this.b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new NettyClientComunicatorInitializer(sslCtx, port, host));
            
            group.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    NettyClientComunicator.this.sendQueue();
                }
            }, 0L, 10L, TimeUnit.MILLISECONDS);
            
            this.log.info("Connecting to master...");
            // Start the connection attempt.
            this.channelToMaster = this.b.connect(host, port).sync().channel();
            
            // Log in.
            this.channelToMaster.writeAndFlush(new NettyMessage(
                    NettyRegisterMesssage.create(authKey, thisSlaveName)));
            
        }
        finally {
            // The connection is closed automatically on shutdown.
            this.log.info("Shutting down..");
            group.shutdownGracefully();
        }
    }
    
    protected void sendQueue() {
        while (!this.payloads.isEmpty()) {
            this.channelToMaster.writeAndFlush(this.payloads.poll());
        }
    }
    
    @Override
    public void send(final ServerInfo target, final byte[] payload, final int priority) {
        if (target != this.master) {
            throw new RuntimeException("Sorry, slave can only send payloads to master.");
        }
        else {
            
            this.payloads.offer(new NettyMessage(payload, priority));
        }
    }
    
    @Override
    public void send(final ServerInfo target, final byte[] payload) {
        this.send(target, payload, 0);
    }
    
    @Override
    public void stop() {
        this.channelToMaster.close();
    }
    
    class NettyClientComunicatorInitializer extends ChannelInitializer<SocketChannel> {
        private final SslContext sslCtx;
        private final String     host;
        private final int        port;
        
        public NettyClientComunicatorInitializer(final SslContext sslCtx,
                final int port, final String host) {
            this.sslCtx = sslCtx;
            this.host = host;
            this.port = port;
        }
        
        @Override
        public void initChannel(final SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(this.sslCtx.newHandler(ch.alloc(), this.host, this.port));
            
            // On top of the SSL handler, add the text line codec.
            pipeline.addLast(new NettyMessageDecoder());
            pipeline.addLast(new NettyMessageEncoder());
            
            // and then business logic.
            pipeline.addLast(new NettyClientCommunicatorHandler());
        }
    }
    
    class NettyClientCommunicatorHandler extends
            SimpleChannelInboundHandler<NettyMessage> {
        @Override
        public void channelRead0(final ChannelHandlerContext ctx, final NettyMessage msg)
                throws Exception {
            NettyClientComunicator.this.onReceive(NettyClientComunicator.this.master,
                    msg.payload);
        }
        
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx,
                final Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
    
    @Override
    public void start() {
        // TODO Auto-generated method stub
        
    }
    
}
