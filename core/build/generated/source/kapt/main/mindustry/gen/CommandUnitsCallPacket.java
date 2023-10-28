package mindustry.gen;

import arc.math.geom.Vec2;
import arc.util.io.Reads;
import arc.util.io.Writes;
import java.lang.Override;

import mindustry.arcModule.ARCVars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class CommandUnitsCallPacket extends Packet {
  private byte[] DATA = NODATA;

  public Player player;

  public int[] unitIds;

  public Building buildTarget;

  public Unit unitTarget;

  public Vec2 posTarget;

  @Override
  public void write(Writes WRITE) {
    if(mindustry.Vars.net.server() || ARCVars.replayController.writing) {
      mindustry.io.TypeIO.writeEntity(WRITE, player);
    }
    mindustry.io.TypeIO.writeInts(WRITE, unitIds);
    mindustry.io.TypeIO.writeBuilding(WRITE, buildTarget);
    mindustry.io.TypeIO.writeUnit(WRITE, unitTarget);
    mindustry.io.TypeIO.writeVec2(WRITE, posTarget);
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
    unitIds = mindustry.io.TypeIO.readInts(READ);
    buildTarget = mindustry.io.TypeIO.readBuilding(READ);
    unitTarget = mindustry.io.TypeIO.readUnit(READ);
    posTarget = mindustry.io.TypeIO.readVec2(READ);
  }

  @Override
  public void handleServer(NetConnection con) {
    if(con.player == null || con.kicked) {
      return;
    }
    mindustry.gen.Player player = con.player;
    mindustry.input.InputHandler.commandUnits(player, unitIds, buildTarget, unitTarget, posTarget);
    mindustry.gen.Call.commandUnits__forward(con, player, unitIds, buildTarget, unitTarget, posTarget);
  }

  @Override
  public void handleClient() {
    mindustry.input.InputHandler.commandUnits(player, unitIds, buildTarget, unitTarget, posTarget);
  }
}
