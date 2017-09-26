package co.neweden.HubManager;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Arrays;

public class Util {

    public static Location parseLocationFromString(String location) { return parseLocationFromString(location, true); }

    public static Location parseLocationFromString(String location, boolean worldRequired) throws IllegalArgumentException {
        Validate.notNull(location);
        String genericError = "Tried to parse location \"" + location + "\" however it is not in the correct format, format should start with \"X-COORD Y-COORD Z-COORD\" and can be followed by \"YAW PITCH\" for example \"0 64 0\" or \"0 64 0 0 0\".  Specific error: ";
        String[] parts = location.split(" ");
        int validLength = worldRequired ? 4 : 3;
        World world = null;

        if (parts.length < validLength)
            throw new IllegalArgumentException(genericError + "when splitting location string into parts, less than " + validLength + " parts were found.");

        if (worldRequired) {
            genericError = "Tried to parse location \"" + location + "\" however it is not in the correct format, format should be \"WORLD-NAME X-COORD Y-COORD Z-COORD\" for example \"world 0 64 0\".  Specific error: ";
            world = Bukkit.getWorld(parts[0]);
            parts = Arrays.copyOfRange(parts, 1, parts.length);
        }

        double x; double y; double z;
        try {
            x = Double.parseDouble(parts[0]);
            y = Double.parseDouble(parts[1]);
            z = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(genericError + "some parts of the location string were not numbers, make sure there are valid x, y and z coordinates after the world name.");
        }

        Location loc =  new Location(world, x, y, z);

        try {
            if (parts.length >= 4)
                loc.setYaw(Float.parseFloat(parts[3]));
            if (parts.length >= 5)
                loc.setPitch(Float.parseFloat(parts[4]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(genericError + "yaw or pitch were not valid numbers.");
        }

        return loc;
    }

}
