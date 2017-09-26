package co.neweden.HubManager.Portals;

import org.bukkit.Location;
import org.bukkit.World;

public class Portal {

    World world;
    int x1; int y1; int z1;
    int x2; int y2; int z2;
    Location teleportToOnEnter = null;
    String menuToOpenOnEnter = null;
    boolean spawnOnMenuClose;
    Location teleportToOnMenuClose;
    boolean hidePlayerWhileInMenu;

}
