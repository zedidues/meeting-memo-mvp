import { Router } from "express";
import { summarizeMeeting } from "../services/llmService";
import { SummaryRequestBody } from "../types/api";

export const summaryRouter = Router();

summaryRouter.post("/", async (request, response) => {
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
