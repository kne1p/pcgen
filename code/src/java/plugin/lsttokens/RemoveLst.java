/*
 * Created on Sep 2, 2005
 *
 */
package plugin.lsttokens;

import pcgen.core.PObject;
import pcgen.persistence.lst.GlobalLstToken;
import pcgen.persistence.lst.RemoveLoader;
import pcgen.util.Logging;

/**
 * @author djones4
 * 
 */
public class RemoveLst implements GlobalLstToken
{

	public String getTokenName()
	{
		return "REMOVE";
	}

	public boolean parse(PObject obj, String value, int anInt)
	{
		String key;
		if (value.startsWith("FEAT"))
		{
			key = "FEAT";
		}
		else
		{
			Logging
				.errorPrint(getTokenName() + " only supports FEAT: " + value);
			return false;
		}
		int keyLength = key.length();
		if (value.charAt(keyLength) == '(')
		{
			// 514 abbreviation cleanup
			// Logging
			// .errorPrint("REMOVE: syntax with parenthesis is deprecated.");
			// Logging.errorPrint("Please use REMOVE:" + key + "|...");
			if (anInt > -9)
			{
				obj.setRemoveString(anInt + "|" + value);
			}
			else
			{
				obj.setRemoveString("0|" + value);
			}
			return true;
		}
		// Guaranteed new format here
		RemoveLoader.parseLine(obj, key, value.substring(keyLength + 1), anInt);
		return true;
	}
}
