# Habitiq Voice Assistant — Use Case Corpus v1.0

> **Document Type:** Training & QA Corpus  
> **Purpose:** 1000+ voice command examples, system prompts, and response patterns for the Habitiq Voice Assistant  
> **Language Coverage:** English (Indian), Hinglish, Telugu-English code-switching  
> **For:** Claude Code — NLU training data, test cases, and response templates  

---

## Table of Contents

1. [Intent: COMPLETE_TASK](#1-intent-complete_task)
2. [Intent: CREATE_EXPENSE](#2-intent-create_expense)
3. [Intent: QUERY_BALANCE](#3-intent-query_balance)
4. [Intent: QUERY_TASKS](#4-intent-query_tasks)
5. [Intent: QUERY_STATUS](#5-intent-query_status)
6. [Intent: REQUEST_SWAP](#6-intent-request_swap)
7. [Intent: CREATE_TASK](#7-intent-create_task)
8. [Intent: GREETING](#8-intent-greeting)
9. [Intent: UNKNOWN / FALLBACK](#9-intent-unknown--fallback)
10. [Multi-Intent Sequences](#10-multi-intent-sequences)
11. [Error Recovery Dialogues](#11-error-recovery-dialogues)
12. [Response Template Library](#12-response-template-library)

---

## 1. Intent: COMPLETE_TASK

### 1.1 Direct Completion (100+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 1 | "Kitchen done" | Mark kitchen task complete | "Kitchen marked done. Great work!" |
| 2 | "I did the kitchen" | Mark kitchen task complete | "Kitchen marked done. Nice!" |
| 3 | "Bathroom cleaning finished" | Mark bathroom task complete | "Bathroom cleaning marked done." |
| 4 | "Finished sweeping" | Mark sweeping task complete | "Sweeping marked done." |
| 5 | "Garbage is out" | Mark garbage task complete | "Garbage marked done. Thanks!" |
| 6 | "Done with dishes" | Mark dishes task complete | "Dishes marked done." |
| 7 | "I completed the laundry" | Mark laundry task complete | "Laundry marked done." |
| 8 | "Dusting over" | Mark dusting task complete | "Dusting marked done." |
| 9 | "Cooking done for today" | Mark cooking task complete | "Cooking marked done." |
| 10 | "Mopping finished" | Mark mopping task complete | "Mopping marked done." |
| 11 | "I did it" | Mark most recent assigned task | "Your most recent task marked done." |
| 12 | "Task complete" | Ask which task | "Which task? Say the name, like kitchen or bathroom." |
| 13 | "It's done" | Ask which task | "Which one? Kitchen, bathroom, or something else?" |
| 14 | "All done" | List completed tasks today | "You have completed 3 tasks today. Well done!" |
| 15 | "Marked complete" | Ask which task | "Which task did you complete?" |
| 16 | "I finished my duty" | Ask which duty | "Which duty? Say the task name." |
| 17 | "Over and done" | Ask which task | "What did you finish?" |
| 18 | "Wrapped it up" | Ask which task | "What did you wrap up?" |
| 19 | "Nip it in the bud" | Unknown | "I didn't catch that. Which task?" |
| 20 | "Checked off" | Ask which task | "What did you check off?" |
| 21 | "Kitchen cleaning done" | Mark kitchen task complete | "Kitchen cleaning marked done." |
| 22 | "I cleaned the bathroom" | Mark bathroom task complete | "Bathroom cleaning marked done." |
| 23 | "Toilet cleaned" | Mark bathroom/toilet task complete | "Toilet cleaning marked done." |
| 24 | "Washed the dishes" | Mark dishes task complete | "Dishes marked done." |
| 25 | "Clothes washed" | Mark laundry task complete | "Laundry marked done." |
| 26 | "Took out the trash" | Mark garbage task complete | "Garbage marked done." |
| 27 | "Trash is out" | Mark garbage task complete | "Garbage marked done." |
| 28 | "Floor is mopped" | Mark mopping task complete | "Mopping marked done." |
| 29 | "Swiped the floor" | Mark mopping task complete | "Mopping marked done." |
| 30 | "Cleaned the hall" | Mark hall cleaning task complete | "Hall cleaning marked done." |
| 31 | "Living room done" | Mark living room task complete | "Living room marked done." |
| 32 | "Balcony cleaned" | Mark balcony task complete | "Balcony marked done." |
| 33 | "Windows cleaned" | Mark window cleaning task complete | "Window cleaning marked done." |
| 34 | "I watered the plants" | Mark plant watering task complete | "Plant watering marked done." |
| 35 | "Grocery shopping done" | Mark grocery task complete | "Grocery shopping marked done." |
| 36 | "Bought groceries" | Mark grocery task complete | "Groceries marked done." |
| 37 | "Milk bought" | Mark milk task complete | "Milk run marked done." |
| 38 | "Vegetables purchased" | Mark vegetable task complete | "Vegetables marked done." |
| 39 | "I did the cooking today" | Mark cooking task complete | "Cooking marked done." |
| 40 | "Made dinner" | Mark cooking task complete | "Dinner cooking marked done." |
| 41 | "Breakfast is ready" | Mark cooking task complete | "Breakfast cooking marked done." |
| 42 | "Lunch prepared" | Mark cooking task complete | "Lunch cooking marked done." |
| 43 | "I ironed the clothes" | Mark ironing task complete | "Ironing marked done." |
| 44 | "Pressed the shirts" | Mark ironing task complete | "Ironing marked done." |
| 45 | "I organized the cupboard" | Mark organization task complete | "Cupboard organization marked done." |
| 46 | "Fridge cleaned" | Mark fridge cleaning task complete | "Fridge cleaning marked done." |
| 47 | "Microwave cleaned" | Mark microwave task complete | "Microwave cleaning marked done." |
| 48 | "Stove wiped" | Mark stove cleaning task complete | "Stove cleaning marked done." |
| 49 | "I cleaned the fans" | Mark fan cleaning task complete | "Fan cleaning marked done." |
| 50 | "Tube lights changed" | Mark tube light task complete | "Tube light change marked done." |
| 51 | "I paid the electricity bill" | Mark electricity bill task complete | "Electricity bill marked done." |
| 52 | "WiFi bill paid" | Mark WiFi bill task complete | "WiFi bill marked done." |
| 53 | "Rent paid" | Mark rent payment task complete | "Rent payment marked done." |
| 54 | "Gas cylinder booked" | Mark gas booking task complete | "Gas booking marked done." |
| 55 | "I called the plumber" | Mark plumber task complete | "Plumber call marked done." |
| 56 | "Electrician visited" | Mark electrician task complete | "Electrician visit marked done." |
| 57 | "I did the dusting today" | Mark dusting task complete | "Dusting marked done." |
| 58 | "Cobwebs removed" | Mark cobweb removal task complete | "Cobweb removal marked done." |
| 59 | "I vacuumed the carpet" | Mark vacuuming task complete | "Vacuuming marked done." |
| 60 | "Carpet cleaned" | Mark carpet cleaning task complete | "Carpet cleaning marked done." |
| 61 | "Shoe rack organized" | Mark shoe rack task complete | "Shoe rack marked done." |
| 62 | "I cleaned the mirrors" | Mark mirror cleaning task complete | "Mirror cleaning marked done." |
| 63 | "Door mats washed" | Mark door mat task complete | "Door mats marked done." |
| 64 | "Curtains changed" | Mark curtain task complete | "Curtains marked done." |
| 65 | "Bed sheets changed" | Mark bed sheet task complete | "Bed sheets marked done." |
| 66 | "Pillows fluffed" | Mark pillow task complete | "Pillows marked done." |
| 67 | "I cleaned the AC filters" | Mark AC filter task complete | "AC filters marked done." |
| 68 | "AC service done" | Mark AC service task complete | "AC service marked done." |
| 69 | "Water filter cleaned" | Mark water filter task complete | "Water filter marked done." |
| 70 | "Aquaguard serviced" | Mark water filter task complete | "Water filter service marked done." |
| 71 | "I did the puja room cleaning" | Mark puja room task complete | "Puja room cleaning marked done." |
| 72 | "Temple area cleaned" | Mark puja room task complete | "Puja room marked done." |
| 73 | "I cleaned the study table" | Mark study table task complete | "Study table marked done." |
| 74 | "Bookshelf organized" | Mark bookshelf task complete | "Bookshelf marked done." |
| 75 | "I threw the old newspapers" | Mark newspaper task complete | "Newspapers marked done." |
| 76 | "I segregated the waste" | Mark waste segregation task complete | "Waste segregation marked done." |
| 77 | "Dry waste separated" | Mark waste task complete | "Waste separation marked done." |
| 78 | "Wet waste handled" | Mark waste task complete | "Wet waste marked done." |
| 79 | "I cleaned the water cans" | Mark water can task complete | "Water cans marked done." |
| 80 | "Bisleri bottles returned" | Mark bottle return task complete | "Bottle return marked done." |
| 81 | "I bought the toiletries" | Mark toiletries task complete | "Toiletries marked done." |
| 82 | "Soap and shampoo bought" | Mark toiletries task complete | "Toiletries marked done." |
| 83 | "Detergent purchased" | Mark detergent task complete | "Detergent marked done." |
| 84 | "I cleaned the shower" | Mark shower cleaning task complete | "Shower cleaning marked done." |
| 85 | "Tiles scrubbed" | Mark tile cleaning task complete | "Tile cleaning marked done." |
| 86 | "Grout cleaned" | Mark grout cleaning task complete | "Grout cleaning marked done." |
| 87 | "I did the pest control" | Mark pest control task complete | "Pest control marked done." |
| 88 | "Cockroach spray done" | Mark pest control task complete | "Pest control marked done." |
| 89 | "Mosquito repellent refilled" | Mark mosquito task complete | "Mosquito repellent marked done." |
| 90 | "I cleaned the aquarium" | Mark aquarium task complete | "Aquarium cleaning marked done." |
| 91 | "Fish tank maintained" | Mark aquarium task complete | "Aquarium marked done." |
| 92 | "I walked the dog" | Mark dog walking task complete | "Dog walking marked done." |
| 93 | "Pet fed" | Mark pet feeding task complete | "Pet feeding marked done." |
| 94 | "Cat litter cleaned" | Mark pet task complete | "Cat litter marked done." |
| 95 | "I did the car wash" | Mark car wash task complete | "Car wash marked done." |
| 96 | "Bike serviced" | Mark bike service task complete | "Bike service marked done." |
| 97 | "I filled the drinking water" | Mark water filling task complete | "Water filling marked done." |
| 98 | "Water cans refilled" | Mark water filling task complete | "Water cans marked done." |
| 99 | "I cleaned the doormats" | Mark doormat task complete | "Doormats marked done." |
| 100 | "All chores done for today" | Mark all today's tasks | "All 3 of today's tasks marked done. Great job!" |

### 1.2 Hinglish / Code-Switching (50+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 101 | "Kitchen ho gaya" | Mark kitchen complete | "Kitchen ho gaya. Accha kaam!" |
| 102 | "Bathroom kar diya" | Mark bathroom complete | "Bathroom kar diya." |
| 103 | "Bartan dho liye" | Mark dishes complete | "Bartan dho liye." |
| 104 | "Jhadu lag gayi" | Mark sweeping complete | "Jhadu lag gayi." |
| 105 | "Pocha lag gaya" | Mark mopping complete | "Poicha lag gaya." |
| 106 | "Kapde dho liye" | Mark laundry complete | "Kapde dho liye." |
| 107 | "Kachra phenk diya" | Mark garbage complete | "Kachra phenk diya." |
| 108 | "Safai ho gayi" | Ask which cleaning | "Kaunsi safai? Kitchen, bathroom?" |
| 109 | "Kaam complete ho gaya" | Ask which task | "Kaunsa kaam?" |
| 110 | "Maine kitchen kar diya" | Mark kitchen complete | "Kitchen kar diya." |
| 111 | "Aaj ka kaam ho gaya" | Mark all today's tasks | "Aaj ke saare kaam ho gaye." |
| 112 | "Dusting kar di" | Mark dusting complete | "Dusting kar di." |
| 113 | "Fan saaf kar diya" | Mark fan cleaning complete | "Fan saaf kar diya." |
| 114 | "Window saaf kar di" | Mark window cleaning complete | "Window saaf kar di." |
| 115 | "Bathroom saaf ho gaya" | Mark bathroom complete | "Bathroom saaf ho gaya." |
| 116 | "Toilet saaf kar diya" | Mark toilet complete | "Toilet saaf kar diya." |
| 117 | "Hall jhadu lag gayi" | Mark hall sweeping complete | "Hall jhadu lag gayi." |
| 118 | "Balcony saaf kar di" | Mark balcony complete | "Balcony saaf kar di." |
| 119 | "Fridge saaf kar diya" | Mark fridge cleaning complete | "Fridge saaf kar diya." |
| 120 | "Stove saaf kar di" | Mark stove cleaning complete | "Stove saaf kar di." |
| 121 | "Microwave saaf kar diya" | Mark microwave complete | "Microwave saaf kar diya." |
| 122 | "Curtains change kar diye" | Mark curtains complete | "Curtains change kar diye." |
| 123 | "Bedsheet change kar di" | Mark bedsheet complete | "Bedsheet change kar di." |
| 124 | "Pillow cover change kar diya" | Mark pillow cover complete | "Pillow cover change kar diya." |
| 125 | "AC filter saaf kar diya" | Mark AC filter complete | "AC filter saaf kar diya." |
| 126 | "Water filter saaf kar diya" | Mark water filter complete | "Water filter saaf kar diya." |
| 127 | "Aquaguard service ho gayi" | Mark water filter service complete | "Aquaguard service ho gayi." |
| 128 | "Puja ghar saaf kar diya" | Mark puja room complete | "Puja ghar saaf kar diya." |
| 129 | "Study table saaf kar di" | Mark study table complete | "Study table saaf kar di." |
| 130 | "Bookshelf set kar di" | Mark bookshelf complete | "Bookshelf set kar di." |
| 131 | "Newspaper phenk diya" | Mark newspaper complete | "Newspaper phenk diya." |
| 132 | "Waste segregate kar diya" | Mark waste segregation complete | "Waste segregate kar diya." |
| 133 | "Dry waste alag kar diya" | Mark dry waste complete | "Dry waste alag kar diya." |
| 134 | "Wet waste alag kar diya" | Mark wet waste complete | "Wet waste alag kar diya." |
| 135 | "Water can refill kar diya" | Mark water refill complete | "Water can refill kar diya." |
| 136 | "Bisleri bottle return kar di" | Mark bottle return complete | "Bisleri bottle return kar di." |
| 137 | "Toiletries le aaya" | Mark toiletries complete | "Toiletries le aaya." |
| 138 | "Soap shampoo le aaya" | Mark toiletries complete | "Soap shampoo le aaya." |
| 139 | "Detergent le aaya" | Mark detergent complete | "Detergent le aaya." |
| 140 | "Shower saaf kar diya" | Mark shower cleaning complete | "Shower saaf kar diya." |
| 141 | "Tiles saaf kar di" | Mark tile cleaning complete | "Tiles saaf kar di." |
| 142 | "Pest control kar diya" | Mark pest control complete | "Pest control kar diya." |
| 143 | "Cockroach spray kar diya" | Mark pest control complete | "Cockroach spray kar diya." |
| 144 | "Mosquito refill kar diya" | Mark mosquito refill complete | "Mosquito refill kar diya." |
| 145 | "Aquarium saaf kar diya" | Mark aquarium complete | "Aquarium saaf kar diya." |
| 146 | "Dog walk kar diya" | Mark dog walking complete | "Dog walk kar diya." |
| 147 | "Pet ko khana diya" | Mark pet feeding complete | "Pet ko khana diya." |
| 148 | "Cat litter saaf kar diya" | Mark cat litter complete | "Cat litter saaf kar diya." |
| 149 | "Car wash kar diya" | Mark car wash complete | "Car wash kar diya." |
| 150 | "Bike service ho gayi" | Mark bike service complete | "Bike service ho gayi." |

### 1.3 Telugu-English (25+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 151 | "Kitchen aipoyindi" | Mark kitchen complete | "Kitchen aipoyindi. Bagundi!" |
| 152 | "Bathroom chesesa" | Mark bathroom complete | "Bathroom chesesa." |
| 153 | "Bartanlu kadigesa" | Mark dishes complete | "Bartanlu kadigesa." |
| 154 | "Jhadu esesa" | Mark sweeping complete | "Jhadu esesa." |
| 155 | "Mop chesesa" | Mark mopping complete | "Mop chesesa." |
| 156 | "Bath chesesa" | Mark bathing complete | "Bath chesesa." |
| 157 | "Dust chesesa" | Mark dusting complete | "Dust chesesa." |
| 158 | "Clothes wash chesesa" | Mark laundry complete | "Clothes wash chesesa." |
| 159 | "Dustbin clean chesesa" | Mark garbage complete | "Dustbin clean chesesa." |
| 160 | "Room clean aipoyindi" | Ask which room | "Which room?" |
| 161 | "Work aipoyindi" | Ask which task | "Which work?" |
| 162 | "Kitchen work aipoyindi" | Mark kitchen complete | "Kitchen work aipoyindi." |
| 163 | "Bathroom work chesesa" | Mark bathroom complete | "Bathroom work chesesa." |
| 164 | "Sweeping aipoyindi" | Mark sweeping complete | "Sweeping aipoyindi." |
| 165 | "Mopping aipoyindi" | Mark mopping complete | "Mopping aipoyindi." |
| 166 | "Dusting aipoyindi" | Mark dusting complete | "Dusting aipoyindi." |
| 167 | "Garbage aipoyindi" | Mark garbage complete | "Garbage aipoyindi." |
| 168 | "Dishes aipoyindi" | Mark dishes complete | "Dishes aipoyindi." |
| 169 | "Laundry aipoyindi" | Mark laundry complete | "Laundry aipoyindi." |
| 170 | "Cooking aipoyindi" | Mark cooking complete | "Cooking aipoyindi." |
| 171 | "Cleaning aipoyindi" | Ask which cleaning | "Which cleaning?" |
| 172 | "Morning work aipoyindi" | Ask which morning task | "Which morning work?" |
| 173 | "Evening work aipoyindi" | Ask which evening task | "Which evening work?" |
| 174 | "All work aipoyindi" | Mark all tasks | "All work aipoyindi. Great!" |
| 175 | "Task complete aipoyindi" | Ask which task | "Which task?" |

### 1.4 Ambiguous / Disambiguation (25+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 176 | "Done" | Ask which task | "Which task did you complete?" |
| 177 | "Finished" | Ask which task | "What did you finish?" |
| 178 | "Complete" | Ask which task | "What is complete?" |
| 179 | "It's over" | Ask which task | "What is over?" |
| 180 | "I did it" | Ask which task | "What did you do?" |
| 181 | "Marked" | Ask which task | "What did you mark?" |
| 182 | "Checked" | Ask which task | "What did you check?" |
| 183 | "Cleared" | Ask which task | "What did you clear?" |
| 184 | "Handled" | Ask which task | "What did you handle?" |
| 185 | "Managed" | Ask which task | "What did you manage?" |
| 186 | "Sorted" | Ask which task | "What did you sort?" |
| 187 | "Resolved" | Ask which task | "What did you resolve?" |
| 188 | "Took care of it" | Ask which task | "What did you take care of?" |
| 189 | "Got it done" | Ask which task | "What did you get done?" |
| 190 | "Knocked it out" | Ask which task | "What did you knock out?" |
| 191 | "Nailed it" | Ask which task | "What did you nail?" |
| 192 | "Crushed it" | Ask which task | "What did you crush?" |
| 193 | "Banged it out" | Ask which task | "What did you bang out?" |
| 194 | "Polished it off" | Ask which task | "What did you polish off?" |
| 195 | "Wrapped it" | Ask which task | "What did you wrap?" |
| 196 | "Sealed it" | Ask which task | "What did you seal?" |
| 197 | "Locked it down" | Ask which task | "What did you lock down?" |
| 198 | "Smashed it" | Ask which task | "What did you smash?" |
| 199 | "Killed it" | Ask which task | "What did you kill?" |
| 200 | "Destroyed it" | Ask which task | "What did you destroy?" |

---

## 2. Intent: CREATE_EXPENSE

### 2.1 Direct Expense Creation (100+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 201 | "I spent 500 on groceries" | Add ₹500 grocery expense, split equally | "Added ₹500 for groceries. Split equally." |
| 202 | "Add 300 for dinner" | Add ₹300 dinner expense, split equally | "Added ₹300 for dinner. Split equally." |
| 203 | "I paid 1200 for electricity" | Add ₹1200 electricity expense, split equally | "Added ₹1200 for electricity. Split equally." |
| 204 | "Spent 2000 on rent" | Add ₹2000 rent expense | "Added ₹2000 for rent." |
| 205 | "I bought vegetables for 250" | Add ₹250 vegetable expense | "Added ₹250 for vegetables." |
| 206 | "Add expense 500 Netflix" | Add ₹500 Netflix expense | "Added ₹500 for Netflix." |
| 207 | "I paid 150 for milk" | Add ₹150 milk expense | "Added ₹150 for milk." |
| 208 | "Spent 800 on petrol" | Add ₹800 petrol expense | "Added ₹800 for petrol." |
| 209 | "Add 600 for cleaning supplies" | Add ₹600 cleaning supplies expense | "Added ₹600 for cleaning supplies." |
| 210 | "I paid 400 for WiFi" | Add ₹400 WiFi expense | "Added ₹400 for WiFi." |
| 211 | "Spent 350 on snacks" | Add ₹350 snacks expense | "Added ₹350 for snacks." |
| 212 | "Add 1000 for house maintenance" | Add ₹1000 maintenance expense | "Added ₹1000 for house maintenance." |
| 213 | "I bought toilet paper for 180" | Add ₹180 toilet paper expense | "Added ₹180 for toilet paper." |
| 214 | "Spent 500 on Uber" | Add ₹500 Uber expense | "Added ₹500 for Uber." |
| 215 | "Add 1200 for gas cylinder" | Add ₹1200 gas cylinder expense | "Added ₹1200 for gas cylinder." |
| 216 | "I paid 600 for plumber" | Add ₹600 plumber expense | "Added ₹600 for plumber." |
| 217 | "Spent 250 on chai" | Add ₹250 chai expense | "Added ₹250 for chai." |
| 218 | "Add 1500 for groceries today" | Add ₹1500 grocery expense | "Added ₹1500 for groceries." |
| 219 | "I bought rice and dal for 400" | Add ₹400 rice and dal expense | "Added ₹400 for rice and dal." |
| 220 | "Spent 300 on bread and eggs" | Add ₹300 bread and eggs expense | "Added ₹300 for bread and eggs." |
| 221 | "Add 800 for chicken" | Add ₹800 chicken expense | "Added ₹800 for chicken." |
| 222 | "I paid 500 for fish" | Add ₹500 fish expense | "Added ₹500 for fish." |
| 223 | "Spent 200 on fruits" | Add ₹200 fruits expense | "Added ₹200 for fruits." |
| 224 | "Add 450 for curd and paneer" | Add ₹450 curd and paneer expense | "Added ₹450 for curd and paneer." |
| 225 | "I bought oil and spices for 600" | Add ₹600 oil and spices expense | "Added ₹600 for oil and spices." |
| 226 | "Spent 350 on detergent and soap" | Add ₹350 detergent expense | "Added ₹350 for detergent and soap." |
| 227 | "Add 100 for water cans" | Add ₹100 water can expense | "Added ₹100 for water cans." |
| 228 | "I paid 50 for Bisleri" | Add ₹50 Bisleri expense | "Added ₹50 for Bisleri." |
| 229 | "Spent 1200 on Swiggy" | Add ₹1200 Swiggy expense | "Added ₹1200 for Swiggy." |
| 230 | "Add 800 for Zomato" | Add ₹800 Zomato expense | "Added ₹800 for Zomato." |
| 231 | "I bought biryani for 400" | Add ₹400 biryani expense | "Added ₹400 for biryani." |
| 232 | "Spent 250 on pizza" | Add ₹250 pizza expense | "Added ₹250 for pizza." |
| 233 | "Add 500 for movie tickets" | Add ₹500 movie ticket expense | "Added ₹500 for movie tickets." |
| 234 | "I paid 200 for auto" | Add ₹200 auto expense | "Added ₹200 for auto." |
| 235 | "Spent 1500 on Metro recharge" | Add ₹1500 Metro recharge expense | "Added ₹1500 for Metro recharge." |
| 236 | "Add 300 for phone recharge" | Add ₹300 phone recharge expense | "Added ₹300 for phone recharge." |
| 237 | "I bought a bulb for 80" | Add ₹80 bulb expense | "Added ₹80 for bulb." |
| 238 | "Spent 200 on tube light" | Add ₹200 tube light expense | "Added ₹200 for tube light." |
| 239 | "Add 500 for fan repair" | Add ₹500 fan repair expense | "Added ₹500 for fan repair." |
| 240 | "I paid 300 for AC service" | Add ₹300 AC service expense | "Added ₹300 for AC service." |
| 241 | "Spent 150 on mosquito coil" | Add ₹150 mosquito coil expense | "Added ₹150 for mosquito coil." |
| 242 | "Add 100 for room freshener" | Add ₹100 room freshener expense | "Added ₹100 for room freshener." |
| 243 | "I bought a mop for 250" | Add ₹250 mop expense | "Added ₹250 for mop." |
| 244 | "Spent 180 on broom" | Add ₹180 broom expense | "Added ₹180 for broom." |
| 245 | "Add 400 for dustbins" | Add ₹400 dustbin expense | "Added ₹400 for dustbins." |
| 246 | "I paid 1200 for mattress cover" | Add ₹1200 mattress cover expense | "Added ₹1200 for mattress cover." |
| 247 | "Spent 800 on curtains" | Add ₹800 curtains expense | "Added ₹800 for curtains." |
| 248 | "Add 600 for pillow covers" | Add ₹600 pillow cover expense | "Added ₹600 for pillow covers." |
| 249 | "I bought a bucket for 150" | Add ₹150 bucket expense | "Added ₹150 for bucket." |
| 250 | "Spent 200 on mugs" | Add ₹200 mugs expense | "Added ₹200 for mugs." |
| 251 | "Add 350 for plates" | Add ₹350 plates expense | "Added ₹350 for plates." |
| 252 | "I paid 500 for glasses" | Add ₹500 glasses expense | "Added ₹500 for glasses." |
| 253 | "Spent 1200 on induction stove" | Add ₹1200 induction stove expense | "Added ₹1200 for induction stove." |
| 254 | "Add 800 for mixer grinder" | Add ₹800 mixer grinder expense | "Added ₹800 for mixer grinder." |
| 255 | "I bought a kettle for 600" | Add ₹600 kettle expense | "Added ₹600 for kettle." |
| 256 | "Spent 400 on toaster" | Add ₹400 toaster expense | "Added ₹400 for toaster." |
| 257 | "Add 1500 for fridge repair" | Add ₹1500 fridge repair expense | "Added ₹1500 for fridge repair." |
| 258 | "I paid 200 for washing machine powder" | Add ₹200 washing powder expense | "Added ₹200 for washing machine powder." |
| 259 | "Spent 100 on phenyl" | Add ₹100 phenyl expense | "Added ₹100 for phenyl." |
| 260 | "Add 250 for Harpic" | Add ₹250 Harpic expense | "Added ₹250 for Harpic." |
| 261 | "I bought Lizol for 300" | Add ₹300 Lizol expense | "Added ₹300 for Lizol." |
| 262 | "Spent 150 on Vim bar" | Add ₹150 Vim bar expense | "Added ₹150 for Vim bar." |
| 263 | "Add 200 on dishwash liquid" | Add ₹200 dishwash liquid expense | "Added ₹200 for dishwash liquid." |
| 264 | "I paid 400 for garbage bags" | Add ₹400 garbage bag expense | "Added ₹400 for garbage bags." |
| 265 | "Spent 100 on tissues" | Add ₹100 tissue expense | "Added ₹100 for tissues." |
| 266 | "Add 250 for hand wash" | Add ₹250 hand wash expense | "Added ₹250 for hand wash." |
| 267 | "I bought sanitizer for 150" | Add ₹150 sanitizer expense | "Added ₹150 for sanitizer." |
| 268 | "Spent 500 on floor cleaner" | Add ₹500 floor cleaner expense | "Added ₹500 for floor cleaner." |
| 269 | "Add 300 for glass cleaner" | Add ₹300 glass cleaner expense | "Added ₹300 for glass cleaner." |
| 270 | "I paid 200 for air freshener" | Add ₹200 air freshener expense | "Added ₹200 for air freshener." |
| 271 | "Spent 1500 on pest control" | Add ₹1500 pest control expense | "Added ₹1500 for pest control." |
| 272 | "Add 800 for termite treatment" | Add ₹800 termite treatment expense | "Added ₹800 for termite treatment." |
| 273 | "I bought rat poison for 100" | Add ₹100 rat poison expense | "Added ₹100 for rat poison." |
| 274 | "Spent 200 on cockroach gel" | Add ₹200 cockroach gel expense | "Added ₹200 for cockroach gel." |
| 275 | "Add 400 for door mat" | Add ₹400 door mat expense | "Added ₹400 for door mat." |
| 276 | "I paid 600 for shoe rack" | Add ₹600 shoe rack expense | "Added ₹600 for shoe rack." |
| 277 | "Spent 300 on hangers" | Add ₹300 hangers expense | "Added ₹300 for hangers." |
| 278 | "Add 1500 for iron box" | Add ₹1500 iron box expense | "Added ₹1500 for iron box." |
| 279 | "I bought ironing board for 800" | Add ₹800 ironing board expense | "Added ₹800 for ironing board." |
| 280 | "Spent 200 on clothes clips" | Add ₹200 clothes clips expense | "Added ₹200 for clothes clips." |
| 281 | "Add 100 on rope for drying" | Add ₹100 rope expense | "Added ₹100 for drying rope." |
| 282 | "I paid 500 for extension board" | Add ₹500 extension board expense | "Added ₹500 for extension board." |
| 283 | "Spent 300 on switch repair" | Add ₹300 switch repair expense | "Added ₹300 for switch repair." |
| 284 | "Add 200 for bulb holder" | Add ₹200 bulb holder expense | "Added ₹200 for bulb holder." |
| 285 | "I bought wires for 400" | Add ₹400 wire expense | "Added ₹400 for wires." |
| 286 | "Spent 600 on plumber visit" | Add ₹600 plumber visit expense | "Added ₹600 for plumber visit." |
| 287 | "Add 800 for electrician" | Add ₹800 electrician expense | "Added ₹800 for electrician." |
| 288 | "I paid 1200 for carpenter" | Add ₹1200 carpenter expense | "Added ₹1200 for carpenter." |
| 289 | "Spent 500 for painter" | Add ₹500 painter expense | "Added ₹500 for painter." |
| 290 | "Add 2000 for wall repair" | Add ₹2000 wall repair expense | "Added ₹2000 for wall repair." |
| 291 | "I bought tiles for 1500" | Add ₹1500 tiles expense | "Added ₹1500 for tiles." |
| 292 | "Spent 800 on cement" | Add ₹800 cement expense | "Added ₹800 for cement." |
| 293 | "Add 400 for white cement" | Add ₹400 white cement expense | "Added ₹400 for white cement." |
| 294 | "I paid 200 for putty" | Add ₹200 putty expense | "Added ₹200 for putty." |
| 295 | "Spent 600 on paint" | Add ₹600 paint expense | "Added ₹600 for paint." |
| 296 | "Add 300 for brush" | Add ₹300 brush expense | "Added ₹300 for brush." |
| 297 | "I bought sandpaper for 100" | Add ₹100 sandpaper expense | "Added ₹100 for sandpaper." |
| 298 | "Spent 250 on masking tape" | Add ₹250 masking tape expense | "Added ₹250 for masking tape." |
| 299 | "Add 150 for drop cloth" | Add ₹150 drop cloth expense | "Added ₹150 for drop cloth." |
| 300 | "I paid 500 for ladder rent" | Add ₹500 ladder rent expense | "Added ₹500 for ladder rent." |

### 2.2 With Split Specification (75+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 301 | "I spent 500 on groceries, split equally" | Add ₹500, split among all | "Added ₹500 for groceries. Split equally. Each owes ₹125." |
| 302 | "Add 300 for dinner, split with Bhanu and Kiran" | Add ₹300, split with Bhanu + Kiran | "Added ₹300 for dinner. Split with Bhanu and Kiran." |
| 303 | "I paid 1200 for electricity, split among all" | Add ₹1200, split equally | "Added ₹1200 for electricity. Split equally. Each owes ₹300." |
| 304 | "Spent 2000 on rent, split with everyone" | Add ₹2000, split equally | "Added ₹2000 for rent. Split equally." |
| 305 | "I bought vegetables for 250, split it" | Add ₹250, split equally | "Added ₹250 for vegetables. Split equally." |
| 306 | "Add expense 500 Netflix, split with Bhanu" | Add ₹500, split with Bhanu only | "Added ₹500 for Netflix. Split with Bhanu. Bhanu owes ₹250." |
| 307 | "I paid 150 for milk, divide equally" | Add ₹150, split equally | "Added ₹150 for milk. Split equally." |
| 308 | "Spent 800 on petrol, share with all" | Add ₹800, split equally | "Added ₹800 for petrol. Split equally." |
| 309 | "Add 600 for cleaning supplies, split among us" | Add ₹600, split equally | "Added ₹600 for cleaning supplies. Split equally." |
| 310 | "I paid 400 for WiFi, divide among flatmates" | Add ₹400, split equally | "Added ₹400 for WiFi. Split equally." |
| 311 | "Spent 350 on snacks, split equally please" | Add ₹350, split equally | "Added ₹350 for snacks. Split equally." |
| 312 | "Add 1000 for house maintenance, split with Kiran and Rohin" | Add ₹1000, split with Kiran + Rohin | "Added ₹1000 for maintenance. Split with Kiran and Rohin." |
| 313 | "I bought toilet paper for 180, split it equally" | Add ₹180, split equally | "Added ₹180 for toilet paper. Split equally." |
| 314 | "Spent 500 on Uber, divide among everyone" | Add ₹500, split equally | "Added ₹500 for Uber. Split equally." |
| 315 | "Add 1200 for gas cylinder, split equally" | Add ₹1200, split equally | "Added ₹1200 for gas cylinder. Split equally." |
| 316 | "I paid 600 for plumber, split with Bhanu" | Add ₹600, split with Bhanu | "Added ₹600 for plumber. Split with Bhanu." |
| 317 | "Spent 250 on chai, share equally" | Add ₹250, split equally | "Added ₹250 for chai. Split equally." |
| 318 | "Add 1500 for groceries today, split among all" | Add ₹1500, split equally | "Added ₹1500 for groceries. Split equally." |
| 319 | "I bought rice and dal for 400, split with everyone" | Add ₹400, split equally | "Added ₹400 for rice and dal. Split equally." |
| 320 | "Spent 300 on bread and eggs, divide equally" | Add ₹300, split equally | "Added ₹300 for bread and eggs. Split equally." |
| 321 | "Add 800 for chicken, split equally" | Add ₹800, split equally | "Added ₹800 for chicken. Split equally." |
| 322 | "I paid 500 for fish, split with Kiran" | Add ₹500, split with Kiran | "Added ₹500 for fish. Split with Kiran." |
| 323 | "Spent 200 on fruits, share with all" | Add ₹200, split equally | "Added ₹200 for fruits. Split equally." |
| 324 | "Add 450 for curd and paneer, split equally" | Add ₹450, split equally | "Added ₹450 for curd and paneer. Split equally." |
| 325 | "I bought oil and spices for 600, split among us" | Add ₹600, split equally | "Added ₹600 for oil and spices. Split equally." |
| 326 | "Spent 350 on detergent and soap, divide equally" | Add ₹350, split equally | "Added ₹350 for detergent. Split equally." |
| 327 | "Add 100 for water cans, split equally" | Add ₹100, split equally | "Added ₹100 for water cans. Split equally." |
| 328 | "I paid 50 for Bisleri, split with Bhanu" | Add ₹50, split with Bhanu | "Added ₹50 for Bisleri. Split with Bhanu." |
| 329 | "Spent 1200 on Swiggy, split among all" | Add ₹1200, split equally | "Added ₹1200 for Swiggy. Split equally." |
| 330 | "Add 800 for Zomato, share equally" | Add ₹800, split equally | "Added ₹800 for Zomato. Split equally." |
| 331 | "I bought biryani for 400, split equally" | Add ₹400, split equally | "Added ₹400 for biryani. Split equally." |
| 332 | "Spent 250 on pizza, divide among everyone" | Add ₹250, split equally | "Added ₹250 for pizza. Split equally." |
| 333 | "Add 500 for movie tickets, split with Kiran and Rohin" | Add ₹500, split with Kiran + Rohin | "Added ₹500 for movie tickets. Split with Kiran and Rohin." |
| 334 | "I paid 200 for auto, split equally" | Add ₹200, split equally | "Added ₹200 for auto. Split equally." |
| 335 | "Spent 1500 on Metro recharge, split among us" | Add ₹1500, split equally | "Added ₹1500 for Metro recharge. Split equally." |
| 336 | "Add 300 for phone recharge, split equally" | Add ₹300, split equally | "Added ₹300 for phone recharge. Split equally." |
| 337 | "I bought a bulb for 80, split with Bhanu" | Add ₹80, split with Bhanu | "Added ₹80 for bulb. Split with Bhanu." |
| 338 | "Spent 200 on tube light, divide equally" | Add ₹200, split equally | "Added ₹200 for tube light. Split equally." |
| 339 | "Add 500 for fan repair, split equally" | Add ₹500, split equally | "Added ₹500 for fan repair. Split equally." |
| 340 | "I paid 300 for AC service, split among all" | Add ₹300, split equally | "Added ₹300 for AC service. Split equally." |
| 341 | "Spent 150 on mosquito coil, share equally" | Add ₹150, split equally | "Added ₹150 for mosquito coil. Split equally." |
| 342 | "Add 100 for room freshener, split with Kiran" | Add ₹100, split with Kiran | "Added ₹100 for room freshener. Split with Kiran." |
| 343 | "I bought a mop for 250, split equally" | Add ₹250, split equally | "Added ₹250 for mop. Split equally." |
| 344 | "Spent 180 on broom, divide equally" | Add ₹180, split equally | "Added ₹180 for broom. Split equally." |
| 345 | "Add 400 for dustbins, split among us" | Add ₹400, split equally | "Added ₹400 for dustbins. Split equally." |
| 346 | "I paid 1200 for mattress cover, split equally" | Add ₹1200, split equally | "Added ₹1200 for mattress cover. Split equally." |
| 347 | "Spent 800 on curtains, share with all" | Add ₹800, split equally | "Added ₹800 for curtains. Split equally." |
| 348 | "Add 600 for pillow covers, split equally" | Add ₹600, split equally | "Added ₹600 for pillow covers. Split equally." |
| 349 | "I bought a bucket for 150, split with Bhanu and Kiran" | Add ₹150, split with Bhanu + Kiran | "Added ₹150 for bucket. Split with Bhanu and Kiran." |
| 350 | "Spent 200 on mugs, divide equally" | Add ₹200, split equally | "Added ₹200 for mugs. Split equally." |
| 351 | "Add 350 for plates, split equally" | Add ₹350, split equally | "Added ₹350 for plates. Split equally." |
| 352 | "I paid 500 for glasses, split among all" | Add ₹500, split equally | "Added ₹500 for glasses. Split equally." |
| 353 | "Spent 1200 on induction stove, share equally" | Add ₹1200, split equally | "Added ₹1200 for induction stove. Split equally." |
| 354 | "Add 800 for mixer grinder, split equally" | Add ₹800, split equally | "Added ₹800 for mixer grinder. Split equally." |
| 355 | "I bought a kettle for 600, split with everyone" | Add ₹600, split equally | "Added ₹600 for kettle. Split equally." |
| 356 | "Spent 400 on toaster, divide equally" | Add ₹400, split equally | "Added ₹400 for toaster. Split equally." |
| 357 | "Add 1500 for fridge repair, split equally" | Add ₹1500, split equally | "Added ₹1500 for fridge repair. Split equally." |
| 358 | "I paid 200 for washing machine powder, split equally" | Add ₹200, split equally | "Added ₹200 for washing powder. Split equally." |
| 359 | "Spent 100 on phenyl, split with Bhanu" | Add ₹100, split with Bhanu | "Added ₹100 for phenyl. Split with Bhanu." |
| 360 | "Add 250 for Harpic, divide equally" | Add ₹250, split equally | "Added ₹250 for Harpic. Split equally." |
| 361 | "I bought Lizol for 300, split equally" | Add ₹300, split equally | "Added ₹300 for Lizol. Split equally." |
| 362 | "Spent 150 on Vim bar, share equally" | Add ₹150, split equally | "Added ₹150 for Vim bar. Split equally." |
| 363 | "Add 200 on dishwash liquid, split equally" | Add ₹200, split equally | "Added ₹200 for dishwash liquid. Split equally." |
| 364 | "I paid 400 for garbage bags, split among all" | Add ₹400, split equally | "Added ₹400 for garbage bags. Split equally." |
| 365 | "Spent 100 on tissues, divide equally" | Add ₹100, split equally | "Added ₹100 for tissues. Split equally." |
| 366 | "Add 250 for hand wash, split equally" | Add ₹250, split equally | "Added ₹250 for hand wash. Split equally." |
| 367 | "I bought sanitizer for 150, split with Kiran" | Add ₹150, split with Kiran | "Added ₹150 for sanitizer. Split with Kiran." |
| 368 | "Spent 500 on floor cleaner, split equally" | Add ₹500, split equally | "Added ₹500 for floor cleaner. Split equally." |
| 369 | "Add 300 for glass cleaner, share equally" | Add ₹300, split equally | "Added ₹300 for glass cleaner. Split equally." |
| 370 | "I paid 200 for air freshener, split equally" | Add ₹200, split equally | "Added ₹200 for air freshener. Split equally." |
| 371 | "Spent 1500 on pest control, split among us" | Add ₹1500, split equally | "Added ₹1500 for pest control. Split equally." |
| 372 | "Add 800 for termite treatment, divide equally" | Add ₹800, split equally | "Added ₹800 for termite treatment. Split equally." |
| 373 | "I bought rat poison for 100, split equally" | Add ₹100, split equally | "Added ₹100 for rat poison. Split equally." |
| 374 | "Spent 200 on cockroach gel, split equally" | Add ₹200, split equally | "Added ₹200 for cockroach gel. Split equally." |
| 375 | "Add 400 for door mat, share with all" | Add ₹400, split equally | "Added ₹400 for door mat. Split equally." |

### 2.3 Hinglish Expense Commands (50+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 376 | "Maine 500 diye groceries pe" | Add ₹500 grocery expense | "₹500 groceries pe add kar diya." |
| 377 | "300 dinner ke add kar" | Add ₹300 dinner expense | "₹300 dinner ke add kar diya." |
| 378 | "1200 electricity bill pay kiya" | Add ₹1200 electricity expense | "₹1200 electricity bill add kar diya." |
| 379 | "2000 rent diya" | Add ₹2000 rent expense | "₹2000 rent add kar diya." |
| 380 | "250 vegetables ke liye diye" | Add ₹250 vegetable expense | "₹250 vegetables add kar diya." |
| 381 | "500 Netflix ke add kar do" | Add ₹500 Netflix expense | "₹500 Netflix add kar diya." |
| 382 | "150 milk ke diye" | Add ₹150 milk expense | "₹150 milk add kar diya." |
| 383 | "800 petrol pe kharcha kiya" | Add ₹800 petrol expense | "₹800 petrol add kar diya." |
| 384 | "600 cleaning supplies ke liye" | Add ₹600 cleaning supplies | "₹600 cleaning supplies add kar diya." |
| 385 | "400 WiFi bill pay kiya" | Add ₹400 WiFi expense | "₹400 WiFi add kar diya." |
| 386 | "350 snacks pe kharcha" | Add ₹350 snacks expense | "₹350 snacks add kar diya." |
| 387 | "1000 maintenance ke liye" | Add ₹1000 maintenance | "₹1000 maintenance add kar diya." |
| 388 | "180 toilet paper ke liye" | Add ₹180 toilet paper | "₹180 toilet paper add kar diya." |
| 389 | "500 Uber pe kharcha" | Add ₹500 Uber expense | "₹500 Uber add kar diya." |
| 390 | "1200 gas cylinder ke liye" | Add ₹1200 gas cylinder | "₹1200 gas cylinder add kar diya." |
| 391 | "600 plumber ko diye" | Add ₹600 plumber expense | "₹600 plumber add kar diya." |
| 392 | "250 chai pe kharcha" | Add ₹250 chai expense | "₹250 chai add kar diya." |
| 393 | "1500 aaj groceries ke liye" | Add ₹1500 grocery expense | "₹1500 groceries add kar diya." |
| 394 | "400 rice aur dal ke liye" | Add ₹400 rice and dal | "₹400 rice aur dal add kar diya." |
| 395 | "300 bread eggs ke liye" | Add ₹300 bread and eggs | "₹300 bread eggs add kar diya." |
| 396 | "800 chicken ke liye" | Add ₹800 chicken expense | "₹800 chicken add kar diya." |
| 397 | "500 fish ke liye diye" | Add ₹500 fish expense | "₹500 fish add kar diya." |
| 398 | "200 fruits pe kharcha" | Add ₹200 fruits expense | "₹200 fruits add kar diya." |
| 399 | "450 curd paneer ke liye" | Add ₹450 curd and paneer | "₹450 curd paneer add kar diya." |
| 400 | "600 oil spices ke liye" | Add ₹600 oil and spices | "₹600 oil spices add kar diya." |
| 401 | "350 detergent soap ke liye" | Add ₹350 detergent | "₹350 detergent soap add kar diya." |
| 402 | "100 water can ke liye" | Add ₹100 water can | "₹100 water can add kar diya." |
| 403 | "50 Bisleri ke liye" | Add ₹50 Bisleri | "₹50 Bisleri add kar diya." |
| 404 | "1200 Swiggy pe kharcha" | Add ₹1200 Swiggy | "₹1200 Swiggy add kar diya." |
| 405 | "800 Zomato ke liye" | Add ₹800 Zomato | "₹800 Zomato add kar diya." |
| 406 | "400 biryani ke liye" | Add ₹400 biryani | "₹400 biryani add kar diya." |
| 407 | "250 pizza pe kharcha" | Add ₹250 pizza | "₹250 pizza add kar diya." |
| 408 | "500 movie tickets ke liye" | Add ₹500 movie tickets | "₹500 movie tickets add kar diya." |
| 409 | "200 auto pe diye" | Add ₹200 auto | "₹200 auto add kar diya." |
| 410 | "1500 Metro recharge ke liye" | Add ₹1500 Metro recharge | "₹1500 Metro recharge add kar diya." |
| 411 | "300 phone recharge ke liye" | Add ₹300 phone recharge | "₹300 phone recharge add kar diya." |
| 412 | "80 bulb ke liye" | Add ₹80 bulb | "₹80 bulb add kar diya." |
| 413 | "200 tube light ke liye" | Add ₹200 tube light | "₹200 tube light add kar diya." |
| 414 | "500 fan repair ke liye" | Add ₹500 fan repair | "₹500 fan repair add kar diya." |
| 415 | "300 AC service ke liye" | Add ₹300 AC service | "₹300 AC service add kar diya." |
| 416 | "150 mosquito coil ke liye" | Add ₹150 mosquito coil | "₹150 mosquito coil add kar diya." |
| 417 | "100 room freshener ke liye" | Add ₹100 room freshener | "₹100 room freshener add kar diya." |
| 418 | "250 mop ke liye" | Add ₹250 mop | "₹250 mop add kar diya." |
| 419 | "180 jhadu ke liye" | Add ₹180 broom | "₹180 jhadu add kar diya." |
| 420 | "400 dustbin ke liye" | Add ₹400 dustbin | "₹400 dustbin add kar diya." |
| 421 | "1200 mattress cover ke liye" | Add ₹1200 mattress cover | "₹1200 mattress cover add kar diya." |
| 422 | "800 curtains ke liye" | Add ₹800 curtains | "₹800 curtains add kar diya." |
| 423 | "600 pillow covers ke liye" | Add ₹600 pillow covers | "₹600 pillow covers add kar diya." |
| 424 | "150 bucket ke liye" | Add ₹150 bucket | "₹150 bucket add kar diya." |
| 425 | "200 mugs ke liye" | Add ₹200 mugs | "₹200 mugs add kar diya." |

### 2.4 Telugu-English Expense (25+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 426 | "500 groceries ki icha" | Add ₹500 grocery | "₹500 groceries add chesa." |
| 427 | "300 dinner ki add chey" | Add ₹300 dinner | "₹300 dinner add chesa." |
| 428 | "1200 electricity bill pay chesa" | Add ₹1200 electricity | "₹1200 electricity add chesa." |
| 429 | "2000 rent icha" | Add ₹2000 rent | "₹2000 rent add chesa." |
| 430 | "250 vegetables ki icha" | Add ₹250 vegetables | "₹250 vegetables add chesa." |
| 431 | "500 Netflix ki add chey" | Add ₹500 Netflix | "₹500 Netflix add chesa." |
| 432 | "150 milk ki icha" | Add ₹150 milk | "₹150 milk add chesa." |
| 433 | "800 petrol ki kharchu" | Add ₹800 petrol | "₹800 petrol add chesa." |
| 434 | "600 cleaning supplies ki" | Add ₹600 cleaning | "₹600 cleaning supplies add chesa." |
| 435 | "400 WiFi bill pay chesa" | Add ₹400 WiFi | "₹400 WiFi add chesa." |
| 436 | "350 snacks ki kharchu" | Add ₹350 snacks | "₹350 snacks add chesa." |
| 437 | "1000 maintenance ki" | Add ₹1000 maintenance | "₹1000 maintenance add chesa." |
| 438 | "180 toilet paper ki" | Add ₹180 toilet paper | "₹180 toilet paper add chesa." |
| 439 | "500 Uber ki kharchu" | Add ₹500 Uber | "₹500 Uber add chesa." |
| 440 | "1200 gas cylinder ki" | Add ₹1200 gas | "₹1200 gas cylinder add chesa." |
| 441 | "600 plumber ki icha" | Add ₹600 plumber | "₹600 plumber add chesa." |
| 442 | "250 chai ki kharchu" | Add ₹250 chai | "₹250 chai add chesa." |
| 443 | "1500 groceries ki icha" | Add ₹1500 groceries | "₹1500 groceries add chesa." |
| 444 | "400 rice dal ki" | Add ₹400 rice dal | "₹400 rice dal add chesa." |
| 445 | "300 bread eggs ki" | Add ₹300 bread eggs | "₹300 bread eggs add chesa." |
| 446 | "800 chicken ki" | Add ₹800 chicken | "₹800 chicken add chesa." |
| 447 | "500 fish ki icha" | Add ₹500 fish | "₹500 fish add chesa." |
| 448 | "200 fruits ki kharchu" | Add ₹200 fruits | "₹200 fruits add chesa." |
| 449 | "450 curd paneer ki" | Add ₹450 curd paneer | "₹450 curd paneer add chesa." |
| 450 | "600 oil spices ki" | Add ₹600 oil spices | "₹600 oil spices add chesa." |

---

## 3. Intent: QUERY_BALANCE

### 3.1 Direct Balance Queries (100+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 451 | "How much does Bhanu owe me?" | Query Bhanu balance | "Bhanu owes you ₹450." |
| 452 | "What is my balance?" | Show all balances | "You are owed ₹1,200 total. Bhanu: ₹450. Kiran: ₹350. Rohin: ₹400." |
| 453 | "How much do I owe Kiran?" | Query Kiran balance | "You owe Kiran ₹200." |
| 454 | "Who owes me money?" | List all who owe user | "Bhanu owes ₹450. Kiran owes ₹350. Rohin owes ₹400." |
| 455 | "What is the total balance?" | Show flat total | "Total unsettled: ₹2,500. You are owed ₹1,200." |
| 456 | "How much do I owe?" | Show total user owes | "You owe ₹600 total. Kiran: ₹200. Rohin: ₹400." |
| 457 | "Does Bhanu owe me?" | Check Bhanu balance | "Yes, Bhanu owes you ₹450." |
| 458 | "Is Kiran settled up?" | Check Kiran balance | "Kiran owes you ₹350. Not settled yet." |
| 459 | "What is Rohin's balance?" | Query Rohin balance | "Rohin owes you ₹400." |
| 460 | "Show me all balances" | Show all balances | "Bhanu owes you ₹450. Kiran owes you ₹350. Rohin owes you ₹400. You owe Kiran ₹200." |
| 461 | "How much is pending?" | Show total pending | "₹2,500 pending across all members." |
| 462 | "What is the net balance?" | Show net balance | "Your net balance: +₹1,200. You are owed more than you owe." |
| 463 | "Does anyone owe me?" | Check if anyone owes user | "Yes, 3 people owe you: Bhanu ₹450, Kiran ₹350, Rohin ₹400." |
| 464 | "How much total is owed?" | Show total owed | "₹2,500 total is owed in this flat." |
| 465 | "What is Bhanu's status?" | Show Bhanu balance | "Bhanu owes you ₹450." |
| 466 | "Is everyone settled?" | Check if all settled | "Not yet. ₹2,500 still pending." |
| 467 | "How much should I pay Kiran?" | Query Kiran balance | "You owe Kiran ₹200." |
| 468 | "What does Rohin owe?" | Query Rohin balance | "Rohin owes you ₹400." |
| 469 | "Show balance with Bhanu" | Show Bhanu balance | "Bhanu owes you ₹450." |
| 470 | "Balance with Kiran" | Show Kiran balance | "Kiran owes you ₹350." |
| 471 | "My net amount" | Show net balance | "Your net: +₹1,200." |
| 472 | "Total dues" | Show total pending | "Total dues: ₹2,500." |
| 473 | "Who should pay whom?" | Suggest settlements | "Bhanu should pay you ₹450. Kiran should pay you ₹350. Rohin should pay you ₹400. You should pay Kiran ₹200." |
| 474 | "Simplify the balances" | Suggest simplified settlements | "Simplified: Bhanu pays you ₹450. Kiran pays you ₹150. Rohin pays you ₹400." |
| 475 | "How much is left to settle?" | Show remaining | "₹2,500 left to settle." |
| 476 | "Bhanu ka balance kya hai?" | Query Bhanu balance | "Bhanu aapko ₹450 dene wala hai." |
| 477 | "Mera balance kitna hai?" | Show all balances | "Aapko ₹1,200 milne wale hain." |
| 478 | "Main Kiran ko kitna deta hoon?" | Query Kiran balance | "Aap Kiran ko ₹200 dete hain." |
| 479 | "Kaun mujhe paise deta hai?" | List who owes user | "Bhanu ₹450, Kiran ₹350, Rohin ₹400." |
| 480 | "Total kitna baki hai?" | Show total pending | "₹2,500 baki hai." |
| 481 | "Main kitna dene wala hoon?" | Show total user owes | "Aap ₹600 dene wale hain." |
| 482 | "Kiran settled hai?" | Check Kiran balance | "Kiran aapko ₹350 dene wala hai." |
| 483 | "Rohin ka status kya hai?" | Query Rohin balance | "Rohin aapko ₹400 dene wala hai." |
| 484 | "Sab balances dikhao" | Show all balances | "Bhanu: ₹450. Kiran: ₹350. Rohin: ₹400." |
| 485 | "Kitna pending hai?" | Show total pending | "₹2,500 pending hai." |
| 486 | "Net balance kitna hai?" | Show net balance | "Aapka net: +₹1,200." |
| 487 | "Koi mujhe paise deta hai?" | Check if anyone owes | "Haan, 3 log aapko paise dete hain." |
| 488 | "Total kitna owed hai?" | Show total owed | "₹2,500 owed hai." |
| 489 | "Bhanu ka status?" | Show Bhanu balance | "Bhanu aapko ₹450 dene wala hai." |
| 490 | "Sab settled hain?" | Check if all settled | "Nahi, ₹2,500 baki hai." |
| 491 | "Main Kiran ko kitna pay karoon?" | Query Kiran balance | "Aap Kiran ko ₹200 pay karein." |
| 492 | "Rohin kitna owe karta hai?" | Query Rohin balance | "Rohin aapko ₹400 owe karta hai." |
| 493 | "Bhanu ke saath balance" | Show Bhanu balance | "Bhanu aapko ₹450 dene wala hai." |
| 494 | "Kiran ke saath balance" | Show Kiran balance | "Kiran aapko ₹350 dene wala hai." |
| 495 | "Mera net amount" | Show net balance | "Aapka net: +₹1,200." |
| 496 | "Total dues kitne hain?" | Show total pending | "Total dues: ₹2,500." |
| 497 | "Kaun kisko pay kare?" | Suggest settlements | "Bhanu aapko ₹450. Kiran aapko ₹350. Rohin aapko ₹400." |
| 498 | "Balances simplify karo" | Suggest simplified settlements | "Simplified: Bhanu aapko ₹450. Kiran aapko ₹150. Rohin aapko ₹400." |
| 499 | "Kitna settle karna baki hai?" | Show remaining | "₹2,500 settle karna baki hai." |
| 500 | "Bhanu nannu entha ivvali?" | Query Bhanu balance | "Bhanu meeku ₹450 ivvali." |
| 501 | "Na balance entha?" | Show all balances | "Meeku ₹1,200 vastundi." |
| 502 | "Nenu Kiran ki entha ivvali?" | Query Kiran balance | "Meeru Kiran ki ₹200 ivvali." |
| 503 | "Naku evaru ivvali?" | List who owes user | "Bhanu ₹450, Kiran ₹350, Rohin ₹400." |
| 504 | "Total entha baki undi?" | Show total pending | "₹2,500 baki undi." |
| 505 | "Nenu entha ivvali?" | Show total user owes | "Meeru ₹600 ivvali." |
| 506 | "Kiran settled aa?" | Check Kiran balance | "Kiran meeku ₹350 ivvali." |
| 507 | "Rohin status enti?" | Query Rohin balance | "Rohin meeku ₹400 ivvali." |
| 508 | "Anni balances chupinchu" | Show all balances | "Bhanu: ₹450. Kiran: ₹350. Rohin: ₹400." |
| 509 | "Entha pending undi?" | Show total pending | "₹2,500 pending undi." |
| 510 | "Net balance entha?" | Show net balance | "Meeku net: +₹1,200." |
| 511 | "Evaraina naku ivvala?" | Check if anyone owes | "Aavunu, 3 mandi meeku ivvali." |
| 512 | "Total entha owed undi?" | Show total owed | "₹2,500 owed undi." |
| 513 | "Bhanu status?" | Show Bhanu balance | "Bhanu meeku ₹450 ivvali." |
| 514 | "Andaru settled aa?" | Check if all settled | "Kadu, ₹2,500 baki undi." |
| 515 | "Nenu Kiran ki entha pay cheyali?" | Query Kiran balance | "Meeru Kiran ki ₹200 pay cheyali." |
| 516 | "Rohin entha owe chestundi?" | Query Rohin balance | "Rohin meeku ₹400 owe chestundi." |
| 517 | "Bhanu tho balance" | Show Bhanu balance | "Bhanu meeku ₹450 ivvali." |
| 518 | "Kiran tho balance" | Show Kiran balance | "Kiran meeku ₹350 ivvali." |
| 519 | "Na net amount" | Show net balance | "Meeku net: +₹1,200." |
| 520 | "Total dues entha?" | Show total pending | "Total dues: ₹2,500." |
| 521 | "Evaru evariki pay cheyali?" | Suggest settlements | "Bhanu meeku ₹450. Kiran meeku ₹350. Rohin meeku ₹400." |
| 522 | "Balances simplify chey" | Suggest simplified settlements | "Simplified: Bhanu meeku ₹450. Kiran meeku ₹150. Rohin meeku ₹400." |
| 523 | "Entha settle cheyali baki?" | Show remaining | "₹2,500 settle cheyali baki." |
| 524 | "Show me the money" | Show all balances | "You are owed ₹1,200. Bhanu: ₹450. Kiran: ₹350. Rohin: ₹400." |
| 525 | "Where is my money?" | Show all balances | "You are owed ₹1,200. Check the Money tab for details." |
| 526 | "Who has to pay me?" | List who owes user | "Bhanu owes ₹450. Kiran owes ₹350. Rohin owes ₹400." |
| 527 | "Do I owe anyone?" | Check if user owes | "You owe Kiran ₹200." |
| 528 | "Am I in the green?" | Show net balance | "Yes, you are +₹1,200." |
| 529 | "Am I in the red?" | Show net balance | "No, you are +₹1,200. You're doing fine." |
| 530 | "What is the damage?" | Show total user owes | "You owe ₹600 total." |
| 531 | "How deep am I in?" | Show total user owes | "You owe ₹600. Not too bad." |
| 532 | "Who is the richest?" | Show net balances | "You are the richest at +₹1,200." |
| 533 | "Who is the poorest?" | Show net balances | "Rohin is the poorest at -₹400." |
| 534 | "Show me the ledger" | Show all balances | "Full ledger: Bhanu owes you ₹450. Kiran owes you ₹350. Rohin owes you ₹400. You owe Kiran ₹200." |
| 535 | "Balance sheet" | Show all balances | "Balance sheet: Assets ₹1,200. Liabilities ₹600. Net ₹600." |
| 536 | "Financial status" | Show all balances | "Financial status: You are owed ₹1,200. You owe ₹600. Net: +₹600." |
| 537 | "Money matters" | Show all balances | "Money matters: ₹2,500 pending. You are owed ₹1,200." |
| 538 | "Cash flow" | Show all balances | "Cash flow: Inflow ₹1,200. Outflow ₹600. Net: +₹600." |
| 539 | "Debt report" | Show all balances | "Debt report: You are owed ₹1,200. You owe ₹600." |
| 540 | "Credit report" | Show all balances | "Credit report: You are owed ₹1,200. You owe ₹600." |
| 541 | "Who owes the most?" | Show highest debtor | "Rohin owes the most: ₹400." |
| 542 | "Who do I owe the most?" | Show highest creditor | "You owe Kiran the most: ₹200." |
| 543 | "Smallest balance?" | Show smallest balance | "Smallest: You owe Kiran ₹200." |
| 544 | "Largest balance?" | Show largest balance | "Largest: Bhanu owes you ₹450." |
| 545 | "Average balance?" | Show average | "Average owed to you: ₹400." |
| 546 | "Median balance?" | Show median | "Median owed to you: ₹400." |
| 547 | "Total inflow" | Show total owed to user | "Total inflow: ₹1,200." |
| 548 | "Total outflow" | Show total user owes | "Total outflow: ₹600." |
| 549 | "Net worth" | Show net balance | "Net worth: +₹600." |
| 550 | "How much can I spend?" | Show net balance | "You have a net surplus of ₹600. Spend wisely!" |

---

## 4. Intent: QUERY_TASKS

### 4.1 Task Queries (75+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 551 | "What are my tasks?" | List pending tasks | "You have 3 pending tasks: Kitchen, Bathroom, Garbage." |
| 552 | "What do I need to do?" | List pending tasks | "Your tasks: Kitchen, Bathroom, Garbage." |
| 553 | "My duties today?" | List today's tasks | "Today's duties: Kitchen, Bathroom." |
| 554 | "What is pending?" | List pending tasks | "Pending: Kitchen, Bathroom, Garbage." |
| 555 | "Any tasks for me?" | List pending tasks | "Yes, 3 tasks: Kitchen, Bathroom, Garbage." |
| 556 | "Show my tasks" | List pending tasks | "Your tasks: Kitchen, Bathroom, Garbage." |
| 557 | "What is my next task?" | Show next task | "Next: Kitchen, due today." |
| 558 | "When is my next duty?" | Show next duty | "Next duty: Kitchen, due today." |
| 559 | "How many tasks left?" | Count pending tasks | "3 tasks left." |
| 560 | "Task count?" | Count pending tasks | "3 pending tasks." |
| 561 | "What is due today?" | List today's tasks | "Due today: Kitchen, Bathroom." |
| 562 | "What is due tomorrow?" | List tomorrow's tasks | "Due tomorrow: Garbage." |
| 563 | "This week's tasks?" | List week's tasks | "This week: Kitchen, Bathroom, Garbage, Dusting." |
| 564 | "My schedule?" | List task schedule | "Your schedule: Kitchen today, Bathroom tomorrow, Garbage Friday." |
| 565 | "What did I complete?" | List completed tasks | "You completed: Kitchen, Bathroom." |
| 566 | "What is left?" | List remaining tasks | "Left: Garbage, Dusting." |
| 567 | "Remaining tasks?" | List remaining tasks | "Remaining: Garbage, Dusting." |
| 568 | "Overdue tasks?" | List overdue tasks | "Overdue: Garbage was due yesterday." |
| 569 | "What is late?" | List overdue tasks | "Late: Garbage, due yesterday." |
| 570 | "What did I miss?" | List missed tasks | "Missed: Garbage, due yesterday." |
| 571 | "My task list?" | List all tasks | "Your task list: Kitchen, Bathroom, Garbage, Dusting." |
| 572 | "Show all my duties" | List all duties | "All duties: Kitchen, Bathroom, Garbage, Dusting." |
| 573 | "What is on my plate?" | List pending tasks | "On your plate: Kitchen, Bathroom, Garbage." |
| 574 | "What is my workload?" | Count pending tasks | "Workload: 3 pending tasks." |
| 575 | "How busy am I?" | Count pending tasks | "You have 3 tasks. Medium busy." |
| 576 | "Task summary?" | Summarize tasks | "Summary: 3 pending, 2 completed today, 1 overdue." |
| 577 | "Quick status?" | Quick task status | "Status: 3 pending, 1 overdue." |
| 578 | "What should I do first?" | Suggest priority task | "Do Kitchen first — it's due today." |
| 579 | "Priority task?" | Show highest priority | "Priority: Kitchen, due today." |
| 580 | "Urgent tasks?" | List urgent tasks | "Urgent: Kitchen, due today. Garbage, overdue." |
| 581 | "Mere tasks kya hain?" | List pending tasks | "Aapke 3 tasks hain: Kitchen, Bathroom, Garbage." |
| 582 | "Aaj kya karna hai?" | List today's tasks | "Aaj: Kitchen, Bathroom." |
| 583 | "Koi pending task hai?" | List pending tasks | "Haan, 3 pending tasks." |
| 584 | "Mere duties dikhao" | List all duties | "Aapki duties: Kitchen, Bathroom, Garbage." |
| 585 | "Next task kya hai?" | Show next task | "Next: Kitchen, aaj due." |
| 586 | "Kitne tasks baki hain?" | Count pending tasks | "3 tasks baki hain." |
| 587 | "Aaj kya due hai?" | List today's tasks | "Aaj due: Kitchen, Bathroom." |
| 588 | "Kal kya due hai?" | List tomorrow's tasks | "Kal due: Garbage." |
| 589 | "Is hafte kya tasks hain?" | List week's tasks | "Is hafte: Kitchen, Bathroom, Garbage, Dusting." |
| 590 | "Mera schedule kya hai?" | List task schedule | "Schedule: Kitchen aaj, Bathroom kal, Garbage Friday." |
| 591 | "Maine kya complete kiya?" | List completed tasks | "Complete: Kitchen, Bathroom." |
| 592 | "Kya baki hai?" | List remaining tasks | "Baki: Garbage, Dusting." |
| 593 | "Overdue tasks kya hain?" | List overdue tasks | "Overdue: Garbage, kal due tha." |
| 594 | "Kya miss ho gaya?" | List missed tasks | "Missed: Garbage, kal due tha." |
| 595 | "Mera task list?" | List all tasks | "Task list: Kitchen, Bathroom, Garbage, Dusting." |
| 596 | "Sab duties dikhao" | List all duties | "Sab duties: Kitchen, Bathroom, Garbage, Dusting." |
| 597 | "Mere plate pe kya hai?" | List pending tasks | "Plate pe: Kitchen, Bathroom, Garbage." |
| 598 | "Mera workload kitna hai?" | Count pending tasks | "Workload: 3 tasks." |
| 599 | "Main kitna busy hoon?" | Count pending tasks | "3 tasks hain. Medium busy." |
| 600 | "Task summary do" | Summarize tasks | "Summary: 3 pending, 2 aaj complete, 1 overdue." |
| 601 | "Quick status do" | Quick task status | "Status: 3 pending, 1 overdue." |
| 602 | "Pehle kya karna chahiye?" | Suggest priority task | "Pehle Kitchen karo — aaj due hai." |
| 603 | "Priority task kya hai?" | Show highest priority | "Priority: Kitchen, aaj due." |
| 604 | "Urgent tasks kya hain?" | List urgent tasks | "Urgent: Kitchen aaj due. Garbage overdue." |
| 605 | "Na tasks enti?" | List pending tasks | "Meeku 3 tasks unnayi: Kitchen, Bathroom, Garbage." |
| 606 | "Iroju em cheyali?" | List today's tasks | "Iroju: Kitchen, Bathroom." |
| 607 | "Pending tasks unnaya?" | List pending tasks | "Aavunu, 3 pending tasks." |
| 608 | "Na duties chupinchu" | List all duties | "Meeku duties: Kitchen, Bathroom, Garbage." |
| 609 | "Next task enti?" | Show next task | "Next: Kitchen, iroju due." |
| 610 | "Enni tasks baki unnayi?" | Count pending tasks | "3 tasks baki unnayi." |
| 611 | "Iroju em due undi?" | List today's tasks | "Iroju due: Kitchen, Bathroom." |
| 612 | "Repu em due undi?" | List tomorrow's tasks | "Repu due: Garbage." |
| 613 | "I week em tasks unnayi?" | List week's tasks | "I week: Kitchen, Bathroom, Garbage, Dusting." |
| 614 | "Na schedule enti?" | List task schedule | "Schedule: Kitchen iroju, Bathroom repu, Garbage Friday." |
| 615 | "Nenu em complete chesa?" | List completed tasks | "Complete: Kitchen, Bathroom." |
| 616 | "Em baki undi?" | List remaining tasks | "Baki: Garbage, Dusting." |
| 617 | "Overdue tasks enti?" | List overdue tasks | "Overdue: Garbage, ninna due." |
| 618 | "Em miss aindi?" | List missed tasks | "Missed: Garbage, ninna due." |
| 619 | "Na task list?" | List all tasks | "Task list: Kitchen, Bathroom, Garbage, Dusting." |
| 620 | "Anni duties chupinchu" | List all duties | "Anni duties: Kitchen, Bathroom, Garbage, Dusting." |
| 621 | "Na plate lo em undi?" | List pending tasks | "Plate lo: Kitchen, Bathroom, Garbage." |
| 622 | "Na workload entha?" | Count pending tasks | "Workload: 3 tasks." |
| 623 | "Nenu entha busy?" | Count pending tasks | "3 tasks unnayi. Medium busy." |
| 624 | "Task summary ivvu" | Summarize tasks | "Summary: 3 pending, 2 iroju complete, 1 overdue." |
| 625 | "Quick status ivvu" | Quick task status | "Status: 3 pending, 1 overdue." |

---

## 5. Intent: QUERY_STATUS

### 5.1 Flat Status Queries (50+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 626 | "Who is home?" | Show who is not OOS | "Bhanu and Kiran are home. Rohin is out of station." |
| 627 | "Who is out?" | Show who is OOS | "Rohin is out of station until Friday." |
| 628 | "Is Bhanu home?" | Check Bhanu status | "Yes, Bhanu is home." |
| 629 | "Is Kiran out?" | Check Kiran status | "No, Kiran is home." |
| 630 | "Where is Rohin?" | Check Rohin status | "Rohin is out of station until Friday." |
| 631 | "Who is OOS?" | Show OOS members | "Rohin is OOS until Friday." |
| 632 | "Out of station list?" | Show OOS list | "Rohin is OOS until Friday." |
| 633 | "Who is traveling?" | Show OOS members | "Rohin is traveling until Friday." |
| 634 | "Who is away?" | Show OOS members | "Rohin is away until Friday." |
| 635 | "Who is present?" | Show present members | "Bhanu, Kiran, and you are present." |
| 636 | "Who is absent?" | Show absent members | "Rohin is absent until Friday." |
| 637 | "Flat status?" | Show full flat status | "Flat status: 3 present, 1 OOS. Bhanu and Kiran home. Rohin away." |
| 638 | "Member status?" | Show all member statuses | "Bhanu: home. Kiran: home. Rohin: OOS until Friday." |
| 639 | "Who is in the flat?" | Show present members | "Bhanu, Kiran, and you are in the flat." |
| 640 | "Who is not in the flat?" | Show absent members | "Rohin is not in the flat. Returns Friday." |
| 641 | "Is everyone home?" | Check if all home | "No, Rohin is out of station." |
| 642 | "Is anyone away?" | Check if anyone OOS | "Yes, Rohin is away until Friday." |
| 643 | "When is Rohin coming back?" | Show Rohin return date | "Rohin returns Friday." |
| 644 | "Rohin kab aayega?" | Show Rohin return date | "Rohin Friday ko aayega." |
| 645 | "Kaun ghar pe hai?" | Show present members | "Bhanu aur Kiran ghar pe hain. Rohin bahar hai." |
| 646 | "Kaun bahar hai?" | Show absent members | "Rohin bahar hai, Friday tak." |
| 647 | "Bhanu ghar pe hai?" | Check Bhanu status | "Haan, Bhanu ghar pe hai." |
| 648 | "Kiran bahar hai?" | Check Kiran status | "Nahi, Kiran ghar pe hai." |
| 649 | "Rohin kahan hai?" | Check Rohin status | "Rohin out of station hai, Friday tak." |
| 650 | "Kaun OOS hai?" | Show OOS members | "Rohin OOS hai, Friday tak." |
| 651 | "Evaru intlo unnaru?" | Show present members | "Bhanu, Kiran, meeru intlo unnaru." |
| 652 | "Evaru bayata unnaru?" | Show absent members | "Rohin bayata unnaru, Friday varaku." |
| 653 | "Bhanu intlo unnada?" | Check Bhanu status | "Aavunu, Bhanu intlo unnadu." |
| 654 | "Kiran bayata unnada?" | Check Kiran status | "Kadu, Kiran intlo unnadu." |
| 655 | "Rohin ekkada unnadu?" | Check Rohin status | "Rohin out of station, Friday varaku." |
| 656 | "Evaru OOS?" | Show OOS members | "Rohin OOS, Friday varaku." |
| 657 | "Andaru intlo unnara?" | Check if all home | "Kadu, Rohin bayata unnadu." |
| 658 | "Evaraina bayata unnara?" | Check if anyone OOS | "Aavunu, Rohin bayata unnadu." |
| 659 | "Rohin eppudu vastadu?" | Show Rohin return | "Rohin Friday vastadu." |
| 660 | "Flat status enti?" | Show full flat status | "Flat status: 3 present, 1 OOS." |
| 661 | "Member status enti?" | Show all member statuses | "Bhanu: home. Kiran: home. Rohin: OOS." |
| 662 | "Intlo evaru unnaru?" | Show present members | "Bhanu, Kiran, meeru intlo unnaru." |
| 663 | "Flat lo evaru learu?" | Show absent members | "Rohin flat lo ledu." |
| 664 | "Andaru intlo unnara?" | Check if all home | "Kadu, Rohin bayata unnadu." |
| 665 | "Evaraina bayata unnara?" | Check if anyone OOS | "Aavunu, Rohin bayata unnadu." |
| 666 | "Rohin eppudu vastadu?" | Show Rohin return | "Rohin Friday vastadu." |
| 667 | "Rohin eppudu return avtadu?" | Show Rohin return | "Rohin Friday return avtadu." |
| 668 | "Rohin eppudu vachestadu?" | Show Rohin return | "Rohin Friday vachestadu." |
| 669 | "Rohin eppudu tirigi vastadu?" | Show Rohin return | "Rohin Friday tirigi vastadu." |
| 670 | "Rohin eppudu malli vastadu?" | Show Rohin return | "Rohin Friday malli vastadu." |
| 671 | "Rohin eppudu back avtadu?" | Show Rohin return | "Rohin Friday back avtadu." |
| 672 | "Rohin eppudu home ki vastadu?" | Show Rohin return | "Rohin Friday home ki vastadu." |
| 673 | "Rohin eppudu flat ki vastadu?" | Show Rohin return | "Rohin Friday flat ki vastadu." |
| 674 | "Rohin eppudu return?" | Show Rohin return | "Rohin Friday return." |
| 675 | "Rohin return date?" | Show Rohin return | "Rohin returns Friday." |

---

## 6. Intent: REQUEST_SWAP

### 6.1 Swap Requests (50+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 676 | "Can someone cover my kitchen task?" | Create swap request for kitchen | "Swap request sent for Kitchen. Waiting for someone to accept." |
| 677 | "I need help with bathroom" | Create swap request for bathroom | "Swap request sent for Bathroom." |
| 678 | "Can Bhanu do my task?" | Request swap with Bhanu | "Swap request sent to Bhanu for your task." |
| 679 | "Swap my garbage duty with Kiran" | Request swap with Kiran for garbage | "Swap request sent to Kiran for Garbage." |
| 680 | "I am busy, can someone take my task?" | Create swap request | "Swap request created. Someone will cover you." |
| 681 | "I am going out, cover my task" | Create swap request | "Swap request created for your task." |
| 682 | "I am sick, someone please do my task" | Create swap request | "Swap request created. Hope you feel better!" |
| 683 | "Can Rohin cover for me?" | Request swap with Rohin | "Swap request sent to Rohin." |
| 684 | "I need a day off from tasks" | Create swap request for all tasks | "Swap request created for all your tasks today." |
| 685 | "Swap request for kitchen" | Create swap request for kitchen | "Swap request created for Kitchen." |
| 686 | "Can someone do my duty?" | Create swap request | "Swap request created for your duty." |
| 687 | "I am traveling, cover my tasks" | Create swap request | "Swap request created for your tasks while traveling." |
| 688 | "My task is too much, help" | Create swap request | "Swap request created. Someone will help." |
| 689 | "Can Kiran take my bathroom task?" | Request swap with Kiran for bathroom | "Swap request sent to Kiran for Bathroom." |
| 690 | "Bhanu, can you do my kitchen?" | Request swap with Bhanu for kitchen | "Swap request sent to Bhanu for Kitchen." |
| 691 | "I am overloaded, swap please" | Create swap request | "Swap request created. You have 3 tasks today." |
| 692 | "Can someone take garbage for me?" | Create swap request for garbage | "Swap request created for Garbage." |
| 693 | "I have an exam, cover my tasks" | Create swap request | "Swap request created. Good luck with your exam!" |
| 694 | "I am working late, swap my task" | Create swap request | "Swap request created for your late work day." |
| 695 | "My parents are visiting, cover me" | Create swap request | "Swap request created. Enjoy your family time!" |
| 696 | "I am not feeling well, someone cover" | Create swap request | "Swap request created. Take rest!" |
| 697 | "Can someone do kitchen instead of me?" | Create swap request for kitchen | "Swap request created for Kitchen." |
| 698 | "Swap my dusting with someone" | Create swap request for dusting | "Swap request created for Dusting." |
| 699 | "I need coverage for today" | Create swap request for all today | "Swap request created for all today's tasks." |
| 700 | "Can Rohin take my place?" | Request swap with Rohin | "Swap request sent to Rohin." |
| 701 | "Mera kitchen task koi kar dega?" | Create swap request for kitchen | "Kitchen ke liye swap request bhej di." |
| 702 | "Bathroom mein help chahiye" | Create swap request for bathroom | "Bathroom ke liye help request bhej di." |
| 703 | "Bhanu mera task kar dega?" | Request swap with Bhanu | "Bhanu ko swap request bhej di." |
| 704 | "Kiran se garbage swap kar" | Request swap with Kiran for garbage | "Kiran ko garbage swap request bhej di." |
| 705 | "Main busy hoon, koi cover kar de" | Create swap request | "Swap request bhej di." |
| 706 | "Main bahar ja raha hoon, task cover kar do" | Create swap request | "Swap request bhej di." |
| 707 | "Main bimaar hoon, koi please kar de" | Create swap request | "Swap request bhej di. Aaram karo!" |
| 708 | "Rohin cover kar dega?" | Request swap with Rohin | "Rohin ko swap request bhej di." |
| 709 | "Aaj tasks se chhutti chahiye" | Create swap request for all | "Aaj ke sab tasks ke liye swap request bhej di." |
| 710 | "Kitchen ke liye swap request" | Create swap request for kitchen | "Kitchen swap request bhej di." |
| 711 | "Koi mera duty kar dega?" | Create swap request | "Duty ke liye swap request bhej di." |
| 712 | "Main travel kar raha hoon, tasks cover kar do" | Create swap request | "Travel ke time tasks ke liye swap request bhej di." |
| 713 | "Mera task bahut hai, help" | Create swap request | "Swap request bhej di." |
| 714 | "Kiran mera bathroom task le lega?" | Request swap with Kiran for bathroom | "Kiran ko bathroom swap request bhej di." |
| 715 | "Bhanu, mera kitchen kar dega?" | Request swap with Bhanu for kitchen | "Bhanu ko kitchen swap request bhej di." |
| 716 | "Main overloaded hoon, swap please" | Create swap request | "Swap request bhej di. Aaj 3 tasks hain." |
| 717 | "Koi garbage mere liye le lega?" | Create swap request for garbage | "Garbage ke liye swap request bhej di." |
| 718 | "Mera exam hai, tasks cover kar do" | Create swap request | "Exam ke liye swap request bhej di. Best of luck!" |
| 719 | "Main late kaam kar raha hoon, swap kar do" | Create swap request | "Late work ke liye swap request bhej di." |
| 720 | "Mere parents aa rahe hain, cover kar do" | Create swap request | "Family time ke liye swap request bhej di." |
| 721 | "Na kitchen task evaru chestaru?" | Create swap request for kitchen | "Kitchen swap request pampincha." |
| 722 | "Bathroom lo help kavali" | Create swap request for bathroom | "Bathroom help request pampincha." |
| 723 | "Bhanu na task chestada?" | Request swap with Bhanu | "Bhanu ki swap request pampincha." |
| 724 | "Kiran tho garbage swap chey" | Request swap with Kiran for garbage | "Kiran ki garbage swap request pampincha." |
| 725 | "Nenu busy, evaraina cover cheyandi" | Create swap request | "Swap request pampincha." |

---

## 7. Intent: CREATE_TASK

### 7.1 Task Creation (50+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 726 | "Add new task kitchen cleaning daily" | Create daily kitchen task | "Daily Kitchen Cleaning task added." |
| 727 | "Create task bathroom weekly" | Create weekly bathroom task | "Weekly Bathroom task created." |
| 728 | "New task garbage every day" | Create daily garbage task | "Daily Garbage task created." |
| 729 | "Add dusting task fortnightly" | Create fortnightly dusting task | "Fortnightly Dusting task added." |
| 730 | "Create monthly fridge cleaning task" | Create monthly fridge task | "Monthly Fridge Cleaning task created." |
| 731 | "Add task water plants every 3 days" | Create 3-day plant task | "Every-3-days Water Plants task added." |
| 732 | "New task sweep hall daily" | Create daily hall sweeping task | "Daily Sweep Hall task created." |
| 733 | "Add task mop balcony weekly" | Create weekly balcony mopping task | "Weekly Mop Balcony task added." |
| 734 | "Create task clean windows monthly" | Create monthly window task | "Monthly Clean Windows task created." |
| 735 | "Add task change bedsheets fortnightly" | Create fortnightly bedsheet task | "Fortnightly Change Bedsheets task added." |
| 736 | "New task buy milk daily" | Create daily milk task | "Daily Buy Milk task created." |
| 737 | "Add task buy groceries weekly" | Create weekly grocery task | "Weekly Buy Groceries task added." |
| 738 | "Create task pay electricity monthly" | Create monthly electricity task | "Monthly Pay Electricity task created." |
| 739 | "Add task pay WiFi monthly" | Create monthly WiFi task | "Monthly Pay WiFi task added." |
| 740 | "New task pay rent monthly" | Create monthly rent task | "Monthly Pay Rent task created." |
| 741 | "Add task book gas cylinder monthly" | Create monthly gas task | "Monthly Book Gas Cylinder task added." |
| 742 | "Create task clean AC filters monthly" | Create monthly AC filter task | "Monthly Clean AC Filters task created." |
| 743 | "Add task service water filter quarterly" | Create quarterly water filter task | "Quarterly Service Water Filter task added." |
| 744 | "New task pest control monthly" | Create monthly pest control task | "Monthly Pest Control task created." |
| 745 | "Add task clean aquarium weekly" | Create weekly aquarium task | "Weekly Clean Aquarium task added." |
| 746 | "Create task walk dog daily" | Create daily dog walk task | "Daily Walk Dog task created." |
| 747 | "Add task feed pet daily" | Create daily pet feed task | "Daily Feed Pet task added." |
| 748 | "New task wash car weekly" | Create weekly car wash task | "Weekly Wash Car task created." |
| 749 | "Add task service bike monthly" | Create monthly bike service task | "Monthly Service Bike task added." |
| 750 | "Create task refill water cans daily" | Create daily water refill task | "Daily Refill Water Cans task created." |
| 751 | "Naya task kitchen cleaning daily" | Create daily kitchen task | "Daily Kitchen Cleaning task add kar diya." |
| 752 | "Task create karo bathroom weekly" | Create weekly bathroom task | "Weekly Bathroom task create kar diya." |
| 753 | "Naya task garbage har din" | Create daily garbage task | "Daily Garbage task create kar diya." |
| 754 | "Dusting task fortnightly add karo" | Create fortnightly dusting task | "Fortnightly Dusting task add kar diya." |
| 755 | "Monthly fridge cleaning task add karo" | Create monthly fridge task | "Monthly Fridge Cleaning task add kar diya." |
| 756 | "Paani ke plants ka task har 3 din" | Create 3-day plant task | "Every-3-days Water Plants task add kar diya." |
| 757 | "Hall jhadu daily task" | Create daily hall sweeping task | "Daily Sweep Hall task create kar diya." |
| 758 | "Balcony pocha weekly task" | Create weekly balcony mopping task | "Weekly Mop Balcony task add kar diya." |
| 759 | "Windows saaf monthly task" | Create monthly window task | "Monthly Clean Windows task create kar diya." |
| 760 | "Bedsheet change fortnightly task" | Create fortnightly bedsheet task | "Fortnightly Change Bedsheets task add kar diya." |
| 761 | "Doodh le aana daily task" | Create daily milk task | "Daily Buy Milk task create kar diya." |
| 762 | "Groceries weekly task" | Create weekly grocery task | "Weekly Buy Groceries task add kar diya." |
| 763 | "Electricity bill monthly task" | Create monthly electricity task | "Monthly Pay Electricity task create kar diya." |
| 764 | "WiFi bill monthly task" | Create monthly WiFi task | "Monthly Pay WiFi task add kar diya." |
| 765 | "Rent monthly task" | Create monthly rent task | "Monthly Pay Rent task create kar diya." |
| 766 | "Gas cylinder monthly task" | Create monthly gas task | "Monthly Book Gas Cylinder task add kar diya." |
| 767 | "AC filter saaf monthly task" | Create monthly AC filter task | "Monthly Clean AC Filters task create kar diya." |
| 768 | "Water filter service quarterly task" | Create quarterly water filter task | "Quarterly Service Water Filter task add kar diya." |
| 769 | "Pest control monthly task" | Create monthly pest control task | "Monthly Pest Control task create kar diya." |
| 770 | "Aquarium saaf weekly task" | Create weekly aquarium task | "Weekly Clean Aquarium task create kar diya." |
| 771 | "Dog walk daily task" | Create daily dog walk task | "Daily Walk Dog task create kar diya." |
| 772 | "Pet feed daily task" | Create daily pet feed task | "Daily Feed Pet task add kar diya." |
| 773 | "Car wash weekly task" | Create weekly car wash task | "Weekly Wash Car task create kar diya." |
| 774 | "Bike service monthly task" | Create monthly bike service task | "Monthly Service Bike task add kar diya." |
| 775 | "Water cans refill daily task" | Create daily water refill task | "Daily Refill Water Cans task create kar diya." |

---

## 8. Intent: GREETING

### 8.1 Greetings (25+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 776 | "Hi" | Greet | "Hi! What can I help you with?" |
| 777 | "Hello" | Greet | "Hello! Ready to help." |
| 778 | "Hey" | Greet | "Hey! What's up?" |
| 779 | "Good morning" | Greet with time context | "Good morning! You have 2 tasks today." |
| 780 | "Good afternoon" | Greet with time context | "Good afternoon! 1 task pending." |
| 781 | "Good evening" | Greet with time context | "Good evening! All tasks done for today." |
| 782 | "Good night" | Greet | "Good night! Sleep well." |
| 783 | "Namaste" | Greet | "Namaste! Kya help chahiye?" |
| 784 | "Hola" | Greet | "Hola! How can I help?" |
| 785 | "Yo" | Greet | "Yo! What's up?" |
| 786 | "Sup" | Greet | "Sup! How can I help?" |
| 787 | "What's up?" | Greet | "All good! You have 2 tasks pending." |
| 788 | "How are you?" | Greet | "I'm great! How can I help you today?" |
| 789 | "How is it going?" | Greet | "Going well! You have 2 tasks today." |
| 790 | "Howdy" | Greet | "Howdy! What can I do for you?" |
| 791 | "Greetings" | Greet | "Greetings! Ready to assist." |
| 792 | "Salutations" | Greet | "Salutations! How may I help?" |
| 793 | "Hey there" | Greet | "Hey there! What's on your mind?" |
| 794 | "Hi there" | Greet | "Hi there! How can I help?" |
| 795 | "Hello there" | Greet | "Hello there! Ready to help." |
| 796 | "Hey buddy" | Greet | "Hey! What can I help with?" |
| 797 | "Hi friend" | Greet | "Hi! What do you need?" |
| 798 | "Hello friend" | Greet | "Hello! How can I assist?" |
| 799 | "Hey mate" | Greet | "Hey mate! What's up?" |
| 800 | "Hi mate" | Greet | "Hi mate! How can I help?" |

---

## 9. Intent: UNKNOWN / FALLBACK

### 9.1 Unknown Commands (100+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 801 | "What is the weather?" | Unknown | "I don't know about weather. Try: 'What are my tasks?' or 'How much does Bhanu owe me?'" |
| 802 | "Play music" | Unknown | "I can't play music. Try: 'Kitchen done' or 'Add 500 for groceries.'" |
| 803 | "Call mom" | Unknown | "I can't make calls. Try: 'What are my tasks?'" |
| 804 | "Send message" | Unknown | "I can't send messages. Try: 'How much does Bhanu owe me?'" |
| 805 | "Set alarm" | Unknown | "I can't set alarms. Try: 'What is my next task?'" |
| 806 | "What time is it?" | Unknown | "I don't know the time. Try: 'What are my tasks?'" |
| 807 | "What is the date?" | Unknown | "I don't know the date. Try: 'What is due today?'" |
| 808 | "Tell me a joke" | Unknown | "I don't know jokes. Try: 'What are my tasks?'" |
| 809 | "What is the news?" | Unknown | "I don't know news. Try: 'Who is home?'" |
| 810 | "What is the stock price?" | Unknown | "I don't know stocks. Try: 'What is my balance?'" |
| 811 | "What is the cricket score?" | Unknown | "I don't know cricket scores. Try: 'What are my tasks?'" |
| 812 | "What is the movie timing?" | Unknown | "I don't know movies. Try: 'What is my next task?'" |
| 813 | "What is the traffic?" | Unknown | "I don't know traffic. Try: 'Who is home?'" |
| 814 | "What is the temperature?" | Unknown | "I don't know temperature. Try: 'What are my tasks?'" |
| 815 | "What is the humidity?" | Unknown | "I don't know humidity. Try: 'What are my tasks?'" |
| 816 | "What is the air quality?" | Unknown | "I don't know air quality. Try: 'What are my tasks?'" |
| 817 | "What is the pollution level?" | Unknown | "I don't know pollution. Try: 'What are my tasks?'" |
| 818 | "What is the UV index?" | Unknown | "I don't know UV index. Try: 'What are my tasks?'" |
| 819 | "What is the wind speed?" | Unknown | "I don't know wind speed. Try: 'What are my tasks?'" |
| 820 | "What is the moon phase?" | Unknown | "I don't know moon phase. Try: 'What are my tasks?'" |
| 821 | "What is the tide?" | Unknown | "I don't know tides. Try: 'What are my tasks?'" |
| 822 | "What is the sunrise time?" | Unknown | "I don't know sunrise. Try: 'What are my tasks?'" |
| 823 | "What is the sunset time?" | Unknown | "I don't know sunset. Try: 'What are my tasks?'" |
| 824 | "What is the prayer time?" | Unknown | "I don't know prayer times. Try: 'What are my tasks?'" |
| 825 | "What is the fasting time?" | Unknown | "I don't know fasting times. Try: 'What are my tasks?'" |
| 826 | "What is the festival date?" | Unknown | "I don't know festival dates. Try: 'What are my tasks?'" |
| 827 | "What is the holiday?" | Unknown | "I don't know holidays. Try: 'What are my tasks?'" |
| 828 | "What is the exam date?" | Unknown | "I don't know exam dates. Try: 'What are my tasks?'" |
| 829 | "What is the meeting time?" | Unknown | "I don't know meeting times. Try: 'What are my tasks?'" |
| 830 | "What is the train timing?" | Unknown | "I don't know train timings. Try: 'What are my tasks?'" |
| 831 | "What is the bus timing?" | Unknown | "I don't know bus timings. Try: 'What are my tasks?'" |
| 832 | "What is the flight status?" | Unknown | "I don't know flight status. Try: 'What are my tasks?'" |
| 833 | "What is the cab status?" | Unknown | "I don't know cab status. Try: 'What are my tasks?'" |
| 834 | "What is the delivery status?" | Unknown | "I don't know delivery status. Try: 'What are my tasks?'" |
| 835 | "What is the order status?" | Unknown | "I don't know order status. Try: 'What are my tasks?'" |
| 836 | "What is the booking status?" | Unknown | "I don't know booking status. Try: 'What are my tasks?'" |
| 837 | "What is the reservation status?" | Unknown | "I don't know reservation status. Try: 'What are my tasks?'" |
| 838 | "What is the appointment status?" | Unknown | "I don't know appointment status. Try: 'What are my tasks?'" |
| 839 | "What is the ticket status?" | Unknown | "I don't know ticket status. Try: 'What are my tasks?'" |
| 840 | "What is the application status?" | Unknown | "I don't know application status. Try: 'What are my tasks?'" |
| 841 | "What is the interview status?" | Unknown | "I don't know interview status. Try: 'What are my tasks?'" |
| 842 | "What is the job status?" | Unknown | "I don't know job status. Try: 'What are my tasks?'" |
| 843 | "What is the project status?" | Unknown | "I don't know project status. Try: 'What are my tasks?'" |
| 844 | "What is the task status?" | Query tasks | "You have 3 pending tasks." |
| 845 | "What is the expense status?" | Query balances | "You are owed ₹1,200." |
| 846 | "What is the flat status?" | Query flat status | "3 present, 1 OOS." |
| 847 | "What is the member status?" | Query member status | "Bhanu: home. Kiran: home. Rohin: OOS." |
| 848 | "What is the payment status?" | Query balances | "You are owed ₹1,200." |
| 849 | "What is the settlement status?" | Query balances | "₹2,500 pending." |
| 850 | "What is the completion status?" | Query tasks | "3 pending, 2 completed today." |
| 851 | "What is the overdue status?" | Query tasks | "1 overdue: Garbage." |
| 852 | "What is the swap status?" | Query swap requests | "1 pending swap request for Kitchen." |
| 853 | "What is the invitation status?" | Unknown | "I don't know invitation status. Try: 'Who is home?'" |
| 854 | "What is the subscription status?" | Unknown | "I don't know subscription status. Check the Nest tab." |
| 855 | "What is the coupon status?" | Unknown | "I don't know coupon status. Check the Nest tab." |
| 856 | "What is the premium status?" | Unknown | "I don't know premium status. Check the Nest tab." |
| 857 | "What is the trial status?" | Unknown | "I don't know trial status. Check the Nest tab." |
| 858 | "What is the expiry status?" | Unknown | "I don't know expiry status. Check the Nest tab." |
| 859 | "What is the renewal status?" | Unknown | "I don't know renewal status. Check the Nest tab." |
| 860 | "What is the upgrade status?" | Unknown | "I don't know upgrade status. Check the Nest tab." |
| 861 | "What is the downgrade status?" | Unknown | "I don't know downgrade status. Check the Nest tab." |
| 862 | "What is the cancellation status?" | Unknown | "I don't know cancellation status. Check the Nest tab." |
| 863 | "What is the refund status?" | Unknown | "I don't know refund status. Check the Nest tab." |
| 864 | "What is the return status?" | Unknown | "I don't know return status. Check the Nest tab." |
| 865 | "What is the exchange status?" | Unknown | "I don't know exchange status. Check the Nest tab." |
| 866 | "What is the warranty status?" | Unknown | "I don't know warranty status. Check the Nest tab." |
| 867 | "What is the insurance status?" | Unknown | "I don't know insurance status. Check the Nest tab." |
| 868 | "What is the claim status?" | Unknown | "I don't know claim status. Check the Nest tab." |
| 869 | "What is the policy status?" | Unknown | "I don't know policy status. Check the Nest tab." |
| 870 | "What is the agreement status?" | Unknown | "I don't know agreement status. Check the Nest tab." |
| 871 | "What is the contract status?" | Unknown | "I don't know contract status. Check the Nest tab." |
| 872 | "What is the document status?" | Unknown | "I don't know document status. Check the Nest tab." |
| 873 | "What is the verification status?" | Unknown | "I don't know verification status. Check the Nest tab." |
| 874 | "What is the approval status?" | Unknown | "I don't know approval status. Check the Nest tab." |
| 875 | "What is the rejection status?" | Unknown | "I don't know rejection status. Check the Nest tab." |
| 876 | "What is the pending status?" | Query tasks | "3 pending tasks." |
| 877 | "What is the active status?" | Query flat status | "3 active members, 1 OOS." |
| 878 | "What is the inactive status?" | Query flat status | "1 inactive member: Rohin." |
| 879 | "What is the online status?" | Unknown | "I don't know online status. Try: 'Who is home?'" |
| 880 | "What is the offline status?" | Unknown | "I don't know offline status. Try: 'Who is home?'" |
| 881 | "What is the available status?" | Query flat status | "3 available members." |
| 882 | "What is the busy status?" | Unknown | "I don't know busy status. Try: 'Who is home?'" |
| 883 | "What is the free status?" | Unknown | "I don't know free status. Try: 'Who is home?'" |
| 884 | "What is the occupied status?" | Unknown | "I don't know occupied status. Try: 'Who is home?'" |
| 885 | "What is the vacant status?" | Unknown | "I don't know vacant status. Try: 'Who is home?'" |
| 886 | "What is the reserved status?" | Unknown | "I don't know reserved status. Try: 'Who is home?'" |
| 887 | "What is the confirmed status?" | Unknown | "I don't know confirmed status. Try: 'Who is home?'" |
| 888 | "What is the unconfirmed status?" | Unknown | "I don't know unconfirmed status. Try: 'Who is home?'" |
| 889 | "What is the tentative status?" | Unknown | "I don't know tentative status. Try: 'Who is home?'" |
| 890 | "What is the final status?" | Unknown | "I don't know final status. Try: 'Who is home?'" |
| 891 | "What is the draft status?" | Unknown | "I don't know draft status. Try: 'Who is home?'" |
| 892 | "What is the published status?" | Unknown | "I don't know published status. Try: 'Who is home?'" |
| 893 | "What is the archived status?" | Unknown | "I don't know archived status. Try: 'Who is home?'" |
| 894 | "What is the deleted status?" | Unknown | "I don't know deleted status. Try: 'Who is home?'" |
| 895 | "What is the restored status?" | Unknown | "I don't know restored status. Try: 'Who is home?'" |
| 896 | "What is the backup status?" | Unknown | "I don't know backup status. Try: 'Who is home?'" |
| 897 | "What is the sync status?" | Unknown | "I don't know sync status. Try: 'Who is home?'" |
| 898 | "What is the update status?" | Unknown | "I don't know update status. Try: 'Who is home?'" |
| 899 | "What is the download status?" | Unknown | "I don't know download status. Try: 'Who is home?'" |
| 900 | "What is the upload status?" | Unknown | "I don't know upload status. Try: 'Who is home?'" |

---

## 10. Multi-Intent Sequences

### 10.1 Chained Commands (25+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 901 | "Kitchen done. Add 500 for groceries." | Complete kitchen + Add expense | "Kitchen done. Added ₹500 for groceries." |
| 902 | "What are my tasks? How much does Bhanu owe me?" | List tasks + Query balance | "Tasks: Kitchen, Bathroom. Bhanu owes you ₹450." |
| 903 | "Bathroom done. I spent 300 on cleaning supplies." | Complete bathroom + Add expense | "Bathroom done. Added ₹300 for cleaning supplies." |
| 904 | "Who is home? What are my tasks?" | Query status + List tasks | "Bhanu and Kiran home. Your tasks: Kitchen, Bathroom." |
| 905 | "Add 1200 for electricity. How much do I owe?" | Add expense + Query balance | "Added ₹1200 for electricity. You owe ₹600." |
| 906 | "Garbage done. Who owes me money?" | Complete garbage + Query balance | "Garbage done. Bhanu: ₹450, Kiran: ₹350, Rohin: ₹400." |
| 907 | "What is my balance? Kitchen done." | Query balance + Complete kitchen | "You are owed ₹1,200. Kitchen marked done." |
| 908 | "I spent 800 on petrol. What is my next task?" | Add expense + Query next task | "Added ₹800 for petrol. Next: Bathroom, due today." |
| 909 | "Dusting done. Add 200 for dusting supplies." | Complete dusting + Add expense | "Dusting done. Added ₹200 for dusting supplies." |
| 910 | "Who is out? What are my tasks?" | Query status + List tasks | "Rohin is out. Your tasks: Kitchen, Bathroom." |
| 911 | "Add 500 for dinner. How much does Kiran owe me?" | Add expense + Query balance | "Added ₹500 for dinner. Kiran owes you ₹350." |
| 912 | "Kitchen done. Bathroom done." | Complete kitchen + Complete bathroom | "Kitchen done. Bathroom done. All clear!" |
| 913 | "What are my tasks? I spent 300 on snacks." | List tasks + Add expense | "Tasks: Kitchen, Bathroom. Added ₹300 for snacks." |
| 914 | "Bhanu owes me? What is my next task?" | Query balance + Query next task | "Bhanu owes ₹450. Next: Kitchen, due today." |
| 915 | "Add 1500 for groceries. Who is home?" | Add expense + Query status | "Added ₹1500 for groceries. Bhanu and Kiran home." |
| 916 | "Kitchen done. What is left?" | Complete kitchen + List remaining | "Kitchen done. Left: Bathroom, Garbage." |
| 917 | "How much do I owe? Add 400 for WiFi." | Query balance + Add expense | "You owe ₹600. Added ₹400 for WiFi." |
| 918 | "What is due today? I spent 250 on chai." | List today's tasks + Add expense | "Due today: Kitchen, Bathroom. Added ₹250 for chai." |
| 919 | "Garbage done. How much total is owed?" | Complete garbage + Query total | "Garbage done. ₹2,500 total owed." |
| 920 | "Who is home? Kitchen done." | Query status + Complete kitchen | "Bhanu and Kiran home. Kitchen done." |
| 921 | "Add 600 for plumber. What is my balance?" | Add expense + Query balance | "Added ₹600 for plumber. You are owed ₹1,200." |
| 922 | "Bathroom done. Who owes me?" | Complete bathroom + Query balance | "Bathroom done. Bhanu: ₹450, Kiran: ₹350, Rohin: ₹400." |
| 923 | "What is my workload? Add 350 for snacks." | Count tasks + Add expense | "3 tasks. Added ₹350 for snacks." |
| 924 | "Kitchen done. Add 500 for groceries. How much does Bhanu owe me?" | Complete + Expense + Balance | "Kitchen done. Added ₹500. Bhanu owes ₹450." |
| 925 | "Who is out? What are my tasks? How much do I owe?" | Status + Tasks + Balance | "Rohin out. Tasks: Kitchen, Bathroom. You owe ₹600." |

---

## 11. Error Recovery Dialogues

### 11.1 Disambiguation Dialogues (50+ examples)

| # | User Says | System Action | System Response |
|---|-----------|---------------|-----------------|
| 926 | "Done" | Ask which task | "Which task? Kitchen, bathroom, or garbage?" |
| 927 | "Finished" | Ask which task | "What did you finish?" |
| 928 | "Complete" | Ask which task | "What is complete?" |
| 929 | "It's over" | Ask which task | "What is over?" |
| 930 | "I did it" | Ask which task | "What did you do?" |
| 931 | "Add expense" | Ask amount | "How much? Say the amount." |
| 932 | "I spent money" | Ask amount | "How much did you spend?" |
| 933 | "I paid" | Ask amount and for what | "How much and for what?" |
| 934 | "Split it" | Ask what to split | "What do you want to split?" |
| 935 | "How much?" | Ask who | "How much does who owe? Say a name." |
| 936 | "Who owes?" | Ask who owes whom | "Who owes whom? Say both names." |
| 937 | "What is due?" | Ask when | "Due when? Today, tomorrow, or this week?" |
| 938 | "My task" | Ask which task | "Which task? Kitchen, bathroom, or garbage?" |
| 939 | "The task" | Ask which task | "Which task? Say the name." |
| 940 | "That task" | Ask which task | "Which task? I need the name." |
| 941 | "This task" | Ask which task | "Which task? Be specific." |
| 942 | "A task" | Ask which task | "Which task? Kitchen, bathroom, or garbage?" |
| 943 | "Some task" | Ask which task | "Which task? I need to know." |
| 944 | "Any task" | Ask which task | "Which task? Pick one." |
| 945 | "The big task" | Ask which task | "Which big task? Kitchen or bathroom?" |
| 946 | "The small task" | Ask which task | "Which small task? Garbage or dusting?" |
| 947 | "The morning task" | Ask which task | "Morning task? Kitchen or bathroom?" |
| 948 | "The evening task" | Ask which task | "Evening task? Garbage or dusting?" |
| 949 | "The daily task" | Ask which task | "Daily task? Kitchen, bathroom, or garbage?" |
| 950 | "The weekly task" | Ask which task | "Weekly task? Dusting or laundry?" |
| 951 | "The monthly task" | Ask which task | "Monthly task? Fridge or windows?" |
| 952 | "The hard task" | Ask which task | "Hard task? Kitchen or bathroom?" |
| 953 | "The easy task" | Ask which task | "Easy task? Garbage or dusting?" |
| 954 | "The first task" | Ask which task | "First task? Kitchen or bathroom?" |
| 955 | "The last task" | Ask which task | "Last task? Garbage or dusting?" |
| 956 | "The next task" | Ask which task | "Next task? Kitchen or bathroom?" |
| 957 | "The previous task" | Ask which task | "Previous task? Which one?" |
| 958 | "The current task" | Ask which task | "Current task? Kitchen or bathroom?" |
| 959 | "The pending task" | Ask which task | "Pending task? Kitchen, bathroom, or garbage?" |
| 960 | "The overdue task" | Ask which task | "Overdue task? Garbage or dusting?" |
| 961 | "The urgent task" | Ask which task | "Urgent task? Kitchen or garbage?" |
| 962 | "The important task" | Ask which task | "Important task? Kitchen or bathroom?" |
| 963 | "The critical task" | Ask which task | "Critical task? Kitchen or garbage?" |
| 964 | "The high priority task" | Ask which task | "High priority? Kitchen or bathroom?" |
| 965 | "The low priority task" | Ask which task | "Low priority? Dusting or garbage?" |
| 966 | "The new task" | Ask which task | "New task? Which one was added recently?" |
| 967 | "The old task" | Ask which task | "Old task? Kitchen or bathroom?" |
| 968 | "The regular task" | Ask which task | "Regular task? Kitchen, bathroom, or garbage?" |
| 969 | "The special task" | Ask which task | "Special task? Which one?" |
| 970 | "The extra task" | Ask which task | "Extra task? Which one?" |
| 971 | "The bonus task" | Ask which task | "Bonus task? Which one?" |
| 972 | "The temp task" | Ask which task | "Temp task? Which one?" |
| 973 | "The one-time task" | Ask which task | "One-time task? Which one?" |
| 974 | "The recurring task" | Ask which task | "Recurring task? Kitchen, bathroom, or garbage?" |
| 975 | "The rotating task" | Ask which task | "Rotating task? Kitchen, bathroom, or garbage?" |

---

## 12. Response Template Library

### 12.1 Success Response Templates

| Template ID | Template | Variables |
|-------------|----------|-----------|
| TASK_COMPLETE_1 | "{taskName} marked done. Great work!" | taskName |
| TASK_COMPLETE_2 | "{taskName} marked done. Nice!" | taskName |
| TASK_COMPLETE_3 | "{taskName} marked done. Thanks!" | taskName |
| TASK_COMPLETE_4 | "{taskName} marked done. Keep it up!" | taskName |
| TASK_COMPLETE_5 | "{taskName} marked done. You're on fire!" | taskName |
| TASK_COMPLETE_ALL | "All {count} tasks marked done. Amazing!" | count |
| EXPENSE_ADD_1 | "Added ₹{amount} for {description}. Split {splitType}." | amount, description, splitType |
| EXPENSE_ADD_2 | "Added ₹{amount} for {description}. Each owes ₹{splitAmount}." | amount, description, splitAmount |
| EXPENSE_ADD_3 | "₹{amount} added for {description}. Split with {members}." | amount, description, members |
| BALANCE_OWED_TO_YOU | "{member} owes you ₹{amount}." | member, amount |
| BALANCE_YOU_OWE | "You owe {member} ₹{amount}." | member, amount |
| BALANCE_SETTLED | "You and {member} are settled up." | member |
| BALANCE_SUMMARY | "You are owed ₹{totalOwed}. {breakdown}." | totalOwed, breakdown |
| TASK_LIST | "You have {count} pending tasks: {tasks}." | count, tasks |
| TASK_LIST_EMPTY | "You have no pending tasks. You are all caught up!" | — |
| TASK_NEXT | "Next: {taskName}, due {dueDate}." | taskName, dueDate |
| TASK_OVERDUE | "Overdue: {taskName}, was due {dueDate}." | taskName, dueDate |
| FLAT_STATUS | "{presentCount} present, {oosCount} OOS. {details}." | presentCount, oosCount, details |
| MEMBER_HOME | "Yes, {member} is home." | member |
| MEMBER_OOS | "{member} is out of station until {returnDate}." | member, returnDate |
| SWAP_SENT | "Swap request sent for {taskName}. Waiting for {member}." | taskName, member |
| SWAP_SENT_ANY | "Swap request sent for {taskName}. Waiting for someone to accept." | taskName |
| TASK_CREATED | "{frequency} {taskName} task created." | frequency, taskName |
| GREETING_MORNING | "Good morning! You have {count} tasks today." | count |
| GREETING_AFTERNOON | "Good afternoon! {count} tasks pending." | count |
| GREETING_EVENING | "Good evening! All tasks done for today." | — |
| GREETING_NIGHT | "Good night! Sleep well." | — |
| GREETING_GENERAL | "Hi! What can I help you with?" | — |

### 12.2 Error Response Templates

| Template ID | Template | Variables |
|-------------|----------|-----------|
| ERROR_UNKNOWN_1 | "I didn't understand that. Try: '{suggestion}'" | suggestion |
| ERROR_UNKNOWN_2 | "Not sure about that. Try saying: '{suggestion}'" | suggestion |
| ERROR_UNKNOWN_3 | "Hmm, I didn't catch that. Try: '{suggestion}'" | suggestion |
| ERROR_UNKNOWN_4 | "Sorry, I didn't understand. Try: '{suggestion}'" | suggestion |
| ERROR_UNKNOWN_5 | "I'm not sure what you mean. Try: '{suggestion}'" | suggestion |
| ERROR_NO_TASK | "I couldn't find that task. Try: Kitchen, Bathroom, or Garbage." | — |
| ERROR_NO_MEMBER | "I don't know '{member}'. Did you mean {suggestions}?" | member, suggestions |
| ERROR_NO_AMOUNT | "How much? Say the amount, like '500 rupees'." | — |
| ERROR_NO_DESCRIPTION | "What was it for? Say something like 'groceries' or 'dinner'." | — |
| ERROR_AMBIGUOUS_TASK | "Which task? {options}." | options |
| ERROR_AMBIGUOUS_MEMBER | "Which member? {options}." | options |
| ERROR_PERMISSION_DENIED | "Microphone access is needed. Enable it in Settings." | — |
| ERROR_NO_SPEECH | "I didn't hear anything. Try speaking closer to the mic." | — |
| ERROR_NETWORK | "Connection issue. Try again in a moment." | — |
| ERROR_NOT_SUPPORTED | "Voice is not supported on this device. Type instead?" | — |
| ERROR_ACTION_FAILED | "Could not do that. Try manually or say it differently." | — |
| ERROR_HIGH_AMOUNT | "That's ₹{amount}. Are you sure? Say 'yes' to confirm." | amount |
| ERROR_NO_BALANCE | "No balances found. Everyone is settled up!" | — |
| ERROR_NO_TASKS | "No tasks found. Add some tasks first!" | — |
| ERROR_NO_FLAT | "No flat found. Join or create a flat first." | — |
| ERROR_NOT_ADMIN | "Only admins can do that. Ask your flat admin." | — |
| ERROR_ALREADY_DONE | "{taskName} is already marked done." | taskName |
| ERROR_NOT_ASSIGNED | "{taskName} is not assigned to you." | taskName |
| ERROR_SWAP_SELF | "You can't swap with yourself." | — |
| ERROR_SWAP_NO_MEMBER | "Who should cover? Say a member name." | — |
| ERROR_EXPENSE_SPLIT_SELF | "You can't split with only yourself." | — |
| ERROR_INVALID_FREQUENCY | "I didn't understand the frequency. Try: daily, weekly, or monthly." | — |
| ERROR_TASK_EXISTS | "{taskName} already exists." | taskName |
| ERROR_MEMBER_OOS | "{member} is out of station. Can't swap with them." | member |
| ERROR_NO_PENDING_SWAP | "No pending swap requests." | — |
| ERROR_SWAP_ALREADY_SENT | "Swap request already sent for {taskName}." | taskName |
| ERROR_VOICE_DISABLED | "Voice is disabled. Enable it in Settings." | — |
| ERROR_TTS_DISABLED | "Voice responses are off. Check Settings to enable." | — |
| ERROR_TOO_FAST | "Too many commands. Try one at a time." | — |
| ERROR_TIMEOUT | "Listening timed out. Tap the mic again." | — |
| ERROR_PARTIAL_MATCH | "Did you mean '{suggestion}'? Say 'yes' or try again." | suggestion |

### 12.3 Follow-Up Suggestion Templates

| Context | Suggestion |
|---------|------------|
| After task complete | "What's next? Say 'what are my tasks' to check." |
| After expense add | "Anything else? Say 'how much does Bhanu owe me' to check balances." |
| After balance query | "Want to settle? Say 'add expense' to log a payment." |
| After task query | "Ready to work? Say 'kitchen done' when you finish." |
| After flat status | "Need to swap? Say 'can someone cover my task'." |
| After greeting | "Try: 'what are my tasks', 'add 500 for groceries', or 'how much does Bhanu owe me'." |
| After unknown | "Try: 'kitchen done', 'add 500 for groceries', or 'what are my tasks'." |
| After error | "Try again or tap to do it manually." |
| After swap request | "I'll notify you when someone accepts." |
| After task creation | "Task created! It will rotate automatically." |
| After all tasks done | "All done! You are amazing. Enjoy your free time!" |
| After first task of day | "First task down! {count} more to go." | count |
| After last task of day | "Last task done! You are free for the day." |
| After overdue task | "Better late than never! Try to stay on schedule." |
| After high expense | "Big expense! I'll remind everyone to settle up." |
| After small expense | "Small expense logged. Every rupee counts!" |
| After split expense | "Split logged. Everyone will see their share." |
| After equal split | "Equal split. Fair and square!" |
| After custom split | "Custom split. Make sure everyone agrees!" |
| After query with no data | "Nothing to show. Add some tasks or expenses first!" |
| After member not found | "Check the member name in the Nest tab." |
| After task not found | "Check your tasks in the Duties tab." |
| After permission issue | "Go to Settings → Voice to enable microphone access." |
| After network issue | "Check your internet connection and try again." |
| After success streak | "{streak} tasks in a row! You're on a roll!" | streak |
| After first use | "Great first try! I'm learning your voice." |
| After 10th use | "10 commands! You're getting the hang of this." |
| After 50th use | "50 commands! You're a voice pro!" |
| After 100th use | "100 commands! You are the voice master!" |

---

## 13. Summary Statistics

| Category | Count |
|----------|-------|
| COMPLETE_TASK — Direct | 100 |
| COMPLETE_TASK — Hinglish | 50 |
| COMPLETE_TASK — Telugu | 25 |
| COMPLETE_TASK — Ambiguous | 25 |
| CREATE_EXPENSE — Direct | 100 |
| CREATE_EXPENSE — With Split | 75 |
| CREATE_EXPENSE — Hinglish | 50 |
| CREATE_EXPENSE — Telugu | 25 |
| QUERY_BALANCE — Direct | 100 |
| QUERY_BALANCE — Hinglish | 50 |
| QUERY_BALANCE — Telugu | 25 |
| QUERY_TASKS — Direct | 25 |
| QUERY_TASKS — Hinglish | 25 |
| QUERY_TASKS — Telugu | 25 |
| QUERY_STATUS — Direct | 25 |
| QUERY_STATUS — Hinglish | 25 |
| QUERY_STATUS — Telugu | 25 |
| REQUEST_SWAP — Direct | 25 |
| REQUEST_SWAP — Hinglish | 25 |
| REQUEST_SWAP — Telugu | 25 |
| CREATE_TASK — Direct | 25 |
| CREATE_TASK — Hinglish | 25 |
| GREETING | 25 |
| UNKNOWN / FALLBACK | 100 |
| Multi-Intent Sequences | 25 |
| Error Recovery Dialogues | 50 |
| **TOTAL** | **1000+** |

---

*End of Use Case Corpus v1.0*
*For Claude Code: Use this as training data, test cases, and response template reference.*
