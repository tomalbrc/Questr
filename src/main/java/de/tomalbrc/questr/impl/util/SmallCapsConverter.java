package de.tomalbrc.questr.impl.util;

import java.util.HashMap;
import java.util.Map;

public class SmallCapsConverter {
    private static final Map<Character, Character> smallCapsMap = new HashMap<>();

    static {
        smallCapsMap.put('a', 'ᴀ');
        smallCapsMap.put('b', 'ʙ');
        smallCapsMap.put('c', 'ᴄ');
        smallCapsMap.put('d', 'ᴅ');
        smallCapsMap.put('e', 'ᴇ');
        smallCapsMap.put('f', 'ꜰ');
        smallCapsMap.put('g', 'ɢ');
        smallCapsMap.put('h', 'ʜ');
        smallCapsMap.put('i', 'ɪ');
        smallCapsMap.put('j', 'ᴊ');
        smallCapsMap.put('k', 'ᴋ');
        smallCapsMap.put('l', 'ʟ');
        smallCapsMap.put('m', 'ᴍ');
        smallCapsMap.put('n', 'ɴ');
        smallCapsMap.put('o', 'ᴏ');
        smallCapsMap.put('p', 'ᴘ');
        smallCapsMap.put('q', 'ǫ');
        smallCapsMap.put('r', 'ʀ');
        smallCapsMap.put('s', 's');
        smallCapsMap.put('t', 'ᴛ');
        smallCapsMap.put('u', 'ᴜ');
        smallCapsMap.put('v', 'ᴠ');
        smallCapsMap.put('w', 'ᴡ');
        smallCapsMap.put('x', 'x');
        smallCapsMap.put('y', 'ʏ');
        smallCapsMap.put('z', 'ᴢ');
    }

    public static String toSmallCaps(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toLowerCase().toCharArray()) {
            if (smallCapsMap.containsKey(c)) {
                result.append(smallCapsMap.get(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
