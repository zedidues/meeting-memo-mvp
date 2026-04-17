import { onRequest } from "firebase-functions/v2/https";
import { sendMemoEmail } from "../services/emailService";
import { SendEmailRequestBody } from "../types/api";

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const sendEmail = onRequest(async (request, response) => {
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
    const body = request.body as SendEmailRequestBody;

    if (!emailPattern.test(body.toEmail?.trim() ?? "")) {
      response.status(400).json({ success: false, message: "Invalid email address" });
      return;
    }

    await sendMemoEmail({
      toEmail: body.toEmail.trim(),
      title: body.title?.trim() || "회의 메모",
      rawText: body.rawText?.trim() || "",
      summaryText: body.summaryText?.trim() || "",
    });

    response.status(200).json({ success: true, message: "이메일을 발송했습니다." });
  } catch (error) {
    response.status(500).json({
      success: false,
      message: error instanceof Error ? error.message : "Unknown email error",
    });
  }
});

function setCors(response: { set: (name: string, value: string) => void }): void {
  response.set("Access-Control-Allow-Origin", "*");
  response.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
  response.set("Access-Control-Allow-Methods", "POST, OPTIONS");
}
