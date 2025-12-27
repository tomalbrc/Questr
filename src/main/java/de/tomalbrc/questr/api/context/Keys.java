package de.tomalbrc.questr.api.context;

import de.tomalbrc.questr.impl.util.ResourceSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class Keys {
    public static Map<String, DataKey<?>> BY_ID = new Object2ObjectOpenHashMap<>();

    public static final DataKey<String> PLAYER = DataKey.of("player", String.class);
    public static final DataKey<BlockPos> PLAYER_POSITION = DataKey.of("player_position", BlockPos.class);
    public static final DataKey<BlockPos> POSITION = DataKey.of("position", BlockPos.class);
    public static final DataKey<Boolean> IS_RAINING = DataKey.of("is_raining", Boolean.class);
    public static final DataKey<Boolean> IS_THUNDERING = DataKey.of("is_thundering", Boolean.class);
    public static final DataKey<Identifier> DIMENSION = DataKey.of("dimension", Identifier.class);
    public static final DataKey<Item> ITEM = DataKey.of("item", Item.class);
    public static final DataKey<ItemStack> ITEM_STACK = DataKey.of("stack", ItemStack.class);

    public static final DataKey<Float> DISTANCE = DataKey.of("distance", Float.class);
    public static final DataKey<Float> DISTANCE_TRACKING = DataKey.of("distance_tracking", Float.class);

    public static final DataKey<Block> BLOCK = DataKey.of("block", Block.class);
    public static final DataKey<BlockState> BLOCK_STATE = DataKey.of("state", BlockState.class);
    public static final DataKey<Identifier> ENTITY_TYPE = DataKey.of("entity_type", Identifier.class);
    public static final DataKey<Identifier> BLOCK_ENTITY_TYPE = DataKey.of("block_entity_type", Identifier.class);

    public static final DataKey<String> MESSAGE = DataKey.of("message", String.class);
    public static final DataKey<Integer> MESSAGE_LENGTH = DataKey.of("message_length", Integer.class);

    public static final DataKey<ResourceSet> ITEM_TAG = DataKey.of("item_tag", ResourceSet.class);
    public static final DataKey<ResourceSet> BLOCK_TAG = DataKey.of("block_tag", ResourceSet.class);
    public static final DataKey<ResourceSet> ENTITY_TAG = DataKey.of("entity_tag", ResourceSet.class);

    // args
    public static final DataKey<Boolean> TRACK = DataKey.of("track", Boolean.class);
    public static final DataKey<BlockPos> TRACK_POSITION = DataKey.of("tracking_position", BlockPos.class);
    public static final DataKey<Integer> TRACK_RADIUS = DataKey.of("tracking_radius", Integer.class);
}
