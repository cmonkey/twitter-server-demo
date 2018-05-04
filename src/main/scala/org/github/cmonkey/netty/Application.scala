package org.github.cmonkey.netty

import java.awt.Dimension
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

import com.github.sarxos.webcam.Webcam
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.channel.group.{ChannelGroup, DefaultChannelGroup}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.websocketx.{BinaryWebSocketFrame, WebSocketFrame, WebSocketServerProtocolHandler}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.concurrent.GlobalEventExecutor
import javax.imageio.ImageIO

import scala.concurrent.{ExecutionContext, Future}

object Application {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  val WEBSOCKET_PATH = "/websocket"

  val FPS = 10

  def main(args: Array[String]) {
    val allChannels = new DefaultChannelGroup("all", GlobalEventExecutor.INSTANCE)

    startCapture(allChannels)

    startServer(allChannels)
  }

  def startServer(group: ChannelGroup) = {
    val bossGroup = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup

    try{
      new ServerBootstrap()
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new ChannelInitializer[SocketChannel]{
        override def initChannel(ch: SocketChannel): Unit = {
          ch.pipeline
          .addLast(new HttpServerCodec)
          .addLast(new HttpObjectAggregator(65536))
          .addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true))
          .addLast(new WebSocketFrameHandler(group))
        }
      }).bind(8080).sync
      .channel
      .closeFuture.sync
    }finally{
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }

  def startCapture(group: ChannelGroup) =  {
    val webcam: Webcam = Webcam.getDefault

    webcam.setViewSize(new Dimension(640, 480))
    webcam.open

    Future{
      while(true){
        val outputStream = new ByteArrayOutputStream
        ImageIO.write(webcam.getImage, "jpg", outputStream)
        group.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(outputStream.toByteArray)))
        Thread.sleep(1000 / FPS)
      }
    }
  }

}

class WebSocketFrameHandler(group: ChannelGroup)  extends SimpleChannelInboundHandler[WebSocketFrame]{
  override def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any): Unit = {
    if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
      group.add(ctx.channel())
    }else{
      super.userEventTriggered(ctx, evt)
    }
  }
  override def channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame): Unit = {
  }
}
