/*
 * Copyright (c) 2007 Tom Parker <thpr@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package plugin.lsttokens;

import org.junit.Test;

import pcgen.core.PCTemplate;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.GlobalLstToken;
import pcgen.persistence.lst.LstObjectFileLoader;
import pcgen.persistence.lst.PCTemplateLoader;
import plugin.lsttokens.testsupport.AbstractGlobalTokenTestCase;

public class UnencumberedmoveLstTest extends AbstractGlobalTokenTestCase
{

	static GlobalLstToken token = new UnencumberedmoveLst();
	static PCTemplateLoader loader = new PCTemplateLoader();

	@Override
	public LstObjectFileLoader<PCTemplate> getLoader()
	{
		return loader;
	}

	@Override
	public Class<PCTemplate> getCDOMClass()
	{
		return PCTemplate.class;
	}

	@Override
	public GlobalLstToken getToken()
	{
		return token;
	}

	@Test
	public void testInvalidInputPipeOnly() throws PersistenceLayerException
	{
		assertFalse(parse("|"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputRandomString() throws PersistenceLayerException
	{
		assertFalse(parse("String"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputEndPipe() throws PersistenceLayerException
	{
		assertFalse(parse("HeavyLoad|"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputStartPipe() throws PersistenceLayerException
	{
		assertFalse(parse("|HeavyLoad"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputDoublePipe() throws PersistenceLayerException
	{
		assertFalse(parse("HeavyLoad||HeavyArmor"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputDoubleLoad() throws PersistenceLayerException
	{
		assertFalse(parse("HeavyLoad|MediumLoad"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputDoubleArmor() throws PersistenceLayerException
	{
		assertFalse(parse("MediumArmor|HeavyArmor"));
		assertNoSideEffects();
	}

	@Test
	public void testValidInputDoubleArmor() throws PersistenceLayerException
	{
		assertTrue(parse("LightArmor"));
	}

	@Test
	public void testRoundRobinLightLoad() throws PersistenceLayerException
	{
		runRoundRobin("LightLoad");
	}

	@Test
	public void testRoundRobinArmor() throws PersistenceLayerException
	{
		runRoundRobin("HeavyArmor");
	}

	@Test
	public void testRoundRobinLoad() throws PersistenceLayerException
	{
		runRoundRobin("HeavyLoad");
	}

	@Test
	public void testRoundRobinLoadArmor() throws PersistenceLayerException
	{
		runRoundRobin("HeavyLoad|MediumArmor");
	}

}
