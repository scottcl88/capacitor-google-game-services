export interface GoogleGameServicesPlugin {
  /**
   * Attempts to sign in and returns isAuthenticated as true if succeeded. Sign in is automatically attempted upon plugin load. This is only needed if the automatic sign in failed or was logged out.
   */
  signIn(): Promise<{ isAuthenticated: boolean }>;
  /**
   * Returns if the player is authenticated with Google Play Games.
   */
  isAuthenticated(): Promise<{ isAuthenticated: boolean }>;
  /**
   * Attempts to display Google's default saved games screen, returning the saved game object if selected or saving a new game object if player clicked add.
   */
  showSavedGamesUI(): Promise<any>;
  /**
   * Uses Google Play Game services snapshot feature to save a JSON Object as bytes.
   * @param options The title (key) and data (can be json object) to save to the game object
   */
  saveGame(options: { title: string, data: string }): Promise<any>;
  /**
   * Loads the last selected game with the 'savenameTemp' snapshot.
   * @param options The title (key) and data (can be json object) that was saved to the game object
   */
  loadGame(): Promise<{ title: string, data: string }>;
  /**
   * Returns a player object of the currently signed in player
   */
  getCurrentPlayer(): Promise<{ player: Player }>;
}
export interface Player {
  displayName: string;
  iconImageUrl: string;
}