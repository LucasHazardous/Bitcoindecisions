package lucas.hazardous.bitcoindecisions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Bitcoindecisions implements ModInitializer {
    public static final Item BITCOIN_PACK = new BitcoinPack(new FabricItemSettings().group(ItemGroup.MISC));

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier("bitcoindecisions", "bitcoin_pack"), BITCOIN_PACK);
    }
}
