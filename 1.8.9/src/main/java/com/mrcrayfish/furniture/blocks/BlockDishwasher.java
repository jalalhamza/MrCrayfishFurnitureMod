/**
 * MrCrayfish's Furniture Mod
 * Copyright (C) 2016  MrCrayfish (http://www.mrcrayfish.com/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mrcrayfish.furniture.blocks;

import java.util.Random;

import com.mrcrayfish.furniture.MrCrayfishFurnitureMod;
import com.mrcrayfish.furniture.init.FurnitureAchievements;
import com.mrcrayfish.furniture.init.FurnitureItems;
import com.mrcrayfish.furniture.tileentity.TileEntityDishwasher;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockDishwasher extends BlockFurnitureTile
{
	public BlockDishwasher(Material material)
	{
		super(material);
		this.setHardness(0.5F);
		this.setStepSound(Block.soundTypeAnvil);
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		((EntityPlayer) placer).triggerAchievement(FurnitureAchievements.applianceCity);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			TileEntity tile_entity = world.getTileEntity(pos);

			if (tile_entity instanceof TileEntityDishwasher)
			{
				player.openGui(MrCrayfishFurnitureMod.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityDishwasher();
	}

	@Override
	public int getComparatorInputOverride(World world, BlockPos pos) 
	{
		TileEntityDishwasher dishwasher = (TileEntityDishwasher) world.getTileEntity(pos);
		return dishwasher.isWashing() ? 1 : 0;
	}
}
