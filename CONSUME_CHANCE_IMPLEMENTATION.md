# Consume Chance Feature - Implementation Summary

## Changes Made

### 1. FluidInteractionHandler in Common Package
**Location:** `common/src/main/java/grill24/rockreactors/FluidInteractionHandler.java`

**Purpose:** Shared handler used by **both Fabric and NeoForge** implementations

The handler contains:
- Common logic for checking fluid interactions
- Block placement logic
- Consume chance logic (random roll and adjacent block/fluid removal)
- Overloaded methods for `Level` and `LevelAccessor` compatibility

### 2. Consume Chance Property Added
**Modified:** `common/.../data/FluidInteractionData.java`

Added a new optional property `consume_chance` (float, range 0.0-1.0, default 0.0) that controls the probability of consuming the adjacent block/fluid when an interaction occurs.

```java
Codec.floatRange(0.0f, 1.0f).optionalFieldOf("consume_chance", 0.0f)
```

### 3. Fabric Implementation
**Modified:** `fabric/.../mixin/LiquidBlockMixin.java`

The Fabric mixin now:
1. Imports the common `FluidInteractionHandler`
2. Calls `FluidInteractionHandler.handleFluidInteraction()` in the `shouldSpreadLiquid` mixin
3. Fires the fizz effect if interaction occurs
4. All consume logic handled by the common handler

### 4. NeoForge Implementation
**Modified:** `neoforge/.../FluidInteractionIntegrationNeoForge.java`

The NeoForge integration now:
1. Imports the common `FluidInteractionHandler`
2. Creates `InteractionInformation` with a lambda that delegates to `FluidInteractionHandler.handleFluidInteraction()`
3. Fires the fizz effect (1501)
4. All consume logic handled by the common handler

### 5. Enhanced FluidInteractionManager
**Modified:** `common/.../FluidInteractionManager.java`

Added `InteractionResult` class and `findInteractionWithPosition()` method to track both:
- The interaction data
- The adjacent position that triggered it

This allows the common handler to know which block to potentially consume.

## Architecture Summary

```
Common Package
├── FluidInteractionData
│   └── consume_chance property (0.0-1.0)
├── FluidInteractionManager
│   ├── InteractionResult class
│   └── findInteractionWithPosition()
└── FluidInteractionHandler ⭐ (NOW SHARED)
    └── handleFluidInteraction()
        ├── Find interaction
        ├── Place generated block
        └── Consume adjacent block/fluid (based on chance)

Fabric Implementation
└── LiquidBlockMixin
    └── Calls FluidInteractionHandler.handleFluidInteraction()

NeoForge Implementation
└── FluidInteractionIntegrationNeoForge
    └── Creates InteractionInformation
        └── Lambda calls FluidInteractionHandler.handleFluidInteraction()
```

## Key Benefits of Unified Handler

1. **DRY Principle**: Consume logic is written once, used by both platforms
2. **Consistency**: Both platforms behave identically
3. **Maintainability**: Bug fixes and features only need to be updated in one place
4. **Testing**: Easier to test since the logic is centralized

## Testing

### Datapack Test File
The `glowstone_from_shroomlight.json` in the addon datapack demonstrates the feature:

```json
{
  "fluid_type": "any_lava",
  "condition": {
    "type": "adjacent_block",
    "block": "minecraft:shroomlight"
  },
  "result": "minecraft:glowstone",
  "consume_chance": 0.75
}
```

### Expected Behavior (Both Platforms)
1. Place shroomlight blocks
2. Pour lava adjacent to them
3. Lava converts to glowstone
4. **75% of the time:** The shroomlight block disappears
5. **25% of the time:** The shroomlight remains and can trigger another interaction

## Build Status
✅ **All changes compiled successfully** - Both Fabric and NeoForge builds completed without errors.

## Backward Compatibility
- Default `consume_chance` is 0.0 (never consumes)
- Existing interactions without the property continue to work exactly as before
- The property is optional in JSON files
