# Replace Adjacent Feature - Implementation Summary

## Overview

Added a new boolean property `replace_adjacent` that controls whether the interaction replaces the **fluid** (default) or the **adjacent block** with the result.

## Changes Made

### 1. FluidInteractionData.java
**Location:** `common/src/main/java/grill24/rockreactors/data/FluidInteractionData.java`

Added new property to the codec:
```java
Codec.BOOL.optionalFieldOf("replace_adjacent", false).forGetter(data -> data.replaceAdjacent)
```

- Default value: `false` (maintains backward compatibility)
- Added field: `private final boolean replaceAdjacent;`
- Added getter: `public boolean shouldReplaceAdjacent()`

### 2. FluidInteractionHandler.java
**Location:** `common/src/main/java/grill24/rockreactors/FluidInteractionHandler.java`

Updated the interaction logic to support both modes:

```java
if (result.getInteraction().shouldReplaceAdjacent()) {
    // Replace the adjacent block with the result
    targetPos = result.getAdjacentPos();
    consumePos = pos; // Optionally consume the fluid position
} else {
    // Replace the fluid with the result (default behavior)
    targetPos = pos;
    consumePos = result.getAdjacentPos(); // Optionally consume the adjacent block
}
```

The handler now:
1. Checks the `replace_adjacent` property
2. Swaps which position gets the result vs which position can be consumed
3. Places the generated block at the appropriate position
4. Applies consume logic to the "other" position

## Behavior Modes

### Mode 1: Replace Fluid (default, `replace_adjacent: false`)
- **Target:** Fluid position gets the result block
- **Consume:** Adjacent block/fluid can be consumed based on `consume_chance`
- **Example:** Lava + shroomlight → glowstone (replaces lava, optionally consumes shroomlight)

### Mode 2: Replace Adjacent (`replace_adjacent: true`)
- **Target:** Adjacent block position gets the result block
- **Consume:** Fluid can be consumed based on `consume_chance`
- **Example:** Flowing lava + blackstone → basalt (replaces blackstone, lava remains)

## Use Cases

### Conversion Mechanics
```json
{
  "fluid_type": "lava_flowing",
  "condition": {
    "type": "adjacent_block",
    "block": "minecraft:blackstone"
  },
  "result": "minecraft:basalt",
  "replace_adjacent": true
}
```
- Lava "petrifies" blackstone into basalt
- Lava remains and continues flowing
- Creates renewable basalt farms

### Generation Mechanics (default)
```json
{
  "fluid_type": "any_lava",
  "condition": {
    "type": "adjacent_block",
    "block": "minecraft:ice"
  },
  "result": "minecraft:granite"
}
```
- Lava is replaced with granite
- Ice remains (unless consumed via `consume_chance`)

### Combined with Consume Chance
```json
{
  "fluid_type": "any_lava",
  "condition": {
    "type": "adjacent_block",
    "block": "minecraft:blackstone"
  },
  "result": "minecraft:basalt",
  "replace_adjacent": true,
  "consume_chance": 0.5
}
```
- Blackstone transforms into basalt
- 50% chance the lava also disappears

## Testing in Datapack

The `basalt_from_blackstone.json` file demonstrates this feature:
- Place blackstone blocks
- Pour flowing lava next to them
- Blackstone transforms into basalt
- Lava remains flowing
- Can create continuous conversion as lava flows past multiple blackstone blocks

## Backward Compatibility

✅ **Fully backward compatible**
- Default value is `false` (original behavior)
- Existing JSON files without this property work unchanged
- No breaking changes to existing interactions

## Platform Support

✅ **Works on both Fabric and NeoForge**
- Logic is in the shared `FluidInteractionHandler` in common package
- Both platforms use the same handler code
- Identical behavior across platforms

## Build Status

✅ **Build successful** - All changes compile without errors on both platforms.

