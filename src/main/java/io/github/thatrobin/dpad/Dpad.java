package io.github.thatrobin.dpad;

import io.github.thatrobin.dpad.networking.DpadHandshake;
import io.github.thatrobin.dpad.utils.PackResourceDependencyReader;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.nio.file.Path;

public class Dpad implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger(Dpad.class);
    public static final PackResourceDependencyReader READER = new PackResourceDependencyReader();
    public static Path DATAPACK_PATH;

    @Override
    public void onInitialize() {
        DpadHandshake.enable();
    }

}
