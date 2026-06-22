# Image-generation prompts

This folder holds ready-to-use prompts for generating the game's artwork. It
sits right next to the assets themselves: location backgrounds go to
`app/src/main/assets/art/<location>/bg.png` and NPC portraits to
`app/src/main/assets/art/npc/<id>.png` (paths come from
`assets/content/game.json`).

Each prompt is **complete and self-contained** — copy the whole `Prompt`
section of a file into an image generator as-is. Every location prompt already
embeds the global style, the scene, the exact clickable layout, and the
negative prompt, so the generated image lines up with where the player taps.

## Output requirements

- **Aspect ratio:** 9:16, vertical (phone screen). The background fills the
  whole `Canvas`, so keep key objects inside the stated zones with some margin.
- **Recommended size:** 1080 x 1920 px, PNG.
- **Location backgrounds:** no characters and no UI (the engine draws
  characters on top).
- **NPC portraits:** transparent background (alpha), side / three-quarter view,
  full body.

## Coordinate system (how the layout numbers work)

Positions are normalized to `[0..1]` and written as percentages:

- `x` — left to right (0% = left edge, 100% = right edge).
- `y` — top to bottom (0% = top edge, 100% = bottom edge).

The clickable zones are computed deterministically in `engine/SceneBuilder.kt`:

| Type | Purpose | Where it lives |
| --- | --- | --- |
| EXIT | move to a connected location | top band, `y` 5–20%; `x` center depends on exit count |
| NPC | tap opens a dialogue | lower band, `y` 55–90%; `x` center depends on NPC count |
| HOTSPOT | story interactive (elevator lever) | center, `x` 32–68%, `y` 32–48% |

Horizontal centers:

- **One object:** centered at 50% width.
- **Two objects:** centered at 33% and 67% width, in the order they appear in
  `game.json`.

Approximate half-width of each zone: exits ~±10% width, NPCs ~±9% width. Draw
the object so its body falls inside that rectangle.

## Reusable building blocks

These are already pasted into every prompt; they are listed here for reference.

**Global style:**

> Hand-painted 2D illustration, point-and-click adventure game background, cozy
> and slightly melancholic underground town. Soft warm lantern light (amber,
> honey, teal glow) against deep cool shadows (dark brown, burgundy, dusty
> violet). Visible imperfect ink linework, painterly textures, subtle paper
> grain. Static diorama composition with clear foreground, midground and
> background layers, flat soft lighting, no harsh shadows. Inspired by
> Machinarium, Botanicula, Night in the Woods, Studio Ghibli backgrounds.
> Vertical 9:16 aspect ratio (phone screen), no characters, no text, no UI, no
> watermark.

**Negative prompt:**

> 3d render, photorealistic, anime, cel shading, sharp digital lines, neon,
> sci-fi, blood, gore, modern technology, text, watermark, logo.

## Files

- `post-office.md`, `market.md`, `clock-house.md`, `tavern.md`, `archive.md`,
  `tunnel.md`, `old-elevator.md` — one location background each, with the
  clickable layout pinned to the SceneBuilder hit areas.
- `npc-postmaster.md`, `npc-mushroom-seller.md`, `npc-clockmaker.md`,
  `npc-bartender.md`, `npc-courier.md`, `npc-archivist.md`,
  `npc-lamplighter.md`, `npc-engineer.md` — one complete portrait prompt per NPC.
