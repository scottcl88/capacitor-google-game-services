import { registerPlugin } from '@capacitor/core';

import type { GoogleGameServicesPlugin } from './definitions';

const GoogleGameServices = registerPlugin<GoogleGameServicesPlugin>('GoogleGameServices', {
  web: () => import('./web').then(m => new m.GoogleGameServicesWeb()),
});

export * from './definitions';
export { GoogleGameServices };
