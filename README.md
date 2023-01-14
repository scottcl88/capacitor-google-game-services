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
* [`showSavedGamesUI()`](#showsavedgamesui)
* [`saveGame(...)`](#savegame)
* [`loadGame()`](#loadgame)

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
signIn() => Promise<any>
```

**Returns:** <code>Promise&lt;any&gt;</code>

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
loadGame() => Promise<any>
```

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------

</docgen-api>
