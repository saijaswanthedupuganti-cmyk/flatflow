const fs = require('fs');
let txt = fs.readFileSync('C:/garbage/app/dashboard/expenses/page.tsx', 'utf8');

// In String.replace(), $ in replacement has special meaning — escape with $$
const badBlock = /const CURRENCY_SYMBOLS[\s\S]*?(?=\nfunction|\nconst|\nexport)/;

// Use a replacer function to avoid $ special interpretation
txt = txt.replace(badBlock, () =>
`const CURRENCY_SYMBOLS: Record<Currency, string> = {
  INR: '₹', USD: '$', EUR: '€', GBP: '£', AED: 'AED ', SGD: 'S$', AUD: 'A$',
}

`);

fs.writeFileSync('C:/garbage/app/dashboard/expenses/page.tsx', txt, 'utf8');

const lines = txt.split('\n');
console.log('Line 48:', lines[47]);
console.log('Line 49:', lines[48]);
console.log('Line 50:', lines[49]);
