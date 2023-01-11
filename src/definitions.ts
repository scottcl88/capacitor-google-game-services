export interface GoogleGameServicesPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
