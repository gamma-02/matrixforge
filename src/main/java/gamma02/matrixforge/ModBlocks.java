package gamma02.matrixforge;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Registrar(element = Block.class, modid = "matrixforge")
public class ModBlocks {


    @RegistryEntry(value = "test_block")
    public static final Block BLOCK = new Block(BlockBehaviour.Properties.copy(Blocks.DIRT));
}
