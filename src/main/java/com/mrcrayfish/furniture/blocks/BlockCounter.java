package com.mrcrayfish.furniture.blocks;

import com.google.common.collect.Lists;
import com.mrcrayfish.furniture.tileentity.TileEntityKitchenCounter;
import com.mrcrayfish.furniture.util.StateHelper;
import com.mrcrayfish.furniture.util.TileEntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class BlockCounter extends BlockFurnitureTile
{
    public static final PropertyInteger COLOUR = PropertyInteger.create("colour", 0, 15);

    public static final AxisAlignedBB COUNTER_TOP = new AxisAlignedBB(0, 14 * 0.0625, 0, 1, 1, 1);
    public static final AxisAlignedBB[] FORWARD_BOXES = {new AxisAlignedBB(0, 0, 2 * 0.0625, 1, 14 * 0.0625, 1), new AxisAlignedBB(0, 0, 0, 14 * 0.0625, 14 * 0.0625, 1), new AxisAlignedBB(0, 0, 0, 1, 14 * 0.0625, 14 * 0.0625), new AxisAlignedBB(2 * 0.0625, 0, 0, 1, 14 * 0.0625, 1)};
    public static final AxisAlignedBB[] LEFT_CORNER_BOXES = {new AxisAlignedBB(0, 0, 2 * 0.0625, 14 * 0.0625, 14 * 0.0625, 1), new AxisAlignedBB(0, 0, 0, 14 * 0.0625, 14 * 0.0625, 14 * 0.0625), new AxisAlignedBB(2 * 0.0625, 0, 0, 1, 14 * 0.0625, 14 * 0.0625), new AxisAlignedBB(2 * 0.0625, 0, 2 * 0.062, 1, 14 * 0.0625, 1)};
    public static final AxisAlignedBB[] RIGHT_CORNER_BOXES = {new AxisAlignedBB(2 * 0.0625, 0, 2 * 0.0625, 1, 14 * 0.0625, 1), new AxisAlignedBB(0, 0, 2 * 0.0625, 14 * 0.0625, 14 * 0.0625, 1), new AxisAlignedBB(0, 0, 0, 14 * 0.0625, 14 * 0.0625, 14 * 0.0625), new AxisAlignedBB(2 * 0.0625, 0, 0, 1, 14 * 0.0625, 14 * 0.0625)};

    public static final PropertyEnum<CounterType> TYPE = PropertyEnum.create("type", CounterType.class);

    public BlockCounter(Material material)
    {
        super(material);
        this.setHardness(0.5F);
        this.setSoundType(SoundType.STONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TYPE, CounterType.NORMAL));
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState)
    {
        List<AxisAlignedBB> list = getCollisionBoxList(this.getActualState(state, worldIn, pos));
        for(AxisAlignedBB box : list)
        {
            Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, box);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof TileEntityKitchenCounter)
        {
            int colour = ((TileEntityKitchenCounter) tileEntity).getColour();
            state = state.withProperty(COLOUR, colour);
        }

        if(StateHelper.getBlock(world, pos, state.getValue(FACING), StateHelper.Direction.DOWN) instanceof BlockCounter)
        {
            if(StateHelper.getRotation(world, pos, state.getValue(FACING), StateHelper.Direction.DOWN) == StateHelper.Direction.LEFT)
            {
                return state.withProperty(TYPE, CounterType.CORNER_LEFT);
            }
            else if(StateHelper.getRotation(world, pos, state.getValue(FACING), StateHelper.Direction.DOWN) == StateHelper.Direction.RIGHT)
            {
                return state.withProperty(TYPE, CounterType.CORNER_RIGHT);
            }
        }
        if(StateHelper.getBlock(world, pos, state.getValue(FACING), StateHelper.Direction.UP) instanceof BlockCounter)
        {
            if(StateHelper.getRotation(world, pos, state.getValue(FACING), StateHelper.Direction.UP) == StateHelper.Direction.LEFT && !(StateHelper.getBlock(world, pos, state.getValue(FACING), StateHelper.Direction.LEFT) instanceof BlockCounter))
            {
                return state.withProperty(TYPE, CounterType.INVERT_RIGHT);
            }
            else if(StateHelper.getRotation(world, pos, state.getValue(FACING), StateHelper.Direction.UP) == StateHelper.Direction.RIGHT && !(StateHelper.getBlock(world, pos, state.getValue(FACING), StateHelper.Direction.RIGHT) instanceof BlockCounter))
            {
                return state.withProperty(TYPE, CounterType.INVERT_LEFT);
            }
        }
        return state.withProperty(TYPE, CounterType.NORMAL);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityKitchenCounter)
        {
            TileEntityKitchenCounter tileEntityCouch = (TileEntityKitchenCounter) tileEntity;
            if(!heldItem.isEmpty())
            {
                if(heldItem.getItem() instanceof ItemDye)
                {
                    if(tileEntityCouch.getColour() != (15 - heldItem.getItemDamage()))
                    {
                        tileEntityCouch.setColour(heldItem.getItemDamage());
                        if(!playerIn.isCreative())
                        {
                            heldItem.shrink(1);
                        }
                        TileEntityUtil.markBlockForUpdate(worldIn, pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, TYPE, COLOUR);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityKitchenCounter();
    }

    public enum CounterType implements IStringSerializable
    {
        NORMAL,
        CORNER_LEFT,
        CORNER_RIGHT,
        INVERT_LEFT,
        INVERT_RIGHT;

        @Override
        public String getName()
        {
            return this.toString().toLowerCase(Locale.US);
        }
    }

    private List<AxisAlignedBB> getCollisionBoxList(IBlockState state)
    {
        List<AxisAlignedBB> list = Lists.newArrayList();
        EnumFacing facing = state.getValue(FACING);
        list.add(COUNTER_TOP);

        if(state.getValue(TYPE) == CounterType.INVERT_LEFT)
        {
            list.add(LEFT_CORNER_BOXES[facing.getHorizontalIndex()]);
        }
        else if(state.getValue(TYPE) == CounterType.INVERT_RIGHT)
        {
            list.add(RIGHT_CORNER_BOXES[facing.getHorizontalIndex()]);
        }
        else if(state.getValue(TYPE) == CounterType.NORMAL)
        {
            list.add(FORWARD_BOXES[facing.getHorizontalIndex()]);
        }
        else
        {
            list.add(FULL_BLOCK_AABB);
        }

        return list;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        List<RayTraceResult> list = Lists.newArrayList();

        for(AxisAlignedBB axisalignedbb : getCollisionBoxList(this.getActualState(blockState, worldIn, pos)))
        {
            list.add(this.rayTrace(pos, start, end, axisalignedbb));
        }

        RayTraceResult raytraceresult1 = null;
        double d1 = 0.0D;

        for(RayTraceResult raytraceresult : list)
        {
            if(raytraceresult != null)
            {
                double d0 = raytraceresult.hitVec.squareDistanceTo(end);

                if(d0 > d1)
                {
                    raytraceresult1 = raytraceresult;
                    d1 = d0;
                }
            }
        }

        return raytraceresult1;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityKitchenCounter)
        {
            ((TileEntityKitchenCounter) tileEntity).setColour(15 - stack.getMetadata());
        }
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity tileEntity, ItemStack stack)
    {
        if (tileEntity instanceof TileEntityKitchenCounter)
        {
            TileEntityKitchenCounter counter = (TileEntityKitchenCounter) tileEntity;
            ItemStack itemstack = new ItemStack(this, 1, counter.getColour());
            spawnAsEntity(worldIn, pos, itemstack);
        }
        else
        {
            super.harvestBlock(worldIn, player, pos, state, tileEntity, stack);
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items)
    {
        for(int i = 0; i < EnumDyeColor.values().length; i++)
        {
            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        int metadata = 0;
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof TileEntityKitchenCounter)
        {
            metadata = ((TileEntityKitchenCounter) tileEntity).getColour();
        }
        return new ItemStack(this, 1, metadata);
    }
}
