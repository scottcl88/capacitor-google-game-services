import { WebPlugin } from '@capacitor/core';

import type { GoogleGameServicesPlugin } from './definitions';

export class GoogleGameServicesWeb extends WebPlugin implements GoogleGameServicesPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
  signIn(): Promise<any> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve();
  }
  showSavedGamesUI(): Promise<any> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve();
  }
  saveGame(): Promise<any> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve();
  }
  loadGame(): Promise<any> {
    console.warn('GameServices does not have web implementation.');
    return Promise.resolve();
  }
}
