# Tavern — `art/tavern/bg.png`

## Prompt

> Hand-painted 2D illustration, point-and-click adventure game background, cozy and slightly melancholic underground town. Soft warm lantern light (amber, honey, teal glow) against deep cool shadows (dark brown, burgundy, dusty violet). Visible imperfect ink linework, painterly textures, subtle paper grain. Static diorama composition with clear foreground, midground and background layers, flat soft lighting, no harsh shadows. Inspired by Machinarium, Botanicula, Night in the Woods, Studio Ghibli backgrounds. Vertical 9:16 aspect ratio (phone screen), no characters, no text, no UI, no watermark.
>
> Scene: a warm underground tavern — wooden booths, a long bar, barrels and bottles, hanging mugs, a glowing hearth/fireplace, a fiddle on the wall, low ceiling with thick beams.
>
> Composition and clickable layout (origin at top-left, x runs left→right, y runs top→down, values are percent of frame width/height). This location has TWO exits in the top band and TWO characters in the lower band:
> - Top band, exit to the Mushroom Market: a clear archway or passage with a hanging wooden signpost, centered at 33% width and 5–20% height.
> - Top band, exit to the Archive: a clear archway or passage with a signpost, centered at 67% width and 5–20% height.
> - Lower-left area, centered at 33% width (24–42%), 55–90% height: keep it open (e.g. space behind the bar) — the Bartender character is composited here at runtime.
> - Lower-right area, centered at 67% width (58–76%), 55–90% height: keep it open (e.g. a booth/stool) — the Old Courier character is composited here at runtime.
> - Do not place large foreground objects in either lower character zone.

## Negative prompt

> 3d render, photorealistic, anime, cel shading, sharp digital lines, neon, sci-fi, blood, gore, modern technology, text, watermark, logo.
