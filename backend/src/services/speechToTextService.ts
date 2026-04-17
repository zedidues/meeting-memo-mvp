import { env } from "../config/env";

export async function transcribeAudio(params: {
  fileName: string;
  mimeType: string;
  bytes: Buffer;
}): Promise<string> {
  const formData = new FormData();
  const fileBlob = new Blob([new Uint8Array(params.bytes)], {
    type: params.mimeType || "application/octet-stream",
  });

  formData.append("file", fileBlob, params.fileName);
  formData.append("model", env.sttModel);

  const response = await fetch(`${env.sttApiBaseUrl}/audio/transcriptions`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${env.sttApiKey}`,
    },
    body: formData,
  });

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`STT request failed: ${errorBody}`);
  }

  const data = (await response.json()) as { text?: string };
  const transcript = data.text?.trim();
  if (!transcript) {
    throw new Error("STT response was empty.");
  }

  return transcript;
}
