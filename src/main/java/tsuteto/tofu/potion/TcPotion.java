package tsuteto.tofu.potion;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Level;

import tsuteto.tofu.init.TcItems;
import tsuteto.tofu.item.ItemTcFood;
import tsuteto.tofu.network.PacketDispatcher;
import tsuteto.tofu.network.packet.PacketPotionIdCheck;
import tsuteto.tofu.util.ModLog;

public class TcPotion {

    private static final String CONF_CATEGORY = "potion";

    public static Potion glowing = null;
    public static Potion filling = null;

    public static void register(Configuration conf) {
        int id;

        try {
            id = assignId("glowing", conf);
            glowing = new PotionGlowing(id, false, 0xcccc00).setPotionName("potion.glowing");

            id = assignId("filling", conf);
            filling = new PotionFilling(id, false, 0xffa734).setPotionName("potion.filling");
        } catch (Exception e) {
            ModLog.log(Level.WARN, e, e.getLocalizedMessage());
        }

        if (glowing != null) {
            ((ItemTcFood) TcItems.tofuGlow).setPotionEffect(glowing.id, 240, 0, 1.0F);
        }

        if (filling != null) {
            ((ItemTcFood) TcItems.tofuMiso).setPotionEffect(filling.id, 300, 0, 1.0F);
        }
    }

    public static int assignId(String confKey, Configuration conf) throws Exception {
        int maxId = Potion.potionTypes.length - 1;

        int cfgId = conf.get(CONF_CATEGORY, confKey, -1)
            .getInt();

        if (cfgId > 0 && cfgId <= maxId) {
            if (Potion.potionTypes[cfgId] == null) {
                return cfgId;
            } else {
                ModLog.log(Level.WARN, "Potion ID " + cfgId + " for " + confKey + " is already occupied, reassigning");
            }
        }

        for (int i = maxId; i > 0; i--) {
            if (Potion.potionTypes[i] == null) {
                conf.get(CONF_CATEGORY, confKey, i)
                    .set(i);
                return i;
            }
        }

        throw new Exception("Failed to register potion '" + confKey + "': no free potion IDs");
    }

    public static void onLogin(EntityPlayer player) {
        if (player == null || player.worldObj == null || player.worldObj.isRemote) {
            return;
        }

        if (glowing == null || filling == null) {
            return;
        }

        PacketDispatcher.packet(new PacketPotionIdCheck(glowing.getId(), filling.getId()))
            .sendToPlayer(player);
    }
}
