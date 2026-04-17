import { onRequest } from "firebase-functions/v2/https";
import { summarizeMeeting } from "../services/llmService";
import { SummaryRequestBody } from "../types/api";

export const summary = onRequest(async (request, response) => {
  setCors(response);

  if (request.method === "OPTIONS") {
    response.status(204).send("");
    return;
  }

  if (request.method !== "POST") {
    response.status(405).json({ message: "Method not allowed" });
    return;
  }

  try {
    const body = request.body as SummaryRequestBody;
    const rawText = body.rawText?.trim();

    if (!rawText || rawText.length < 20) {
      response.status(400).json({
        message: "rawText must be at least 20 characters long.",
      });
      return;
    }

    const result = await summarizeMeeting(rawText);
    response.status(200).json(result);
  } catch (error) {
    response.status(500).json({
      message: error instanceof Error ? error.message : "Unknown summary error",
    });
  }
});

function setCors(response: { set: (name: string, value: string) => void }): void {
  response.set("Access-Control-Allow-Origin", "*");
  response.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
  response.set("Access-Control-Allow-Methods", "POST, OPTIONS");
}
