package mindustry.gen;

import arc.util.io.Reads;
import arc.util.io.Writes;
import java.lang.Override;
import mindustry.ai.UnitCommand;
import mindustry.arcModule.ARCVars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class SetUnitCommandCallPacket extends Packet {
  private byte[] DATA = NODATA;

  public Player player;

  public int[] unitIds;

  public UnitCommand command;

  @Override
  public void write(Writes WRITE) {
    if(mindustry.Vars.net.server() || ARCVars.replayController.writing) {
      mindustry.io.TypeIO.writeEntity(WRITE, player);
    }
    mindustry.io.TypeIO.writeInts(WRITE, unitIds);
    mindustry.io.TypeIO.writeCommand(WRITE, command);
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
    command = mindustry.io.TypeIO.readCommand(READ);
  }

  @Override
  public void handleServer(NetConnection con) {
    if(con.player == null || con.kicked) {
      return;
    }
    mindustry.gen.Player player = con.player;
    mindustry.input.InputHandler.setUnitCommand(player, unitIds, command);
    mindustry.gen.Call.setUnitCommand__forward(con, player, unitIds, command);
  }

  @Override
  public void handleClient() {
    mindustry.input.InputHandler.setUnitCommand(player, unitIds, command);
  }
}
