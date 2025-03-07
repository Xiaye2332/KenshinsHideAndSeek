/*
 * This file is part of Kenshins Hide and Seek
 *
 * Copyright (c) 2021 Tyler Murphy.
 *
 * Kenshins Hide and Seek free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * he Free Software Foundation version 3.
 *
 * Kenshins Hide and Seek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.tylermurphy.hideAndSeek.world;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings({"unused"})
public class VoidGenerator extends ChunkGenerator {

    // 1.14 And On
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return Collections.emptyList();
    }

    public boolean shouldGenerateNoise() {
    	return false;
    }

    public boolean shouldGenerateSurface() {
    	return false;
    }

    public boolean shouldGenerateBedrock() {
    	return false;
    }

    public boolean shouldGenerateCaves() {
    	return false;
    }

    public boolean shouldGenerateDecorations() {
    	return false;
    }

    public boolean shouldGenerateMobs() {
    	return false;
    }

    public boolean shouldGenerateStructures() {
    	return false;
    }

    public boolean canSpawn(@NotNull World world, int x, int z) {
        return true;
    }

    // 1.13 And Prev
    public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z, @NotNull BiomeGrid biome) {
        return createChunkData(world);
    }

    // 1.8
    public byte[] generate(World world, Random random, int x, int z) {
        return new byte[world.getMaxHeight() / 16];
    }

    public byte[][] generateBlockSections(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid biomes) {
        return new byte[world.getMaxHeight() / 16][];
    }


}
