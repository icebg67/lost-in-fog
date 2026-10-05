
package net.mcreator.lostinfog.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class FlashlightHPRBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public FlashlightHPRBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(-1, 3600000).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			default -> Shapes.or(box(5.4868, -0.2632, 11.0884, 10.0132, 4.2632, 12.4116), box(5.67, -0.08, 10.21, 9.83, 4.08, 12.29), box(5.774, 0.024, 10.262, 9.726, 3.976, 12.238), box(6.0444, 0.2944, 5.3972, 9.4556, 3.7056, 7.1028),
					box(6.0444, 0.2944, 3.3972, 9.4556, 3.7056, 5.1028), box(6.0444, 0.2944, 7.8972, 9.4556, 3.7056, 9.6028), box(6.127, 0.377, 2.83615, 9.373, 3.623, 10.66385));
			case NORTH -> Shapes.or(box(5.9868, -0.2632, 3.5884, 10.5132, 4.2632, 4.9116), box(6.17, -0.08, 3.71, 10.33, 4.08, 5.79), box(6.274, 0.024, 3.762, 10.226, 3.976, 5.738), box(6.5444, 0.2944, 8.8972, 9.9556, 3.7056, 10.6028),
					box(6.5444, 0.2944, 10.8972, 9.9556, 3.7056, 12.6028), box(6.5444, 0.2944, 6.3972, 9.9556, 3.7056, 8.1028), box(6.627, 0.377, 5.33615, 9.873, 3.623, 13.16385));
			case EAST -> Shapes.or(box(11.0884, -0.2632, 5.9868, 12.4116, 4.2632, 10.5132), box(10.21, -0.08, 6.17, 12.29, 4.08, 10.33), box(10.262, 0.024, 6.274, 12.238, 3.976, 10.226), box(5.3972, 0.2944, 6.5444, 7.1028, 3.7056, 9.9556),
					box(3.3972, 0.2944, 6.5444, 5.1028, 3.7056, 9.9556), box(7.8972, 0.2944, 6.5444, 9.6028, 3.7056, 9.9556), box(2.83615, 0.377, 6.627, 10.66385, 3.623, 9.873));
			case WEST -> Shapes.or(box(3.5884, -0.2632, 5.4868, 4.9116, 4.2632, 10.0132), box(3.71, -0.08, 5.67, 5.79, 4.08, 9.83), box(3.762, 0.024, 5.774, 5.738, 3.976, 9.726), box(8.8972, 0.2944, 6.0444, 10.6028, 3.7056, 9.4556),
					box(10.8972, 0.2944, 6.0444, 12.6028, 3.7056, 9.4556), box(6.3972, 0.2944, 6.0444, 8.1028, 3.7056, 9.4556), box(5.33615, 0.377, 6.127, 13.16385, 3.623, 9.373));
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
}
