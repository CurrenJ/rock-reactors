# Rock Reactors Addon Datapack

This is a standalone datapack that adds additional fluid interactions for the Rock Reactors mod.

## Installation

1. Ensure the Rock Reactors mod is installed
2. Copy this entire folder into your world's `datapacks` folder
3. Reload datapacks with `/reload` or restart your world

## Features

### New Fluid Interactions

This datapack adds 8 new fluid interactions:

1. **Obsidian from Magma Block** - Lava source blocks adjacent to magma blocks create obsidian
2. **Granite from Ice** - Any lava adjacent to ice creates granite
3. **Calcite from Packed Ice** - Any lava adjacent to packed ice creates calcite
4. **Basalt from Deepslate** - Flowing lava transforms deepslate blocks within radius 3 into basalt (demonstrates `replace_adjacent` and `replace_radius`)
5. **Tuff from Gravel** - Any lava adjacent to gravel creates tuff
6. **Smooth Basalt from Water** - Lava source blocks meeting water above Y=0 create smooth basalt
7. **Glowstone from Shroomlight** - Any lava adjacent to shroomlight creates glowstone (75% chance to consume the shroomlight)
8. **Terracotta from Clay** - Any lava adjacent to clay creates terracotta (simulating natural firing)

### Replace Radius Feature

The **Basalt from Deepslate** interaction demonstrates the `replace_radius` property:
- When set to a value greater than 1, replaces multiple blocks within Manhattan distance of the fluid
- A value of 1 is the standard adjacent behavior (default)
- With `replace_radius: 3`, flowing lava will transform all deepslate blocks within Manhattan distance 3 into basalt
- Only works when `replace_adjacent: true` and checks each position against the interaction condition
- Creates area-of-effect conversion mechanics

### Replace Adjacent Feature

The **Basalt from Deepslate** interaction also demonstrates the `replace_adjacent` property:
- When set to `true`, the result block replaces the adjacent block(s) instead of the fluid
- Flowing lava next to deepslate will transform the deepslate into basalt, leaving the lava intact
- This creates a "conversion" mechanic rather than a "generation" mechanic
- When `false` or omitted (default), the fluid is replaced as usual

### Consume Chance Feature

The **Glowstone from Shroomlight** interaction demonstrates the `consume_chance` property:
- When lava interacts with shroomlight to create glowstone, there's a 75% chance the shroomlight block will be destroyed
- This creates a resource trade-off mechanic where you sacrifice shroomlight blocks to generate glowstone
- The consume chance can be set to any value between 0.0 (never consumes) and 1.0 (always consumes)
- Works with both `replace_adjacent` modes - consumes the "other" block/fluid

## Custom Interactions

You can add your own interactions by creating JSON files in the `data/<namespace>/rockreactors/fluid_interaction/` folder.

### JSON Format

```json
{
  "fluid_type": "any_lava",
  "condition": {
    "type": "adjacent_block",
    "block": "minecraft:shroomlight"
  },
  "result": "minecraft:glowstone",
  "consume_chance": 0.75,
  "replace_adjacent": false
}
```

### Properties

- **fluid_type**: The type of fluid that triggers the interaction
  - `"lava_source"` - Only lava source blocks
  - `"lava_flowing"` - Only flowing lava
  - `"any_lava"` - Any lava (source or flowing)

- **condition**: The condition that must be met for the interaction
  - `"type": "adjacent_block"` - Checks for an adjacent block
    - `"block"` - The block to check for (e.g., `"minecraft:blue_ice"`)
  - `"type": "adjacent_fluid_with_y"` - Checks for an adjacent fluid with optional Y-level condition
    - `"fluid"` - The fluid to check for (e.g., `"minecraft:water"`)
    - `"y_condition"` (optional) - Y-level requirement: `"below_zero"` or `"above_zero"`

- **result**: The block to generate (e.g., `"minecraft:obsidian"`)

- **replace_adjacent** (optional, default: false): Controls which block is replaced by the result
  - `false` (default) - Replaces the fluid with the result block (standard behavior)
  - `true` - Replaces the adjacent block with the result block (conversion mechanic)

- **replace_radius** (optional, default: 1, range: 1-16): When `replace_adjacent` is true, controls the radius of blocks to replace
  - `1` (default) - Standard adjacent behavior, replaces only the directly adjacent block
  - `2` or higher - Replaces all matching blocks within Manhattan distance from the fluid
  - Manhattan distance means the sum of absolute differences in coordinates (|dx| + |dy| + |dz|)
  - Each position is checked against the interaction condition before replacement
  - Creates area-of-effect conversion - useful for large-scale terrain transformation

- **consume_chance** (optional, default: 0.0): The probability (0.0 to 1.0) that the "other" block/fluid will be destroyed when the interaction occurs
  - When `replace_adjacent` is `false`: consumes the adjacent block
  - When `replace_adjacent` is `true`: consumes the fluid
  - `0.0` - Never consumes (default)
  - `0.5` - 50% chance to consume
  - `1.0` - Always consumes

## Testing

### Test the glowstone interaction with consume chance:

1. Place shroomlight blocks
2. Pour lava adjacent to them
3. The lava will turn into glowstone
4. About 75% of the time, the shroomlight block will disappear
5. About 25% of the time, the shroomlight will remain and can trigger another interaction

### Test the basalt interaction with replace_adjacent:

1. Place deepslate blocks
2. Pour flowing lava adjacent to them
3. The deepslate will transform into basalt
4. The lava remains and continues flowing
5. This creates a "petrification" effect rather than replacing the lava

### Test the basalt interaction with replace_radius:

1. Create a large cluster of deepslate blocks (at least 7x7x7)
2. Pour a single source of flowing lava near the cluster
3. All deepslate blocks within Manhattan distance 3 will transform into basalt
4. Manhattan distance 3 means blocks up to 3 blocks away in total (e.g., 3 blocks in one direction, or 2+1 in two directions, or 1+1+1 in all three)
5. This creates efficient large-scale terrain transformation
6. The lava remains and can continue transforming as it flows to new areas

## Compatibility

- Requires Rock Reactors mod version 1.0.0 or higher
- Compatible with Minecraft 1.21.1 (pack format 48)
- Works on both Fabric and NeoForge
