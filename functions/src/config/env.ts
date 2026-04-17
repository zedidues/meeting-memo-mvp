function requireEnv(name: string): string {
  const value = process.env[name];
  if (!value) {
    throw new Error(`Missing required environment variable: ${name}`);
  }
  return value;
}

export const env = {
  llmApiKey: requireEnv("LLM_API_KEY"),
  llmModel: process.env.LLM_MODEL ?? "gpt-4.1-mini",
  llmApiBaseUrl: process.env.LLM_API_BASE_URL ?? "https://api.openai.com/v1",
  sttApiKey: process.env.STT_API_KEY ?? requireEnv("LLM_API_KEY"),
  sttModel: process.env.STT_MODEL ?? "gpt-4o-mini-transcribe",
  sttApiBaseUrl: process.env.STT_API_BASE_URL ?? process.env.LLM_API_BASE_URL ?? "https://api.openai.com/v1",
  resendApiKey: requireEnv("RESEND_API_KEY"),
  resendFromEmail: requireEnv("RESEND_FROM_EMAIL"),
};
