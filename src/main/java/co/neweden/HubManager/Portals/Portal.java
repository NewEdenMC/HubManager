package co.neweden.HubManager.Portals;

import org.bukkit.Location;
import org.bukkit.World;

public class Portal {

    World world;
    Location pos1;
    Location pos2;
    Location teleportToOnEnter = null;
    String menuToOpenOnEnter = null;
    boolean spawnOnMenuClose;
    Location teleportToOnMenuClose;
    boolean hidePlayerWhileInMenu;

}
