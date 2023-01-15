# capacitor-google-game-services

A native only capacitor plugin for googles play services library and apples game center library

## Install

```bash
npm install capacitor-google-game-services
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`signIn()`](#signin)
* [`isAuthenticated()`](#isauthenticated)
* [`showSavedGamesUI()`](#showsavedgamesui)
* [`saveGame(...)`](#savegame)
* [`loadGame()`](#loadgame)
* [`getCurrentPlayer()`](#getcurrentplayer)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### signIn()

```typescript
signIn() => Promise<{ isAuthenticated: boolean; }>
```

**Returns:** <code>Promise&lt;{ isAuthenticated: boolean; }&gt;</code>

--------------------


### isAuthenticated()

```typescript
isAuthenticated() => Promise<{ isAuthenticated: boolean; }>
```

**Returns:** <code>Promise&lt;{ isAuthenticated: boolean; }&gt;</code>

--------------------


### showSavedGamesUI()

```typescript
showSavedGamesUI() => Promise<any>
```

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### saveGame(...)

```typescript
saveGame(options: { title: string; data: string; }) => Promise<any>
```

| Param         | Type                                          |
| ------------- | --------------------------------------------- |
| **`options`** | <code>{ title: string; data: string; }</code> |

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### loadGame()

```typescript
loadGame() => Promise<{ title: string; data: string; }>
```

**Returns:** <code>Promise&lt;{ title: string; data: string; }&gt;</code>

--------------------


### getCurrentPlayer()

```typescript
getCurrentPlayer() => Promise<{ player: Player; }>
```

**Returns:** <code>Promise&lt;{ player: <a href="#player">Player</a>; }&gt;</code>

--------------------


### Interfaces


#### Player

| Prop               | Type                |
| ------------------ | ------------------- |
| **`displayName`**  | <code>string</code> |
| **`iconImageUrl`** | <code>string</code> |

</docgen-api>
