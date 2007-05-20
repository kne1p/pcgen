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
package plugin.lsttokens.template;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import pcgen.cdom.base.CDOMReference;
import pcgen.cdom.base.Constants;
import pcgen.cdom.util.ReferenceUtilities;
import pcgen.core.Language;
import pcgen.core.LanguageList;
import pcgen.core.PCTemplate;
import pcgen.persistence.GraphChanges;
import pcgen.persistence.LoadContext;
import pcgen.persistence.lst.AbstractToken;
import pcgen.persistence.lst.PCTemplateLstToken;
import pcgen.persistence.lst.utils.TokenUtilities;
import pcgen.util.Logging;

/**
 * Class deals with LANGBONUS Token
 */
public class LangbonusToken extends AbstractToken implements PCTemplateLstToken
{
	private static final Class<Language> LANGUAGE_CLASS = Language.class;

	private static final Class<LanguageList> LANGUAGELIST_CLASS =
			LanguageList.class;

	@Override
	public String getTokenName()
	{
		return "LANGBONUS";
	}

	public boolean parse(PCTemplate template, String value)
	{
		template.setLanguageBonus(value);
		return true;
	}

	public boolean parse(LoadContext context, PCTemplate template, String value)
	{
		if (isEmpty(value) || hasIllegalSeparator(',', value))
		{
			return false;
		}

		StringTokenizer tok = new StringTokenizer(value, Constants.COMMA);
		boolean removeAll = false;
		boolean foundAny = false;
		boolean foundOther = false;
		boolean firstToken = true;
		List<CDOMReference<Language>> addList =
				new ArrayList<CDOMReference<Language>>();
		List<CDOMReference<Language>> removeList =
				new ArrayList<CDOMReference<Language>>();
		CDOMReference<LanguageList> swl =
				context.ref.getCDOMReference(LANGUAGELIST_CLASS, "*Starting");

		while (tok.hasMoreTokens())
		{
			String tokText = tok.nextToken();
			if (Constants.LST_DOT_CLEAR.equals(tokText))
			{
				if (!firstToken)
				{
					Logging.errorPrint("Non-sensical situation was "
						+ "encountered while parsing " + getTokenName()
						+ ": When used, .CLEAR must be the first argument");
					return false;
				}
				removeAll = true;
			}
			else if (tokText.startsWith(Constants.LST_DOT_CLEAR_DOT))
			{
				CDOMReference<Language> lang;
				String clearText = tokText.substring(7);
				if (Constants.LST_ALL.equals(clearText))
				{
					lang = context.ref.getCDOMAllReference(LANGUAGE_CLASS);
				}
				else
				{
					lang =
							TokenUtilities.getTypeOrPrimitive(context,
								LANGUAGE_CLASS, clearText);
				}
				if (lang == null)
				{
					Logging.errorPrint("  Error was encountered while parsing "
						+ getTokenName() + ": " + value
						+ " had an invalid .CLEAR. reference: " + clearText);
					return false;
				}
				/*
				 * TODO These clears commit changes when it is possible to later
				 * return false :(
				 */
				removeList.add(lang);
			}
			else
			{
				/*
				 * Note this is done one-by-one, because .CLEAR. token type
				 * needs to be able to perform the unlink. That could be
				 * changed, but the increase in complexity isn't worth it.
				 * (Changing it to a grouping object that didn't place links in
				 * the graph would also make it harder to trace the source of
				 * class skills, etc.)
				 */
				CDOMReference<Language> skill;
				if (Constants.LST_ALL.equals(tokText))
				{
					foundAny = true;
					skill = context.ref.getCDOMAllReference(LANGUAGE_CLASS);
				}
				else
				{
					foundOther = true;
					skill =
							TokenUtilities.getTypeOrPrimitive(context,
								LANGUAGE_CLASS, tokText);
				}
				if (skill == null)
				{
					Logging.errorPrint("  Error was encountered while parsing "
						+ getTokenName() + ": " + value
						+ " had an invalid reference: " + tokText);
					return false;
				}
				addList.add(skill);
			}
			firstToken = false;
		}
		if (foundAny && foundOther)
		{
			Logging.errorPrint("Non-sensical " + getTokenName()
				+ ": Contains ANY and a specific reference: " + value);
			return false;
		}
		if (removeAll)
		{
			context.list.removeFromList(getTokenName(), template, swl,
				LANGUAGE_CLASS);
		}
		if (!removeList.isEmpty())
		{
			for (CDOMReference<Language> lang : removeList)
			{
				context.list
					.removeFromList(getTokenName(), template, swl, lang);
			}
		}
		if (!addList.isEmpty())
		{
			for (CDOMReference<Language> lang : addList)
			{
				context.list.addToList(getTokenName(), template, swl, lang);
			}
		}
		return true;
	}

	public String[] unparse(LoadContext context, PCTemplate pct)
	{
		CDOMReference<LanguageList> swl =
				context.ref.getCDOMReference(LANGUAGELIST_CLASS, "*Starting");
		GraphChanges<Language> changes =
				context.list.getChangesInList(getTokenName(), pct, swl);
		if (changes == null)
		{
			// Legal if no Language was present in the race
			return null;
		}
		List<String> list = new ArrayList<String>();
		if (changes.hasRemovedItems())
		{
			if (changes.includesGlobalClear())
			{
				context.addWriteMessage("Non-sensical relationship in "
					+ getTokenName()
					+ ": global .CLEAR and local .CLEAR. performed");
				return null;
			}
			list.add(Constants.LST_DOT_CLEAR_DOT
				+ ReferenceUtilities.joinLstFormat(changes.getRemoved(),
					",|.CLEAR."));
		}
		if (changes.includesGlobalClear())
		{
			list.add(Constants.LST_DOT_CLEAR);
		}
		if (changes.hasAddedItems())
		{
			list.add(ReferenceUtilities.joinLstFormat(changes.getAdded(),
				Constants.COMMA));
		}
		return list.toArray(new String[list.size()]);
	}
}
