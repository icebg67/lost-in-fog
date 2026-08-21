
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

public class RadioBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RadioBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
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
			default -> Shapes.or(box(3, 0, 7, 14, 6, 10), box(12.475, 4, 8.25, 12.975, 11, 8.75), box(12.175, 5.25, 8, 13.175, 6.25, 9), box(12.425, 10.5, 8.175, 13.05, 11.025, 8.825), box(4, 1, 9.35, 9, 5, 10.35), box(11, 1, 9.325, 13, 3, 10.325),
					box(10, 3.5, 10, 13, 5, 10.1));
			case NORTH -> Shapes.or(box(2, 0, 6, 13, 6, 9), box(3.025, 4, 7.25, 3.525, 11, 7.75), box(2.825, 5.25, 7, 3.825, 6.25, 8), box(2.95, 10.5, 7.175, 3.575, 11.025, 7.825), box(7, 1, 5.65, 12, 5, 6.65), box(3, 1, 5.675, 5, 3, 6.675),
					box(3, 3.5, 5.9, 6, 5, 6));
			case EAST -> Shapes.or(box(7, 0, 2, 10, 6, 13), box(8.25, 4, 3.025, 8.75, 11, 3.525), box(8, 5.25, 2.825, 9, 6.25, 3.825), box(8.175, 10.5, 2.95, 8.825, 11.025, 3.575), box(9.35, 1, 7, 10.35, 5, 12), box(9.325, 1, 3, 10.325, 3, 5),
					box(10, 3.5, 3, 10.1, 5, 6));
			case WEST -> Shapes.or(box(6, 0, 3, 9, 6, 14), box(7.25, 4, 12.475, 7.75, 11, 12.975), box(7, 5.25, 12.175, 8, 6.25, 13.175), box(7.175, 10.5, 12.425, 7.825, 11.025, 13.05), box(5.65, 1, 4, 6.65, 5, 9), box(5.675, 1, 11, 6.675, 3, 13),
					box(5.9, 3.5, 10, 6, 5, 13));
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
