/*
 * file:       CharsetHelper.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software 2017
 * date:       16/02/2017
 */

/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package net.sf.mpxj.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Commonly used character sets.
 */
public class CharsetHelper
{
   public static final Charset UTF8 = Charset.forName("UTF-8");
   public static final Charset UTF16 = Charset.forName("UTF-16");
   public static final Charset UTF16LE = Charset.forName("UTF-16LE");
   public static final Charset CP1252 = charsetOrFallback(StandardCharsets.ISO_8859_1, "Cp1252", "windows-1252");
   // Some bundled Windows runtimes do not expose a Mac Roman charset at all.
   // Fall back to Cp1252 so import can continue instead of failing at class init.
   public static final Charset MAC_ROMAN = charsetOrFallback(CP1252, "MacRoman", "x-MacRoman");
   public static final Charset CP850 = charsetOrFallback(CP1252, "Cp850", "IBM850");
   public static final Charset CP437 = charsetOrFallback(CP1252, "Cp437", "IBM437");
   public static final Charset GB2312 = charsetOrFallback(CP1252, "GB2312", "GBK");
   public static final Charset CP1251 = charsetOrFallback(CP1252, "Cp1251", "windows-1251");

   private static Charset charsetOrFallback(Charset fallback, String... names)
   {
      for (String name : names)
      {
         try
         {
            return Charset.forName(name);
         }
         catch (Exception ex)
         {
            // Try the next alias.
         }
      }

      return fallback;
   }
}
