package mindustry.gen;

import arc.math.geom.Vec2;
import arc.util.io.Reads;
import arc.util.io.Writes;
import java.lang.Override;

import mindustry.arcModule.ARCVars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class CommandBuildingCallPacket extends Packet {
  private byte[] DATA = NODATA;

  public Player player;

  public int[] buildings;

  public Vec2 target;

  @Override
  public void write(Writes WRITE) {
    if(mindustry.Vars.net.server() || ARCVars.replayController.writing) {
      mindustry.io.TypeIO.writeEntity(WRITE, player);
    }
    mindustry.io.TypeIO.writeInts(WRITE, buildings);
    mindustry.io.TypeIO.writeVec2(WRITE, target);
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
    buildings = mindustry.io.TypeIO.readInts(READ);
    target = mindustry.io.TypeIO.readVec2(READ);
  }

  @Override
  public void handleServer(NetConnection con) {
    if(con.player == null || con.kicked) {
      return;
    }
    mindustry.gen.Player player = con.player;
    mindustry.input.InputHandler.commandBuilding(player, buildings, target);
    mindustry.gen.Call.commandBuilding__forward(con, player, buildings, target);
  }

  @Override
  public void handleClient() {
    mindustry.input.InputHandler.commandBuilding(player, buildings, target);
  }
}
