export interface GoogleGameServicesPlugin {
  signIn(): Promise<{ isAuthenticated: boolean }>;
  isAuthenticated(): Promise<{ isAuthenticated: boolean }>;
  showSavedGamesUI(): Promise<any>;
  saveGame(options: { title: string, data: string }): Promise<any>;
  loadGame(): Promise<{ title: string, data: string }>;
  getCurrentPlayer(): Promise<{ player: Player }>;
}
export interface Player {
  displayName: string;
  iconImageUrl: string;
}