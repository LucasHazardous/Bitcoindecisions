package lucas.hazardous.bitcoindecisions;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

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


    private static int getBitcoinPrice() throws IOException {
        URL url = new URL("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        String inputLine;
        String content = "";
        while ((inputLine = in.readLine()) != null) {
            content += inputLine;
        }
        in.close();
        con.disconnect();

        System.out.println(content);
        Pattern pattern = Pattern.compile("(:[0-9]+})");
        Matcher matcher = pattern.matcher(content);
        String s = "";
        while (matcher.find()) {
            s = matcher.group(1);
            System.out.println(s);
        }

        return Integer.valueOf(s.substring(1, s.length()-1));
    }

    //constructor
    public BitcoinPack(Settings settings) {
        super(settings);
    }

    //override use method to make item work on right click
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        last_price = current_price;
        try{
            current_price = this.getBitcoinPrice();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println(last_price);
        System.out.println(current_price);

        ItemStack itemStack = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld)) return TypedActionResult.success(itemStack);
        if(current_price > last_price) {
                Random generator = new Random();
                EntityType entityType = goodEntityTypes[generator.nextInt(goodEntityTypes.length)];

                if(entityType.spawnFromItemStack((ServerWorld)world, itemStack, user, user.getBlockPos(), SpawnReason.SPAWN_EGG, false, false) == null) {
                    return TypedActionResult.pass(itemStack);
                } else {
                    itemStack.decrement(1);
                    return TypedActionResult.consume(itemStack);
                }

        } else if(current_price < last_price) {
            itemStack.decrement(1);
            user.kill();
            return TypedActionResult.consume(itemStack);
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}
