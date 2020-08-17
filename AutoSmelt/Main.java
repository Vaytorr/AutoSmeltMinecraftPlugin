package AutoSmelt;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;




public class Main extends JavaPlugin implements Listener {
	
	
	@Override
	public void onEnable() {
		register();
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		addSmeltBook();
	}
	
	
//Comandos de chat ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player player = (Player) sender;
		if(label.equalsIgnoreCase("EnchantAutoSmelt")) {
			if(!(sender instanceof Player)) {
				return true;
			}
			
			if (player.getInventory().getItemInOffHand() == null) {
				player.sendMessage(ChatColor.LIGHT_PURPLE+"Hold an AutoSmelt book in your left hand to enchant");
				return true;
			}
			
			if (player.getInventory().getItemInMainHand() == null) {
				player.sendMessage(ChatColor.LIGHT_PURPLE+"Hold a pickaxe (Iron, Diamond or Netherite) in yout right hand to enchant");
				return true;
			}
			
			if(player.getInventory().getItemInOffHand().getEnchantments().containsKey(AUTOSMELT)) {
				if(player.getInventory().getItemInMainHand().getType() == Material.DIAMOND_PICKAXE || player.getInventory().getItemInMainHand().getType() == Material.IRON_PICKAXE || player.getInventory().getItemInMainHand().getType() == Material.NETHERITE_PICKAXE) {
				ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GRAY + "AutoSmelt");
				if(meta.hasLore()) {
					for (String l : meta.getLore()) {
						lore.add(l);
					}
				}
				meta.setLore(lore);
				player.getInventory().getItemInMainHand().setItemMeta(meta);
				player.getInventory().setItemInOffHand(null);
				updateEnchantments(player);
				return true;
				}else {
				player.sendMessage(ChatColor.LIGHT_PURPLE+"Make sure you have a pickaxe (Iron, Diamon or Netherite) in your right hand to enchant");
				return true;
			}
		}
			player.sendMessage(ChatColor.LIGHT_PURPLE+"Make sure you have an AutoSmelt book in your left hand to enchant ");
					return true;
				
			}
		if(label.equalsIgnoreCase("AutoSmelt")) {
		if(!(sender instanceof Player)) {
			return true;
		}
			player.sendMessage(ChatColor.GREEN+"The AutoSmelt enchantment automatically melts Iron and Gold ores for you ");
			player.sendMessage(ChatColor.GREEN+"The enchantment is affected by the fortune enchantment");
			player.sendMessage(ChatColor.LIGHT_PURPLE+"To craft the AutoSmelt book use the following recipe:");
			player.sendMessage(ChatColor.YELLOW+"M B M");
			player.sendMessage(ChatColor.YELLOW+"B D B");
			player.sendMessage(ChatColor.YELLOW+"M B M");
			player.sendMessage(ChatColor.YELLOW+"M = Magma Cream | B = Blaze Powder | D = Diamond Block");	
			player.sendMessage(ChatColor.LIGHT_PURPLE+"To apply the enchatment type /EnchantAutoSmelt in chat with the enchanted book in your left hand and a pickaxe (Iron, Diamond or Netherite) in your right hand");
				return true;
			}
		return true;
	}

//Register Enchantments ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	//
	public static final Enchantment AUTOSMELT = new CustomEnchantments("autosm","AutoSmelt",1);
	public static void register() {
		boolean registered = Arrays.stream(Enchantment.values()).collect(Collectors.toList()).contains(AUTOSMELT);
		if(!registered) {
			registerEnchantment(AUTOSMELT);
			
		}
	}
	public static void registerEnchantment(Enchantment enchantment) {

		boolean registered = true;
		try {
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
			Enchantment.registerEnchantment(enchantment);
		}catch(Exception e){
			registered = false;
			e.printStackTrace();
		}
		if(registered) {
			
		}
	}

	
	//Add crafting ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void addSmeltBook() {
		ItemStack book_autosmelt = new ItemStack(Material.ENCHANTED_BOOK);
		book_autosmelt.addUnsafeEnchantment(AUTOSMELT, 1);
		ItemMeta meta = book_autosmelt.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "AutoSmelt");

		meta.setLore(lore);
		book_autosmelt.setItemMeta(meta);
		NamespacedKey key = new NamespacedKey(this, "autosmelt");
	    ShapedRecipe smeltbook = new ShapedRecipe(key, book_autosmelt);
		smeltbook.shape("*%*","%B%","*%*");
		smeltbook.setIngredient('*', Material.MAGMA_CREAM);
		smeltbook.setIngredient('%', Material.BLAZE_POWDER);
		smeltbook.setIngredient('B', Material.DIAMOND_BLOCK);
		
		//add recipe to server
		Bukkit.getServer().addRecipe(smeltbook);
	}

	
	
	
	public void updateEnchantments(Player player) {
		//Add the enchantment if tool has "AutoSmelt" lore but doesn't have the enchantment yet
		//it happens when enchanting tool after AutoSmelt enchantment has been applied
if (player.getInventory().getItemInMainHand().getItemMeta().getLore().contains(ChatColor.GRAY + "AutoSmelt")) {
	if (!player.getInventory().getItemInMainHand().getItemMeta().hasEnchant(AUTOSMELT)) {
		player.getInventory().getItemInMainHand().addEnchantment(AUTOSMELT, 1);
		}
	}
}
	
	//Make the enchantment work
	@EventHandler 
	public void onBlockBreak(BlockBreakEvent event) {

		//if player doesn't have an item in hand return
		if (event.getPlayer().getInventory().getItemInMainHand() == null) {
			return;
		}
		//if player has an item without meta in hand return
		if (!event.getPlayer().getInventory().getItemInMainHand().hasItemMeta()) {
			return;
		}
		//if player has an item without lore in hand return
		if (!event.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasLore()){
			return;
		}
		//if item has "AutoSmelt" lore but not AutoSmelt Enchantment update enchantment
		if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().contains(ChatColor.GRAY + "AutoSmelt")) {
			if (!event.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasEnchant(AUTOSMELT)) {
				updateEnchantments(event.getPlayer());
			}
		}
		//if player has item without AutoSmelt enchantment return
		if (!event.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasEnchant(AUTOSMELT)) {
			return;
		}
		//if player on gamemode creative return
		if(event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
			return;
		}
		int enchlvl = 0;
		Random random = new Random(); 
		//if mined block == iron ore
		if(event.getBlock().getType().equals(Material.IRON_ORE)) {
			//disable default drops
			event.setDropItems(false);
			//verify if item has fortune enchantment
			if(event.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
				//get fortune level
			 enchlvl = event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
			}
			//apply drops/xp based on fortune level
			int drops = 1+random.nextInt(enchlvl+1);
			event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.IRON_INGOT,drops));
			event.getBlock().getWorld().spawn(event.getBlock().getLocation(), ExperienceOrb.class).setExperience(drops);
		}
		//if mined block == gold ore
		if(event.getBlock().getType().equals(Material.GOLD_ORE)) {
			//disable default drops
			event.setDropItems(false);
			//verify if item has fortune enchantment
			if(event.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
				//get fortune level
			 enchlvl = event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
			}
			//apply drops/xp based on fortune level
			int drops = 1+random.nextInt(enchlvl+1);
			event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT, drops));
			event.getBlock().getWorld().spawn(event.getBlock().getLocation(), ExperienceOrb.class).setExperience(drops);
		}
			
	}
}