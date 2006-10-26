/*
 * ClassDataParser.java
 * Copyright 2006 (C) Aaron Divinsky <boomer70@yahoo.com>
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
 * Last Editor: $Author: $
 * Last Edited: $Date$
 */
package pcgen.core.npcgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pcgen.core.Ability;
import pcgen.core.AbilityCategory;
import pcgen.core.Categorisable;
import pcgen.core.Constants;
import pcgen.core.GameMode;
import pcgen.core.Globals;
import pcgen.core.PCClass;
import pcgen.core.SettingsHandler;
import pcgen.core.Skill;
import pcgen.core.SystemCollections;
import pcgen.util.Logging;
import pcgen.util.enumeration.Visibility;

/**
 * Parse a generator class data file.
 * 
 * @author boomer70 <boomer70@yahoo.com>
 * 
 * @since 5.11.1
 */
public class ClassDataParser
{
	private SAXParser theParser;
	private GameMode theMode;
	
	/**
	 * Creates a new OptionsParser for the specified game mode.
	 * 
	 * @param aMode The game mode to parse options for.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public ClassDataParser(final GameMode aMode) 
		throws ParserConfigurationException, SAXException
	{
		theMode = aMode;
		
		final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(true);
		theParser = parserFactory.newSAXParser();
	}
	
	/**
	 * @param aFileName File to parse.
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public List<ClassData> parse( final File aFileName ) 
		throws SAXException, IOException
	{
		final List<ClassData> ret = new ArrayList<ClassData>();
		
		try
		{
			theParser.parse(aFileName, new ClassDataHandler(theMode, ret));
		}
		catch (IllegalArgumentException ex )
		{
			// Do nothing, means we weren't the right game mode for this file.
		}
		return ret;
	}
}

class ClassDataHandler extends DefaultHandler
{
	private List<ClassData> theList;
	
	private GameMode theGameMode = null;
	private boolean theValidFlag = false;
	
	private enum ParserState { INIT, CLASSDATA, STATDATA, SKILLDATA, ABILITYDATA };
	private ParserState theState = ParserState.INIT;
	
	private ClassData theCurrentData = null;
	
	private AbilityCategory theCurrentCategory = null;

	// Weight for any skills added from *
	private int remainingWeight = -1;
	private List<String> removeList = new ArrayList<String>();
	
	public ClassDataHandler( final GameMode aMode, final List<ClassData> aList )
	{
		theGameMode = aMode;
		theList = aList;
	}
	
	/**
	 * @throws SAXException
	 * @throws IllegalArgumentException if the file being processed is not the
	 * same GameMode as requested.
	 *  
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(	final String uri, final String localName,
								final String aName, final Attributes anAttrs) 
		throws SAXException
	{
		if ( theState == ParserState.INIT && "class_data".equals(aName) ) //$NON-NLS-1$
		{
			if ( anAttrs != null )
			{
				final String gm = anAttrs.getValue("game_mode"); //$NON-NLS-1$
				if ( ! SystemCollections.getGameModeNamed(gm).equals(theGameMode) )
				{
					throw new IllegalArgumentException("Incorrect game mode"); //$NON-NLS-1$
				}
				theValidFlag = true;
			}
			return;
		}
		
		if ( theValidFlag == false )
		{
			throw new SAXException("NPCGen.Options.InvalidFileFormat"); //$NON-NLS-1$
		}
		
		if ( theState == ParserState.INIT )
		{
			if ( "class".equals(aName) ) //$NON-NLS-1$
			{
				if ( anAttrs != null )
				{
					final String classKey = anAttrs.getValue("key"); //$NON-NLS-1$
					final PCClass pcClass = Globals.getClassKeyed(classKey);
					if ( pcClass == null )
					{
						Logging.errorPrintLocalised("Exceptions.PCGenParser.ClassNotFound", classKey); //$NON-NLS-1$
					}
					else
					{
						theCurrentData = new ClassData( classKey );
						theState = ParserState.CLASSDATA;
					}
				}
			}
		}
		else if ( theState == ParserState.CLASSDATA )
		{
			if ( "stats".equals(aName) ) //$NON-NLS-1$
			{
				theState = ParserState.STATDATA;
			}
			else if ( "skills".equals(aName) ) //$NON-NLS-1$
			{
				theState = ParserState.SKILLDATA;
			}
			else if ( "abilities".equals(aName) ) //$NON-NLS-1$
			{
				theState = ParserState.ABILITYDATA;
				theCurrentCategory = AbilityCategory.FEAT;
				if ( anAttrs != null )
				{
					final String catName = anAttrs.getValue("category"); //$NON-NLS-1$
					if ( catName != null )
					{
						theCurrentCategory = SettingsHandler.getGame().getAbilityCategory(catName);
					}
				}
			}
		}
		else if ( theState == ParserState.STATDATA )
		{
			if ( "stat".equals(aName) ) //$NON-NLS-1$
			{
				if ( anAttrs != null )
				{
					final int weight = getWeight(anAttrs);
					final String statAbbr = anAttrs.getValue("value");
					if ( statAbbr != null )
					{
						theCurrentData.addStat(statAbbr, weight);
					}
				}
			}
		}
		else if ( theState == ParserState.SKILLDATA )
		{
			if ( "skill".equals(aName) ) //$NON-NLS-1$
			{
				if ( anAttrs != null )
				{
					final int weight = getWeight(anAttrs);

					final String key = anAttrs.getValue("value"); //$NON-NLS-1$
					if ( key != null )
					{
						if ( "*".equals(key) ) //$NON-NLS-1$
						{
							remainingWeight = weight;
						}
						else if (key.startsWith("TYPE")) //$NON-NLS-1$
						{
							final List<Skill> skillsOfType = Globals.getSkillsByType(key.substring(5));
							if ( skillsOfType.size() == 0 )
							{
								Logging.debugPrint("NPCGenerator: No skills of type found (" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
						else
						{
							final Skill skill = Globals.getSkillKeyed(key);
							if (skill == null)
							{
								Logging.debugPrint("NPCGenerator: Skill not found (" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
						if ( weight > 0 )
						{
							theCurrentData.addSkill(key, weight);
						}
						else
						{
							removeList.add(key);
						}
					}
				}
			}
		}
		else if ( theState == ParserState.ABILITYDATA )
		{
			if ( "ability".equals(aName) ) //$NON-NLS-1$
			{
				if ( anAttrs != null )
				{
					final int weight = getWeight(anAttrs);
					
					final String key = anAttrs.getValue("value"); //$NON-NLS-1$
					if ( key != null )
					{
						if ( "*".equals(key) ) //$NON-NLS-1$
						{
							remainingWeight = weight;
						}
						else if (key.startsWith("TYPE")) //$NON-NLS-1$
						{
							final List<Ability> subFeats = (List<Ability>)Globals.getAbilitiesByType(theCurrentCategory.getAbilityCategory(), key.substring(5));
							for ( final Ability ability : subFeats )
							{
								if (ability.getVisibility() == Visibility.DEFAULT)
								{
									if (weight > 0)
									{
										theCurrentData.addAbility(theCurrentCategory, ability, weight);
									}
									else
									{
										// We have to remove any feats of this
										// type.
										// TODO - This is a little goofy.  We
										// already have the feat but we will 
										// store the key and reretrieve it.
										removeList.add(ability.getKeyName());
									}
								}
							}
						}
						else
						{
							final Ability ability = Globals.getAbilityKeyed(theCurrentCategory.getAbilityCategory(), key);
							if (ability == null)
							{
								Logging.debugPrint("Ability (" + key + ") not found"); //$NON-NLS-1$ //$NON-NLS-2$
							}
							if (weight > 0)
							{
								theCurrentData.addAbility(theCurrentCategory, ability, weight);
							}
							else
							{
								// We have to remove any feats of this
								// type.
								// TODO - This is a little goofy.  We
								// already have the feat but we will 
								// store the key and reretrieve it.
								removeList.add(ability.getKeyName());
							}
						}
					}
				}
			}
		}
	}
	
	public void endElement(final String uri, final String localName, final String qName)
    	throws SAXException
    {
		if ( "skills".equals(qName) && theState == ParserState.SKILLDATA ) //$NON-NLS-1$
		{
			if (remainingWeight > 0)
			{
				// Add all remaining skills at this weight.
				final List<Skill> allSkills = Globals.getSkillList();
				for ( final Skill skill : allSkills )
				{
					if (skill.getVisibility() == Visibility.DEFAULT)
					{
						theCurrentData.addSkill(skill.getKeyName(), remainingWeight);
					}
				}
				remainingWeight = -1;
			}
			for ( final String remove : removeList )
			{
				theCurrentData.removeSkill(remove);
			}
			removeList = new ArrayList<String>();
			theState = ParserState.CLASSDATA;
		}
		else if ( "abilities".equals(qName) && theState == ParserState.ABILITYDATA )
		{
			if ( remainingWeight > 0 )
			{
				// Add all abilities skills at this weight.
				for (Iterator<? extends Categorisable> i = Globals.getAbilityNameIterator(theCurrentCategory.getAbilityCategory()); i.hasNext(); )
				{
					final Ability ability = (Ability)i.next();
					if ( ability.getVisibility() == Visibility.DEFAULT)
					{
						theCurrentData.addAbility(theCurrentCategory, ability, remainingWeight);
					}
				}
				remainingWeight = -1;
			}
			for ( final String remove : removeList )
			{
				final Ability ability = Globals.getAbilityKeyed(theCurrentCategory, remove);
				theCurrentData.removeAbility(theCurrentCategory, ability);
			}
			removeList = new ArrayList<String>();
			theCurrentCategory = null;
			theState = ParserState.CLASSDATA;
		}
		else if ( "class".equals(qName) )
		{
			theList.add(theCurrentData);
			theState = ParserState.INIT;
		}
		else if ( "stats".equals(qName) )
		{
			theState = ParserState.CLASSDATA;
		}
    }

    private int getWeight( final Attributes anAttrs )
	{
		int weight = 1;
		final String wtStr = anAttrs.getValue("weight"); //$NON-NLS-1$
		if ( wtStr != null )
		{
			weight = Integer.parseInt(wtStr);
		}
		return weight;
	}
}
