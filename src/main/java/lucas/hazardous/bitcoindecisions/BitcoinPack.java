package lucas.hazardous.bitcoindecisions;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitcoinPack extends Item {
    private static int last_price = 0;
    private static int current_price = 1;
    private static EntityType<?>[] goodEntityTypes = new EntityType[]{EntityType.FOX, EntityType.AXOLOTL, EntityType.BOAT, EntityType.GLOW_SQUID, EntityType.HORSE};
    private static EntityType<?>[] badEntityTypes = new EntityType[]{EntityType.EVOKER, EntityType.GHAST, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.ILLUSIONER};
    private static Random generator = new Random();

    private static int getBitcoinPrice() throws IOException {
        //open connection to api
        URL url = new URL("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        //read response from api
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        String inputLine;
        String content = "";
        while ((inputLine = in.readLine()) != null) {
            content += inputLine;
        }

        //close reader and connection
        in.close();
        con.disconnect();

        //extract data from response
        Pattern pattern = Pattern.compile("(:[0-9]+})");
        Matcher matcher = pattern.matcher(content);
        String s = "";
        while (matcher.find()) {
            s = matcher.group(1);
        }

        return Integer.valueOf(s.substring(1, s.length() - 1))+generator.nextInt(3)-1;
    }

    //constructor
    public BitcoinPack(Settings settings) {
        super(settings);
    }

    //override use method to make item work on right click
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        last_price = current_price;
        try {
            current_price = this.getBitcoinPrice();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println(last_price);
        System.out.println(current_price);

        ItemStack itemStack = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld)) return TypedActionResult.success(itemStack);
        //price it higher than the last time - something good happens
        if (current_price > last_price) {
            EntityType entityType = goodEntityTypes[generator.nextInt(goodEntityTypes.length)];

            if (entityType.spawnFromItemStack((ServerWorld) world, itemStack, user, user.getBlockPos(), SpawnReason.SPAWN_EGG, false, false) == null) {
                return TypedActionResult.pass(itemStack);
            } else {
                itemStack.decrement(1);
                return TypedActionResult.consume(itemStack);
            }
            //price it lower than the last time - something bad happens
        } else if (current_price < last_price) {
            EntityType entityType = badEntityTypes[generator.nextInt(badEntityTypes.length)];

            if (entityType.spawnFromItemStack((ServerWorld) world, itemStack, user, user.getBlockPos(), SpawnReason.SPAWN_EGG, false, false) == null) {
                return TypedActionResult.pass(itemStack);
            } else {
                itemStack.decrement(1);
                return TypedActionResult.consume(itemStack);
            }
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}
