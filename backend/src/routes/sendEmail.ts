import { Router } from "express";
import { sendMemoEmail } from "../services/emailService";
import { SendEmailRequestBody } from "../types/api";

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const sendEmailRouter = Router();

sendEmailRouter.post("/", async (request, response) => {
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
