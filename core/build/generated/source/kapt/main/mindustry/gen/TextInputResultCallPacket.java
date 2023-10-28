package mindustry.gen;

import arc.util.io.Reads;
import arc.util.io.Writes;
import java.lang.Override;
import java.lang.String;

import mindustry.arcModule.ARCVars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class TextInputResultCallPacket extends Packet {
  private byte[] DATA = NODATA;

  public Player player;

  public int textInputId;

  public String text;

  @Override
  public void write(Writes WRITE) {
    if(mindustry.Vars.net.server() || ARCVars.replayController.writing) {
      mindustry.io.TypeIO.writeEntity(WRITE, player);
    }
    WRITE.i(textInputId);
    mindustry.io.TypeIO.writeString(WRITE, text);
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
    textInputId = READ.i();
    text = mindustry.io.TypeIO.readString(READ);
  }

  @Override
  public void handleServer(NetConnection con) {
    if(con.player == null || con.kicked) {
      return;
    }
    mindustry.gen.Player player = con.player;
    mindustry.ui.Menus.textInputResult(player, textInputId, text);
  }

  @Override
  public void handleClient() {
    mindustry.ui.Menus.textInputResult(player, textInputId, text);
  }
}
