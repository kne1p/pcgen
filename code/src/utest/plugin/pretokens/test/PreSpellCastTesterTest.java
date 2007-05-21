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
package plugin.pretokens.test;

import pcgen.cdom.enumeration.ObjectKey;
import pcgen.cdom.enumeration.Type;
import pcgen.core.Language;
import pcgen.core.PCClass;
import pcgen.core.PObject;
import pcgen.core.prereq.PrerequisiteTest;

public class PreSpellCastTesterTest extends
		AbstractCDOMObjectKeyTestCase<PCClass, Type>
{

	PreSpellCastTester tester = new PreSpellCastTester();

	@Override
	public Class<PCClass> getCDOMClass()
	{
		return PCClass.class;
	}

	@Override
	public Class<? extends PObject> getFalseClass()
	{
		return Language.class;
	}

	@Override
	public String getKind()
	{
		return "spellcast.type";
	}

	@Override
	public PrerequisiteTest getTest()
	{
		return tester;
	}

	@Override
	public Type getFailObject()
	{
		return Type.getConstant("Arcane");
	}

	@Override
	public ObjectKey<Type> getObjectKey()
	{
		return ObjectKey.SPELL_TYPE;
	}

	@Override
	public Type getPassObject()
	{
		return Type.getConstant("Divine");
	}
}
