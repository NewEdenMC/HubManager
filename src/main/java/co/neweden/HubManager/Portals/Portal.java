package co.neweden.HubManager.Portals;

import org.bukkit.Location;
import org.bukkit.World;

public class Portal {

    World world;
    int x1; int y1; int z1;
    int x2; int y2; int z2;
    Location teleportTo = null;
    String menuToOpen = null;
    boolean spawnOnMenuClose;
    boolean hidePlayerWhileInMenu;

}
