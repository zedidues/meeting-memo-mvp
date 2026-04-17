import { env } from "../config/env";
import { SummaryResult } from "../types/api";

const summaryPrompt = `
You summarize meeting notes.
Return strict JSON only.
Schema:
{
  "title": "short meeting title",
  "summary": "concise summary in Korean",
  "actionItems": ["action item 1", "action item 2"]
}
`;

export async function summarizeMeeting(rawText: string): Promise<SummaryResult> {
  const response = await fetch(`${env.llmApiBaseUrl}/chat/completions`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${env.llmApiKey}`,
    },
    body: JSON.stringify({
      model: env.llmModel,
      temperature: 0.2,
      response_format: { type: "json_object" },
      messages: [
        { role: "system", content: summaryPrompt.trim() },
        {
          role: "user",
          content: `Summarize this meeting note in Korean:\n\n${rawText.slice(0, 12000)}`,
        },
      ],
    }),
  });

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`LLM summary request failed: ${errorBody}`);
  }

  const data = (await response.json()) as {
    choices?: Array<{ message?: { content?: string } }>;
  };

  const content = data.choices?.[0]?.message?.content;
  if (!content) {
    throw new Error("LLM summary response was empty.");
  }

  const parsed = JSON.parse(content) as Partial<SummaryResult>;
  return {
    title: parsed.title?.trim() || "회의 메모 요약",
    summary: parsed.summary?.trim() || "",
    actionItems: Array.isArray(parsed.actionItems)
      ? parsed.actionItems.map((item) => String(item))
      : [],
  };
}
