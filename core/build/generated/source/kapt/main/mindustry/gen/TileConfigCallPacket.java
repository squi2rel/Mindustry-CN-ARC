package mindustry.gen;

import arc.util.io.Reads;
import arc.util.io.Writes;
import java.lang.Object;
import java.lang.Override;

import mindustry.arcModule.ARCVars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class TileConfigCallPacket extends Packet {
  private byte[] DATA = NODATA;

  public Player player;

  public Building build;

  public Object value;

  @Override
  public void write(Writes WRITE) {
    if(mindustry.Vars.net.server() || ARCVars.replayController.writing) {
      mindustry.io.TypeIO.writeEntity(WRITE, player);
    }
    mindustry.io.TypeIO.writeBuilding(WRITE, build);
    mindustry.io.TypeIO.writeObject(WRITE, value);
  }

  @Override
  public void read(Reads READ, int LENGTH) {
    DATA = READ.b(LENGTH);
  }

  @Override
  public void handled() {
    BAIS.setBytes(DATA);
    if(mindustry.Vars.net.client()) {
      player = mindustry.io.TypeIO.readEntity(READ);
    }
    build = mindustry.io.TypeIO.readBuilding(READ);
    value = mindustry.io.TypeIO.readObject(READ);
  }

  @Override
  public void handleServer(NetConnection con) {
    if(con.player == null || con.kicked) {
      return;
    }
    mindustry.gen.Player player = con.player;
    mindustry.input.InputHandler.tileConfig(player, build, value);
    mindustry.gen.Call.tileConfig__forward(con, player, build, value);
  }

  @Override
  public void handleClient() {
    mindustry.input.InputHandler.tileConfig(player, build, value);
  }
}
