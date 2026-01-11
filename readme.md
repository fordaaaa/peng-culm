**Status / To‑Do**

- [x] IO created for leaderboard with better scoring system
  - scores saved in `util/leaderboard.txt` (name,score)
  - scoring uses win/loss, steps used, and difficulty bonus

- [x] Enter a number to select difficulty
  - console: `getdifficulty` asks for 1/2/3
  - GUI: popup `askDifficulty()` for 1/2/3 (easy/medium/hard)

- [x] Explosion effect
  - GUI: shows a popup "BOOM! You hit a mine!" when stepping on a bomb
  - bomb image is drawn on top of the tile where you exploded

- [x] Fix scoring + bomb visuals
  - bombs now show correctly in console (final grid is printed after game ends)
  - GUI draws bomb sprites over visited bomb tiles

- [x] WASD and move tracker on the side
  - GUI supports `w a s d` keys, `f` to scan, `g` to give up
  - right‑hand move tracker label shows steps and moves left

- [ ] Optional polish ideas
  - add sound or GIF for explosions
  - tweak hot/cold number ranges per difficulty if needed
- maybe instead let wasd work aswell, and have like a move tracker on the side etc
- movetracker doesnt actually track i want like aiden moved right aiden moved left etc and then (1) as in move 1 and then aiden moved down (20) as in move #20
- i also want wasd and arrow keys to work, so we could have a main var and have the keys that work under the var so for down s and down arrow to work with it