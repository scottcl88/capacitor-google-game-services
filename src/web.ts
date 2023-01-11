import { WebPlugin } from '@capacitor/core';

import type { GoogleGameServicesPlugin } from './definitions';

export class GoogleGameServicesWeb extends WebPlugin implements GoogleGameServicesPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
