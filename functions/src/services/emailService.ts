import { env } from "../config/env";

export async function sendMemoEmail(params: {
  toEmail: string;
  title: string;
  rawText: string;
  summaryText: string;
}): Promise<void> {
  const html = `
    <h2>${escapeHtml(params.title)}</h2>
    <h3>요약</h3>
    <p>${escapeHtml(params.summaryText).replace(/\n/g, "<br/>")}</p>
    <h3>원문</h3>
    <p>${escapeHtml(params.rawText).replace(/\n/g, "<br/>")}</p>
  `;

  const response = await fetch("https://api.resend.com/emails", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${env.resendApiKey}`,
    },
    body: JSON.stringify({
      from: env.resendFromEmail,
      to: [params.toEmail],
      subject: `[회의 메모] ${params.title}`,
      html,
    }),
  });

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`Resend request failed: ${errorBody}`);
  }
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
