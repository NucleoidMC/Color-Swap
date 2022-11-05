package io.github.haykam821.colorswap.game.prism;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public record PrismConfig(
	int spawnPadding,
	int maximumHeld,
	List<Prism> randomlySpawnable,
	IntProvider roundsBetweenSpawns,
	FloatProvider size
) {
	public static final Codec<PrismConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.optionalFieldOf("spawn_padding", 3).forGetter(PrismConfig::spawnPadding),
			Codec.INT.optionalFieldOf("maximum_held", 1).forGetter(PrismConfig::maximumHeld),
			Prisms.REGISTRY.listOf().fieldOf("randomly_spawnable").orElseGet(() -> new ArrayList<>(Prisms.REGISTRY.values())).forGetter(PrismConfig::randomlySpawnable),
			IntProvider.POSITIVE_CODEC.optionalFieldOf("rounds_between_spawns", UniformIntProvider.create(2, 4)).forGetter(PrismConfig::roundsBetweenSpawns),
			FloatProvider.createValidatedCodec(0, Float.MAX_VALUE).optionalFieldOf("size", ConstantFloatProvider.create(2.2f)).forGetter(PrismConfig::size)
		).apply(instance, PrismConfig::new);
	});
}
