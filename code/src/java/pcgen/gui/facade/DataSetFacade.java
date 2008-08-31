/*
 * DataSetFacade.java
 * Copyright 2008 Connor Petty <cpmeister@users.sourceforge.net>
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
 * Created on Aug 19, 2008, 3:31:30 PM
 */
package pcgen.gui.facade;

import java.util.List;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public interface DataSetFacade
{

    public List<AbilityFacade> getAbilities(AbilityCatagoryFacade catagory);

    public List<AbilityCatagoryFacade> getAbilityCatagories();

    public List<SkillFacade> getSkills();

    public List<RaceFacade> getRaces();

    public List<ClassFacade> getClasses();

    public List<TempBonusFacade> getTempBonuses();

    public List<TemplateFacade> getTemplates();

}
