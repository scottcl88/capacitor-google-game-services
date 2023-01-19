import { WebPlugin } from '@capacitor/core';

import type { GoogleGameServicesPlugin, Player } from './definitions';

export class GoogleGameServicesWeb extends WebPlugin implements GoogleGameServicesPlugin {
  signIn(): Promise<{ isAuthenticated: boolean }> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve({ isAuthenticated: false });
  }
  isAuthenticated(): Promise<{ isAuthenticated: boolean }> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve({ isAuthenticated: false });
  }
  showSavedGamesUI(): Promise<any> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve();
  }
  saveGame(options: { title: string, data: string }): Promise<any> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve();
  }
  loadGame(): Promise<{ title: string, data: string }> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve({title: "", data: ""});
  } 
  getCurrentPlayer(): Promise<{player: Player}>{
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve({player: {} as Player});
  }
}
