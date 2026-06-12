const fs = require('fs');
const path = require('path');

const filePath = path.join('C:', 'garbage', 'app', 'dashboard', 'expenses', 'page.tsx');
let txt = fs.readFileSync(filePath, 'utf8');

// These are the mangled UTF-8 sequences produced when PowerShell 5.1 read
// the file as Windows-1252 and wrote it back as UTF-8.
// Each entry: [corrupted string in file, correct Unicode character]
const fixes = [
  // em dash U+2014  (UTF-8: E2 80 94  -> read as â € ")
  ['â€”', '—'],
  // en dash U+2013  (UTF-8: E2 80 93  -> read as â € ")
  ['â€“', '–'],
  // ellipsis U+2026  (UTF-8: E2 80 A6 -> read as â € ¦)
  ['â€¦', '…'],
  // middle dot U+00B7  (UTF-8: C2 B7 -> read as Â ·)
  ['Â·', '·'],
  // rupee sign U+20B9  (UTF-8: E2 82 B9 -> read as â ‚ ¹)
  ['â‚¹', '₹'],
  // euro sign U+20AC  (UTF-8: E2 82 AC -> read as â ‚ ¬)
  ['â‚¬', '€'],
  // pound sign U+00A3  (UTF-8: C2 A3 -> read as Â £)
  ['Â£', '£'],
  // left single quote U+2018  (UTF-8: E2 80 98 -> read as â € ')
  ['â€˜', '‘'],
  // right single quote U+2019  (UTF-8: E2 80 99 -> read as â € ')
  ['â€™', '’'],
  // left double quote U+201C  (UTF-8: E2 80 9C -> read as â € œ)
  ['â€œ', '“'],
  // right double quote U+201D  (UTF-8: E2 80 9D -> read as â € )
  ['â€', '”'],
  // box horizontal U+2500 (UTF-8: E2 94 80 -> read as â " €)
  ['â“€', '─'],
  // bullet U+2022 (UTF-8: E2 80 A2 -> read as â € ¢)
  ['â€¢', '•'],
];

let count = 0;
for (const [bad, good] of fixes) {
  const before = txt.length;
  txt = txt.split(bad).join(good);
  const replaced = (before - txt.length) / (bad.length - good.length);
  if (replaced > 0) count += replaced;
}

fs.writeFileSync(filePath, txt, 'utf8');
console.log('Fixed', count, 'corrupted sequences. File written.');
