/*
 * Copyright 2006-2007 (C) Tom Parker <thpr@users.sourceforge.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Current Ver: $Revision$
 * Last Editor: $Author$
 * Last Edited: $Date$
 */
package plugin.lsttokens.spell;

import pcgen.cdom.base.Constants;
import pcgen.cdom.enumeration.StringKey;
import pcgen.cdom.inst.CDOMSpell;
import pcgen.core.spell.Spell;
import pcgen.persistence.lst.SpellLstToken;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import pcgen.util.Logging;

/**
 * Class deals with SPELLRES Token
 */
public class SpellresToken implements SpellLstToken, CDOMPrimaryToken<CDOMSpell>
{

	public String getTokenName()
	{
		return "SPELLRES";
	}

	public boolean parse(Spell spell, String value)
	{
		spell.setSpellResistance(value);
		return true;
	}

	public boolean parse(LoadContext context, CDOMSpell spell, String value)
	{
		if (value.length() == 0)
		{
			Logging.errorPrint(getTokenName() + " may not have empty argument");
			return false;
		}
		context.getObjectContext().put(spell, StringKey.CAN_BE_RESISTED,
			Constants.LST_DOT_CLEAR.equals(value) ? null : value);
		return true;
	}

	public String[] unparse(LoadContext context, CDOMSpell spell)
	{
		String resistable =
				context.getObjectContext().getString(spell,
					StringKey.CAN_BE_RESISTED);
		if (resistable == null)
		{
			return null;
		}
		return new String[]{resistable};
	}

	public Class<CDOMSpell> getTokenClass()
	{
		return CDOMSpell.class;
	}
}
