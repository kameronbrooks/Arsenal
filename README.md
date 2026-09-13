# Arsenal

Early-1900s firearms for Minecraft — **NeoForge 1.21.1**. Four guns, four calibers, and a
two-stage ammunition pipeline that turns raw metal into live cartridges.

Mod id `arsenal` · package `com.arsenal` · sibling of the Obsidian and Crimson mods.

## The guns

All four are **hitscan** (a shot instantly traces from the muzzle to the first entity or block) and
use a **magazine + reload** system. Loaded rounds show as the item's durability-style bar and in the
tooltip.

| Gun | Caliber | Mag | Damage | Range | Rate | Notes |
|-----|---------|-----|--------|-------|------|-------|
| **Revolver** | .38 Special | 6 | 6 | 32 | fast (semi) | Cheap, accurate all-rounder; 4-hit kill |
| **Submachine Gun** | .45 ACP | 20 | 4 × auto | 30 | very fast (full-auto) | Sustained DPS; ammo-hungry, spready |
| **Bolt-Action Rifle** | .30-06 | 5 | 15 | 64 | slow (semi) | Per-shot king; 2-tap, pinpoint, long range |
| **Pump Shotgun** | 12 Gauge | 5 | 3 × 8 pellets | 18 | medium (semi) | ~24 point-blank, falls off with range |

Balance figures assume all pellets/rounds land — see the i-frame note under Controls.

### Controls
- **Right-click** — fire (if loaded) or reload (if empty).
- **Sneak + right-click** — reload / top up the magazine early.

Reloading pulls matching cartridges from your inventory and briefly locks the gun (the item
cooldown). In creative, the magazine tops up for free.

Bullets deal a dedicated `arsenal:bullet` damage type ("… was gunned down") and interact with armor
normally. That damage type is added to the vanilla `bypasses_cooldown` tag so it ignores the
half-second hurt-invulnerability window — otherwise a point-blank shotgun's 8 same-tick pellets (and
rapid SMG/revolver fire) would be absorbed by i-frames and only ~1 hit would register.

## The ammunition pipeline

Ammo is made in two stages, so metal is the real bottleneck — exactly as asked.

### 1. Cast components at the Munitions Bench

The **Munitions Bench** is a custom furnace-like machine (craft it from 8 iron + a furnace). It has
four slots — **metal input**, **mold**, **fuel**, **output** — and its own GUI with a burn flame and
a casting-progress bar. Put in a metal, a mold, and fuel, and it casts:

| Metal | Mold | Output |
|-------|------|--------|
| Copper ingot | Casing Mold | 4 × Brass Casing |
| Iron ingot | Casing Mold | 4 × Shotgun Hull |
| Iron ingot | Bullet Mold | 4 × Bullet |
| Iron ingot | Shot Mold | 6 × Lead Shot |

Molds are reusable — they select what the bench casts and are never consumed. Craft each from iron
ingots. The bench accepts any normal furnace fuel and, via its item-handler capability, can be
loaded and emptied by hoppers.

### 2. Assemble cartridges at the crafting table

Combine the cast components with **gunpowder** (the propellant). Bigger calibers take a heavier
powder charge, which is how the four recipes stay distinct:

| Cartridge | Recipe (shapeless) | Yield |
|-----------|--------------------|-------|
| **.38 Special** | Brass Casing + Bullet + 1 Gunpowder | 3 |
| **.45 ACP** | Brass Casing + Bullet + 2 Gunpowder | 3 |
| **.30-06** | Brass Casing + Bullet + 3 Gunpowder | 2 |
| **12 Gauge Shell** | Shotgun Hull + 4 Lead Shot + 2 Gunpowder | 2 |

## Building

The machine's default `JAVA_HOME` points at Unity's bundled JDK (path ends in `\bin`, which breaks
the Gradle wrapper), so override it to the full Temurin JDK 21 before any `gradlew` run:

```bash
export JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot'
./gradlew.bat build        # compile + package the jar
./gradlew.bat runClient    # launch a dev client
```

## Layout

```
src/main/java/com/arsenal/
  Arsenal.java                         mod entry point + capability wiring
  item/       GunItem, GunSettings, MoldItem
  block/      MunitionsBenchBlock, entity/MunitionsBenchBlockEntity
  menu/       MunitionsBenchMenu
  recipe/     CastingRecipe, CastingRecipeInput
  client/     ArsenalClient, MunitionsBenchScreen
  registry/   Arsenal{Items,Blocks,BlockEntities,Menus,Recipes,DataComponents,
              CreativeTabs,DamageTypes}
src/main/resources/
  assets/arsenal/    models, blockstates, lang, textures (items, block, GUI)
  data/arsenal/      recipe/ (crafting + casting), loot_table/, damage_type/
```

## Notes & ideas for later

- Guns fire through the crossbow use-animation: the revolver, rifle, and shotgun are **semi-auto**
  (one shot per trigger pull); the **SMG is full-auto** (hold to fire at its cadence).
- Sounds are vanilla placeholders (firework blast, generic explosion, dispenser click) chosen to
  read as gunfire; swap in custom `.ogg`s for real gunshots.
- Possible additions: iron sights / scope zoom, bullet penetration vs. armor, tracer rendering,
  more calibers (e.g. a lever-action .44), and a JEI plugin for the casting recipes.

## License

Arsenal is released under the [MIT License](LICENSE) — free to use, modify, and redistribute.
