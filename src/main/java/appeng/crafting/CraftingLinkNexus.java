/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.crafting;


import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.me.cache.CraftingGridCache;


public class CraftingLinkNexus
{

	public final String CraftID;
	boolean canceled = false;
	boolean done = false;
	int tickOfDeath = 0;
	CraftingLink req;
	CraftingLink cpu;

	public CraftingLinkNexus( final String craftID )
	{
		this.CraftID = craftID;
	}

	public boolean isDead( final IGrid g, final CraftingGridCache craftingGridCache )
	{
		if( this.canceled || this.done )
		{
			return true;
		}

		if( this.req == null || this.cpu == null )
		{
			this.tickOfDeath++;
		}
		else
		{
			final boolean hasCpu = craftingGridCache.hasCpu( this.cpu.cpu );
			final boolean hasMachine = this.req.req.getActionableNode().getGrid() == g;

			if( hasCpu && hasMachine )
			{
				this.tickOfDeath = 0;
			}
			else
			{
				this.tickOfDeath += 60;
			}
		}

		if( this.tickOfDeath > 60 )
		{
			this.cancel();
			return true;
		}

		return false;
	}

	public void cancel()
	{
		this.canceled = true;

		if( this.req != null )
		{
			this.req.canceled = true;
			if( this.req.req != null )
			{
				this.req.req.jobStateChange( this.req );
			}
		}

		if( this.cpu != null )
		{
			this.cpu.canceled = true;
		}
	}

	public void remove( final CraftingLink craftingLink )
	{
		if( this.req == craftingLink )
		{
			this.req = null;
		}
		else if( this.cpu == craftingLink )
		{
			this.cpu = null;
		}
	}

	public void add( final CraftingLink craftingLink )
	{
		if( craftingLink.cpu != null )
		{
			this.cpu = craftingLink;
		}
		else if( craftingLink.req != null )
		{
			this.req = craftingLink;
		}
	}

	public boolean isCanceled()
	{
		return this.canceled;
	}

	public boolean isDone()
	{
		return this.done;
	}

	public void markDone()
	{
		this.done = true;

		if( this.req != null )
		{
			this.req.done = true;
			if( this.req.req != null )
			{
				this.req.req.jobStateChange( this.req );
			}
		}

		if( this.cpu != null )
		{
			this.cpu.done = true;
		}
	}

	public boolean isMachine( final IGridHost machine )
	{
		return this.req == machine;
	}

	public void removeNode()
	{
		if( this.req != null )
		{
			this.req.setNexus( null );
		}

		this.req = null;
		this.tickOfDeath = 0;
	}
}
