package de.tomalbrc.questr.impl.json;

import com.google.gson.*;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.condition.Condition;
import de.tomalbrc.questr.api.reward.Reward;
import de.tomalbrc.questr.impl.json.deserializer.ConditionDeserializer;
import de.tomalbrc.questr.impl.json.deserializer.SimpleCodecDeserializer;
import de.tomalbrc.questr.impl.util.ResourceSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class Json {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(Reward.class, "type")
                    .registerSubtype(Reward.XpReward.class, "xp")
                    .registerSubtype(Reward.ItemReward.class, "item")
                    .registerSubtype(Reward.CommandReward.class, "command"))
            .registerTypeHierarchyAdapter(Condition.class, new ConditionDeserializer())
            .registerTypeHierarchyAdapter(BlockState.class, new RegistryAccessDeserializer<>(BlockState.CODEC))
            .registerTypeHierarchyAdapter(EquipmentSlot.class, new SimpleCodecDeserializer<>(EquipmentSlot.CODEC))
            .registerTypeHierarchyAdapter(BlockPos.class, new SimpleCodecDeserializer<>(BlockPos.CODEC))
            .registerTypeHierarchyAdapter(Vector3f.class, new SimpleCodecDeserializer<>(ExtraCodecs.VECTOR3F))
            .registerTypeHierarchyAdapter(Vector2f.class, new SimpleCodecDeserializer<>(ExtraCodecs.VECTOR2F))
            .registerTypeHierarchyAdapter(Quaternionf.class, new QuaternionfDeserializer())
            .registerTypeHierarchyAdapter(ResourceLocation.class, new SimpleCodecDeserializer<>(ResourceLocation.CODEC))
            .registerTypeHierarchyAdapter(Block.class, new RegistryDeserializer<>(BuiltInRegistries.BLOCK))
            .registerTypeHierarchyAdapter(Item.class, new RegistryDeserializer<>(BuiltInRegistries.ITEM))
            .registerTypeHierarchyAdapter(ResourceSet.class, new ResourceSet.Deserializer())
            .registerTypeAdapter(SoundEvent.class, new RegistryDeserializer<>(BuiltInRegistries.SOUND_EVENT))
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .create();

    public record RegistryAccessDeserializer<T>(Codec<T> codec) implements JsonDeserializer<T> {
        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var decoded = codec.decode(RegistryOps.create(JsonOps.INSTANCE, QuestrMod.SERVER.registryAccess()), json);
            return decoded.getOrThrow().getFirst();
        }
    }

    public static class QuaternionfDeserializer implements JsonDeserializer<Quaternionf> {
        @Override
        public Quaternionf deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            if (jsonArray.size() < 3) {
                throw new JsonParseException("Array size should be at least 3 for euler angle deserialization.");
            }

            float x = jsonArray.get(0).getAsFloat();
            float y = jsonArray.get(1).getAsFloat();
            float z = jsonArray.get(2).getAsFloat();

            return new Quaternionf().rotateXYZ(x * Mth.DEG_TO_RAD, y * Mth.DEG_TO_RAD, z * Mth.DEG_TO_RAD);
        }
    }

    private record RegistryDeserializer<T>(Registry<T> registry) implements JsonDeserializer<T> {
        @Override
        public T deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            return this.registry.getValue(ResourceLocation.parse(element.getAsString()));
        }
    }
}
