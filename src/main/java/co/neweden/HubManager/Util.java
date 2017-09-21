package co.neweden.HubManager;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Util {

    public static Location parseLocationFromString(String location) throws IllegalArgumentException {
        Validate.notNull(location);
        String genericError = "Tried to parse location \"" + location + "\" however it is not in the correct format, format should be \"WORLD-NAME X-COORD Y-COORD Z-COORD\" for example \"world 0 64 0\".  Specific error: ";
        String[] parts = location.split(" ");

        if (parts.length < 4)
            throw new IllegalArgumentException(genericError + "when splitting location string into parts, less than 4 parts were found.");

        World world = Bukkit.getWorld(parts[0]);
        double x; double y; double z;
        try {
            x = Double.parseDouble(parts[1]);
            y = Double.parseDouble(parts[2]);
            z = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(genericError + "some parts of the location string were not numbers, make sure there are valid x, y and z coordinates after the world name.");
        }

        return new Location(world, x, y, z);
    }

}
