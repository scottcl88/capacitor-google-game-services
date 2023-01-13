export interface GoogleGameServicesPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  signIn(): Promise<any>;
  showSavedGamesUI(): Promise<any>;
  saveGame(): Promise<any>;
  loadGame(): Promise<any>;
}
