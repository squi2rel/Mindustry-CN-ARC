package mindustry.gen;

import arc.util.io.Reads;
import arc.util.io.Writes;
import java.lang.Override;

import mindustry.arcModule.ARCVars;
import mindustry.game.Team;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class SetPlayerTeamEditorCallPacket extends Packet {
  private byte[] DATA = NODATA;

  public Player player;

  public Team team;

  @Override
  public void write(Writes WRITE) {
    if(mindustry.Vars.net.server() || ARCVars.replayController.writing) {
      mindustry.io.TypeIO.writeEntity(WRITE, player);
    }
    mindustry.io.TypeIO.writeTeam(WRITE, team);
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
    team = mindustry.io.TypeIO.readTeam(READ);
  }

  @Override
  public void handleServer(NetConnection con) {
    if(con.player == null || con.kicked) {
      return;
    }
    mindustry.gen.Player player = con.player;
    mindustry.ui.fragments.HudFragment.setPlayerTeamEditor(player, team);
    mindustry.gen.Call.setPlayerTeamEditor__forward(con, player, team);
  }

  @Override
  public void handleClient() {
    mindustry.ui.fragments.HudFragment.setPlayerTeamEditor(player, team);
  }
}
