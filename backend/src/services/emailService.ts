import nodemailer from "nodemailer";
import { env } from "../config/env";

const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: env.gmailUser,
    pass: env.gmailAppPassword,
  },
});

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

  await transporter.sendMail({
    from: `"회의 메모" <${env.gmailUser}>`,
    to: params.toEmail,
    subject: `[회의 메모] ${params.title}`,
    html,
  });
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
