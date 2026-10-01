const fs = require('fs');
const path = require('path');

const filePath = path.join('C:', 'garbage', 'app', 'dashboard', 'expenses', 'page.tsx');
let txt = fs.readFileSync(filePath, 'utf8');

// The file was read by PowerShell 5.1 as Windows-1252 and written back as UTF-8,
// which double-encodes every non-ASCII byte. We need to reverse that.
// Strategy: decode each "character" as if it were a Windows-1252 byte, rebuild proper UTF-8.

// Windows-1252 to Unicode mapping for the 0x80-0x9F range (otherwise same as Latin-1)
const win1252 = {
  0x80: 0x20AC, 0x82: 0x201A, 0x83: 0x0192, 0x84: 0x201E, 0x85: 0x2026,
  0x86: 0x2020, 0x87: 0x2021, 0x88: 0x02C6, 0x89: 0x2030, 0x8A: 0x0160,
  0x8B: 0x2039, 0x8C: 0x0152, 0x8E: 0x017D, 0x91: 0x2018, 0x92: 0x2019,
  0x93: 0x201C, 0x94: 0x201D, 0x95: 0x2022, 0x96: 0x2013, 0x97: 0x2014,
  0x98: 0x02DC, 0x99: 0x2122, 0x9A: 0x0161, 0x9B: 0x203A, 0x9C: 0x0153,
  0x9E: 0x017E, 0x9F: 0x0178,
};

function win1252ToUnicode(byte) {
  if (byte <= 0x7F) return byte;
  if (win1252[byte] !== undefined) return win1252[byte];
  return byte; // Latin-1 passthrough for 0xA0-0xFF
}

// The corruption pattern: each original UTF-8 byte B was re-encoded as a UTF-8 character.
// If B <= 0x7F: single ASCII char (no corruption)
// If B is 0x80-0xBF or 0xC0-0xFF: appears as the Windows-1252 interpretation of that byte,
//   which is then UTF-8 encoded again.
// To reverse: scan the JS string, for each unicode codepoint C, find which original byte B it came from.

function reverseDoubleEncode(str) {
  // Build a byte array by reversing: each JS char → its Windows-1252 byte value
  const bytes = [];
  for (let i = 0; i < str.length; i++) {
    const cp = str.codePointAt(i);
    if (cp > 0xFFFF) i++; // skip surrogate pairs (shouldn't happen in a corrupted win1252 stream)

    if (cp <= 0x7F) {
      // ASCII - came from an ASCII byte, unchanged
      bytes.push(cp);
    } else {
      // Find which Windows-1252 byte produced this Unicode codepoint
      let found = false;
      // Check 0x80-0x9F range (special Windows-1252 mappings)
      for (const [b, u] of Object.entries(win1252)) {
        if (u === cp) { bytes.push(parseInt(b)); found = true; break; }
      }
      if (!found) {
        // For 0xA0-0xFF: direct Latin-1 (cp === byte value)
        if (cp >= 0xA0 && cp <= 0xFF) {
          bytes.push(cp);
        } else {
          // This codepoint wasn't from a single byte — pass through as-is (UTF-8 encode it)
          const encoded = Buffer.from(String.fromCodePoint(cp), 'utf8');
          for (const b of encoded) bytes.push(b);
        }
      }
    }
  }
  return Buffer.from(bytes);
}

// Only process non-ASCII runs — ASCII portions are fine
// Scan the file for sequences of non-ASCII chars and attempt re-decode
let result = '';
let i = 0;
while (i < txt.length) {
  const cp = txt.codePointAt(i);
  const charLen = cp > 0xFFFF ? 2 : 1;

  if (cp <= 0x7F) {
    result += txt[i];
    i++;
  } else {
    // Collect a run of non-ASCII characters
    let run = '';
    while (i < txt.length) {
      const c = txt.codePointAt(i);
      const cl = c > 0xFFFF ? 2 : 1;
      if (c <= 0x7F) break;
      run += txt.slice(i, i + cl);
      i += cl;
    }
    // Try to reverse-decode this run
    try {
      const buf = reverseDoubleEncode(run);
      const decoded = buf.toString('utf8');
      // Validate: decoded should be valid UTF-8 with reasonable characters
      result += decoded;
    } catch (e) {
      result += run; // keep as-is if we can't decode
    }
  }
}

fs.writeFileSync(filePath, result, 'utf8');
console.log('Done. File length:', result.length);

// Verify a few key spots
const lines = result.split('\n');
console.log('Line 32 (rent emoji):', lines[31]);
console.log('Line 49 (currency):', lines[48]);
